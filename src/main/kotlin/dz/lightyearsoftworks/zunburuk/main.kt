package dz.lightyearsoftworks.zunburuk

import java.lang.Thread.sleep
import javax.sound.sampled.*
import kotlin.collections.ArrayList
import kotlin.math.*

var angVel = 2 * Math.PI * 1000
//minfreq = 70Hz?
//maxfreq = 14,000Hz?
//I dont know why, but this is the best tradeoff between sampling rate/nbr of samples and audio quality
var samplingRate = 48000.0F
var numberOfSamples = 6000
fun main(args: Array<String>) {

    println("Hello World! $numberOfSamples")
    var function: ArrayList<ODEDataPoint> = ArrayList()
    var currentSystem = DifferentialSolver(DifferentialEquationType.ORDER2_UNDAMPED,
                                            EquationParameters(10.0, 0.0,
                                                                angVel * angVel,0.0, 0.0),
                                            0.0, 1.0 / (samplingRate * 500.0))
    for (i in 0..numberOfSamples)   {
        function.add(currentSystem.nextDataPoint())
        for (j in 1..499) {
            currentSystem.nextDataPoint()
        }
    }
    audioTest(function)
}

fun audioTest(function: ArrayList<ODEDataPoint>) {
    val strem = AudioInputStream(FunctionalInputStream(function, 10.0),
                                AudioFormat(samplingRate, 16, 1, true, false),
                        numberOfSamples.toLong())
    val clippy = AudioSystem.getClip()
    try {
        clippy.open(strem)
        clippy.loop(Clip.LOOP_CONTINUOUSLY)
        clippy.start()
        sleep(2000)
        println("2 secs")
        sleep(2500) //need to wait for the sound to play in a background thread
    } catch (e: LineUnavailableException)    {

    } finally {
        clippy.close() //need to close audio handle because they're exhaustible
    }
}
