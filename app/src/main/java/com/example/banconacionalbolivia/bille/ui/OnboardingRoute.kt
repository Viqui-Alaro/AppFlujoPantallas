package com.example.bille.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bille.model.OnboardingEffect
import com.example.bille.model.OnboardingEvent
import com.example.bille.ui.authentication.AuthenticationScreen
import com.example.bille.ui.registration.LocationBottomSheet
import com.example.bille.ui.registration.RegistrationScreen
import com.example.bille.ui.theme.BilleGreen
import com.example.bille.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

/** Se considera concedido si el usuario dio ubicación precisa o aproximada. */
private fun Context.hasLocationPermission(): Boolean = LOCATION_PERMISSIONS.any {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

@Composable
fun OnboardingRoute(
    onExit: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope ()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        val activity = context.findActivity()
        // Tras una solicitud denegada, si Android ya no ofrece el diálogo, el permiso quedó bloqueado
        val permanentlyDenied = !granted && activity != null &&
                LOCATION_PERMISSIONS.none { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
        viewModel.onEvent(OnboardingEvent.PermissionResult(granted, permanentlyDenied))
    }

    // Efectos de una sola vez emitidos por el ViewModel
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                OnboardingEffect.RequestLocationPermission -> permissionLauncher.launch(LOCATION_PERMISSIONS)
                is OnboardingEffect.ShowMessage -> scope.launch {
                    snackbarHostState.showSnackbar(effect.text)
                }

                is OnboardingEffect.ShowSettingsPrompt -> scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.text,
                        actionLabel = "Ajustes",
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) context.openAppSettings()
                }

                OnboardingEffect.CloseFlow -> onExit()
            }
        }
    }

    BackHandler { viewModel.onEvent(OnboardingEvent.BackClicked) }

    Box(Modifier.fillMaxSize().background(BilleGreen)) {
        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                val forward = targetState > initialState
                (slideInHorizontally { if (forward) it else -it } togetherWith
                        slideOutHorizontally { if (forward) -it else it })
                    .using(SizeTransform(clip = false))
            },
            label = "onboardingStep"
        ) { step ->
            when (step) {
                1 -> RegistrationScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onNextClick = {
                        viewModel.onEvent(OnboardingEvent.NextClicked(context.hasLocationPermission()))
                    }
                )

                else -> AuthenticationScreen(
                    state = state,
                    onEvent = viewModel::onEvent
                )
            }
        }

        if (state.showLocationSheet) {
            LocationBottomSheet(
                onDismiss = { viewModel.onEvent(OnboardingEvent.LocationSheetDismissed) },
                onContinue = { viewModel.onEvent(OnboardingEvent.LocationSheetContinue) }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}
