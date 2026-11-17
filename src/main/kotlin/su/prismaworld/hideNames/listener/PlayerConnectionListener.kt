package su.prismaworld.hideNames.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import su.prismaworld.hideNames.esp.EspManager
import su.prismaworld.hideNames.name.NameTagHider

class PlayerConnectionListener(
    private val nameTagHider: NameTagHider,
    private val espManager: EspManager,
) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(event: PlayerJoinEvent) {
        nameTagHider.hide(event.player)
        espManager.restore(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        espManager.forget(event.player)
        // Keeps the main scoreboard free of entries for offline players.
        nameTagHider.remove(event.player)
    }
}
