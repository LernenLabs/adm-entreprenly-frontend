package online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects

/** Product measurement type. Backend values are "unit" and "weight". */
enum class ProductType(val value: String) {
    UNIT("unit"),
    WEIGHT("weight");

    companion object {
        fun fromName(name: String?): ProductType? =
            entries.firstOrNull { it.value.equals(name, ignoreCase = true) }

        fun fromNameOrDefault(name: String?, default: ProductType = UNIT): ProductType =
            fromName(name) ?: default
    }
}
