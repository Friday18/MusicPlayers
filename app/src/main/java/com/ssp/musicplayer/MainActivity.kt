package com.ssp.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.ssp.musicplayer.data.Track
import com.ssp.musicplayer.data.songs
import com.ssp.musicplayer.ui.theme.MusicPlayerTheme
import com.ssp.musicplayer.utils.MusicPlayerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val isPlaying = MutableStateFlow(false)
    private val maxDuration = MutableStateFlow(0f)
    private val currentDuration = MutableStateFlow(0f)
    private val currentTrack = MutableStateFlow(Track())

    private var service: MusicPlayerService? = null
    private var isBound = false


    val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as MusicPlayerService.MusicBinder).getService()
            binder.setMusicList(songs)
            lifecycleScope.launch {
                binder.isPlaying().collectLatest {
                    isPlaying.value = it
                }
            }
            lifecycleScope.launch {
                binder.maxDuration().collectLatest {
                    maxDuration.value = it
                }
            }
            lifecycleScope.launch {
                binder.currentDuration().collectLatest {
                    currentDuration.value = it
                }
            }

            lifecycleScope.launch {
                binder.getCurrentTrack().collectLatest {
                    currentTrack.value = it
                }
            }
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }

    }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicPlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = { Text("Music PLayer") },
                            actions = {
                                IconButton(onClick = {
                                    val intent =
                                        Intent(this@MainActivity, MusicPlayerService::class.java)
                                    startService(intent)
                                    bindService(intent, connection, BIND_AUTO_CREATE)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = ""
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val intent = Intent(
                                            this@MainActivity,
                                            MusicPlayerService::class.java
                                        )
                                        stopService(intent)
                                        unbindService(connection)

                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "")

                                }
                            }
                        )
                    }) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        val track by currentTrack.collectAsState()
                        val max by maxDuration.collectAsState()
                        val current by currentDuration.collectAsState()
                        val isPlaying by isPlaying.collectAsState()

                        Image(
                            painter = painterResource(track.image),
                            contentDescription = "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = track.name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = current.div(1000).toString()
                            )
                            Slider(
                                value = current, onValueChange = {},
                                valueRange = 0f..max
                            )
                            Text(
                                text = max.div(1000).toString()
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                service?.prev()
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.previous),
                                    contentDescription = ""
                                )
                            }
                            IconButton(onClick = {
                                service?.playPause()
                            }) {
                                Icon(
                                    painter = if (isPlaying) {
                                        painterResource(R.drawable.pause)
                                    } else painterResource(R.drawable.play),
                                    contentDescription = ""
                                )
                            }

                            IconButton(onClick = {
                                service?.next()
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.next),
                                    contentDescription = ""
                                )

                            }


                        }

                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MusicPlayerTheme {
        Greeting("Android")
    }
}