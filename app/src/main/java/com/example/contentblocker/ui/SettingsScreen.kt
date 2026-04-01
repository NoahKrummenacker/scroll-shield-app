package com.example.contentblocker.ui

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contentblocker.data.PrefsManager

@Composable
fun SettingsScreen(context: Context) {
    val prefs = remember { PrefsManager(context) }

    var scheduleEnabled     by remember { mutableStateOf(prefs.scheduleEnabled) }
    var startHour           by remember { mutableIntStateOf(prefs.scheduleStartHour) }
    var startMin            by remember { mutableIntStateOf(prefs.scheduleStartMin) }
    var endHour             by remember { mutableIntStateOf(prefs.scheduleEndHour) }
    var endMin              by remember { mutableIntStateOf(prefs.scheduleEndMin) }
    var pinEnabled          by remember { mutableStateOf(prefs.pinEnabled) }
    var showSetPinDialog    by remember { mutableStateOf(false) }
    var showRemovePinDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Paramètres", fontWeight = FontWeight.Bold, fontSize = 22.sp)

        // ── Horaires ──────────────────────────────────────────────────────────
        SectionLabel("Horaires de blocage")

        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Activer les horaires", fontWeight = FontWeight.Medium)
                        Text(
                            "Bloquer uniquement pendant une plage définie",
                            fontSize = 12.sp,
                            color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked         = scheduleEnabled,
                        onCheckedChange = { scheduleEnabled = it; prefs.scheduleEnabled = it }
                    )
                }

                AnimatedVisibility(
                    visible = scheduleEnabled,
                    enter   = expandVertically(tween(300)) + fadeIn(tween(300)),
                    exit    = shrinkVertically(tween(300)) + fadeOut(tween(300))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HorizontalDivider()

                        Text(
                            "Appuie sur une heure pour la modifier",
                            fontSize = 12.sp,
                            color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            TimePickerButton("Début", startHour, startMin, context) { h, m ->
                                startHour = h; startMin = m
                                prefs.scheduleStartHour = h; prefs.scheduleStartMin = m
                            }
                            Text(
                                "→",
                                fontSize = 22.sp,
                                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            TimePickerButton("Fin", endHour, endMin, context) { h, m ->
                                endHour = h; endMin = m
                                prefs.scheduleEndHour = h; prefs.scheduleEndMin = m
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "Blocage actif de %02d:%02d à %02d:%02d".format(startHour, startMin, endHour, endMin),
                                modifier   = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontSize   = 13.sp,
                                color      = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

        // ── PIN ───────────────────────────────────────────────────────────────
        SectionLabel("Mot de passe (PIN)")

        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        if (pinEnabled) "PIN activé" else "PIN désactivé",
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        if (pinEnabled) "Un PIN est requis pour désactiver le blocage"
                        else "Protège le blocage avec un code à 4 chiffres",
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                AnimatedContent(
                    targetState = pinEnabled,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label = "pinButton"
                ) { enabled ->
                    if (!enabled) {
                        Button(
                            onClick  = { showSetPinDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp)
                        ) { Text("Définir un PIN") }
                    } else {
                        OutlinedButton(
                            onClick  = { showRemovePinDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) { Text("Supprimer le PIN") }
                    }
                }
            }
        }
    }

    if (showSetPinDialog) {
        SetPinDialog(
            onConfirm = { pin ->
                prefs.setPin(pin)
                pinEnabled       = true
                showSetPinDialog = false
            },
            onDismiss = { showSetPinDialog = false }
        )
    }

    if (showRemovePinDialog) {
        VerifyPinDialog(
            title     = "Confirme ton PIN pour le supprimer",
            onConfirm = { pin ->
                if (prefs.verifyPin(pin)) {
                    prefs.removePin()
                    pinEnabled          = false
                    showRemovePinDialog = false
                    true
                } else false
            },
            onDismiss = { showRemovePinDialog = false }
        )
    }
}

@Composable
fun TimePickerButton(
    label:        String,
    hour:         Int,
    minute:       Int,
    context:      Context,
    onTimePicked: (Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.clickable {
            TimePickerDialog(context, { _, h, m -> onTimePicked(h, m) }, hour, minute, true).show()
        },
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 28.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                label,
                fontSize = 12.sp,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                "%02d:%02d".format(hour, minute),
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
    }
}
