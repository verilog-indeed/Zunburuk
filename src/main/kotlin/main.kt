package dz.lightyearsoftworks.zunburuk

import kotlin.math.cos
import kotlin.math.abs
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    println("Hello World!")
    val deltaT = measureTimeMillis {
        heunMethod()
        //eulerMethod()
    }
    println("Executed with love in $deltaT milliseconds.")
}

fun heunMethod()    {
    val h = 0.01
    val Y_factor = 1
    var Xi = 0.0
    var Yi = 5.0
    var Pi = 0.0 //P is the first derivative of Y (Y' = P)
    var k1: Double
    var k2: Double
    var u1: Double
    var u2: Double

    for (i in 1..630)   {
        println("Y($Xi) = $Yi")
        println("5cos($Xi) = ${5 * cos(Xi)}")
        val dev = abs(Yi - 5*cos(Xi))/(5*cos(Xi)) * 100
        println("deviation is $dev%")

        k1 = Pi
        k2 = Pi - h * Y_factor * Yi
        u1 = - Y_factor * Yi
        u2 = - Y_factor * Yi - Y_factor * h * Pi

        Xi += h
        Yi += 0.5 * (k1 + k2) * h
        Pi += 0.5 * (u1 + u2) * h
    }
}

fun eulerMethod()   {
    val h = 0.01
    val Y_factor = 1
    var Xn = 0.0
    var Yn = 5.0
    var Yn_prime = 0.0
    var Ynext_prime: Double
    var Ynext: Double = Yn + Yn_prime * h

    for (i in 1..630)  {
        println("Y($Xn) = $Yn")
        println("5cos($Xn) = ${5 * cos(Xn)}")
        val dev = abs(Yn - 5*cos(Xn))/(5*cos(Xn)) * 100
        println("deviation is $dev% ")

        Xn = Xn + h
        Ynext_prime = Yn_prime - Y_factor * (0.5 * (Ynext + Yn) * h)
        Yn = Ynext
        Ynext = Ynext + Ynext_prime * h
        Yn_prime = Ynext_prime
    }
}