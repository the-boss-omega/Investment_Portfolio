import kotlin.math.*
import kotlin.random.Random

// ============================================================================
// 1. CORE MATHEMATICS (Vectors, Matrices, Quaternions)
// ============================================================================

data class Vector3(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) {
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Double) = Vector3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Double) = Vector3(x / scalar, y / scalar, z / scalar)
    operator fun unaryMinus() = Vector3(-x, -y, -z)

    fun dot(other: Vector3): Double = x * other.x + y * other.y + z * other.z
    fun cross(other: Vector3): Vector3 = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun magnitudeSquared(): Double = x * x + y * y + z * z
    fun magnitude(): Double = sqrt(magnitudeSquared())
    fun normalized(): Vector3 {
        val mag = magnitude()
        return if (mag > 1e-8) this / mag else Vector3()
    }

    override fun toString() = "Vec3(%.3f, %.3f, %.3f)".format(x, y, z)
}

data class Quaternion(var w: Double = 1.0, var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) {
    fun normalize(): Quaternion {
        val mag = sqrt(w * w + x * x + y * y + z * z)
        return if (mag > 1e-8) Quaternion(w / mag, x / mag, y / mag, z / mag) else Quaternion()
    }

    operator fun times(q: Quaternion): Quaternion {
        return Quaternion(
            w * q.w - x * q.x - y * q.y - z * q.z,
            w * q.x + x * q.w + y * q.z - z * q.y,
            w * q.y - x * q.z + y * q.w + z * q.x,
            w * q.z + x * q.y - y * q.x + z * q.w
        )
    }

    fun rotate(v: Vector3): Vector3 {
        val qVec = Vector3(x, y, z)
        val uv = qVec.cross(v)
        val uuv = qVec.cross(uv)
        return v + uv * (2.0 * w) + uuv * 2.0
    }
}

class Matrix3(val m: DoubleArray = DoubleArray(9)) {
    operator fun get(row: Int, col: Int) = m[row * 3 + col]
    operator fun set(row: Int, col: Int, value: Double) { m[row * 3 + col] = value }

    operator fun times(v: Vector3): Vector3 {
        return Vector3(
            this[0, 0] * v.x + this[0, 1] * v.y + this[0, 2] * v.z,
            this[1, 0] * v.x + this[1, 1] * v.y + this[1, 2] * v.z,
            this[2, 0] * v.x + this[2, 1] * v.y + this[2, 2] * v.z
        )
    }

    fun inverse(): Matrix3 {
        val det = this[0,0] * (this[1,1] * this[2,2] - this[2,1] * this[1,2]) -
                  this[0,1] * (this[1,0] * this[2,2] - this[1,2] * this[2,0]) +
                  this[0,2] * (this[1,0] * this[2,1] - this[1,1] * this[2,0])
        val inv = Matrix3()
        if (abs(det) < 1e-8) return inv 
        val invDet = 1.0 / det
        inv[0,0] = (this[1,1] * this[2,2] - this[2,1] * this[1,2]) * invDet
        inv[0,1] = (this[0,2] * this[2,1] - this[0,1] * this[2,2]) * invDet
        inv[0,2] = (this[0,1] * this[1,2] - this[0,2] * this[1,1]) * invDet
        inv[1,0] = (this[1,2] * this[2,0] - this[1,0] * this[2,2]) * invDet
        inv[1,1] = (this[0,0] * this[2,2] - this[0,2] * this[2,0]) * invDet
        inv[1,2] = (this[1,0] * this[0,2] - this[0,0] * this[1,2]) * invDet
        inv[2,0] = (this[1,0] * this[2,1] - this[2,0] * this[1,1]) * invDet
        inv[2,1] = (this[2,0] * this[0,1] - this[0,0] * this[2,1]) * invDet
        inv[2,2] = (this[0,0] * this[1,1] - this[1,0] * this[0,1]) * invDet
        return inv
    }
}

// ============================================================================
// 2. PROCEDURAL TERRAIN (Perlin Noise)
// ============================================================================

class ProceduralHill(val seed: Int = 42, val scale: Double = 0.05, val amplitude: Double = 20.0) {
    private val p = IntArray(512)

    init {
        val random = Random(seed)
        val permutation = IntArray(256) { it }.apply { shuffle(random) }
        for (i in 0..255) {
            p[i] = permutation[i]
            p[i + 256] = permutation[i]
        }
    }

    private fun fade(t: Double) = t * t * t * (t * (t * 6 - 15) + 10)
    private fun lerp(t: Double, a: Double, b: Double) = a + t * (b - a)
    private fun grad(hash: Int, x: Double, z: Double): Double {
        val h = hash and 15
        val u = if (h < 8) x else z
        val v = if (h < 4) z else if (h == 12 || h == 14) x else 0.0
        return (if (h and 1 == 0) u else -u) + (if (h and 2 == 0) v else -v)
    }

    fun getHeight(x: Double, z: Double): Double {
        val nx = x * scale
        val nz = z * scale
        val X = floor(nx).toInt() and 255
        val Z = floor(nz).toInt() and 255
        val xf = nx - floor(nx)
        val zf = nz - floor(nz)
        
        val u = fade(xf)
        val v = fade(zf)

        val a = p[X] + Z
        val aa = p[a]
        val ab = p[a + 1]
        val b = p[X + 1] + Z
        val ba = p[b]
        val bb = p[b + 1]

        val res = lerp(v, lerp(u, grad(p[aa], xf, zf), grad(p[ba], xf - 1, zf)),
                          lerp(u, grad(p[ab], xf, zf - 1), grad(p[bb], xf - 1, zf - 1)))
        
        // Tilt the hill downwards along the Z axis to create a slope
        val slope = -z * 0.2 
        return (res * amplitude) + slope
    }

    fun getNormal(x: Double, z: Double): Vector3 {
        val eps = 0.01
        val hL = getHeight(x - eps, z)
        val hR = getHeight(x + eps, z)
        val hD = getHeight(x, z - eps)
        val hU = getHeight(x, z + eps)
        
        val normal = Vector3(-(hR - hL), 2 * eps, -(hU - hD))
        return normal.normalized()
    }
}

// ============================================================================
// 3. PHYSICS CORE (Rigidbodies, Materials, Contacts)
// ============================================================================

data class Material(
    val restitution: Double = 0.4, // Bounciness
    val staticFriction: Double = 0.6,
    val dynamicFriction: Double = 0.4,
    val rollingResistance: Double = 0.05
)

class RigidBody(
    var position: Vector3,
    val radius: Double,
    val mass: Double,
    val material: Material
) {
    var velocity = Vector3()
    var angularVelocity = Vector3()
    var orientation = Quaternion()
    
    val invMass = if (mass > 0) 1.0 / mass else 0.0
    
    // Inertia tensor for a solid sphere: I = 2/5 * m * r^2
    private val inertia = (2.0 / 5.0) * mass * (radius * radius)
    val invInertia = if (mass > 0) 1.0 / inertia else 0.0
    
    var forceAccumulator = Vector3()
    var torqueAccumulator = Vector3()

    fun applyForce(force: Vector3, point: Vector3? = null) {
        forceAccumulator += force
        if (point != null) {
            torqueAccumulator += (point - position).cross(force)
        }
    }

    fun integrate(dt: Double) {
        if (invMass <= 0.0) return

        // Linear integration (Semi-Implicit Euler)
        val acceleration = forceAccumulator * invMass
        velocity += acceleration * dt
        position += velocity * dt

        // Angular integration
        val angularAcceleration = torqueAccumulator * invInertia
        angularVelocity += angularAcceleration * dt
        
        // Update orientation quaternion
        val qMag = angularVelocity.magnitude()
        if (qMag > 0.0) {
            val angle = qMag * dt
            val axis = angularVelocity / qMag
            val halfAngle = angle * 0.5
            val sinHalf = sin(halfAngle)
            val dq = Quaternion(cos(halfAngle), axis.x * sinHalf, axis.y * sinHalf, axis.z * sinHalf)
            orientation = (dq * orientation).normalize()
        }

        // Clear accumulators
        forceAccumulator = Vector3()
        torqueAccumulator = Vector3()
    }
}

data class Contact(
    val point: Vector3,
    val normal: Vector3,
    val penetration: Double
)

// ============================================================================
// 4. PHYSICS WORLD & SOLVER
// ============================================================================

class PhysicsWorld(val dt: Double = 0.016) {
    val gravity = Vector3(0.0, -9.81, 0.0)
    val airDensity = 1.225
    val terrain = ProceduralHill()
    
    fun step(body: RigidBody) {
        applyEnvironmentalForces(body)
        body.integrate(dt)
        resolveCollisions(body)
    }

    private fun applyEnvironmentalForces(body: RigidBody) {
        // Gravity
        body.applyForce(gravity * body.mass)

        // Aerodynamic Drag: Fd = 0.5 * p * v^2 * Cd * A
        val speedSq = body.velocity.magnitudeSquared()
        if (speedSq > 0.0) {
            val dragCoeff = 0.47 // Sphere drag coefficient
            val area = PI * body.radius * body.radius
            val dragMag = 0.5 * airDensity * speedSq * dragCoeff * area
            val dragForce = body.velocity.normalized() * -dragMag
            body.applyForce(dragForce)
        }

        // Magnus Effect (Lift generated by spin): Fl = 8/3 * PI * r^3 * p * (w x v)
        val magnusFactor = (8.0 / 3.0) * PI * (body.radius * body.radius * body.radius) * airDensity
        val magnusForce = body.angularVelocity.cross(body.velocity) * magnusFactor
        body.applyForce(magnusForce)
    }

    private fun resolveCollisions(body: RigidBody) {
        val terrainY = terrain.getHeight(body.position.x, body.position.z)
        val distanceToTerrain = body.position.y - terrainY

        if (distanceToTerrain < body.radius) {
            // Collision detected!
            val normal = terrain.getNormal(body.position.x, body.position.z)
            val penetration = body.radius - distanceToTerrain
            val contactPoint = body.position - (normal * body.radius)
            val contact = Contact(contactPoint, normal, penetration)

            // Positional correction (Baumgarte Stabilization)
            val percent = 0.8
            val slop = 0.01
            val correctionMag = max(contact.penetration - slop, 0.0) * percent
            body.position += contact.normal * correctionMag

            // Impulse Resolution
            val r = contact.point - body.position
            val vAtContact = body.velocity + body.angularVelocity.cross(r)
            val vRelNormal = vAtContact.dot(contact.normal)

            if (vRelNormal > 0) return // Moving apart

            // 1. Normal Impulse (Bounce)
            val e = body.material.restitution
            var j = -(1.0 + e) * vRelNormal
            val rCrossN = r.cross(contact.normal)
            val invInertiaTerm = rCrossN.dot(rCrossN) * body.invInertia
            j /= (body.invMass + invInertiaTerm)

            val normalImpulse = contact.normal * j
            body.velocity += normalImpulse * body.invMass
            body.angularVelocity += r.cross(normalImpulse) * body.invInertia

            // 2. Tangential Impulse (Friction)
            val newVAtContact = body.velocity + body.angularVelocity.cross(r)
            var tangent = newVAtContact - (contact.normal * newVAtContact.dot(contact.normal))
            val tangentLen = tangent.magnitude()
            
            if (tangentLen > 1e-6) {
                tangent /= tangentLen
                var jt = -newVAtContact.dot(tangent)
                val rCrossT = r.cross(tangent)
                val invInertiaTermT = rCrossT.dot(rCrossT) * body.invInertia
                jt /= (body.invMass + invInertiaTermT)

                val frictionImpulse: Vector3
                if (abs(jt) < j * body.material.staticFriction) {
                    frictionImpulse = tangent * jt
                } else {
                    frictionImpulse = tangent * (-j * body.material.dynamicFriction)
                }

                body.velocity += frictionImpulse * body.invMass
                body.angularVelocity += r.cross(frictionImpulse) * body.invInertia
                
                // 3. Rolling Resistance (dampens angular velocity over time)
                body.angularVelocity -= body.angularVelocity * body.material.rollingResistance * dt
            }
        }
    }
}

// ============================================================================
// 5. SIMULATION EXECUTION
// ============================================================================

fun main() {
    println("Initializing Complex Physics Simulation...")
    val world = PhysicsWorld(dt = 0.016) // 60 FPS standard step
    
    // Create a steel ball
    val steelMaterial = Material(restitution = 0.3, staticFriction = 0.5, dynamicFriction = 0.35, rollingResistance = 0.02)
    val ball = RigidBody(
        position = Vector3(0.0, 50.0, 0.0), // Start high up
        radius = 1.0, 
        mass = 10.0, 
        material = steelMaterial
    )
    
    // Give it an initial forward spin to demonstrate the Magnus effect
    ball.angularVelocity = Vector3(10.0, 0.0, 0.0) 

    val totalTimeSteps = 500
    println("--- Simulation Started ---")
    println("Format: [Step] Pos(x, y, z) | Vel(x, y, z) | Terrain Height")
    
    for (step in 1..totalTimeSteps) {
        world.step(ball)
        
        // Output every 10 steps to keep the console readable
        if (step % 20 == 0) {
            val tY = world.terrain.getHeight(ball.position.x, ball.position.z)
            println("[Step ${step.toString().padStart(4)}] Pos: ${ball.position} | Vel: ${ball.velocity} | Terrain Y: %.3f".format(tY))
        }
    }
    println("--- Simulation Complete ---")
    println("Final Position: ${ball.position}")
}