package galaxyraiders.core.game

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import galaxyraiders.Config
import galaxyraiders.ports.RandomGenerator
import galaxyraiders.ports.ui.Controller
import galaxyraiders.ports.ui.Controller.PlayerCommand
import galaxyraiders.ports.ui.Visualizer
import kotlin.system.measureTimeMillis
import java.io.File
import java.io.StringReader

const val MILLISECONDS_PER_SECOND: Int = 1000

object GameEngineConfig {
  private val config = Config(prefix = "GR__CORE__GAME__GAME_ENGINE__")

  val frameRate = config.get<Int>("FRAME_RATE")
  val spaceFieldWidth = config.get<Int>("SPACEFIELD_WIDTH")
  val spaceFieldHeight = config.get<Int>("SPACEFIELD_HEIGHT")
  val asteroidProbability = config.get<Double>("ASTEROID_PROBABILITY")
  val coefficientRestitution = config.get<Double>("COEFFICIENT_RESTITUTION")

  val msPerFrame: Int = MILLISECONDS_PER_SECOND / this.frameRate
}

@Suppress("TooManyFunctions")
class GameEngine(
  val generator: RandomGenerator,
  val controller: Controller,
  val visualizer: Visualizer,
) {
  val field = SpaceField(
    width = GameEngineConfig.spaceFieldWidth,
    height = GameEngineConfig.spaceFieldHeight,
    generator = generator
  )

  var playing = true

  var gameScore: Double = 0.0
  var destroyedAsteroids = 0

  var scoreboardFile = File("src/main/kotlin/galaxyraiders/core/score/Scoreboard.json")
  var leaderboardFile = File("src/main/kotlin/galaxyraiders/core/score/Leaderboard.json")
  var scoreboardJSON = JsonObject()
  var leaderboardJSON = JsonObject()

  fun execute() {

    this.updateJSONs()

    while (true) {
      val duration = measureTimeMillis { this.tick() }

      Thread.sleep(
        maxOf(0, GameEngineConfig.msPerFrame - duration)
      )
    }
  }

  fun execute(maxIterations: Int) {
    repeat(maxIterations) {
      this.tick()
    }
  }

  fun tick() {
    this.processPlayerInput()
    this.updateSpaceObjects()
    this.renderSpaceField()
  }

  fun processPlayerInput() {
    this.controller.nextPlayerCommand()?.also {
      when (it) {
        PlayerCommand.MOVE_SHIP_UP ->
          this.field.ship.boostUp()
        PlayerCommand.MOVE_SHIP_DOWN ->
          this.field.ship.boostDown()
        PlayerCommand.MOVE_SHIP_LEFT ->
          this.field.ship.boostLeft()
        PlayerCommand.MOVE_SHIP_RIGHT ->
          this.field.ship.boostRight()
        PlayerCommand.LAUNCH_MISSILE ->
          this.field.generateMissile()
        PlayerCommand.PAUSE_GAME ->
          this.playing = !this.playing
      }
    }
  }

  fun updateSpaceObjects() {
    if (!this.playing) return
    this.handleCollisions()
    this.moveSpaceObjects()
    this.trimSpaceObjects()
    this.generateAsteroids()
  }

  fun handleCollisions() {
    this.field.spaceObjects.forEachPair {
        (first, second) ->
      if (first.impacts(second)) {
        if ((first is Asteroid) and (second is Missile) or 
            (first is Missile) and (second is Asteroid)) {
              var asteroid = if (first is Asteroid) first else second as Asteroid
              var missile = if (first is Missile) first else second as Missile
              field.generateExplosion(missile.center, missile.radius)
              field.removeAsteroid(asteroid)
              field.removeMissile(missile)
              this.destroyedAsteroids += 1
              this.gameScore += (asteroid.radius * asteroid.mass)
              this.updateJSONs()
        }
        first.collideWith(second, GameEngineConfig.coefficientRestitution)
      }
    }
  }

  fun checkJSONsExistence() {
    if (!this.scoreboardFile.exists()) this.scoreboardFile.createNewFile()
    if (this.scoreboardFile.readText().isEmpty()) this.scoreboardFile.writeText("{}")

    if (!this.leaderboardFile.exists()) this.leaderboardFile.createNewFile()
    if (this.leaderboardFile.readText().isEmpty()) this.leaderboardFile.writeText("{}")
  }
  
  fun updateJSONs() {
    
    this.checkJSONsExistence()
    
    this.scoreboardJSON = Klaxon().parseJsonObject(this.scoreboardFile.reader())
    this.leaderboardJSON = Klaxon().parseJsonObject(this.leaderboardFile.reader())

    var line = "{\"score\": ${this.gameScore}, \"destroyedAsteroids\": ${this.destroyedAsteroids}}"
    this.scoreboardJSON = Klaxon().parseJsonObject(StringReader(line))
    this.scoreboardFile.writeText(this.scoreboardJSON.toJsonString(prettyPrint = true))

    var sorted = this.scoreboardJSON.toSortedMap((compareByDescending { this.scoreboardJSON.obj(it)?.int("score") }))
    var top3 = sorted.toList().take(3)
    this.leaderboardJSON.clear()
    this.leaderboardJSON.putAll(top3)
    this.leaderboardFile.writeText(this.leaderboardJSON.toJsonString(prettyPrint = true))
  }

  fun moveSpaceObjects() {
    this.field.moveShip()
    this.field.moveAsteroids()
    this.field.moveMissiles()
  }

  fun trimSpaceObjects() {
    this.field.trimAsteroids()
    this.field.trimMissiles()
  }

  fun generateAsteroids() {
    val probability = generator.generateProbability()

    if (probability <= GameEngineConfig.asteroidProbability) {
      this.field.generateAsteroid()
    }
  }

  fun renderSpaceField() {
    this.visualizer.renderSpaceField(this.field)
  }
}

fun <T> List<T>.forEachPair(action: (Pair<T, T>) -> Unit) {
  for (i in 0 until this.size) {
    for (j in i + 1 until this.size) {
      action(Pair(this[i], this[j]))
    }
  }
}
