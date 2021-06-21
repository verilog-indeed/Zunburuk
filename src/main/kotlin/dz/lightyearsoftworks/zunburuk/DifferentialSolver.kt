package dz.lightyearsoftworks.zunburuk

import kotlin.math.ceil
import kotlin.math.sin

//const val h = 0.01 //the time-step used
const val h = 1.0 / 44100.0
/*for generating sound we need about 22050Hz/44100Hz sampling, much crisper audio is achieved with higher rates
* but some systems just don't like those sampling rates for whatever reason*/

class DifferentialSolver  {

    fun solve(eqnType: DifferentialEquationType, initialConditions: EquationParameters,
              startTime: Double, stopTime: Double): ArrayList<dataPoint> {

        val solution =  ArrayList<dataPoint>()
        var Xi = startTime
        var Yi = initialConditions.Y_NAUGHT
        var Pi = initialConditions.Y_PRIME_NAUGHT
        var k1 = 0.0
        var k2 = 0.0
        var u1 = 0.0
        var u2 = 0.0

        //this essentially solves a system of two equations: Y' = P and P' = f(x,y,p)


        /*the equations i intend to solve for now follow the same pattern, only thing that changes is how we calculate
         *the heun factors so we treat the updateHeunFunction as a lambda that gets assigned the correct calculation
         * procedure based on which type of equation we're solving, I hope hope hope theres a better way of doing this
         */

        val updateHeunFactors = {
            when (eqnType) {
                DifferentialEquationType.ORDER2_UNDAMPED -> {
                    k1 = Pi
                    u1 = -initialConditions.Y_FACTOR * Yi
                    k2 = Pi + h * u1
                    u2 = -initialConditions.Y_FACTOR * (Yi + h * k1)
                }

                DifferentialEquationType.ORDER2_DAMPED -> {
                    k1 = Pi
                    u1 = -initialConditions.Y_FACTOR * Yi - initialConditions.Y_PRIME_FACTOR * Pi
                    k2 = Pi + h * u1
                    u2 = -initialConditions.Y_FACTOR * (Yi + h * k1) - initialConditions.Y_PRIME_FACTOR * (Pi + h * u1)
                }

                DifferentialEquationType.ORDER2_PENDULUM -> {
                    k1 = Pi
                    u1 = -initialConditions.Y_FACTOR * sin(Yi)
                    k2 = Pi + h * u1
                    u2 = -initialConditions.Y_FACTOR * sin(Yi + h * k1)
                }
            }
        }

        val advanceOneTimeStep = {
            //Yi+1 = Yi + 1/2 (k1 + k2) * h
            //Pi+1 = Pi + 1/2 (u1 + u2) * h
            Xi += h
            Yi += 0.5 * (k1 + k2) * h
            Pi += 0.5 * (u1 + u2) * h
        }

        //there are deltaT/h time-steps that are h-sized between start and stop, obviously
        for (i in 0..ceil(((stopTime - startTime)/h)).toInt())  {
            solution.add(dataPoint(Xi, Yi))
            updateHeunFactors()
            advanceOneTimeStep()
        }

        return solution
    }
}

