package dz.lightyearsoftworks.zunburuk

import java.io.InputStream

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
    private var position: Long
    override fun read(): Int {
        if (position >= 2 * pcmValues.size) return -1
        val currentIndex =  (position/2).toInt()
        val currentShort = pcmValues[currentIndex]

        /*returns the LSByte if this is the first time this index has occurred by ANDing with 0b11111111
        * else returns the MSByte if this is the second time this index has occurred (currentIndex = position - 1)
        * and it does so by ANDing the current number with 0b1111111100000000 and then bit-shifting
        * the result by 8 bits to the right*/

        val result = if (position.toInt() % 2 == 0) currentShort and 0xFF else (currentShort and 0xFF00) shr 8
        position++

        return result
    }

    init {
        pcmValues = IntArray(function.size)
        for (i in pcmValues.indices) {
            pcmValues[i] = kotlin.math.floor(function[i].y * 32767 / amplitude).toInt()
        }
        position = 0
    }
}
