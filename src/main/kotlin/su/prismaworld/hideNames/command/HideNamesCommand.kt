package su.prismaworld.hideNames.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import su.prismaworld.hideNames.HideNames

/**
 * `/hidenames reload` — reloads `config.yml`.
 */
class HideNamesCommand(private val plugin: HideNames) {

    companion object {
        const val RELOAD_PERMISSION = "hidenames.reload"
    }

    fun create(): LiteralCommandNode<CommandSourceStack> = Commands.literal("hidenames")
        .requires { it.sender.hasPermission(RELOAD_PERMISSION) }
        .then(
            Commands.literal("reload").executes { context ->
                plugin.loadSettings()
                context.source.sender.sendRichMessage(plugin.settings.messages.reloaded)
                Command.SINGLE_SUCCESS
            },
        )
        .build()
}
