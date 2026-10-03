package online.entreprenly.entreprenlyapp.shared.interfaces.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.theme.extraColors

private val CardShape = RoundedCornerShape(14.dp)
private val ButtonShape = RoundedCornerShape(28.dp)

/** Orange top bar used by every inner screen (title plus optional back arrow). */
@Composable
fun AppTopBar(title: String, onBack: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.nav_back),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Box(Modifier.padding(start = 8.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/** White rounded card with a hairline border; clickable when [onClick] is given. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val border = BorderStroke(1.dp, MaterialTheme.extraColors.border)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = CardShape,
            color = colors.surface,
            border = border
        ) { content() }
    } else {
        Surface(modifier = modifier.fillMaxWidth(), shape = CardShape, color = colors.surface, border = border) {
            content()
        }
    }
}

/** Row card: bold title, muted subtitle and a chevron. */
@Composable
fun NavigationCard(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AppCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.extraColors.muted
                    )
                }
            }
            if (onClick != null) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.extraColors.muted
                )
            }
        }
    }
}

enum class BannerKind { SUCCESS, WARNING }

/** Highlighted message: green for success, amber for warnings. */
@Composable
fun MessageBanner(
    title: String,
    message: String,
    kind: BannerKind,
    modifier: Modifier = Modifier
) {
    val extra = MaterialTheme.extraColors
    val (fg, bg) = when (kind) {
        BannerKind.SUCCESS -> extra.success to extra.successBackground
        BannerKind.WARNING -> extra.warning to extra.warningBackground
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = bg,
        border = BorderStroke(1.dp, fg)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = fg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(message, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Full-width pill button, orange background. */
@Composable
fun PrimaryPillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.extraColors.border,
            disabledContentColor = MaterialTheme.extraColors.muted
        ),
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

/** Full-width pill button with a surface background and a hairline border. */
@Composable
fun SecondaryPillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        shape = ButtonShape,
        border = BorderStroke(1.dp, MaterialTheme.extraColors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) { Text(text, fontWeight = FontWeight.Bold) }
}
