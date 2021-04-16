package dz.lightyearsoftworks.zunburuk

import java.lang.Thread.sleep
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineUnavailableException
import kotlin.collections.ArrayList
import kotlin.math.pow

fun main(args: Array<String>) {
    println("Hello World!")
    var function: ArrayList<dataPoint>
    for (i in 2..250) {
        println("${i * 100.0} Hz")
        function = DifferentialSolver().solve(DifferentialEquationType.ORDER2_UNDAMPED,
                EquationParameters(5.0,
                        0.0,
                        (i * 100.0).pow(2.0),
                        0.0,
                        0.0),
                0.0, 0.1)
        audioTest(function)
    }
}

fun audioTest(function: ArrayList<dataPoint>) {
    val strem = AudioInputStream(functionalInputStream(function, 5.0),
                                AudioFormat(96000.0F, 16, 1, true, false),
                                    96000 * 10)
    val clippy = AudioSystem.getClip()
    try {
        clippy.open(strem)
    } catch (e: LineUnavailableException)    {
    /*do nothing :)*/
    }
    clippy.start()
    sleep(100) //need to wait for the sound to play in a background thread
}
