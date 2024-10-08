package com.ssp.musicplayer.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ssp.musicplayer.R
import kotlin.math.roundToInt

@Preview
@Composable
fun MusicScreen() {
    Box(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Red,
                    Color.White
                )
            )
        )
    ) {
        MusicView()
    }
}

@Composable
fun MusicView() {

    Column(
        modifier = Modifier.padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        //image views
        Row(
            modifier = Modifier.weight(.8f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            MusicImages()
        }
        Text("Tuze mere jana kabhi nahi jana", color = Color.Gray)
        // songs views
        Row(modifier = Modifier.weight(.2f)) {
            SongsViews()
        }
        val onSliderValueChange by remember { mutableFloatStateOf(512F) }

        Slider(
            value = 100f,
            modifier = Modifier.fillMaxWidth(),
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(Color.Red),
            onValueChange = { onSliderValueChange.roundToInt().toFloat() }
        )
        //scroller

    }
}

@Composable
fun SongsViews() {

    Icon(
        painter = painterResource(R.drawable.previous),
        contentDescription = "",
        Modifier
            .clickable {

            }
    )

    Icon(
        painter = painterResource(R.drawable.play),
        contentDescription = "",
        Modifier.clickable {

        }
    )


    Icon(
        painter = painterResource(R.drawable.next),
        contentDescription = "",
        Modifier.clickable {

        }
    )


}

@Composable
fun MusicImages() {

    Image(
        painter = painterResource(R.drawable.ktm250),
        contentDescription = "",
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize(),
        alignment = Alignment.Center,
    )
}
