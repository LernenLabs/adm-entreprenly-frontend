package online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects

@JvmInline
value class PhoneNumber private constructor(val value: String) {
    companion object {
        private val format = Regex("[+]?[0-9]{7,15}")

        fun parse(input: String): PhoneNumber? =
            input.trim().takeIf(format::matches)?.let(::PhoneNumber)
    }
}
