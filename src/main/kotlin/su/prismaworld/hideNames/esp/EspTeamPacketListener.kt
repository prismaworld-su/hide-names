package su.prismaworld.hideNames.esp

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams
import su.prismaworld.hideNames.name.NameTagHider

/**
 * Makes the hidden-names team show name tags for ESP viewers only, by rewriting the
 * name tag visibility of every team packet sent to them. Runs on Netty threads.
 */
class EspTeamPacketListener(private val espManager: EspManager) : PacketListener {

    override fun onPacketSend(event: PacketSendEvent) {
        if (event.packetType != PacketType.Play.Server.TEAMS) return

        val uuid = event.user.uuid ?: return
        if (!espManager.isActive(uuid)) return

        val packet = WrapperPlayServerTeams(event)
        if (packet.teamName != NameTagHider.TEAM_NAME) return

        val info = packet.teamInfo.orElse(null) ?: return
        if (info.tagVisibility == WrapperPlayServerTeams.NameTagVisibility.ALWAYS) return

        info.tagVisibility = WrapperPlayServerTeams.NameTagVisibility.ALWAYS
        event.markForReEncode(true)
    }
}
