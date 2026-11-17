package su.prismaworld.hideNames.name

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player

/**
 * Renders a MiniMessage template that may contain `%displayName%` and PlaceholderAPI placeholders.
 *
 * Every placeholder value is inserted as a separate component instead of being pasted into the
 * template, so values can never inject MiniMessage tags, and legacy colour codes returned by
 * PlaceholderAPI expansions (`&a`, `§a`, `&#rrggbb`, `&x&r&r&g&g&b&b`) are rendered correctly.
 */
class PlayerNameFormatter(private val placeholderApiEnabled: Boolean) {

    private companion object {
        const val DISPLAY_NAME = "displayName"
        const val RELATIONAL_PREFIX = "rel_"
        const val TAG_PREFIX = "hidenames_placeholder_"

        val PLACEHOLDER = Regex("%([^%\\s]+)%")

        val LEGACY: LegacyComponentSerializer = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.AMPERSAND_CHAR)
            .hexColors()
            .build()
    }

    /**
     * @param viewer the player who clicked; used for relational placeholders
     * @param target the player whose name is shown; placeholders are resolved for this player
     */
    fun format(template: String, viewer: Player, target: Player): Component {
        val resolvers = mutableListOf<TagResolver>()

        val prepared = PLACEHOLDER.replace(template) { match ->
            val value = resolve(match.value, match.groupValues[1], viewer, target)
                ?: return@replace match.value

            val tag = TAG_PREFIX + resolvers.size
            resolvers += Placeholder.component(tag, value)
            "<$tag>"
        }

        return MiniMessage.miniMessage().deserialize(prepared, TagResolver.resolver(resolvers))
    }

    private fun resolve(placeholder: String, identifier: String, viewer: Player, target: Player): Component? {
        if (identifier == DISPLAY_NAME) return target.displayName()
        if (!placeholderApiEnabled) return null

        val value = if (identifier.startsWith(RELATIONAL_PREFIX)) {
            PlaceholderAPI.setRelationalPlaceholders(viewer, target, placeholder)
        } else {
            PlaceholderAPI.setPlaceholders(target, placeholder)
        }

        // Unknown placeholders are returned unchanged; keep them as plain text.
        if (value == placeholder) return null

        return LEGACY.deserialize(value.replace(LegacyComponentSerializer.SECTION_CHAR, LegacyComponentSerializer.AMPERSAND_CHAR))
    }
}
