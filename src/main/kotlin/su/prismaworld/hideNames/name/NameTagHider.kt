package su.prismaworld.hideNames.name

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
}
