package com.sagar.fluenty.ui.screen.audio

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sagar.fluenty.R
import com.sagar.fluenty.ui.utils.collectInLaunchedEffectWithLifecycle
import java.io.File

@Composable
fun AudioRecordScreen(
    viewModel: AudioRecordScreenViewModel = viewModel(
        factory = AudioRecordScreenViewModel.getFactory(LocalContext.current.applicationContext)
    ),
    onBack: () -> Unit
) {
    val state = viewModel.screenState
    val conversationList = viewModel.conversationList.reversed()
    val context = LocalContext.current
    viewModel.messageChannelFlow.collectInLaunchedEffectWithLifecycle {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
    }

    val lazyListState = rememberLazyListState()
    LaunchedEffect(key1 = conversationList.size) {
        if (lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.index != 0) {
            lazyListState.animateScrollToItem(0)
        }
    }

    Scaffold(
        modifier = Modifier
            .background(Color.Black)
            .statusBarsPadding()
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(Color.Black),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Text(text = "Pronunciation Practice", color = Color.White)
            }
            Spacer(Modifier.height(20.dp))
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .background(Color.Black)
                    .padding(horizontal = 20.dp)
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true,
                verticalArrangement = Arrangement.Bottom
            ) {
                items(
                    items = conversationList,
                    key = {
                        it.id
                    }
                ) {
                    if (it.isUser) {
                        UserAudioFile(
                            modifier = Modifier.animateItem(),
                            audioFile = it.file,
                            isAudioPlaying = state is AudioRecordScreenState.PlayingRecording,
                            onStop = {
                                viewModel.onStopAudioPlaying()
                            },
                            onStartAudio = {
                                viewModel.startPlayingAudio(it)
                            },
                            isResponseError = it.isError,
                            onRetryClick = {
                                viewModel.resendPreviousMessage()
                            }
                        )
                    } else {
                        AssistantMessage(
                            modifier = Modifier.animateItem(),
                            message = it.message
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            AudioRecorder(
                isEnabled = !(
                        state is AudioRecordScreenState.PlayingRecording ||
                                state is AudioRecordScreenState.ProcessingRecording ||
                                state is AudioRecordScreenState.ListeningToResponse
                        ),
                onStart = {
                    viewModel.startRecording(context = context)
                },
                onStop = {
                    viewModel.stopRecording()
                },
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AudioRecorder(
    isEnabled: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val micSize by animateDpAsState(
        if (isPressed) {
            80.dp
        } else {
            50.dp
        },
        label = ""
    )
    LaunchedEffect(isPressed) {
        if (isPressed) {
            onStart()
        } else {
            onStop()
        }
    }
    val cancelDistance = remember { 300f }
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                }
                .size(micSize)
                .align(Alignment.Center)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = isEnabled
                ) { },
            painter = painterResource(R.drawable.mic),
            contentDescription = null,
            tint = Color.Unspecified
        )
    }
}


@Composable
private fun AssistantMessage(modifier: Modifier, message: String) {
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
private fun UserAudioFile(
    modifier: Modifier,
    audioFile: File?,
    isAudioPlaying: Boolean,
    isResponseError: Boolean,
    onStartAudio: (file: File?) -> Unit,
    onStop: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "You", color = Color.White, fontWeight = FontWeight.Bold)
            AnimatedVisibility(isResponseError) {
                Icon(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .clickable { onRetryClick() },
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.Red
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .animateContentSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 10.dp,
                            bottomEnd = 10.dp,
                            bottomStart = 10.dp
                        )
                    )
                    .animateContentSize()
                    .background(Color.DarkGray)
                    .clickable {
                        if (isAudioPlaying) {
                            onStop()
                        } else {
                            onStartAudio(audioFile)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAudioPlaying) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Color.White
                            )
                    )
                }
            }
        }
    }
}
