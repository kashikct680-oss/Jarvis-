package com.kishore.jarvis.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun HologramCore(
    isListening: Boolean = false,
    isProcessing: Boolean = false,
    isSpeaking: Boolean = false,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "jarvis_hologram")

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val coreColor = when {
        isListening -> Color.Cyan
        isProcessing -> Color.Magenta
        isSpeaking -> Color(0xFF00FF88)
        else -> Color(0xFF00BFFF)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.minDimension * 0.22f

            drawCircle(
                color = coreColor.copy(alpha = 0.12f),
                radius = baseRadius * pulse * 1.7f,
                center = center
            )

            drawCircle(
                color = coreColor.copy(alpha = 0.35f),
                radius = baseRadius * pulse,
                center = center,
                style = Stroke(width = 3f)
            )

            drawCircle(
                color = coreColor,
                radius = baseRadius * 0.55f,
                center = center
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = baseRadius * 0.15f,
                center = center
            )

            drawArc(
                color = coreColor,
                startAngle = rotation,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(
                    center.x - baseRadius * 1.5f,
                    center.y - baseRadius * 1.5f
                ),
                size = androidx.compose.ui.geometry.Size(
                    baseRadius * 3f,
                    baseRadius * 3f
                ),
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )

            drawArc(
                color = Color.White.copy(alpha = 0.6f),
                startAngle = rotation + 180f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(
                    center.x - baseRadius * 1.8f,
                    center.y - baseRadius * 1.8f
                ),
                size = androidx.compose.ui.geometry.Size(
                    baseRadius * 3.6f,
                    baseRadius * 3.6f
                ),
                style = Stroke(width = 2f)
            )
        }
    }
}
