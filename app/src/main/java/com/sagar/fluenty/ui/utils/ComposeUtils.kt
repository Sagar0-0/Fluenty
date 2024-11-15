package com.sagar.fluenty.ui.utils

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Suppress("ComposableNaming")
@Composable
fun <T> Flow<T>.collectInLaunchedEffectWithLifecycle(
    vararg keys: Any?,
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend CoroutineScope.(T) -> Unit
) {
    val flow = this
    val currentCollector by rememberUpdatedState(collector)

    LaunchedEffect(flow, lifecycle, minActiveState, *keys) {
        withContext(Dispatchers.Main.immediate) {
            lifecycle.repeatOnLifecycle(minActiveState) {
                flow.collect { currentCollector(it) }
            }
        }
    }
}

@Composable
fun HomeItemShadow() {
    Box(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black
                    )
                )
            )
    )
}

@Composable
fun AssistantMessage(modifier: Modifier, message: String) {
    Column(
        modifier = modifier.animateContentSize()
    ) {
        Text(text = "Assistant", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .animateContentSize()
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topEnd = 10.dp,
                            bottomEnd = 10.dp,
                            bottomStart = 10.dp
                        )
                    )
                    .animateContentSize()
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(message.isEmpty(), label = "") {
                    if (it) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .padding(10.dp)
                                .width(50.dp)
                        )
                    } else {
                        Text(
                            modifier = Modifier.padding(10.dp),
                            text = message, fontSize = 16.sp, color = Color.White
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    text: String,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onLeadingIconClick: () -> Unit = {},
    onTrailingIconClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.DarkGray.copy(0.2f))
            .padding(vertical = 5.dp, horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(leadingIcon!=null) {
            IconButton(
                onClick = onLeadingIconClick
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        } else {
            Spacer(Modifier.width(20.dp))
        }

        Text(
            modifier = Modifier
                .weight(1f),
            text = text,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        trailingIcon?.let {
            IconButton(
                onClick = onTrailingIconClick
            ) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}