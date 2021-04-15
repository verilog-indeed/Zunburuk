package dz.lightyearsoftworks.zunburuk

import java.io.InputStream
import java.lang.Math.floor

/**
 * Represents the parameters and initial conditions tied to an
 * ordinary differential equation, example for a second order
 * linear equation we get y" + by' + ay = c
 * where: c is RHS_constant
 *        a is Y_FACTOR
 *        etc...
 * */

//Y_FACTOR also represents the factor of the sin(y) term (y" + aSin(y) = c)
//in the ODE for a pendulum system, not sure if i should be doing this though..
data class EquationParameters(val Y_NAUGHT: Double, val Y_PRIME_NAUGHT: Double,
                              val Y_FACTOR: Double, val Y_PRIME_FACTOR: Double,
                              val RHS_constant: Double)

/**Represents a single preimage/image pair (t, F(t))*/
data class dataPoint(val t: Double, val y: Double)  //is this overkill?

/**Defines possible types of ordinary differential equations that can be solved*/
enum class DifferentialEquationType {
    ORDER2_UNDAMPED, ORDER2_DAMPED, ORDER2_PENDULUM
}

class functionalInputStream(function: ArrayList<dataPoint>, amplitude: Double) : InputStream() {
    private var pcmValues: IntArray
    private var position: Long = 0
    override fun read(): Int {
        if (position >= 2 * pcmValues.size) return -1
        val currentIndex =  (position/2).toInt()
        val currentShort = pcmValues[currentIndex]
        /*//if position is a multiple of 2 it lines up with the LSB of the current value, isolate LSB to the extreme left
        //else, it lines up with the MSB of the value at (position - 1), isolate MSB to the extreme left
        var result = if (position.toInt() % 2 == 0) (currentShort shl 24) else (currentShort shl 16)
        result = result shr 24 //whichever byte was chosen, it is shifted back to the extreme right*/
        /*or you could do this the sensible way*/
        val result = if (position.toInt() % 2 == 0) currentShort and 11 else (currentShort and 1100) shr 2
        position++
        return result
    }

    init {
        pcmValues = IntArray(function.size)
        for (i in pcmValues.indices) {
            pcmValues[i] = kotlin.math.floor(function[i].y * 32767 / amplitude).toInt()
        }
    }
}
