package online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects

@JvmInline
value class Email private constructor(val value: String) {
    companion object {
        const val MAX_LENGTH = 120
        private val PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        /** Devuelve null si el email no cumple formato o longitud. */
        fun of(raw: String): Email? =
            raw.trim().takeIf { it.length <= MAX_LENGTH && PATTERN.matches(it) }?.let(::Email)
    }
}
