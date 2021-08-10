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
data class EquationParameters(val Y_NAUGHT: Double, val Y_PRIME_NAUGHT: Double,
                              val Y_FACTOR: Double, val Y_PRIME_FACTOR: Double,
                              val RHS_constant: Double)

/**Represents a single preimage/image pair (t, F(t))*/
data class ODEDataPoint(val t: Double, val y: Double)

/**Defines possible types of ordinary differential equations that can be solved*/
enum class DifferentialEquationType {
    ORDER2_UNDAMPED, ORDER2_DAMPED, ORDER2_PENDULUM
}

/**Turns a function (array of datapoints) into an audio InputStream as defined in the Javadocs
 *It is specifically designed to generate audio streams that are to be played at 16-bit single channel signed little-endian PCM */
class FunctionalInputStream(function: ArrayList<ODEDataPoint>, amplitude: Double) : InputStream() {
    private var pcmValues: IntArray
    private var position: Long
    /**Sends the data one byte at a time, we send each value in pcmValues twice:
     *first for the LSB the second for the MSB before moving on to the next value*/
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
        //linearly normalize given values where 100% amplitude is 32767 (0x7FFF)
        //32767 = 2^(16 - 1) max magnitude in a 16bit two's complement number
        for (i in pcmValues.indices) {
            pcmValues[i] = kotlin.math.floor(function[i].y * 32767  / amplitude).toInt()
        }
        position = 0
    }
}
