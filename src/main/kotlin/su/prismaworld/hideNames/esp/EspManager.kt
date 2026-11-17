package su.prismaworld.hideNames.esp

import com.github.retrooper.packetevents.PacketEvents
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import su.prismaworld.hideNames.name.NameTagHider
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks players that see every name tag. The preference is stored in the player's
 * persistent data container, so it survives reconnects and restarts.
 */
class EspManager(plugin: Plugin, private val nameTagHider: NameTagHider) {

    companion object {
        const val PERMISSION = "hidenames.esp"
    }

    private val key = NamespacedKey(plugin, "esp")

    // Read from Netty threads by the packet listener, written from the main thread.
    private val activeViewers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    fun isActive(uuid: UUID): Boolean = uuid in activeViewers

    /**
     * Toggles ESP for [player] and returns the new state.
     */
    fun toggle(player: Player): Boolean {
        val enable = !isActive(player.uniqueId)
        if (enable) {
            player.persistentDataContainer.set(key, PersistentDataType.BOOLEAN, true)
        } else {
            player.persistentDataContainer.remove(key)
        }
        apply(player, enable)
        return enable
    }

    /**
     * Restores the stored state of a player that has just joined. ESP is only
     * activated while the player still has the permission for it.
     */
    fun restore(player: Player) {
        val stored = player.persistentDataContainer.getOrDefault(key, PersistentDataType.BOOLEAN, false)
        if (stored && player.hasPermission(PERMISSION)) {
            apply(player, true)
        }
    }

    fun forget(player: Player) {
        activeViewers.remove(player.uniqueId)
    }

    private fun apply(player: Player, enable: Boolean) {
        // Update the state before sending, so the packet listener and the packet agree.
        if (enable) {
            activeViewers.add(player.uniqueId)
        } else {
            activeViewers.remove(player.uniqueId)
        }

        // The client already knows the team, so resend its options with the right visibility.
        val packet = nameTagHider.createUpdatePacket(revealNames = enable) ?: return
        PacketEvents.getAPI().playerManager.sendPacket(player, packet)
    }
}
