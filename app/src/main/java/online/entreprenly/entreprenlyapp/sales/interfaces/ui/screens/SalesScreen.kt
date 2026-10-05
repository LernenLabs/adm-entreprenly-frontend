package online.entreprenly.entreprenlyapp.sales.interfaces.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.sales.domain.model.entities.SaleProduct
import online.entreprenly.entreprenlyapp.sales.domain.model.valueobjects.PaymentMethod
import online.entreprenly.entreprenlyapp.sales.interfaces.ui.viewmodels.SalesUiState
import online.entreprenly.entreprenlyapp.sales.interfaces.ui.viewmodels.SalesViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppCard
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppTopBar
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PrimaryPillButton
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(viewModel: SalesViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Bottom sheet state for Ticket
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showTicketSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Punto de Venta",
                onBack = null // Handle in navigation layer if needed
            )
        },
        floatingActionButton = {
            if (uiState.currentTicket.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showTicketSheet = true },
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Ver Ticket") },
                    text = { 
                        Text("Ver Ticket (${uiState.currentTicket.size}) - S/ ${String.format(Locale.US, "%.2f", uiState.ticketTotal)}")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // US-36: Caja Summary and History Button
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Resumen de Caja", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.onToggleHistory(true) }) {
                    Icon(Icons.Filled.History, contentDescription = "Historial", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Efectivo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.extraColors.muted)
                        Text("S/ ${String.format(Locale.US, "%.2f", uiState.cashTotalToday)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Yape/Plin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.extraColors.muted)
                        Text("S/ ${String.format(Locale.US, "%.2f", uiState.digitalTotalToday)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.extraColors.muted)
                        Text(
                            "S/ ${String.format(Locale.US, "%.2f", uiState.cashTotalToday + uiState.digitalTotalToday)}", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // US-28: Buscador
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar producto (unidad o peso)...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Lista de Productos
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(uiState.filteredProducts) { product ->
                    ProductItemRow(product = product, onAddClick = { qty -> viewModel.onAddItemToTicket(product, qty) })
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // space for FAB
                }
            }
        }
    }

    // Modal para Ver Ticket (US-31, US-32, US-33)
    if (showTicketSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTicketSheet = false },
            sheetState = sheetState
        ) {
            TicketSheetContent(
                uiState = uiState,
                onRemoveItem = { viewModel.onRemoveItem(it) },
                onSelectPayment = { viewModel.onSelectPaymentMethod(it) },
                onFinishSale = { 
                    viewModel.onFinishSale()
                    showTicketSheet = false
                }
            )
        }
    }

    // Modal para Historial (US-97)
    if (uiState.isHistoryOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.onToggleHistory(false) },
            title = { Text("Historial de Ventas (Hoy)") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.salesHistory.isEmpty()) {
                        item { Text("No hay ventas registradas hoy.") }
                    } else {
                        items(uiState.salesHistory.reversed()) { sale ->
                            AppCard {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                    Text("ID: ${sale.id.take(8)}...", style = MaterialTheme.typography.labelSmall)
                                    Text("Método: ${sale.paymentMethod.label}", style = MaterialTheme.typography.bodySmall)
                                    Text("Total: S/ ${String.format(Locale.US, "%.2f", sale.total)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Hora: ${sale.timestamp.toLocalTime().withNano(0)}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onToggleHistory(false) }) { Text("Cerrar") }
            }
        )
    }

    // Success Dialog (US-33)
    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissSuccessDialog() },
            title = { Text("¡Venta Exitosa!") },
            text = {
                Column {
                    Text("Total cobrado: S/ ${String.format(Locale.US, "%.2f", uiState.lastSale?.total ?: 0.0)}")
                    Text("Método de pago: ${uiState.lastSale?.paymentMethod?.label}")
                }
            },
            confirmButton = {
                PrimaryPillButton("Cerrar", onClick = { viewModel.onDismissSuccessDialog() })
            }
        )
    }
}

// US-29: Selector/Diálogo de cantidad
@Composable
fun ProductItemRow(product: SaleProduct, onAddClick: (Double) -> Unit) {
    var showQtyDialog by remember { mutableStateOf(false) }
    
    AppCard(onClick = { showQtyDialog = true }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                val typeText = if (product.isUnit) "Por Unidad" else "Por Peso (Kg)"
                Text(typeText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.extraColors.muted)
                Text("Stock: ${product.stockAvailable}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.extraColors.muted)
            }
            Text("S/ ${String.format(Locale.US, "%.2f", product.price)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            IconButton(onClick = { showQtyDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (showQtyDialog) {
        var qtyInput by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { showQtyDialog = false },
            title = { Text("Agregar ${product.name}") },
            text = {
                Column {
                    Text("Precio: S/ ${String.format(Locale.US, "%.2f", product.price)} ${if (product.isUnit) "c/u" else "por Kg"}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it },
                        label = { Text(if (product.isUnit) "Cantidad (Unidades)" else "Peso (Kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val qty = qtyInput.toDoubleOrNull() ?: 0.0
                    if (qty > 0) {
                        onAddClick(qty)
                        showQtyDialog = false
                    }
                }) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQtyDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun TicketSheetContent(
    uiState: SalesUiState,
    onRemoveItem: (Int) -> Unit,
    onSelectPayment: (PaymentMethod) -> Unit,
    onFinishSale: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ticket de Venta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        if (uiState.currentTicket.isEmpty()) {
            Text("El ticket está vacío.", color = MaterialTheme.extraColors.muted)
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.currentTicket.size) { index ->
                    val item = uiState.currentTicket[index]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.productName, fontWeight = FontWeight.Bold)
                            val unitStr = if (item.isUnit) "unid" else "kg"
                            Text("${item.quantity} $unitStr x S/ ${String.format(Locale.US, "%.2f", item.unitPrice)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.extraColors.muted)
                        }
                        Text("S/ ${String.format(Locale.US, "%.2f", item.subtotal)}", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onRemoveItem(index) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total a Cobrar:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("S/ ${String.format(Locale.US, "%.2f", uiState.ticketTotal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            // US-32: Medio de pago
            Text("Medio de Pago", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethodButton(
                    method = PaymentMethod.CASH,
                    selected = uiState.selectedPaymentMethod == PaymentMethod.CASH,
                    onClick = { onSelectPayment(PaymentMethod.CASH) },
                    modifier = Modifier.weight(1f)
                )
                PaymentMethodButton(
                    method = PaymentMethod.DIGITAL,
                    selected = uiState.selectedPaymentMethod == PaymentMethod.DIGITAL,
                    onClick = { onSelectPayment(PaymentMethod.DIGITAL) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryPillButton(
                text = "Cobrar / Finalizar Venta",
                onClick = onFinishSale,
                enabled = uiState.selectedPaymentMethod != null
            )
        }
    }
}

@Composable
fun PaymentMethodButton(
    method: PaymentMethod,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor, contentColor = contentColor)
    ) {
        Text(method.label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
