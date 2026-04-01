package com.example.contentblocker.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

@Composable
fun SetPinDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin     by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error   by remember { mutableStateOf("") }

    val shakeAnim = remember { Animatable(0f) }
    val scope     = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(0.dp)) {
            Column(
                modifier            = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Définir un PIN", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                Text(
                    "Ce PIN sera demandé pour désactiver le blocage.",
                    fontSize = 13.sp,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("PIN", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    PinDots(length = pin.length)
                    OutlinedTextField(
                        value                = pin,
                        onValueChange        = { if (it.length <= 4 && it.all(Char::isDigit)) pin = it },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier             = Modifier.fillMaxWidth(),
                        singleLine           = true,
                        placeholder          = { Text("••••") }
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Confirmer", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    PinDots(length = confirm.length)
                    OutlinedTextField(
                        value                = confirm,
                        onValueChange        = { if (it.length <= 4 && it.all(Char::isDigit)) confirm = it },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier             = Modifier.fillMaxWidth(),
                        singleLine           = true,
                        placeholder          = { Text("••••") }
                    )
                }

                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text("Annuler")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        onClick  = {
                            error = when {
                                pin.length != 4 -> "Le PIN doit faire 4 chiffres"
                                pin != confirm  -> "Les PINs ne correspondent pas"
                                else            -> { onConfirm(pin); return@Button }
                            }
                        }
                    ) { Text("Valider") }
                }
            }
        }
    }
}

@Composable
fun VerifyPinDialog(
    title:     String = "Entrer le PIN",
    onConfirm: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    var pin   by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val shakeAnim = remember { Animatable(0f) }
    val scope     = rememberCoroutineScope()

    LaunchedEffect(error) {
        if (error.isNotEmpty()) {
            repeat(3) {
                shakeAnim.animateTo(10f, tween(50))
                shakeAnim.animateTo(-10f, tween(50))
            }
            shakeAnim.animateTo(0f, tween(50))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape     = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                PinDots(length = pin.length)

                OutlinedTextField(
                    value                = pin,
                    onValueChange        = { if (it.length <= 4 && it.all(Char::isDigit)) pin = it },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier             = Modifier.fillMaxWidth(),
                    singleLine           = true,
                    isError              = error.isNotEmpty(),
                    placeholder          = { Text("••••") }
                )

                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text("Annuler")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        onClick  = {
                            if (!onConfirm(pin)) {
                                error = "PIN incorrect"
                                pin   = ""
                                scope.launch { shakeAnim.animateTo(0f) }
                            }
                        }
                    ) { Text("Confirmer") }
                }
            }
        }
    }
}

@Composable
fun PinDots(length: Int, maxLength: Int = 4) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(maxLength) { index ->
            val filled = index < length
            val dotScale by animateFloatAsState(
                targetValue   = if (filled) 1f else 0.55f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium
                ),
                label = "dot$index"
            )
            val dotColor by animateColorAsState(
                targetValue   = if (filled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                animationSpec = tween(150),
                label         = "dotColor$index"
            )
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .scale(dotScale)
                    .background(color = dotColor, shape = CircleShape)
            )
        }
    }
}
