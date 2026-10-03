package online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects

enum class Roles(val roleName: String) {
    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN");

    companion object {
        fun fromName(name: String): Roles? = entries.firstOrNull { it.roleName == name }
    }
}
