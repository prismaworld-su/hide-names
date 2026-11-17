package su.prismaworld.hideNames.listener

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import su.prismaworld.hideNames.HideNames
import su.prismaworld.hideNames.name.PlayerNameFormatter

/**
 * Shows the name of a player in the action bar when another player right-clicks them.
 */
class PlayerInspectListener(
    private val plugin: HideNames,
    private val formatter: PlayerNameFormatter,
) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onInteract(event: PlayerInteractEntityEvent) {
        // The client sends one interaction per hand; react to the main hand only.
        if (event.hand != EquipmentSlot.HAND) return

        val target = event.rightClicked as? Player ?: return
        // NPC plugins (Citizens and others) mark their fake players with this metadata.
        if (target.hasMetadata("NPC")) return

        val viewer = event.player
        if (!viewer.canSee(target)) return

        viewer.sendActionBar(formatter.format(plugin.settings.actionBarFormat, viewer, target))
    }
}
