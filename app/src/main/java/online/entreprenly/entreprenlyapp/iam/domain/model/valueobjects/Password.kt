package online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects

@JvmInline
value class Password private constructor(val value: String) {
    companion object {
        const val MIN_LENGTH = 8
        const val MAX_LENGTH = 255

        /** Devuelve null si no cumple la longitud exigida por el backend (8..255). */
        fun of(raw: String): Password? =
            raw.takeIf { it.length in MIN_LENGTH..MAX_LENGTH }?.let(::Password)
    }
}
