package com.example.contentblocker.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.contentblocker.data.PrefsManager

@Composable
fun HomeScreen(context: Context) {
    val prefs = remember { PrefsManager(context) }

    var serviceEnabled  by remember { mutableStateOf(false) }
    var blockReels      by remember { mutableStateOf(prefs.blockReels) }
    var blockReelsFeed  by remember { mutableStateOf(prefs.blockReelsFeed) }
    var blockShorts     by remember { mutableStateOf(prefs.blockShorts) }
    var blockShortsFeed by remember { mutableStateOf(prefs.blockShortsFeed) }

    var showPinDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    LifecycleResumeEffect(context) {
        serviceEnabled  = isAccessibilityServiceEnabled(context)
        blockReelsFeed  = prefs.blockReelsFeed
        blockShortsFeed = prefs.blockShortsFeed
        onPauseOrDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusCard(serviceEnabled)

        AnimatedVisibility(
            visible = !serviceEnabled,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Button(
                onClick  = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp)
            ) {
                Text("Activer dans les paramètres d'accessibilité", modifier = Modifier.padding(vertical = 6.dp))
            }
        }

        AnimatedVisibility(
            visible = serviceEnabled,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            TextButton(
                onClick  = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Paramètres d'accessibilité", fontSize = 13.sp)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Text("Contenu à bloquer", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

        SectionLabel("Instagram")

        BlockOptionCard(
            title    = "Reels Instagram",
            subtitle = "Bloque la section Reels d'Instagram",
            checked  = blockReels,
            enabled  = serviceEnabled,
            onToggle = { v ->
                if (!v && prefs.pinEnabled) {
                    pendingAction = { blockReels = false; prefs.blockReels = false }
                    showPinDialog = true
                } else { blockReels = v; prefs.blockReels = v }
            }
        )

        BlockOptionCard(
            title    = "Reels partout",
            subtitle = "Bloque aussi les Reels ouverts depuis le fil ou l'explore",
            checked  = blockReelsFeed,
            enabled  = serviceEnabled,
            onToggle = { v ->
                if (!v && prefs.pinEnabled) {
                    pendingAction = { blockReelsFeed = false; prefs.blockReelsFeed = false }
                    showPinDialog = true
                } else { blockReelsFeed = v; prefs.blockReelsFeed = v }
            }
        )

        Spacer(Modifier.height(4.dp))
        SectionLabel("YouTube")

        BlockOptionCard(
            title    = "Shorts YouTube",
            subtitle = "Bloque la section Shorts de YouTube",
            checked  = blockShorts,
            enabled  = serviceEnabled,
            onToggle = { v ->
                if (!v && prefs.pinEnabled) {
                    pendingAction = { blockShorts = false; prefs.blockShorts = false }
                    showPinDialog = true
                } else { blockShorts = v; prefs.blockShorts = v }
            }
        )

        BlockOptionCard(
            title    = "Shorts partout",
            subtitle = "Bloque les Shorts même lancés depuis l'accueil YouTube",
            checked  = blockShortsFeed,
            enabled  = serviceEnabled,
            onToggle = { v ->
                if (!v && prefs.pinEnabled) {
                    pendingAction = { blockShortsFeed = false; prefs.blockShortsFeed = false }
                    showPinDialog = true
                } else { blockShortsFeed = v; prefs.blockShortsFeed = v }
            }
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "Le service d'accessibilité détecte et bloque uniquement le contenu ciblé. Aucune donnée n'est collectée.",
            fontSize   = 11.sp,
            color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            lineHeight = 16.sp
        )
    }

    if (showPinDialog) {
        VerifyPinDialog(
            title     = "PIN requis pour désactiver",
            onConfirm = { pin ->
                if (prefs.verifyPin(pin)) {
                    pendingAction?.invoke()
                    pendingAction = null
                    showPinDialog = false
                    true
                } else false
            },
            onDismiss = { pendingAction = null; showPinDialog = false }
        )
    }
}

// ─── Composants ───────────────────────────────────────────────────────────────

@Composable
fun SectionLabel(text: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Text(
            text,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 13.sp,
            color      = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun StatusCard(serviceEnabled: Boolean) {
    val dotColor by animateColorAsState(
        targetValue   = if (serviceEnabled) Color(0xFF4CAF50) else Color(0xFFE53935),
        animationSpec = tween(600),
        label         = "dotColor"
    )
    val bgColor by animateColorAsState(
        targetValue   = if (serviceEnabled) Color(0xFF1B5E20).copy(alpha = 0.25f)
                        else Color(0xFF7F0000).copy(alpha = 0.25f),
        animationSpec = tween(600),
        label         = "bgColor"
    )

    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue  = 1f,
        targetValue   = if (serviceEnabled) 1.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier.padding(18.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .scale(pulseScale)
                        .background(dotColor.copy(alpha = 0.25f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .background(dotColor, CircleShape)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (serviceEnabled) "Service actif" else "Service inactif",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
                Text(
                    if (serviceEnabled) "Le blocage est opérationnel"
                    else "Active le service ci-dessous pour commencer",
                    fontSize = 13.sp,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
fun BlockOptionCard(
    title:    String,
    subtitle: String,
    checked:  Boolean,
    enabled:  Boolean,
    onToggle: (Boolean) -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue   = if (checked && enabled)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        animationSpec = tween(300),
        label         = "cardBg"
    )

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier             = Modifier.weight(1f),
                verticalArrangement  = Arrangement.spacedBy(3.dp)
            ) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(
                    subtitle,
                    fontSize   = 12.sp,
                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    lineHeight = 16.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(checked = checked, onCheckedChange = onToggle, enabled = enabled)
        }
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .any { it.resolveInfo.serviceInfo.packageName == context.packageName }
}
