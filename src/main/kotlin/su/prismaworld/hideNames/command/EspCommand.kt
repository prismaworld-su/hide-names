package su.prismaworld.hideNames.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import su.prismaworld.hideNames.HideNames
import su.prismaworld.hideNames.esp.EspManager

/**
 * `/esp` — toggles the visibility of every player's name tag for the executing player only.
 */
class EspCommand(
    private val plugin: HideNames,
    private val espManager: EspManager,
) {

    fun create(): LiteralCommandNode<CommandSourceStack> = Commands.literal("esp")
        .requires { it.sender.hasPermission(EspManager.PERMISSION) }
        .executes { context ->
            val source = context.source
            val messages = plugin.settings.messages

            val player = source.executor as? Player
            if (player == null) {
                source.sender.sendRichMessage(messages.playersOnly)
                return@executes 0
            }

            val enabled = espManager.toggle(player)
            player.sendRichMessage(if (enabled) messages.espEnabled else messages.espDisabled)
            Command.SINGLE_SUCCESS
        }
        .build()
}
