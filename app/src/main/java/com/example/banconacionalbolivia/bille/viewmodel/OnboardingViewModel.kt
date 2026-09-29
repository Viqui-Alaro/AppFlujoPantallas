package com.example.bille.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bille.model.OnboardingEffect
import com.example.bille.model.OnboardingEvent
import com.example.bille.model.OnboardingUiState
import com.example.bille.model.Rules
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    private val _effects = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effects: Flow<OnboardingEffect> = _effects.receiveAsFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.PhoneChanged -> _state.update {
                it.copy(phone = event.value.replace(Rules.NON_DIGITS, "").take(Rules.PHONE_LENGTH))
            }

            is OnboardingEvent.CarnetChanged -> _state.update {
                it.copy(carnet = event.value.replace(Rules.NON_DIGITS, "").take(Rules.CARNET_MAX))
            }

            is OnboardingEvent.ComplementToggled -> _state.update {
                it.copy(
                    hasComplement = event.enabled,
                    complement = if (event.enabled) it.complement else ""
                )
            }

            is OnboardingEvent.ComplementChanged -> _state.update {
                it.copy(
                    complement = event.value
                        .replace(Rules.NON_ALPHANUMERIC, "")
                        .take(Rules.COMPLEMENT_MAX)
                        .uppercase()
                )
            }

            is OnboardingEvent.NextClicked -> onNext(event.hasLocationPermission)

            OnboardingEvent.LocationSheetDismissed ->
                _state.update { it.copy(showLocationSheet = false) }

            OnboardingEvent.LocationSheetContinue -> {
                _state.update { it.copy(showLocationSheet = false) }
                _effects.trySend(OnboardingEffect.RequestLocationPermission)
            }

            is OnboardingEvent.PermissionResult -> {
                if (event.granted) {
                    callService()
                } else if (event.permanentlyDenied) {
                    _effects.trySend(
                        OnboardingEffect.ShowSettingsPrompt(
                            "El permiso de ubicación está bloqueado. Actívalo desde los ajustes de la app para continuar."
                        )
                    )
                } else {
                    _effects.trySend(
                        OnboardingEffect.ShowMessage("La ubicación es requerida para continuar con la creación de tu Bille.")
                    )
                }
            }

            OnboardingEvent.BackClicked -> onBack()

            is OnboardingEvent.AuthPageChanged ->
                _state.update { it.copy(authPage = event.page) }

            OnboardingEvent.AuthFinished -> _effects.trySend(
                OnboardingEffect.ShowMessage("Prueba de autenticación lista para iniciar (paso 3 pendiente de implementar).")
            )
        }
    }

    private fun onNext(hasLocationPermission: Boolean) {
        val current = _state.value
        if (!current.canContinue || current.isLoading) return

        if (hasLocationPermission) {
            callService()
        } else {
            // No se consume el servicio hasta tener permisos
            _state.update { it.copy(showLocationSheet = true) }
        }
    }

    /** Simula el consumo de un servicio remoto y avanza al paso 2. */
    private fun callService() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(1_800)
            _state.update { it.copy(isLoading = false, step = 2) }
        }
    }

    private fun onBack() {
        val current = _state.value
        when {
            current.isLoading -> Unit
            current.step > 1 -> _state.update { it.copy(step = it.step - 1) }
            else -> _effects.trySend(OnboardingEffect.CloseFlow)
        }
    }
}
