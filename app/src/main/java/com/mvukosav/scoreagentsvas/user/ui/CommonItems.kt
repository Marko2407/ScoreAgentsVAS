package com.mvukosav.scoreagentsvas.user.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun ScoreInputText(state: InputTextContent) {
    OutlinedTextField(
        value = state.value, onValueChange =  state.onValueChange ,
        label = { Text(text = state.label, color = Color.Black) },
        shape = RectangleShape,
    )
}

data class InputTextContent(
    var value: TextFieldValue,
    val label: String,
    val onValueChange: (value: TextFieldValue) -> Unit
)