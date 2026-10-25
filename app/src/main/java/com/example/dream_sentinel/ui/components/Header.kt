package com.example.dream_sentinel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dream_sentinel.R
import com.example.dream_sentinel.ui.theme.DreamSentinelTheme
import com.example.dream_sentinel.ui.theme.MainColor
import com.example.dream_sentinel.ui.theme.MainColorWhite
import com.example.dream_sentinel.ui.theme.SideColorWhite

@Composable
fun Header(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    brandName: String = "Dream Sentinel",
    showSettingsIcon: Boolean = true,
    onSettingsClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_moon),
                    contentDescription = null,
                    tint = MainColor,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = brandName,
                    color = MainColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (showSettingsIcon) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = MainColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            color = MainColorWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 38.sp
        )

        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                color = SideColorWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F19)
@Composable
private fun HeaderPrepareMixPreview() {
    DreamSentinelTheme {
        Column(
            modifier = Modifier
                .background(Color(0xFF0D0F19))
                .padding(20.dp)
        ) {
            Header(
                title = "Prepare your mix",
                subtitle = "A softer soundtrack for switching off."
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F19)
@Composable
private fun HeaderScheduleSleepPreview() {
    DreamSentinelTheme {
        Column(
            modifier = Modifier
                .background(Color(0xFF0D0F19))
                .padding(20.dp)
        ) {
            Header(
                title = "Schedule your sleep",
                subtitle = "Choose when your next day begins."
            )
        }
    }
}
