package dz.lightyearsoftworks.zunburuk

import java.lang.Thread.sleep
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineUnavailableException
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin

fun main(args: Array<String>) {

    println("Hello World!")
    var function: ArrayList<ODEDataPoint> = ArrayList()
    /*
    var currentSystem = DifferentialSolver(DifferentialEquationType.ORDER2_UNDAMPED,
                                            EquationParameters(10.0, 0.0,
                                                                1000000.0,0.0, 0.0),
                                            0.0, 1.0 / 4410000.0)
    for (i in 0..441000)   {
        function.add(currentSystem.nextDataPoint())
        for (j in 0..99) {
            currentSystem.nextDataPoint()
        }
    }*/

    var Xi = 0.0
    for (i in 0..441000)    {
        function.add(ODEDataPoint(Xi, 10.0 * cos(1000*Xi)))
        Xi += 1.0 / 44100.0
    }
    audioTest(function)
}

fun audioTest(function: ArrayList<ODEDataPoint>) {
    val strem = AudioInputStream(FunctionalInputStream(function, 10.0),
                                AudioFormat(44100.0F, 16, 1, true, false),
                                    441000)
    val clippy = AudioSystem.getClip()
    try {
        clippy.open(strem)
        clippy.start()
        sleep(2000)
        println("2 secs")
        sleep(9000) //need to wait for the sound to play in a background thread
    } catch (e: LineUnavailableException)    {

    } finally {
        clippy.close() //need to close audio handle because they're exhaustible
    }
}
