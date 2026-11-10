package online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects

class PasswordResetLink private constructor(val userId: Long, val token: String) {
    companion object {
        private val pattern = Regex(
            "entreprenly-av2://reset-password/([1-9][0-9]{0,18})/" +
                "([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})"
        )

        fun parse(value: String?): PasswordResetLink? {
            val match = value?.let(pattern::matchEntire) ?: return null
            val id = match.groupValues[1].toLongOrNull() ?: return null
            return PasswordResetLink(id, match.groupValues[2])
        }
    }
}
