package dz.lightyearsoftworks.zunburuk

import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    println("Hello World!")
    val function: ArrayList<dataPoint>
    val deltaT = measureTimeMillis {
        function = DifferentialSolver().solve(DifferentialEquationType.ORDER2_UNDAMPED,
                                                                EquationParameters(5.0,
                                                                                    0.0,
                                                                                    1.0,
                                                                                    0.0,
                                                                                    0.0), 0.0, 10.0)
        for (dp in function)    {
            println("Y(" + String.format("%.2f", dp.t) + ") = " + String.format("%.4f",dp.y) )
        }
    }
    println("Executed with love in $deltaT milliseconds.")
}
