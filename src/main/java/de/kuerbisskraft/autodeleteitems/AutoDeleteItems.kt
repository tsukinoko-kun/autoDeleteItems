package de.kuerbisskraft.autodeleteitems

import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.concurrent.timerTask

class AutoDeleteItems : JavaPlugin() {
    private lateinit var purger: Purger
    private val timer = Timer()

    override fun onEnable() {
        purger = Purger(this)

        timer.schedule(timerTask {
            purger.tick()
        }, 1000, 1000)
    }

    override fun onDisable() {
        timer.cancel()
        timer.purge()
    }
}
