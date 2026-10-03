package online.entreprenly.entreprenlyapp.shared.interfaces.navigation

/** Every navigation destination in the app. Each module registers its own graph (see `<context>/interfaces/ui/navigation`). */
object Routes {
    // IAM
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val ACCOUNT = "account"

    // Bottom navigation tabs
    const val HOME = "home"
    const val INVENTORY = "inventory"
    const val SELL = "sell"
    const val ORDERS = "orders"
    const val MORE = "more"

    // Profile
    const val PROFILE = "profile"
    const val PROFILE_EDIT = "profile/edit"
    const val PREFERENCES = "preferences"
    const val NOTIFICATIONS = "notifications"

    // Subscription
    const val SUBSCRIPTION = "subscription"
}
