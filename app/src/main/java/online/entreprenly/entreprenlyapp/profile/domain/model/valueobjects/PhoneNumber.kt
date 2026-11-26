package online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects

@JvmInline
value class PhoneNumber private constructor(val value: String) {
    companion object {
        private val format = Regex("(?:9[0-9]{8}|[+][1-9][0-9]{6,14})")

        fun parse(input: String): PhoneNumber? =
            input.filterNot(Char::isWhitespace).takeIf(format::matches)?.let(::PhoneNumber)
    }
}
