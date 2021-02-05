package dz.lightyearsoftworks.zunburuk

import kotlin.math.cos

fun main(args: Array<String>) {
    println("Hello World!")
    val h = 0.001
    val Y_factor = 1
    var Xn = 0.0
    var Yn = 5.0
    var Yn_prime = 0.0
    var Ynext_prime: Double
    var Xnext: Double = Xn + h
    var Ynext: Double = Yn + Yn_prime * h
    var Ynext2: Double

    for (i in 1..500)  {
        println("Y($Xn) = $Yn")
        println("5cos($Xn) = ${5 * cos(Xn)}")
        Xn = Xnext
        Xnext += h
        Ynext_prime = Yn_prime - Y_factor * (0.5 * (Ynext + Yn) * h)
        Ynext2 = Ynext + Ynext_prime * h
        Yn = Ynext
        Ynext = Ynext2
        Yn_prime = Ynext_prime
    }
}