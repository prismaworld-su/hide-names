package su.prismaworld.hideNames.name

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags

/**
 * Turns a PlaceholderAPI value into a component.
 *
 * The value may use MiniMessage tags (for example a LuckPerms prefix `<yellow>⭐</yellow>`) and legacy codes
 * (`&e`, `§e`, `&#rrggbb`). It is parsed on its own, separately from the template, so its tags cannot affect the rest
 * of the message. Only styling tags are allowed: interactive tags such as `<click>` and `<hover>` stay plain text.
 */
internal object PlaceholderValue {

    private val PARSER: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.resolver(
                StandardTags.color(),
                StandardTags.decorations(),
                StandardTags.gradient(),
                StandardTags.rainbow(),
                StandardTags.transition(),
                StandardTags.reset(),
                StandardTags.font(),
            ),
        )
        .build()

    fun toComponent(value: String): Component = PARSER.deserialize(LegacyFormatting.toMiniMessage(value))
}
