package com.example.bille.ui.authentication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bille.model.OnboardingEvent
import com.example.bille.model.OnboardingUiState
import com.example.bille.model.Rules
import com.example.bille.ui.components.DotsIndicator
import com.example.bille.ui.components.PrimaryButton
import com.example.bille.ui.components.StepHeader
import com.example.bille.ui.theme.BilleGreen
import com.example.bille.ui.theme.BilleRed
import com.example.bille.ui.theme.BilleTextGray
import kotlinx.coroutines.launch

private data class AuthTip(
    val text: String,
    val badIcon: ImageVector,
    val goodIcon: ImageVector,
    val badBackground: Color,
    val goodBackground: Color
)

private val tips = listOf(
    AuthTip(
        "Sitúate en un lugar con buena iluminación.",
        Icons.Default.Person, Icons.Default.WbSunny,
        Color(0xFF37474F), Color(0xFFFFF3C4)
    ),
    AuthTip(
        "Retira lentes, gorra o cualquier accesorio que cubra tu rostro.",
        Icons.Default.VisibilityOff, Icons.Default.Face,
        Color(0xFFECEFF1), Color(0xFFE3F5EA)
    ),
    AuthTip(
        "Mantén tu rostro centrado y el celular a la altura de tus ojos.",
        Icons.Default.PhoneAndroid, Icons.Default.CameraAlt,
        Color(0xFFECEFF1), Color(0xFFE3F5EA)
    )
)

@Composable
fun AuthenticationScreen(
    state: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { tips.size }
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == tips.lastIndex
    var audioEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(pagerState.currentPage) {
        onEvent(OnboardingEvent.AuthPageChanged(pagerState.currentPage))
    }

    Column(modifier.fillMaxSize().background(BilleGreen)) {
        StepHeader(
            step = 2,
            stepLabel = "PASO 2 / ${Rules.TOTAL_STEPS} - Autenticación",
            title = "Autenticación",
            stepIcon = Icons.Default.Face,
            onBack = { onEvent(OnboardingEvent.BackClicked) },
            trailing = {
                IconButton(onClick = { audioEnabled = !audioEnabled }) {
                    Icon(
                        if (audioEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = if (audioEnabled) "Desactivar audio" else "Activar audio",
                        tint = Color.White
                    )
                }
            }
        )

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Antes de comenzar con la prueba de autenticación, te recomendamos:",
                style = MaterialTheme.typography.bodyLarge,
                color = BilleTextGray,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                val tip = tips[page]
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        tip.text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(28.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ComparisonCard(tip.badIcon, tip.badBackground, good = false, dark = page == 0)
                        ComparisonCard(tip.goodIcon, tip.goodBackground, good = true, dark = false)
                    }
                }
            }

            DotsIndicator(count = tips.size, current = pagerState.currentPage)
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Siguiente",
                onClick = {
                    if (isLast) {
                        onEvent(OnboardingEvent.AuthFinished)
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                }
            )
        }
    }
}

@Composable
private fun ComparisonCard(icon: ImageVector, background: Color, good: Boolean, dark: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(width = 132.dp, height = 156.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = when {
                    dark -> Color(0xFF90A4AE)
                    good -> BilleGreen
                    else -> Color(0xFF78909C)
                },
                modifier = Modifier.size(64.dp)
            )
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (good) BilleGreen else BilleRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (good) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (good) "Correcto" else "Incorrecto",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            if (good) "Correcto" else "Incorrecto",
            color = if (good) BilleGreen else BilleRed,
            fontWeight = FontWeight.SemiBold
        )
    }
}
