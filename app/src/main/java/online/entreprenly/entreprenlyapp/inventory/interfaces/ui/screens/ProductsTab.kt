package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Lot
import online.entreprenly.entreprenlyapp.inventory.domain.model.aggregates.Product
import online.entreprenly.entreprenlyapp.inventory.domain.model.entities.StockAlert
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.InventoryRules
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.ProductType
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.FilterChipsRow
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.InventoryEmpty
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.ProductCard
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.SearchField
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.StockPillKind
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.formatStock
import online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components.productSubtitle

private enum class ProductFilter { ALL, UNIT, WEIGHT }

/** Products tab: search, Todos/Unidad/Peso chips, result count and the list. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsTab(
    products: List<Product>,
    lots: List<Lot>,
    alerts: List<StockAlert>,
    recentlyAdded: String?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        InventoryEmpty(
            icon = Icons.Outlined.Inventory2,
            title = stringResource(R.string.inventory_empty_title),
            message = stringResource(R.string.inventory_empty_message),
            actionText = stringResource(R.string.inventory_empty_action),
            onAction = onAddClick,
            modifier = modifier
        )
        return
    }

    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val filterValue = ProductFilter.entries[filter]
    val searching = query.isNotBlank()

    val visible = products
        .filter {
            when (filterValue) {
                ProductFilter.ALL -> true
                ProductFilter.UNIT -> it.type == ProductType.UNIT
                ProductFilter.WEIGHT -> it.type == ProductType.WEIGHT
            }
        }
        .filter { InventoryRules.matchesSearch(it, query) }
        .sortedBy { if (it.key == recentlyAdded) 0 else 1 }

    PullToRefreshBox(isRefreshing = refreshing, onRefresh = onRefresh, modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            SearchField(
                value = query,
                onChange = { query = it },
                placeholder = stringResource(R.string.inventory_search_product),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            val chipOptions = listOf(
                stringResource(R.string.inventory_filter_all),
                stringResource(R.string.inventory_filter_unit),
                stringResource(R.string.inventory_filter_weight)
            )
            FilterChipsRow(
                options = chipOptions,
                selected = filter,
                onSelect = { filter = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (searching) {
                Text(
                    pluralStringResource(R.plurals.inventory_results, visible.size, visible.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            if (visible.isEmpty()) {
                InventoryEmpty(
                    icon = Icons.Outlined.SearchOff,
                    title = stringResource(R.string.inventory_no_results),
                    message = null,
                    actionText = stringResource(R.string.inventory_add_product),
                    onAction = onAddClick
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visible, key = { it.key }) { product ->
                        val stock = InventoryRules.productStock(product, lots)
                        val outOfStock = stock <= 0.0
                        val lowStock = !outOfStock && InventoryRules.isLowStock(alerts, product)
                        val (pillText, pillKind) = when {
                            outOfStock -> stringResource(R.string.inventory_pill_out_of_stock) to StockPillKind.OUT
                            lowStock -> stringResource(R.string.inventory_pill_low_stock) to StockPillKind.LOW
                            else -> formatStock(product, stock) to StockPillKind.QUANTITY
                        }
                        val isNew = product.key == recentlyAdded
                        val cardModifier = if (isNew) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                        } else {
                            Modifier
                        }
                        // Price is part of the subtitle; formatPrice is a @Composable helper.
                        ProductCardRow(
                            product = product,
                            pillText = pillText,
                            pillKind = pillKind,
                            isNew = isNew,
                            onClick = { onProductClick(product) },
                            modifier = cardModifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCardRow(
    product: Product,
    pillText: String,
    pillKind: StockPillKind,
    isNew: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProductCard(
        name = product.name,
        subtitle = productSubtitle(product),
        stockText = pillText,
        stockKind = pillKind,
        isNew = isNew,
        newText = stringResource(R.string.inventory_pill_new),
        onClick = onClick,
        modifier = modifier
    )
}
