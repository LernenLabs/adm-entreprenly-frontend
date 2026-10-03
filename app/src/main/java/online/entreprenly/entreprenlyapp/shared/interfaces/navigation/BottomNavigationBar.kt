package online.entreprenly.entreprenlyapp.shared.interfaces.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

/** The five tabs of the bottom navigation (US-81). */
enum class BottomTab(val route: String, val label: Int, val icon: ImageVector) {
    HOME(Routes.HOME, R.string.nav_home, Icons.Outlined.Home),
    INVENTORY(Routes.INVENTORY, R.string.nav_inventory, Icons.Outlined.Inventory2),
    SELL(Routes.SELL, R.string.nav_sell, Icons.Filled.ShoppingCart),
    ORDERS(Routes.ORDERS, R.string.nav_orders, Icons.Outlined.ChatBubbleOutline),
    MORE(Routes.MORE, R.string.nav_more, Icons.Filled.Menu)
}

/** Routes that belong to the "More" tab (profile, preferences, ...). */
private val moreRoutes = setOf(
    Routes.MORE, Routes.PROFILE, Routes.PREFERENCES, Routes.SUBSCRIPTION, Routes.ACCOUNT
)

fun tabForRoute(route: String?): BottomTab? = when (route) {
    null -> null
    in moreRoutes -> BottomTab.MORE
    else -> BottomTab.entries.firstOrNull { it.route == route }
}

@Composable
fun BottomNavigationBar(selected: BottomTab?, onSelect: (BottomTab) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.extraColors.border)
    ) {
        Row(
            Modifier.navigationBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            BottomTab.entries.forEach { tab ->
                if (tab == BottomTab.SELL) {
                    SellButton(tab, selected == tab, onSelect)
                } else {
                    TabItem(tab, selected == tab, onSelect)
                }
            }
        }
    }
}

@Composable
private fun TabItem(tab: BottomTab, isSelected: Boolean, onSelect: (BottomTab) -> Unit) {
    Column(
        Modifier.clickable { onSelect(tab) }.padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .background(
                    if (isSelected) MaterialTheme.extraColors.highlight else androidx.compose.ui.graphics.Color.Transparent,
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Icon(
                tab.icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.extraColors.muted
            )
        }
        Text(
            stringResource(tab.label),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.extraColors.muted
        )
    }
}

/** Raised round "Sell" button in the middle of the bar. */
@Composable
private fun SellButton(tab: BottomTab, isSelected: Boolean, onSelect: (BottomTab) -> Unit) {
    Column(
        Modifier.clickable { onSelect(tab) }.offset(y = (-18).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(60.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .padding(4.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(tab.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
        }
        Text(
            stringResource(tab.label),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
