package dz.lightyearsoftworks.zunburuk

import java.lang.Thread.sleep
import java.util.*
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import kotlin.collections.ArrayList
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    println("Hello World!")
    val function: ArrayList<dataPoint>
    val deltaT = measureTimeMillis {
        function = DifferentialSolver().solve(DifferentialEquationType.ORDER2_UNDAMPED,
                                                                EquationParameters(5.0,
                                                                                    0.0,
                                                                                    1600000.0,
                                                                                    0.0,
                                                                                    0.0),
                                                                                    0.0, 3.0)
    }
    audioTest(function)
    //extend the InputStream class for audio?
}

fun audioTest(function: ArrayList<dataPoint>) {
    val strem = AudioInputStream(functionalInputStream(function, 5.0),
                                AudioFormat(22050.0F, 16, 1, true, false),
                                    22050 * 10)
    val clippy = AudioSystem.getClip()
    clippy.open(strem)
    clippy.start()
    sleep(4000) //need to wait for the sound to play in a background thread
}
