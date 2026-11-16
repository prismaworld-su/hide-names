package su.prismaworld.hideNames

import org.bukkit.plugin.java.JavaPlugin

class HideNames : JavaPlugin() {

    companion object {
        lateinit var instance: HideNames
            private set
    }

    fun loadSettings() {
        saveDefaultConfig()
        reloadConfig()
    }

    override fun onEnable() {
        instance = this

        loadSettings()
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
