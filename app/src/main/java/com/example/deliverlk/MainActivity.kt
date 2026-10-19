package com.example.deliverlk

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.deliverlk.di.AppContainer

class MainActivity : AppCompatActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = (application as DeliverLkApplication).appContainer
        enableEdgeToEdge()
        setContent {
            DeliverLkApp(appContainer = appContainer)
        }
    }
}