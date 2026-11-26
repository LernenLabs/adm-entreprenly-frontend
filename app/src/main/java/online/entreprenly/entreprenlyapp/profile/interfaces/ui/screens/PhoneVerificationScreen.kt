package online.entreprenly.entreprenlyapp.profile.interfaces.ui.screens

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.components.ProfileStateGate
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels.PhoneVerificationState
import online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels.ProfileViewModel
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.BannerKind
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.MessageBanner
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PlainTopBar
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.SecondaryPillButton
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.components.PrimaryPillButton

private const val CodeLength = 6

@Composable
fun PhoneVerificationScreen(viewModel: ProfileViewModel, onBack: () -> Unit, onSignIn: () -> Unit) {
    val profileState by viewModel.uiState.collectAsState()
    val state by viewModel.phoneVerification.collectAsState()
    var changingNumber by rememberSaveable { mutableStateOf(false) }
    val goBack = {
        if (!state.form.loading) {
            when {
                state.sentTo != null -> viewModel.resetPhoneVerification()
                changingNumber && !state.verified -> changingNumber = false
                else -> onBack()
            }
        }
    }
    BackHandler(enabled = changingNumber || state.sentTo != null || state.form.loading, onBack = goBack)

    Column(Modifier.fillMaxSize().imePadding().navigationBarsPadding()) {
        val title = if (state.sentTo != null) {
            R.string.profile_phone_verify
        } else {
            R.string.profile_phone_title
        }
        PlainTopBar(stringResource(title), goBack)
        ProfileStateGate(profileState, onRetry = viewModel::load, onSignIn = onSignIn) { profile ->
            if (state.verified || (profile.phoneVerified && !changingNumber)) {
                PhoneVerifiedContent(
                    phone = profile.phone.orEmpty(),
                    animate = state.verified,
                    onDone = onBack,
                    onChangeNumber = {
                        viewModel.resetPhoneVerification()
                        changingNumber = true
                    }
                )
            } else {
                PhoneVerificationForm(
                    state = state,
                    currentPhone = if (profile.phoneVerified) "" else profile.phone.orEmpty(),
                    onSend = viewModel::requestPhoneVerification,
                    onConfirm = viewModel::confirmPhoneVerification,
                    onChangeNumber = viewModel::resetPhoneVerification
                )
            }
        }
    }
}

@Composable
private fun PhoneVerificationForm(
    state: PhoneVerificationState,
    currentPhone: String,
    onSend: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onChangeNumber: () -> Unit
) {
    var phone by rememberSaveable { mutableStateOf(currentPhone) }
    var code by remember(state.sentTo, state.resendAtMillis) { mutableStateOf("") }
    var resendSeconds by remember(state.resendAtMillis) { mutableIntStateOf(remainingSeconds(state.resendAtMillis)) }
    LaunchedEffect(state.resendAtMillis) {
        while (resendSeconds > 0) {
            delay(250)
            resendSeconds = remainingSeconds(state.resendAtMillis)
        }
    }

    val loading = state.form.loading
    val destination = state.sentTo
    val title = when {
        destination == null -> R.string.profile_phone_number_heading
        else -> R.string.profile_phone_code_heading
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            when {
                destination == null -> {
                    Text(stringResource(R.string.profile_phone_number_intro), style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(stringResource(R.string.profile_phone_title)) },
                        placeholder = { Text("+51 999 000 123") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        enabled = !loading,
                        isError = state.form.error != null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    Text(
                        stringResource(R.string.profile_phone_destination, destination.value),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    VerificationCodeField(code, { code = it }, enabled = !loading)
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(
                            onClick = { onSend(destination.value) },
                            enabled = !loading && resendSeconds == 0
                        ) {
                            Text(
                                if (resendSeconds > 0) {
                                    stringResource(R.string.profile_phone_resend_timer, resendSeconds)
                                } else {
                                    stringResource(R.string.profile_phone_resend)
                                }
                            )
                        }
                        TextButton(onClick = onChangeNumber, enabled = !loading) {
                            Text(stringResource(R.string.profile_phone_change_number))
                        }
                    }
                }
            }
            state.form.error?.let {
                MessageBanner(stringResource(R.string.profile_phone_error_title), it.asString(), BannerKind.ERROR)
            }
        }

        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            when {
                loading -> CircularProgressIndicator(Modifier.height(40.dp))
                destination == null -> PrimaryPillButton(
                    stringResource(R.string.profile_phone_send),
                    onClick = { onSend(phone) },
                    enabled = phone.isNotBlank()
                )
                else -> PrimaryPillButton(
                    stringResource(R.string.profile_phone_confirm),
                    onClick = { onConfirm(code) },
                    enabled = code.length == CodeLength
                )
            }
        }
    }
}

@Composable
private fun PhoneVerifiedContent(
    phone: String,
    animate: Boolean,
    onDone: () -> Unit,
    onChangeNumber: () -> Unit
) {
    var visible by remember { mutableStateOf(!animate) }
    LaunchedEffect(Unit) { visible = true }
    val colors = MaterialTheme.extraColors
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + scaleIn(
                    initialScale = 0.55f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                Box(
                    Modifier.size(108.dp).background(colors.successBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, null, Modifier.size(58.dp), tint = colors.success)
                }
            }
            Text(
                stringResource(R.string.profile_phone_verified),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(phone, color = colors.muted)
        }
        Column(Modifier.padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryPillButton(stringResource(R.string.profile_phone_done), onDone)
            SecondaryPillButton(stringResource(R.string.profile_phone_change_number), onChangeNumber)
        }
    }
}

@Composable
private fun VerificationCodeField(code: String, onCodeChange: (String) -> Unit, enabled: Boolean) {
    val label = stringResource(R.string.profile_phone_code)
    BasicTextField(
        value = code,
        onValueChange = { value -> onCodeChange(value.filter { it in '0'..'9' }.take(CodeLength)) },
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = MaterialTheme.typography.titleLarge.copy(color = Color.Transparent),
        cursorBrush = SolidColor(Color.Transparent),
        decorationBox = { innerTextField ->
            Box {
                Row(
                    Modifier.fillMaxWidth().clearAndSetSemantics { },
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(CodeLength) { index ->
                        val active = enabled && index == code.length
                        val outline = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        Box(
                            modifier = Modifier.weight(1f).height(54.dp)
                                .border(if (active) 2.dp else 1.dp, outline, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                code.getOrNull(index)?.toString().orEmpty(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Box(Modifier.matchParentSize().padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                    innerTextField()
                }
            }
        }
    )
}

private fun remainingSeconds(resendAtMillis: Long): Int =
    ((resendAtMillis - SystemClock.elapsedRealtime()).coerceAtLeast(0L) + 999L).div(1_000L).toInt()
