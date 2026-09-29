package com.example.bille.ui.registration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bille.model.OnboardingEvent
import com.example.bille.model.OnboardingUiState
import com.example.bille.model.Rules
import com.example.bille.ui.components.PrimaryButton
import com.example.bille.ui.components.StepHeader
import com.example.bille.ui.theme.BilleGreen
import com.example.bille.ui.theme.BilleTextGray

@Composable
fun RegistrationScreen(
    state: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().background(BilleGreen)) {
        StepHeader(
            step = 1,
            stepLabel = "PASO 1 / ${Rules.TOTAL_STEPS} - Información",
            title = "Información",
            stepIcon = Icons.Default.Person,
            onBack = { onEvent(OnboardingEvent.BackClicked) },
            heading = "Ingresa tus datos",
            subtitle = "¡Únete a nuestra app hoy! Completa los siguientes datos para comenzar a disfrutar de tu Bille."
        )

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .navigationBarsPadding()
                .imePadding()
                .padding(24.dp)
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BilleTextField(
                    value = state.phone,
                    onValueChange = { onEvent(OnboardingEvent.PhoneChanged(it)) },
                    label = "Número de celular",
                    maxLength = Rules.PHONE_LENGTH,
                    keyboardType = KeyboardType.Number,
                    error = if (state.phone.isNotEmpty() && !state.isPhoneValid)
                        "Debe tener ${Rules.PHONE_LENGTH} dígitos" else null
                )

                BilleTextField(
                    value = state.carnet,
                    onValueChange = { onEvent(OnboardingEvent.CarnetChanged(it)) },
                    label = "Número de carnet",
                    maxLength = Rules.CARNET_MAX,
                    keyboardType = KeyboardType.Number,
                    error = if (state.carnet.isNotEmpty() && !state.isCarnetValid)
                        "Debe tener entre ${Rules.CARNET_MIN} y ${Rules.CARNET_MAX} dígitos" else null
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onEvent(OnboardingEvent.ComplementToggled(!state.hasComplement)) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.hasComplement,
                        onCheckedChange = { onEvent(OnboardingEvent.ComplementToggled(it)) },
                        colors = CheckboxDefaults.colors(checkedColor = BilleGreen)
                    )
                    Text("¿Tiene complemento?", fontWeight = FontWeight.Medium)
                }

                AnimatedVisibility(visible = state.hasComplement) {
                    BilleTextField(
                        value = state.complement,
                        onValueChange = { onEvent(OnboardingEvent.ComplementChanged(it)) },
                        label = "Complemento",
                        maxLength = Rules.COMPLEMENT_MAX,
                        keyboardType = KeyboardType.Ascii,
                        capitalization = KeyboardCapitalization.Characters,
                        error = if (!state.isComplementValid && state.complement.isNotEmpty())
                            "Solo letras y números" else null
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                text = "Siguiente",
                onClick = onNextClick,
                enabled = state.canContinue,
                loading = state.isLoading
            )
        }
    }
}

@Composable
private fun BilleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    maxLength: Int,
    keyboardType: KeyboardType,
    error: String?,
    modifier: Modifier = Modifier,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = capitalization),
        supportingText = {
            Row(Modifier.fillMaxWidth()) {
                Text(error ?: "", modifier = Modifier.weight(1f))
                Text("${value.length}/$maxLength", color = BilleTextGray)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BilleGreen,
            focusedLabelColor = BilleGreen,
            cursorColor = BilleGreen
        ),
        modifier = modifier.fillMaxWidth()
    )
}
