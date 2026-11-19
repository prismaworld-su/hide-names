package su.prismaworld.hideNames.name

/**
 * Converts legacy formatting codes to MiniMessage tags and leaves the rest of the text, including MiniMessage tags, as is.
 *
 * Supports codes with `&` and `§`:
 * - colours `&0`-`&f`, decorations `&k`-`&o`, reset `&r`;
 * - hex colours `&#RRGGBB` and `&x&R&R&G&G&B&B`.
 *
 * As in the legacy format, a colour resets the decorations set before it. All open tags are closed at the end of
 * the text, so formatting from legacy codes does not leak out of it. `&` and `§` not followed by a code stay plain text.
 *
 * Same conversion as in the chatter plugin, so placeholder values look the same in chat and in the action bar.
 */
internal object LegacyFormatting {

    private val COLORS = mapOf(
        '0' to "black",
        '1' to "dark_blue",
        '2' to "dark_green",
        '3' to "dark_aqua",
        '4' to "dark_red",
        '5' to "dark_purple",
        '6' to "gold",
        '7' to "gray",
        '8' to "dark_gray",
        '9' to "blue",
        'a' to "green",
        'b' to "aqua",
        'c' to "red",
        'd' to "light_purple",
        'e' to "yellow",
        'f' to "white",
    )

    private val DECORATIONS = mapOf(
        'k' to "obfuscated",
        'l' to "bold",
        'm' to "strikethrough",
        'n' to "underlined",
        'o' to "italic",
    )

    private const val RESET = 'r'
    private const val COLOR_TAG = "color"
    private const val HEX_LENGTH = 6

    fun toMiniMessage(text: String): String {
        if (text.none(::isMarker)) return text

        val result = StringBuilder(text.length + 32)
        // Open tags, the last one is the innermost.
        val open = ArrayDeque<String>()

        fun closeAll() {
            while (open.isNotEmpty()) result.append("</").append(open.removeLast()).append('>')
        }

        fun openTag(name: String, argument: String? = null) {
            result.append('<').append(name)
            if (argument != null) result.append(':').append(argument)
            result.append('>')
            open.addLast(name)
        }

        var index = 0
        while (index < text.length) {
            val char = text[index]
            val code = text.getOrNull(index + 1)?.lowercaseChar()
            if (!isMarker(char) || code == null) {
                result.append(char)
                index++
                continue
            }

            val hex = hexColor(text, index)
            val color = COLORS[code]
            val decoration = DECORATIONS[code]
            when {
                hex != null -> {
                    closeAll()
                    openTag(COLOR_TAG, "#${hex.first}")
                    index += hex.second
                }
                color != null -> {
                    closeAll()
                    openTag(COLOR_TAG, color)
                    index += 2
                }
                decoration != null -> {
                    openTag(decoration)
                    index += 2
                }
                code == RESET -> {
                    closeAll()
                    index += 2
                }
                else -> {
                    result.append(char)
                    index++
                }
            }
        }
        closeAll()
        return result.toString()
    }

    /**
     * Hex colour starting with the marker at [index]: `&#RRGGBB` or `&x&R&R&G&G&B&B`.
     *
     * @return the colour in lower case and the code length, or `null` if there is no hex colour at [index]
     */
    private fun hexColor(text: String, index: Int): Pair<String, Int>? {
        when (text.getOrNull(index + 1)?.lowercaseChar()) {
            '#' -> {
                val end = index + 2 + HEX_LENGTH
                if (end > text.length) return null
                val hex = text.substring(index + 2, end)
                return if (hex.all(::isHexDigit)) hex.lowercase() to end - index else null
            }
            'x' -> {
                val end = index + 2 + HEX_LENGTH * 2
                if (end > text.length) return null
                val hex = StringBuilder(HEX_LENGTH)
                for (position in index + 2 until end step 2) {
                    if (!isMarker(text[position]) || !isHexDigit(text[position + 1])) return null
                    hex.append(text[position + 1].lowercaseChar())
                }
                return hex.toString() to end - index
            }
            else -> return null
        }
    }

    private fun isMarker(char: Char) = char == '&' || char == '§'

    private fun isHexDigit(char: Char) = char in '0'..'9' || char.lowercaseChar() in 'a'..'f'
}
