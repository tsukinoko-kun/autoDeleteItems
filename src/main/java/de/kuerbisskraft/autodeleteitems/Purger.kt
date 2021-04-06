package de.kuerbisskraft.autodeleteitems

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.EntityType
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class Purger(plugin: Plugin) {
    private val filePath: String
    private val settings: Settings
    private var clock = -1

    init {
        val dir = "plugins/${plugin.name}/"
        filePath = dir + "settings.json"
        val path = File(dir)
        if (!path.exists()) {
            File(dir).mkdirs()
        }

        val gson = Gson()

        val file = File(filePath)
        if (file.exists()) {
            // Read File
            val fr = FileReader(filePath)
            val json = fr.readText()
            fr.close()

            // Parse Json
            val type = object : TypeToken<Settings>() {}.type
            settings = gson.fromJson(json, type)
        } else {
            settings = Settings(
                300,
                "${ChatColor.DARK_GRAY}[${ChatColor.DARK_AQUA}${plugin.server.name}${ChatColor.DARK_GRAY}]»",
                arrayOf(),
                "de"
            )
            val json = gson.toJson(settings)
            val fw = FileWriter(filePath)
            fw.write(json)
            fw.close()
        }
    }

    fun tick() {
        if (clock >= settings.mainLoop) {
            purge()
            clock = -1
        } else {
            val timeLeft = settings.mainLoop - clock
            if (timeLeft == 60 || timeLeft == 20) {
                for (player in Bukkit.getOnlinePlayers()) {
                    player.sendMessage(getDeleteWarning(timeLeft, player.locale))
                }
            }
        }
        clock++
    }

    private fun getDeleteWarning(timeLeft: Int, locale: String): String {
        return if (settings.language == "de" || locale.toLowerCase().contains("de")) {
            "${settings.prefix}${ChatColor.DARK_RED}WARNUNG!${ChatColor.RED} Alle liegenden Items werden in ${ChatColor.DARK_RED}$timeLeft${ChatColor.RED} Sekunden entfernt"
        } else {
            "${settings.prefix}${ChatColor.DARK_RED}WARNING!${ChatColor.RED} All lying items are removed in ${ChatColor.DARK_RED}$timeLeft${ChatColor.RED} seconds"
        }
    }

    private fun getDeletedInfo(removeCounter: Int, locale: String): String {
        return if (settings.language == "de" || locale.toLowerCase().contains("de")) {
            "${settings.prefix}${ChatColor.AQUA}Es wurden ${ChatColor.DARK_GREEN}$removeCounter${ChatColor.AQUA} Items entfernt"
        } else {
            "${settings.prefix}${ChatColor.AQUA} ${ChatColor.DARK_GREEN}$removeCounter${ChatColor.AQUA} items were removed"
        }
    }

    private fun purge() {
        var removeCounter = 0

        for (world in Bukkit.getWorlds()) {
            if (!settings.worldsToIgnore.contains(world.name)) {
                for (entity in world.entities) {
                    if (entity.type == EntityType.DROPPED_ITEM && entity.isOnGround) {
                        entity.remove()
                        removeCounter++
                    }
                }
            }
        }

        if (removeCounter > 0) {
            for (player in Bukkit.getOnlinePlayers()) {
                player.sendMessage(getDeletedInfo(removeCounter, player.locale))
            }
        }
    }
}
