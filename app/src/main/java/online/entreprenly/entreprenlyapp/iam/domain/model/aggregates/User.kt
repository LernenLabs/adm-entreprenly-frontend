package online.entreprenly.entreprenlyapp.iam.domain.model.aggregates

import online.entreprenly.entreprenlyapp.iam.domain.model.valueobjects.Roles

data class User(val id: Long, val email: String, val roles: List<Roles>)
