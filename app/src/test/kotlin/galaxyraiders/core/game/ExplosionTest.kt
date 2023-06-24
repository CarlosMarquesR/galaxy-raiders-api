/************************************************************************************
 *  MACO218 - Técnicas de Programação II
 *  Profº Dr. Alfredo Goldman vel Lejbman
 *  Carlos Alberto Marques Rabelo - NUSP: 12623946
 *  EP2
 *  25/06/2023
 ************************************************************************************/

package galaxyraiders.core.game

import galaxyraiders.core.physics.Point2D
import galaxyraiders.core.physics.Vector2D
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("Given an explosion")
class ExplosionTest {
  private val explosion = Explosion(
    initialPosition = Point2D(1.0, 1.0),
    initialVelocity = Vector2D(1.0, 0.0),
    radius = 1.0,
    mass = 1.0,
    isTriggered = true
  )

  @Test
  fun `it has a type Asteroid `() {
    assertEquals("Explosion", explosion.type)
  }

  @Test
  fun `it has a symbol x `() {
    assertEquals('x', explosion.symbol)
  }

  @Test
  fun `it shows the type Explosion when converted to String `() {
    assertTrue(explosion.toString().contains("Explosion"))
  }

  @Test
  fun `its isTriggered value is true`() {
    assertTrue(explosion.isTriggered == true)
  } 
}
