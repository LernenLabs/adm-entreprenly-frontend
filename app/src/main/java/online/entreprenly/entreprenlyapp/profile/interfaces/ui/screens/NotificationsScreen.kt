package online.entreprenly.entreprenlyapp.profile.interfaces.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.components.ProfileStateGate
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels.ProfileViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppCard
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.AppTopBar
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.BannerKind
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.MessageBanner
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PrimaryPillButton
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.SecondaryPillButton
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

/**
 * US-68: choose whether to receive stock and expiration push alerts.
 * Uses a device resource: the system notification permission and the app's notification settings.
 */
@Composable
fun NotificationsScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val form by viewModel.notificationsForm.collectAsState()
    val context = LocalContext.current

    var permissionGranted by remember { mutableStateOf(areNotificationsEnabled(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionGranted = areNotificationsEnabled(context)
    }
    // Ask once when the screen opens (Android 13+); re-check when returning from system Settings.
    LaunchedEffect(Unit) {
        if (!permissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { permissionGranted = areNotificationsEnabled(context) }
    DisposableEffect(Unit) { onDispose { viewModel.resetNotificationsForm() } }

    Column(modifier.fillMaxSize()) {
        AppTopBar(stringResource(R.string.notif_title), onBack)
        ProfileStateGate(state, onRetry = viewModel::load, onSignIn = onSignIn) { profile ->
            var stockAlerts by rememberSaveable { mutableStateOf(profile.notificationSettings.stockAlerts) }
            val saved = form.success != null

            Column(Modifier.fillMaxSize().padding(14.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (saved) {
                        MessageBanner(
                            stringResource(R.string.notif_saved_title),
                            form.success!!.asString(),
                            BannerKind.SUCCESS
                        )
                    }
                    Text(
                        stringResource(R.string.notif_intro),
                        color = MaterialTheme.extraColors.muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (permissionGranted) {
                        MessageBanner(
                            stringResource(R.string.notif_permission_on_title),
                            stringResource(R.string.notif_permission_on_message),
                            BannerKind.SUCCESS
                        )
                    } else {
                        MessageBanner(
                            stringResource(R.string.notif_permission_off_title),
                            stringResource(R.string.notif_permission_off_message),
                            BannerKind.WARNING
                        )
                    }
                    AppCard {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.notif_stock_title), fontWeight = FontWeight.Bold)
                                Text(
                                    stringResource(R.string.notif_stock_sub),
                                    color = MaterialTheme.extraColors.muted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = stockAlerts && permissionGranted,
                                onCheckedChange = { stockAlerts = it },
                                enabled = permissionGranted && !saved,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    checkedThumbColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                    form.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
                    Text(
                        stringResource(R.string.notif_footer),
                        color = MaterialTheme.extraColors.muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when {
                        saved -> PrimaryPillButton(stringResource(R.string.notif_back_to_profile), onBack)
                        !permissionGranted -> {
                            PrimaryPillButton(stringResource(R.string.notif_open_settings), { openAppNotificationSettings(context) })
                            SecondaryPillButton(stringResource(R.string.notif_back_to_profile), onBack)
                        }
                        else -> {
                            PrimaryPillButton(
                                stringResource(R.string.notif_save),
                                onClick = { viewModel.saveNotifications(stockAlerts) },
                                enabled = !form.loading
                            )
                            SecondaryPillButton(stringResource(R.string.notif_cancel), onBack)
                        }
                    }
                }
            }
        }
    }
}

private fun areNotificationsEnabled(context: Context): Boolean =
    NotificationManagerCompat.from(context).areNotificationsEnabled()

private fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
