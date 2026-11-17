package su.prismaworld.hideNames.name

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team

/**
 * Hides player name tags by putting every online player into a team of the main scoreboard
 * whose name tag visibility is [Team.OptionStatus.NEVER].
 */
class NameTagHider {

    companion object {
        const val TEAM_NAME = "hide_names"
    }

    private var team: Team? = null

    fun enable() {
        val scoreboard = Bukkit.getScoreboardManager().mainScoreboard

        // A team left over from an unclean shutdown may hold stale entries or edited options.
        scoreboard.getTeam(TEAM_NAME)?.unregister()

        team = scoreboard.registerNewTeam(TEAM_NAME).apply {
            setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
            // Vanilla teams make invisible teammates translucent; everyone is a "teammate" here.
            setCanSeeFriendlyInvisibles(false)
        }
    }

    fun disable() {
        val team = team ?: return
        this.team = null

        if (Bukkit.getScoreboardManager().mainScoreboard.getTeam(TEAM_NAME) === team) {
            team.unregister()
        }
    }

    fun hide(player: Player) {
        team?.addEntity(player)
    }

    fun remove(player: Player) {
        team?.removeEntity(player)
    }

    /**
     * Builds a team update packet describing the current team state. When [revealNames] is set,
     * the name tag visibility is replaced with [WrapperPlayServerTeams.NameTagVisibility.ALWAYS].
     */
    fun createUpdatePacket(revealNames: Boolean): WrapperPlayServerTeams? {
        val team = team ?: return null
        val visibility = if (revealNames) {
            WrapperPlayServerTeams.NameTagVisibility.ALWAYS
        } else {
            team.getOption(Team.Option.NAME_TAG_VISIBILITY).toNameTagVisibility()
        }
        return WrapperPlayServerTeams(TEAM_NAME, WrapperPlayServerTeams.TeamMode.UPDATE, createTeamInfo(team, visibility))
    }

    private fun createTeamInfo(
        team: Team,
        visibility: WrapperPlayServerTeams.NameTagVisibility,
    ) = WrapperPlayServerTeams.ScoreBoardTeamInfo(
        team.displayName(),
        team.prefix(),
        team.suffix(),
        visibility,
        team.getOption(Team.Option.COLLISION_RULE).toCollisionRule(),
        if (team.hasColor()) NamedTextColor.nearestTo(team.color()) else null,
        optionData(team.allowFriendlyFire(), team.canSeeFriendlyInvisibles()),
    )

    private fun optionData(friendlyFire: Boolean, seeFriendlyInvisibles: Boolean) = when {
        friendlyFire && seeFriendlyInvisibles -> WrapperPlayServerTeams.OptionData.ALL
        friendlyFire -> WrapperPlayServerTeams.OptionData.FRIENDLY_FIRE
        seeFriendlyInvisibles -> WrapperPlayServerTeams.OptionData.FRIENDLY_CAN_SEE_INVISIBLE
        else -> WrapperPlayServerTeams.OptionData.NONE
    }

    private fun Team.OptionStatus.toNameTagVisibility() = when (this) {
        Team.OptionStatus.ALWAYS -> WrapperPlayServerTeams.NameTagVisibility.ALWAYS
        Team.OptionStatus.NEVER -> WrapperPlayServerTeams.NameTagVisibility.NEVER
        Team.OptionStatus.FOR_OTHER_TEAMS -> WrapperPlayServerTeams.NameTagVisibility.HIDE_FOR_OTHER_TEAMS
        Team.OptionStatus.FOR_OWN_TEAM -> WrapperPlayServerTeams.NameTagVisibility.HIDE_FOR_OWN_TEAM
    }

    private fun Team.OptionStatus.toCollisionRule() = when (this) {
        Team.OptionStatus.ALWAYS -> WrapperPlayServerTeams.CollisionRule.ALWAYS
        Team.OptionStatus.NEVER -> WrapperPlayServerTeams.CollisionRule.NEVER
        Team.OptionStatus.FOR_OTHER_TEAMS -> WrapperPlayServerTeams.CollisionRule.PUSH_OTHER_TEAMS
        Team.OptionStatus.FOR_OWN_TEAM -> WrapperPlayServerTeams.CollisionRule.PUSH_OWN_TEAM
    }
}
