package com.example.kawansekolah.ui.splash

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2500L)
        onTimeout()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_loading_slowmo")
    val dotCount = 4

    val dotScales = List(dotCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.55f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(index * 180)
            ),
            label = "dot_scale_$index"
        )
    }

    val androidVersionName = try { Build.VERSION.RELEASE ?: "14" } catch (_: Exception) { "14" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2563EB),
                        Color(0xFF1D4ED8),
                        Color(0xFF1E40AF)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Static Logo Box Badge
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.logo_scolarsia),
                    contentDescription = "Logo Scolarsia",
                    modifier = Modifier.size(88.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Static Title Text
            Text(
                text = "Scolarsia",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Static Tagline Text
            Text(
                text = "Sinergi Sekolah, Panduan Masa Depan",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // 4 Animated Dots with Fixed Layout Height (Logo & Text Stay 100% Still)
            Row(
                modifier = Modifier.height(20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dotScales.forEach { scale ->
                    val curScale = scale.value
                    val dotSize = (8f * curScale).dp
                    val dotOpacity = (0.35f + 0.65f * (curScale - 0.55f) / 0.8f).coerceIn(0.35f, 1.0f)

                    Box(
                        modifier = Modifier
                            .size(14.dp), // Fixed Container so outer row height never fluctuates
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(dotSize)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = dotOpacity))
                        )
                    }
                }
            }
        }

        Text(
            text = "Versi 2.0.3 • Android $androidVersionName",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.75f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
