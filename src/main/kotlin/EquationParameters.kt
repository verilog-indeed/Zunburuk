package dz.lightyearsoftworks.zunburuk

data class EquationParameters(val Y_NAUGHT: Double, val Y_PRIME_NAUGHT: Double,
                              val Y_FACTOR: Double, val Y_PRIME_FACTOR: Double,
                              val RHS_constant: Double)
/*
* Represents the parameters and initial conditions tied to an
* ordinary differential equation, example for a second order
* linear equation we get y" + ay' + by = c
* where: c is RHS_constant
*        b is Y_FACTOR
*        etc...
* */

//Y_FACTOR also represents the factor of the sin(y) term (y" + aSin(y) = c)
// in the ODE for a pendulum system, not sure if i should be doing this though..