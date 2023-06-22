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

class Asteroid(
  initialPosition: Point2D,
  initialVelocity: Vector2D,
  radius: Double,
  mass: Double,
  isTriggered: Boolean
) :
  SpaceObject("Asteroid", 'x', initialPosition, initialVelocity, radius, mass)