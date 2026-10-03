package online.entreprenly.entreprenlyapp.shared.interfaces.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppCard
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.BannerKind
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.MessageBanner
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PrimaryPillButton
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.state.HomeAlert
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.state.HomeAlertKind
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.state.HomeOrder
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.state.HomeUiState
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.BrandBrown
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

/**
 * US-70 to US-75: home dashboard. With the default [HomeUiState] it shows the empty state of a
 * new account; the sales, alerts and orders cards fill in as the other modules provide data.
 */
@Composable
fun HomeScreen(
    userName: String,
    currencySymbol: String,
    onSell: () -> Unit,
    onInventory: () -> Unit,
    onOrders: () -> Unit,
    modifier: Modifier = Modifier,
    state: HomeUiState = HomeUiState()
) {
    Column(modifier.fillMaxSize()) {
        HomeHeader(userName)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SalesCard(state, currencySymbol)
            StatTiles(state)
            Shortcuts(onInventory, onSell, onOrders)
            ChatbotCard(state.chatbotConnected, onConnect = onOrders)
            SectionTitle(stringResource(R.string.home_alerts))
            if (state.alerts.isEmpty()) {
                MessageBanner(
                    stringResource(R.string.home_alerts_ok_title),
                    stringResource(R.string.home_alerts_ok_message),
                    BannerKind.SUCCESS
                )
            } else {
                state.alerts.forEach { AlertCard(it) }
            }
            SectionTitle(stringResource(R.string.home_orders_title))
            if (state.recentOrders.isEmpty()) {
                EmptyCard(
                    Icons.Outlined.Description,
                    stringResource(R.string.home_orders_empty_title),
                    stringResource(R.string.home_orders_empty_message)
                )
            } else {
                state.recentOrders.forEach { OrderRow(it) }
            }
            if (state.productCount == 0) {
                SectionTitle(stringResource(R.string.nav_inventory))
                EmptyCard(
                    Icons.Outlined.Inventory2,
                    stringResource(R.string.home_inventory_empty),
                    null,
                    onClick = onInventory
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(userName: String) {
    val locale = LocalConfiguration.current.locales[0]
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern(stringResource(R.string.home_date_format), locale))
    Box(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                val greeting = if (userName.isNotBlank()) stringResource(R.string.home_greeting, userName)
                else stringResource(R.string.home_title)
                Text(
                    greeting,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary)
            }
            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

private fun money(symbol: String, amount: Double) = "$symbol ${String.format(Locale.US, "%.2f", amount)}"

@Composable
private fun SalesCard(state: HomeUiState, symbol: String) {
    Surface(shape = RoundedCornerShape(14.dp), color = BrandBrown, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.home_sales_today), color = Color(0xFFE9C9A5), style = MaterialTheme.typography.bodySmall)
            Text(
                money(symbol, state.salesToday),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "${stringResource(R.string.home_cash)} ${money(symbol, state.cashToday)}",
                    color = Color(0xFFE9C9A5),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "${stringResource(R.string.home_digital)} ${money(symbol, state.digitalToday)}",
                    color = Color(0xFFE9C9A5),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StatTiles(state: HomeUiState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(state.salesCount, stringResource(R.string.home_stat_sales), MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
        StatTile(state.expiringLots, stringResource(R.string.home_stat_lots), BrandBrown, Modifier.weight(1f))
        StatTile(state.ordersToValidate, stringResource(R.string.home_stat_orders), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(value: Int, label: String, valueColor: Color, modifier: Modifier) {
    AppCard(modifier) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.toString(), color = valueColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(
                label,
                color = MaterialTheme.extraColors.muted,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Shortcuts(onInventory: () -> Unit, onSell: () -> Unit, onOrders: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Shortcut(Icons.Outlined.Inventory2, stringResource(R.string.home_quick_new_product), onInventory)
        Shortcut(Icons.Outlined.Layers, stringResource(R.string.home_quick_create_lot), onInventory)
        Shortcut(Icons.Filled.ShoppingCart, stringResource(R.string.nav_sell), onSell)
        Shortcut(Icons.Outlined.ChatBubbleOutline, stringResource(R.string.nav_orders), onOrders)
    }
}

@Composable
private fun Shortcut(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        Modifier.clickable(onClick = onClick).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier.size(52.dp).background(MaterialTheme.extraColors.highlight, CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(72.dp)
        )
    }
}

@Composable
private fun ChatbotCard(connected: Boolean, onConnect: () -> Unit) {
    AppCard {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.home_chatbot_title), fontWeight = FontWeight.Bold)
                val (label, fg, bg) = if (connected) {
                    Triple(stringResource(R.string.home_chatbot_connected), MaterialTheme.extraColors.success, MaterialTheme.extraColors.successBackground)
                } else {
                    Triple(stringResource(R.string.home_chatbot_disconnected), MaterialTheme.colorScheme.error, MaterialTheme.extraColors.errorBackground)
                }
                Surface(shape = RoundedCornerShape(50), color = bg) {
                    Text("● $label", color = fg, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            if (!connected) {
                Text(
                    stringResource(R.string.home_chatbot_hint),
                    color = MaterialTheme.extraColors.muted,
                    style = MaterialTheme.typography.bodySmall
                )
                PrimaryPillButton(stringResource(R.string.home_chatbot_connect), onConnect)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun EmptyCard(icon: ImageVector, title: String, message: String?, onClick: (() -> Unit)? = null) {
    AppCard(onClick = onClick) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.extraColors.border, modifier = Modifier.size(36.dp))
            Text(title, color = MaterialTheme.extraColors.muted, fontWeight = FontWeight.SemiBold)
            if (message != null) {
                Text(message, color = MaterialTheme.extraColors.muted, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun AlertCard(alert: HomeAlert) {
    val extra = MaterialTheme.extraColors
    val (fg, bg) = when (alert.kind) {
        HomeAlertKind.WARNING -> extra.warning to extra.warningBackground
        HomeAlertKind.ERROR -> MaterialTheme.colorScheme.error to extra.errorBackground
    }
    Surface(shape = RoundedCornerShape(10.dp), color = bg, border = BorderStroke(1.dp, fg.copy(alpha = 0.3f)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(alert.title, color = fg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(alert.detail, color = fg, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun OrderRow(order: HomeOrder) {
    AppCard {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("${order.code} — ${order.customer}", fontWeight = FontWeight.Bold)
                Text(order.detail, color = MaterialTheme.extraColors.muted, style = MaterialTheme.typography.bodySmall)
            }
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.extraColors.highlight) {
                Text(order.status, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
    }
}
