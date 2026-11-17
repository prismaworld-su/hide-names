package su.prismaworld.hideNames

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerCommon
import com.github.retrooper.packetevents.event.PacketListenerPriority
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import su.prismaworld.hideNames.command.EspCommand
import su.prismaworld.hideNames.command.HideNamesCommand
import su.prismaworld.hideNames.config.Settings
import su.prismaworld.hideNames.esp.EspManager
import su.prismaworld.hideNames.esp.EspTeamPacketListener
import su.prismaworld.hideNames.listener.PlayerConnectionListener
import su.prismaworld.hideNames.listener.PlayerInspectListener
import su.prismaworld.hideNames.name.NameTagHider
import su.prismaworld.hideNames.name.PlayerNameFormatter

class HideNames : JavaPlugin() {

    companion object {
        lateinit var instance: HideNames
            private set
    }

    lateinit var settings: Settings
        private set

    private lateinit var nameTagHider: NameTagHider
    private var packetListener: PacketListenerCommon? = null

    fun loadSettings() {
        saveDefaultConfig()
        reloadConfig()
        settings = Settings.load(config)
    }

    override fun onEnable() {
        instance = this

        loadSettings()

        nameTagHider = NameTagHider().also { it.enable() }
        val espManager = EspManager(this, nameTagHider)

        val placeholderApiEnabled = server.pluginManager.isPluginEnabled("PlaceholderAPI")
        if (!placeholderApiEnabled) {
            logger.info("PlaceholderAPI не найден: плейсхолдеры PlaceholderAPI обрабатываться не будут.")
        }
        val formatter = PlayerNameFormatter(placeholderApiEnabled)

        packetListener = PacketEvents.getAPI().eventManager
            .registerListener(EspTeamPacketListener(espManager), PacketListenerPriority.NORMAL)

        server.pluginManager.registerEvents(PlayerConnectionListener(nameTagHider, espManager), this)
        server.pluginManager.registerEvents(PlayerInspectListener(this, formatter), this)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()
            registrar.register(EspCommand(this, espManager).create(), "Показать или скрыть ники всех игроков")
            registrar.register(HideNamesCommand(this).create(), "Управление плагином HideNames")
        }

        // Covers a reload of the plugin while players are online.
        server.onlinePlayers.forEach { player ->
            nameTagHider.hide(player)
            espManager.restore(player)
        }
    }

    override fun onDisable() {
        packetListener?.let(PacketEvents.getAPI().eventManager::unregisterListener)
        packetListener = null

        if (::nameTagHider.isInitialized) {
            nameTagHider.disable()
        }
    }
}
