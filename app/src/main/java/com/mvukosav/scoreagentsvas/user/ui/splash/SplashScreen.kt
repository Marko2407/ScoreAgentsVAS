package com.mvukosav.scoreagentsvas.user.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mvukosav.scoreagentsvas.R

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val imageSize = remember { Animatable(300f) }
        LaunchedEffect(key1 = "animation") {
            imageSize.animateTo(
                targetValue = 600f,
                animationSpec = repeatable(
                    iterations = Int.MAX_VALUE,
                    animation = tween(
                        durationMillis = 2000,
                        easing = {
                            if (it <= 0.6f) {
                                2 * it
                            } else {
                                2 - 2 * it
                            }
                        }
                    )
                )
            )
        }

        Image(
            painter = painterResource(id = R.drawable.logo_app),
            contentDescription = null,
            modifier = Modifier.size(imageSize.value.dp)
        )
    }
}

