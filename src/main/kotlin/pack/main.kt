package pack

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.physics.bullet.Bullet
import mlogger.MLogger

fun main(vararg args:String) {
    val logger = MLogger.logger
    Bullet.init()
    val config = Lwjgl3ApplicationConfiguration()
    config.setForegroundFPS(50)
    config.setTitle("GDX bullet")
    config.setWindowedMode(500, 500)
    Lwjgl3Application(AppAdapter(), config)
}