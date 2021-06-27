package dz.lightyearsoftworks.zunburuk

import kotlin.math.sin


/*for generating sound we need about 22050Hz/44100Hz sampling, much crisper audio is achieved with higher rates
* but some systems just don't like those sampling rates for whatever reason*/

/**Represents a Differential Equation system so to speak, it takes in an equation type, initial conditions
 * and a given start time and time-step (h), the next state of the system (n+1) can be obtained with nextDataPoint()*/
class DifferentialSolver(eqnType: DifferentialEquationType, initialConditions: EquationParameters,
                         startTime: Double, timeStep: Double)  {
    private var Xi = startTime
    private var Yi = initialConditions.Y_NAUGHT
    private var Pi = initialConditions.Y_PRIME_NAUGHT
    private var k1 = 0.0
    private var k2 = 0.0
    private var u1 = 0.0
    private var u2 = 0.0

    private val updateHeunFactors = {
        when (eqnType) {
            DifferentialEquationType.ORDER2_UNDAMPED -> {
                k1 = Pi
                u1 = -initialConditions.Y_FACTOR * Yi
                k2 = Pi + timeStep * u1
                u2 = -initialConditions.Y_FACTOR * (Yi + timeStep * k1)
            }

            DifferentialEquationType.ORDER2_DAMPED -> {
                k1 = Pi
                u1 = -initialConditions.Y_FACTOR * Yi - initialConditions.Y_PRIME_FACTOR * Pi
                k2 = Pi + timeStep * u1
                u2 = -initialConditions.Y_FACTOR * (Yi + timeStep * k1) - initialConditions.Y_PRIME_FACTOR * (Pi + timeStep * u1)
            }

            DifferentialEquationType.ORDER2_PENDULUM -> {
                k1 = Pi
                u1 = -initialConditions.Y_FACTOR * sin(Yi)
                k2 = Pi + timeStep * u1
                u2 = -initialConditions.Y_FACTOR * sin(Yi + timeStep * k1)
            }
        }
    }

    private val advanceOneTimeStep = {
        //Yi+1 = Yi + 1/2 (k1 + k2) * h
        //Pi+1 = Pi + 1/2 (u1 + u2) * h
        Xi += timeStep
        Yi += 0.5 * (k1 + k2) * timeStep
        Pi += 0.5 * (u1 + u2) * timeStep
    }

    fun nextDataPoint(): ODEDataPoint {
        val result = ODEDataPoint(Xi, Yi)
        updateHeunFactors()
        advanceOneTimeStep()
        return result
    }
}

