package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.inventory.domain.model.valueobjects.InventoryRules
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.UiText
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppCard
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.BrandBrown
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusDanger
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusDangerContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusNeutral
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusNeutralContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusSuccess
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusSuccessContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusWarning
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.StatusWarningContainer
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

private val CardShape = RoundedCornerShape(14.dp)
private val PillShape = RoundedCornerShape(50)

/** Square avatar with the product initials on a peach background. */
@Composable
fun ProductAvatar(name: String, modifier: Modifier = Modifier, size: Dp = 52.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.extraColors.highlight, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = InventoryRules.initials(name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

enum class StockPillKind { QUANTITY, LOW, OUT, NEW }

@Composable
fun StockPill(text: String, kind: StockPillKind, modifier: Modifier = Modifier) {
    val (fg, bg) = when (kind) {
        StockPillKind.QUANTITY -> MaterialTheme.extraColors.muted to MaterialTheme.extraColors.border
        StockPillKind.LOW -> StatusDanger to StatusDangerContainer
        StockPillKind.OUT -> StatusNeutral to StatusNeutralContainer
        StockPillKind.NEW -> StatusSuccess to StatusSuccessContainer
    }
    Text(
        text = text,
        color = fg,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        modifier = modifier
            .background(bg, PillShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

enum class LotPillKind { ACTIVE, EXPIRING, EXPIRED, OUT }

@Composable
fun LotStatusPill(text: String, kind: LotPillKind, modifier: Modifier = Modifier) {
    val (fg, bg) = when (kind) {
        LotPillKind.ACTIVE -> StatusSuccess to StatusSuccessContainer
        LotPillKind.EXPIRING -> StatusWarning to StatusWarningContainer
        LotPillKind.EXPIRED -> StatusDanger to StatusDangerContainer
        LotPillKind.OUT -> StatusNeutral to StatusNeutralContainer
    }
    Text(
        text = text,
        color = fg,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        modifier = modifier
            .background(bg, PillShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Product row: avatar, bold name, muted subtitle and a stock pill. */
@Composable
fun ProductCard(
    name: String,
    subtitle: String,
    stockText: String,
    stockKind: StockPillKind,
    isNew: Boolean,
    newText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier, onClick = onClick) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductAvatar(name)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.extraColors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isNew) StockPill(newText, StockPillKind.NEW)
            }
            StockPill(stockText, stockKind)
        }
    }
}

/** Lot row: product avatar, bold product name, detail line and status pill. */
@Composable
fun LotCard(
    productName: String,
    detail: String,
    pillText: String,
    pillKind: LotPillKind,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier, onClick = onClick) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductAvatar(productName, size = 48.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.extraColors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LotStatusPill(pillText, pillKind)
        }
    }
}

/** Gray track with a white active option. Used for Products|Lots and Unit|Weight. */
@Composable
fun SegmentedControl(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.extraColors.border
    ) {
        Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEachIndexed { index, option ->
                val active = index == selected
                Box(
                    Modifier
                        .weight(1f)
                        .background(
                            if (active) MaterialTheme.colorScheme.surface else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelect(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        option,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/** Horizontal filter chips; selected chip is dark brown with white text. */
@Composable
fun FilterChipsRow(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options.size) { index ->
            val active = index == selected
            FilterChip(
                selected = active,
                onClick = { onSelect(index) },
                label = {
                    Text(
                        options[index],
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrandBrown,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

enum class InventoryBannerKind { WARNING, ERROR }

@Composable
fun InventoryAlertBanner(
    title: String,
    message: String,
    kind: InventoryBannerKind,
    modifier: Modifier = Modifier
) {
    val extra = MaterialTheme.extraColors
    val (fg, bg, icon) = when (kind) {
        InventoryBannerKind.WARNING -> Triple(extra.warning, extra.warningBackground, Icons.Outlined.Info)
        InventoryBannerKind.ERROR -> Triple(MaterialTheme.colorScheme.error, extra.errorBackground, Icons.Outlined.ErrorOutline)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, fg.copy(alpha = 0.4f))
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = fg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(message, color = fg, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/** Big number plus label, used for the lots dashboard counters. */
@Composable
fun CounterCard(value: String, label: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                label,
                color = MaterialTheme.extraColors.muted,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Bottom-sheet header: drag handle, title and close button. */
@Composable
fun SheetHeader(title: String, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(MaterialTheme.extraColors.border, PillShape)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = null)
            }
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.extraColors.muted) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.extraColors.border
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun InventoryTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null,
    suffix: String? = null
) {
    val extra = MaterialTheme.extraColors
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            enabled = enabled,
            isError = isError,
            placeholder = placeholder?.let { { Text(it) } },
            suffix = suffix?.let { { Text(it, color = extra.muted) } },
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = extra.errorBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = extra.border,
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
            trailingIcon = if (isError) {
                { Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
        if (errorText != null) {
            Text(errorText, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun InventoryLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun InventoryError(message: UiText, onRetry: () -> Unit, retryText: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message.asString(), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        OutlinedButton(onClick = onRetry) { Text(retryText) }
    }
}

@Composable
fun InventoryEmpty(
    icon: ImageVector,
    title: String,
    message: String?,
    actionText: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(88.dp)
                .background(MaterialTheme.extraColors.highlight, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (message != null) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.extraColors.muted, textAlign = TextAlign.Center)
        }
        if (actionText != null && onAction != null) {
            OutlinedButton(onClick = onAction, modifier = Modifier.fillMaxWidth()) { Text(actionText) }
        }
    }
}
