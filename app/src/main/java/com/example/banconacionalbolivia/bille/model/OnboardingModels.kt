package com.example.bille.model

/** Reglas de negocio de validación (longitudes y expresiones regulares). */
object Rules {
    const val TOTAL_STEPS = 6

    const val PHONE_LENGTH = 8
    const val CARNET_MIN = 5
    const val CARNET_MAX = 10
    const val COMPLEMENT_MAX = 2

    // Filtros de entrada en tiempo real: eliminan todo lo que no sea válido
    val NON_DIGITS = Regex("[^0-9]")
    val NON_ALPHANUMERIC = Regex("[^A-Za-z0-9]")

    // Validación final de cada campo
    val PHONE_REGEX = Regex("^[0-9]{8}$")
    val CARNET_REGEX = Regex("^[0-9]{5,10}$")
    val COMPLEMENT_REGEX = Regex("^[A-Za-z0-9]{1,2}$")
}

data class OnboardingUiState(
    val step: Int = 1,
    val phone: String = "",
    val carnet: String = "",
    val hasComplement: Boolean = false,
    val complement: String = "",
    val showLocationSheet: Boolean = false,
    val isLoading: Boolean = false,
    val authPage: Int = 0
) {
    val isPhoneValid: Boolean get() = Rules.PHONE_REGEX.matches(phone)
    val isCarnetValid: Boolean get() = Rules.CARNET_REGEX.matches(carnet)
    val isComplementValid: Boolean
        get() = !hasComplement || Rules.COMPLEMENT_REGEX.matches(complement)

    /** El botón "Siguiente" del paso 1 se habilita solo si todo es válido. */
    val canContinue: Boolean get() = isPhoneValid && isCarnetValid && isComplementValid
}

/** Eventos UI -> ViewModel (flujo unidireccional). */
sealed interface OnboardingEvent {
    data class PhoneChanged(val value: String) : OnboardingEvent
    data class CarnetChanged(val value: String) : OnboardingEvent
    data class ComplementToggled(val enabled: Boolean) : OnboardingEvent
    data class ComplementChanged(val value: String) : OnboardingEvent
    data class NextClicked(val hasLocationPermission: Boolean) : OnboardingEvent
    data object LocationSheetDismissed : OnboardingEvent
    data object LocationSheetContinue : OnboardingEvent
    //data class PermissionResult(val granted: Boolean) : OnboardingEvent
    data class PermissionResult(
        val granted: Boolean,
        val permanentlyDenied: Boolean = false
    ) : OnboardingEvent
    data object BackClicked : OnboardingEvent
    data class AuthPageChanged(val page: Int) : OnboardingEvent
    data object AuthFinished : OnboardingEvent
}

/** Efectos de una sola vez ViewModel -> UI. */
sealed interface OnboardingEffect {
    data object RequestLocationPermission : OnboardingEffect
    data class ShowMessage(val text: String) : OnboardingEffect
    data object CloseFlow : OnboardingEffect
    data class ShowSettingsPrompt(val text: String) : OnboardingEffect
}
