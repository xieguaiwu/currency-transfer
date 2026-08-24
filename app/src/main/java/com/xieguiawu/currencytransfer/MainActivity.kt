package com.xieguiawu.currencytransfer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.xieguiawu.currencytransfer.ui.MainScreen
import com.xieguiawu.currencytransfer.ui.theme.CurrencyTransferTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CurrencyTransferTheme {
                MainScreen()
            }
        }
    }
}
