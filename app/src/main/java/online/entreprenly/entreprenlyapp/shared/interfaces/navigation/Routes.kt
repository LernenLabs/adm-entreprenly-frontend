package online.entreprenly.entreprenlyapp.shared.interfaces.navigation

/** Every navigation destination in the app. Each module registers its own graph (see `<context>/interfaces/ui/navigation`). */
object Routes {
    // IAM
    const val WELCOME = "welcome"
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

    // Chatbot (the "Orders" tab)
    const val ARG_CONVERSATION_ID = "conversationId"
    const val CHAT = "conversations/{$ARG_CONVERSATION_ID}"
    const val ARG_ORDER_ID = "orderId"
    const val ORDER_DETAIL = "orders/{$ARG_ORDER_ID}"

    // Inventory
    const val ARG_PRODUCT_TYPE = "productType"
    const val ARG_PRODUCT_ID = "productId"
    const val PRODUCT_DETAIL = "inventory/products/{$ARG_PRODUCT_TYPE}/{$ARG_PRODUCT_ID}"
    const val ARG_LOT_TYPE = "lotType"
    const val ARG_LOT_ID = "lotId"
    const val LOT_DETAIL = "inventory/lots/{$ARG_LOT_TYPE}/{$ARG_LOT_ID}"

    fun chat(conversationId: Long) = "conversations/$conversationId"
    fun orderDetail(orderId: Long) = "orders/$orderId"

    fun productDetail(type: String, id: Long) = "inventory/products/$type/$id"
    fun lotDetail(type: String, id: Long) = "inventory/lots/$type/$id"
}
