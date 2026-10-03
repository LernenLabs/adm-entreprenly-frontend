package online.entreprenly.entreprenlyapp.profile.interfaces.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppCurrency
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppLanguage
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppTheme
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.AppTimezone
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.components.ProfileStateGate
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels.ProfileViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppCard
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppTopBar
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.NavigationCard
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

private enum class Sheet { LANGUAGE, TIMEZONE, CURRENCY }

/** US-67: language, time zone, light/dark theme and currency. Changes apply instantly. */
@Composable
fun PreferencesScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val error by viewModel.preferencesError.collectAsState()
    var sheet by remember { mutableStateOf<Sheet?>(null) }

    Column(modifier.fillMaxSize()) {
        AppTopBar(stringResource(R.string.prefs_title), onBack)
        ProfileStateGate(state, onRetry = viewModel::load, onSignIn = onSignIn) { profile ->
            val prefs = profile.preferences
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.prefs_intro),
                    color = MaterialTheme.extraColors.muted,
                    style = MaterialTheme.typography.bodyMedium
                )
                NavigationCard(
                    stringResource(R.string.prefs_language),
                    prefs.language.nativeName,
                    onClick = { sheet = Sheet.LANGUAGE }
                )
                NavigationCard(
                    stringResource(R.string.prefs_timezone),
                    "${prefs.timezone.id} (${prefs.timezone.offset})",
                    onClick = { sheet = Sheet.TIMEZONE }
                )
                ThemeCard(prefs.theme) { viewModel.updatePreferences(prefs.copy(theme = it)) }
                NavigationCard(
                    stringResource(R.string.prefs_currency),
                    "${prefs.currency.code} · ${prefs.currency.displayName}",
                    onClick = { sheet = Sheet.CURRENCY }
                )
                AppCard {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            stringResource(R.string.prefs_amount_format),
                            color = MaterialTheme.extraColors.muted,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "${prefs.currency.symbol} 24.90",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
                Text(
                    stringResource(R.string.prefs_footer),
                    color = MaterialTheme.extraColors.muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            when (sheet) {
                Sheet.LANGUAGE -> OptionSheet(
                    title = stringResource(R.string.prefs_language_sheet),
                    options = AppLanguage.entries.map { Option(it.code, it.nativeName, null) },
                    selectedKey = prefs.language.code,
                    onSelect = { viewModel.updatePreferences(prefs.copy(language = AppLanguage.fromCode(it))) },
                    onDismiss = { sheet = null }
                )
                Sheet.TIMEZONE -> OptionSheet(
                    title = stringResource(R.string.prefs_timezone_sheet),
                    options = AppTimezone.entries.map { Option(it.id, it.id, it.offset) },
                    selectedKey = prefs.timezone.id,
                    onSelect = { viewModel.updatePreferences(prefs.copy(timezone = AppTimezone.fromId(it))) },
                    onDismiss = { sheet = null }
                )
                Sheet.CURRENCY -> OptionSheet(
                    title = stringResource(R.string.prefs_currency_sheet),
                    options = AppCurrency.entries.map { Option(it.code, "${it.code} · ${it.displayName}", it.symbol) },
                    selectedKey = prefs.currency.code,
                    onSelect = { viewModel.updatePreferences(prefs.copy(currency = AppCurrency.fromCode(it))) },
                    onDismiss = { sheet = null }
                )
                null -> Unit
            }
        }
    }
}

@Composable
private fun ThemeCard(selected: AppTheme, onSelect: (AppTheme) -> Unit) {
    AppCard {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.prefs_theme), fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTheme.entries.forEach { theme ->
                    val isSelected = theme == selected
                    val label = stringResource(
                        if (theme == AppTheme.LIGHT) R.string.prefs_theme_light else R.string.prefs_theme_dark
                    )
                    OutlinedButton(
                        onClick = { onSelect(theme) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.extraColors.border
                        ),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.extraColors.highlight
                            else MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        if (isSelected) Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text(label, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private data class Option(val key: String, val title: String, val subtitle: String?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionSheet(
    title: String,
    options: List<Option>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(horizontal = 14.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.prefs_close))
                }
            }
            options.forEach { option ->
                val check = if (option.key == selectedKey) "✓ " else ""
                NavigationCard(
                    title = check + option.title,
                    subtitle = option.subtitle,
                    onClick = {
                        onSelect(option.key)
                        onDismiss()
                    }
                )
            }
            Column(Modifier.height(16.dp)) {}
        }
    }
}
