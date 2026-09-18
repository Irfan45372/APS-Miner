package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MiningScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MiningViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

class MainActivity : ComponentActivity() {
    private val viewModel: MiningViewModel by viewModels()
    private lateinit var activityResultSender: ActivityResultSender

    override fun onCreate(savedInstanceState: Bundle?) {
        activityResultSender = ActivityResultSender(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MiningScreen(
                    viewModel = viewModel,
                    activity = this,
                    activityResultSender = activityResultSender
                )
            }
        }
    }
}
