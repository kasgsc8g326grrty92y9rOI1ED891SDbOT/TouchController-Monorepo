package top.fifthlight.combine.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import top.fifthlight.mergetools.api.ExpectFactory

interface TextBuilder {
    fun bold(bold: Boolean = true, block: TextBuilder.() -> Unit)
    fun underline(underline: Boolean = true, block: TextBuilder.() -> Unit)
    fun italic(italic: Boolean = true, block: TextBuilder.() -> Unit)
    fun append(string: String)
    fun appendWithoutStyle(text: Text)
}

interface TextFactory {
    fun build(block: TextBuilder.() -> Unit): Text
    fun literal(string: String): Text
    fun of(identifier: Identifier): Text
    fun empty(): Text
    fun format(identifier: Identifier, vararg arguments: Any?): Text
    fun toNative(text: Text): Any

    @ExpectFactory
    interface Factory {
        fun of(): TextFactory
    }

    companion object {
        val current: TextFactory by lazy {
            TextFactoryFactory.of()
        }
    }
}

interface Text {
    val string: String

    fun bold(): Text
    fun underline(): Text
    fun italic(): Text
    fun copy(): Text
    operator fun plus(other: Text): Text

    @Composable
    fun native(): Any = TextFactory.current.toNative(this)

    companion object {
        fun translatable(identifier: Identifier) = TextFactory.current.of(identifier)

        fun format(identifier: Identifier, vararg arguments: Any?): Text {
            val factory = TextFactory.current
            val outArguments = Array(arguments.size) { index ->
                val item = arguments[index]
                if (item is Text) {
                    factory.toNative(item)
                } else {
                    item
                }
            }
            return factory.format(identifier, *outArguments)
        }

        fun empty() = TextFactory.current.empty()

        fun literal(string: String) = TextFactory.current.literal(string)

        fun build(block: TextBuilder.() -> Unit) = TextFactory.current.build(block)
    }
}
