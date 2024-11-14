package com.sagar.fluenty.ui.screen.audio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AudioRecordScreen(
    viewModel: AudioRecordScreenViewModel = viewModel(
        factory = AudioRecordScreenViewModel.getFactory(LocalContext.current.applicationContext)
    ),
    onBack: () -> Unit
) {
    val state = viewModel.screenState
    val conversationList = viewModel.conversationList.reversed()

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
                        UserMessage(
                            modifier = Modifier.animateItem(),
                            message = it.message,
                            isResponseError = it.isError,
                            isEditingEnabled = it.isEditingEnabled,
                            onEditClick = {

                            },
                            onRetryClick = {

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
        }
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
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = message, fontSize = 16.sp, color = Color.White
                )
            }
        }
    }
}

@Composable
private fun UserMessage(
    modifier: Modifier,
    message: String,
    isResponseError: Boolean,
    isEditingEnabled: Boolean,
    onEditClick: () -> Unit,
    onRetryClick: () -> Unit
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
            AnimatedVisibility(isEditingEnabled) {
                Icon(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .clickable { onEditClick() },
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color.White
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
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = message, fontSize = 16.sp, color = Color.White
                )
            }
        }
    }
}
