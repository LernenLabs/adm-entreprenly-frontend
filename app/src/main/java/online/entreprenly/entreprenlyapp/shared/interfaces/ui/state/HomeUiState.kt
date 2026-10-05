package online.entreprenly.entreprenlyapp.shared.interfaces.ui.state

enum class HomeAlertKind { WARNING, ERROR }

data class HomeAlert(val kind: HomeAlertKind, val title: String, val detail: String)

data class HomeOrder(val code: String, val customer: String, val detail: String, val status: String)

/**
 * Everything the home dashboard shows. The defaults describe a brand-new account (empty state);
 * the Sales, Inventory and Chatbot modules fill it in when their data is available.
 */
data class HomeUiState(
    val salesToday: Double = 0.0,
    val cashToday: Double = 0.0,
    val digitalToday: Double = 0.0,
    val salesCount: Int = 0,
    val expiringLots: Int = 0,
    val ordersToValidate: Int = 0,
    val chatbotConnected: Boolean = false,
    val alerts: List<HomeAlert> = emptyList(),
    val recentOrders: List<HomeOrder> = emptyList(),
    val productCount: Int = 0
)
