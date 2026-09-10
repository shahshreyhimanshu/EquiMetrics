package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.TradePulseApp
import com.example.ui.theme.DarkBg
import com.example.ui.theme.TradePulseTheme
import com.example.viewmodel.MarketViewModel

class MainActivity : ComponentActivity() {
  private val marketViewModel: MarketViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      TradePulseTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = DarkBg
        ) {
          TradePulseApp(viewModel = marketViewModel)
        }
      }
    }
  }
}

