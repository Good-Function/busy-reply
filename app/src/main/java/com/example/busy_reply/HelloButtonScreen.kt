package com.example.busy_reply

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


@Composable
fun HelloButtonScreen() {
    var showHello by remember { mutableStateOf(false) }

    Column {
        Button(onClick = { showHello = true}) {
            Text("Click me")
        }
        if(showHello) {
            Text("Hello")
        }
    }
}