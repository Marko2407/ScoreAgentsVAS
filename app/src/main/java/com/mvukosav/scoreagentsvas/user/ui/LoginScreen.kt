package com.mvukosav.scoreagentsvas.user.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mvukosav.scoreagentsvas.R
import com.mvukosav.scoreagentsvas.user.presentation.login.LoginScreenViewModel

@Composable
fun LoginScreen(viewModel: LoginScreenViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
        var textPasswordValue by remember { mutableStateOf(TextFieldValue("")) }

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(300.dp)
        )
        ScoreInputText(InputTextContent(textFieldValue, "Username", onValueChange = {
            textFieldValue = it
        }))
        Spacer(modifier = Modifier.height(10.dp))
        ScoreInputText(InputTextContent(textPasswordValue, "Password", onValueChange = {
            textPasswordValue = it
        }))

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.login(textFieldValue.text, textPasswordValue.text) },
            shape = RectangleShape, modifier = Modifier.width(140.dp)
        ) {
            Text(text = "Login")
        }
    }
}