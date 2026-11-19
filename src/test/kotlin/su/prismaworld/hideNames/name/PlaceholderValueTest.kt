package su.prismaworld.hideNames.name

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlaceholderValueTest {

    private fun plain(component: Component) = PlainTextComponentSerializer.plainText().serialize(component)

    /** Colour of the first text part that contains [text], inherited from parents if not set on the part itself. */
    private fun colorOf(component: Component, text: String, inherited: TextColor? = null): TextColor? {
        val color = component.color() ?: inherited
        if (component is TextComponent && text in component.content()) return color
        return component.children().firstNotNullOfOrNull { colorOf(it, text, color) }
    }

    private fun hasDecoration(component: Component, decoration: TextDecoration): Boolean =
        component.decoration(decoration) == TextDecoration.State.TRUE ||
            component.children().any { hasDecoration(it, decoration) }

    private fun hasClickEvent(component: Component): Boolean =
        component.clickEvent() != null || component.children().any(::hasClickEvent)

    @Test
    fun `keeps plain text`() {
        val component = PlaceholderValue.toComponent("[Admin] ")

        assertEquals("[Admin] ", plain(component))
        assertNull(colorOf(component, "[Admin]"))
    }

    @Test
    fun `renders MiniMessage colours`() {
        val component = PlaceholderValue.toComponent("<yellow>⭐</yellow> ")

        assertEquals("⭐ ", plain(component))
        assertEquals(NamedTextColor.YELLOW, colorOf(component, "⭐"))
    }

    @Test
    fun `renders legacy colours`() {
        assertEquals(NamedTextColor.YELLOW, colorOf(PlaceholderValue.toComponent("&e⭐"), "⭐"))
        assertEquals(NamedTextColor.YELLOW, colorOf(PlaceholderValue.toComponent("§e⭐"), "⭐"))
        assertEquals(TextColor.color(0xFF0000), colorOf(PlaceholderValue.toComponent("&#ff0000A"), "A"))
        assertEquals(TextColor.color(0x00FF00), colorOf(PlaceholderValue.toComponent("&x&0&0&f&f&0&0A"), "A"))
    }

    @Test
    fun `renders mixed legacy codes and MiniMessage tags`() {
        val component = PlaceholderValue.toComponent("&c[A] <bold>B</bold>")

        assertEquals("[A] B", plain(component))
        assertEquals(NamedTextColor.RED, colorOf(component, "[A]"))
        assertTrue(hasDecoration(component, TextDecoration.BOLD))
    }

    @Test
    fun `renders gradients`() {
        assertEquals("VIP", plain(PlaceholderValue.toComponent("<gradient:red:blue>VIP</gradient>")))
    }

    @Test
    fun `keeps interactive tags as plain text`() {
        val value = "<click:run_command:/op Steve>x</click>"
        val component = PlaceholderValue.toComponent(value)

        assertEquals(value, plain(component))
        assertTrue(!hasClickEvent(component))
    }

    @Test
    fun `keeps text that only looks like a tag`() {
        assertEquals("I <3 you", plain(PlaceholderValue.toComponent("I <3 you")))
    }
}
