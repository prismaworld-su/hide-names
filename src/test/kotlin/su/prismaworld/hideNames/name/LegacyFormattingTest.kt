package su.prismaworld.hideNames.name

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyFormattingTest {

    private fun convert(text: String) = LegacyFormatting.toMiniMessage(text)

    @Test
    fun `keeps text without codes`() {
        assertEquals("[Admin] ", convert("[Admin] "))
        assertEquals("<gradient:red:blue>VIP</gradient>", convert("<gradient:red:blue>VIP</gradient>"))
    }

    @Test
    fun `converts colors and closes them at the end`() {
        assertEquals("<color:red>[Admin] </color>", convert("&c[Admin] "))
        assertEquals("<color:red>[Admin] </color>", convert("§c[Admin] "))
        assertEquals("<color:gold>A</color>", convert("&6A"))
        assertEquals("<color:white>A</color>", convert("&FA"))
    }

    @Test
    fun `color resets decorations set before it`() {
        assertEquals("<bold>A</bold><color:red>B</color>", convert("&lA&cB"))
        assertEquals("<color:red><bold>A</bold></color><color:blue>B</color>", convert("&c&lA&9B"))
    }

    @Test
    fun `converts all decorations`() {
        assertEquals(
            "<obfuscated><bold><strikethrough><underlined><italic>A</italic></underlined></strikethrough></bold></obfuscated>",
            convert("&k&l&m&n&oA"),
        )
    }

    @Test
    fun `reset closes all formatting`() {
        assertEquals("<color:red><bold>A</bold></color>B", convert("&c&lA&rB"))
    }

    @Test
    fun `converts hex colors`() {
        assertEquals("<color:#ff5500>A</color>", convert("&#FF5500A"))
        assertEquals("<color:#ff5500>A</color>", convert("§x§F§F§5§5§0§0A"))
        assertEquals("<color:#ff5500>A</color>", convert("&x&f&f&5&5&0&0A"))
    }

    @Test
    fun `keeps markers that are not codes`() {
        assertEquals("Tom & Jerry", convert("Tom & Jerry"))
        assertEquals("A&", convert("A&"))
        assertEquals("&zA", convert("&zA"))
        assertEquals("&#FF55A", convert("&#FF55A"))
        assertEquals("&#GG5500", convert("&#GG5500"))
        // Incomplete &x colour: &x itself is not a code, and &f after it is a regular colour.
        assertEquals("&x<color:white>F</color>", convert("&x&fF"))
    }

    @Test
    fun `combines with MiniMessage tags`() {
        assertEquals("<color:red><gradient:red:blue>VIP</gradient></color>", convert("&c<gradient:red:blue>VIP</gradient>"))
    }
}
