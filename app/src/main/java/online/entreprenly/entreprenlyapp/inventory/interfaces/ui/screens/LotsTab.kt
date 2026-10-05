package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.InventoryRules
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.LotStatus
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.CounterCard
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.FilterChipsRow
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryAlertBanner
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryBannerKind
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryEmpty
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.LotCard
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.lotPill
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.lotRowDetail

private enum class LotFilter { ALL, EXPIRING, EXPIRED, ACTIVE }

/** Lots dashboard (US-13): banner, counters, filters and urgency-sorted list. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotsTab(
    lots: List<Lot>,
    products: List<Product>,
    recentlyAdded: String?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onLotClick: (String, Long) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (lots.isEmpty()) {
        InventoryEmpty(
            icon = Icons.Outlined.Layers,
            title = stringResource(R.string.inventory_lots_empty_title),
            message = stringResource(R.string.inventory_lots_empty_message),
            actionText = stringResource(R.string.inventory_lot_create_title),
            onAction = onAddClick,
            modifier = modifier
        )
        return
    }

    var filter by rememberSaveable { mutableIntStateOf(0) }
    val today = InventoryRules.todayUtc()
    val statuses = lots.associateWith { InventoryRules.lotStatus(it, today) }
    val counters = InventoryRules.lotCounters(lots, today)

    val filtered = lots.filter {
        when (LotFilter.entries[filter]) {
            LotFilter.ALL -> true
            LotFilter.EXPIRING -> statuses.getValue(it) == LotStatus.EXPIRING_SOON
            LotFilter.EXPIRED -> statuses.getValue(it) == LotStatus.EXPIRED
            // "Activos" matches the counter: in stock and not expired.
            LotFilter.ACTIVE -> statuses.getValue(it) == LotStatus.ACTIVE ||
                statuses.getValue(it) == LotStatus.EXPIRING_SOON
        }
    }
    val sorted = InventoryRules.sortLots(filtered, today)
    // The newly created lot stays pinned on top until the next refresh.
    val highlighted = sorted.firstOrNull { it.key == recentlyAdded }
    val visible = if (highlighted == null) sorted else listOf(highlighted) + sorted.filter { it.key != highlighted.key }

    PullToRefreshBox(isRefreshing = refreshing, onRefresh = onRefresh, modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            if (counters.expired > 0) {
                InventoryAlertBanner(
                    title = stringResource(R.string.inventory_lot_expired_title),
                    message = pluralStringResource(
                        R.plurals.inventory_lots_banner_expired,
                        counters.expired,
                        counters.expired
                    ),
                    kind = InventoryBannerKind.ERROR,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else if (counters.expiringSoon > 0) {
                InventoryAlertBanner(
                    title = stringResource(R.string.inventory_lots_expiring),
                    message = pluralStringResource(
                        R.plurals.inventory_lots_banner_expiring,
                        counters.expiringSoon,
                        counters.expiringSoon
                    ),
                    kind = InventoryBannerKind.WARNING,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CounterCard(
                    value = counters.active.toString(),
                    label = stringResource(R.string.inventory_lots_active),
                    modifier = Modifier.weight(1f)
                )
                CounterCard(
                    value = counters.expiringSoon.toString(),
                    label = stringResource(R.string.inventory_lots_expiring),
                    modifier = Modifier.weight(1f)
                )
                CounterCard(
                    value = counters.expired.toString(),
                    label = stringResource(R.string.inventory_lots_expired),
                    modifier = Modifier.weight(1f)
                )
            }
            FilterChipsRow(
                options = listOf(
                    stringResource(R.string.inventory_filter_all),
                    stringResource(R.string.inventory_lots_filter_expiring),
                    stringResource(R.string.inventory_lots_filter_expired),
                    stringResource(R.string.inventory_lots_filter_active)
                ),
                selected = filter,
                onSelect = { filter = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (visible.isEmpty()) {
                InventoryEmpty(
                    icon = Icons.Outlined.Layers,
                    title = stringResource(R.string.inventory_no_results),
                    message = null,
                    actionText = null,
                    onAction = null
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visible, key = { it.key }) { lot ->
                        val productName = products.firstOrNull {
                            it.type == lot.type && it.id == lot.productId
                        }?.name ?: lot.label
                        val pill = lotPill(lot, today)
                        val cardModifier = if (lot.key == recentlyAdded) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                        } else {
                            Modifier
                        }
                        Box(cardModifier) {
                            LotCard(
                                productName = productName,
                                detail = lotRowDetail(lot),
                                pillText = pill.text,
                                pillKind = pill.kind,
                                onClick = { onLotClick(lot.type.value, lot.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
