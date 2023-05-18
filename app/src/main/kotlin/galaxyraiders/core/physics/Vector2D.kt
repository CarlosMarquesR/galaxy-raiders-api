@file:Suppress("UNUSED_PARAMETER") // <- REMOVE
package galaxyraiders.core.physics

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties("unit", "normal", "degree", "magnitude")
data class Vector2D(val dx: Double, val dy: Double) {
  override fun toString(): String {
    return "Vector2D(dx=$dx, dy=$dy)"
  }

  val magnitude: Double
    get() = Math.sqrt(Math.pow(this.dx, 2.0)
            + Math.pow(this.dy, 2.0))

  val radiant: Double
    get() = angle()

  val degree: Double
    get() = Math.abs(this.radiant * 180.0 / Math.PI)

  val unit: Vector2D
    get() = Vector2D(this.dx / this.magnitude,
                    this.dy / this.magnitude)

  val normal: Vector2D
    get() = Vector2D(this.dy, -this.dx)

  operator fun times(scalar: Double): Vector2D {
    return Vector2D(this.dx * scalar, this.dy * scalar)
  }

  operator fun div(scalar: Double): Vector2D {
    return Vector2D(this.dx / scalar, this.dy / scalar)
  }

  operator fun times(v: Vector2D): Double {
    return this.dx * v.dx + this.dy * v.dy
  }

  operator fun plus(v: Vector2D): Vector2D {
    return Vector2D(this.dx + v.dx, this.dy + v.dy)
  }

  operator fun plus(p: Point2D): Point2D {
    return Point2D(this.dx + p.x, this.dy + p.y)
  }

  operator fun unaryMinus(): Vector2D {
    return Vector2D(-(this.dx), -(this.dy))
  }

  operator fun minus(v: Vector2D): Vector2D {
    return Vector2D(this.dx - v.dx, this.dy - v.dy)
  }

  fun scalarProject(target: Vector2D): Double {
    val dotProduct: Double = this * target
    return dotProduct / target.magnitude
  }

  fun vectorProject(target: Vector2D): Vector2D {
    val scalarProj: Double = this.scalarProject(target)
    return scalarProj * target.unit
  }

  private fun angle(): Double {
    var cosine: Double = this.dx / this.magnitude
    return Math.acos(cosine)
  }
}

operator fun Double.times(v: Vector2D): Vector2D {
  return Vector2D(this * v.dx, this * v.dy)
}