package com.sagar.fluenty.ui.screen.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sagar.fluenty.R
import com.sagar.fluenty.ui.utils.AppTopBar
import com.sagar.fluenty.ui.utils.HomeItemShadow

@Composable
@Preview
fun HomeScreen(
    onConversationClick: () -> Unit = {},
    onAudioClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {

    val context = LocalContext.current
    var isAudioPermissionGranted by rememberSaveable {
        mutableStateOf(
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        isAudioPermissionGranted = isGranted
    }
    if (isAudioPermissionGranted) {
        Scaffold(
            modifier = Modifier
                .background(Color.Black)
                .statusBarsPadding(),
            topBar = {
                AppTopBar(
                    text = "Fluenty",
                    trailingIcon = Icons.Default.Settings,
                    onTrailingIconClick = onSettingsClick
                )
            }
        ) { inner ->
            Column(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(inner)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                SectionItem(
                    painter = painterResource(R.drawable.english),
                    title = "English Practice"
                ) {
                    onConversationClick()
                }
                SectionItem(
                    painter = painterResource(R.drawable.pronunciation),
                    title = "Pronunciation Practice"
                ) {
                    onAudioClick()
                }
                Spacer(Modifier)
            }
        }

    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            ) {
                Text("Grant Audio Permission")
            }
        }
    }
}

@Composable
private fun ColumnScope.SectionItem(painter: Painter, title: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.DarkGray)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        HomeItemShadow()
        TitleText(title)
    }
}

@Composable
private fun BoxScope.TitleText(text: String) {
    Text(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(20.dp),
        text = text,
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
}