package com.mvukosav.scoreagentsvas.utils

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.placeholder.placeholder

@Composable
fun Modifier.shimmer() = placeholder(
    visible = true,
    highlight = PlaceholderHighlight.shimmer(
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, delayMillis = 500),
            repeatMode = RepeatMode.Restart,
        ),
    ),
    color = Color.Blue,
    shape = RoundedCornerShape(12.dp),
)
