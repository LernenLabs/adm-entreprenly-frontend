package online.entreprenly.entreprenlyapp.shared.interfaces.ui.state

import androidx.annotation.StringRes

enum class HomeAlertKind { WARNING, ERROR }

data class HomeAlert(val kind: HomeAlertKind, @param:StringRes val title: Int, val detail: String)

data class HomeOrder(val code: String, val customer: String?, val total: Double, @param:StringRes val status: Int)

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
    /** Null until loaded, so the "add your first product" card does not flash for existing stores. */
    val productCount: Int? = null
)
