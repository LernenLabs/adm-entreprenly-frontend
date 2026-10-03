package online.entreprenly.entreprenlyapp.profile.domain.model.queries

data class GetProfileByUserIdQuery(val userId: Long)

/** Preferences cached on the device (applied at startup before the network answers). */
data object GetLocalPreferencesQuery
