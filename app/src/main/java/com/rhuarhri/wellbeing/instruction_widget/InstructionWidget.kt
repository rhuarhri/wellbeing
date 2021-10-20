package com.rhuarhri.wellbeing.instruction_widget

import android.content.res.Configuration
import android.os.CountDownTimer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.rhuarhri.wellbeing.screen_drawing_widget.Circle

class InstructionWidget {

    @ExperimentalAnimationApi
    @Composable
    fun widget(visible : Boolean, expanded : Boolean, expand : () -> Unit, minimise: () -> Unit) {
        //var darkMode = LocalConfiguration.current.uiMode == Configuration.UI_MODE_NIGHT_YES

        val modifier: Modifier = Modifier.animateContentSize()

        if (expanded == true) {
            modifier.fillMaxSize()
        } else {
            modifier.fillMaxWidth()
                .height(80.dp)
        }

        AnimatedVisibility(visible = visible,) {
            Card(
                backgroundColor = MaterialTheme.colors.primaryVariant,
                border = BorderStroke(0.dp, Color.Transparent),
                modifier = modifier
            ) {
                Column(verticalArrangement = Arrangement.SpaceEvenly) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround, Alignment.Top) {
                        Text(
                            "Instruction",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(15.dp)
                        )

                        Button(modifier = Modifier
                            .padding(15.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                            onClick = {
                                if (expanded == true) {
                                    minimise.invoke()
                                } else {
                                    expand.invoke()
                                }
                            }) {
                            if (expanded == false) {
                                Text("Expand")
                            } else {
                                Text("Minimise")
                            }
                        }
                    }

                    if (expanded == true) {
                        Text(color = Color.White, modifier = Modifier.padding(15.dp),
                            text = "Move you finger across the screen. This will allow you to draw circles " +
                                    "on the screen which will slowly fade away."
                        )
                        Text(color = Color.White, modifier = Modifier.padding(15.dp),
                            text = "Every 30 seconds links to useful information will be displayed. If" +
                                    " you would like to see this information click on the link and " +
                                    "a website containing the information will appear."
                        )

                        Text(
                            "ABOUT",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(15.dp)
                        )
                        Text(color = Color.White, modifier = Modifier.padding(15.dp),
                            text = "Mental health is important. However there is no cure-all for mental health. " +
                                    "This app is designed to be a calming starting point, " +
                                    "by relaxing you and providing you with reliable information. " +
                                    "All to ensure that you can find a solution that works for you."
                        )

                        Text(
                            "DEVELOPED BY",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(15.dp)
                        )
                        Text(color = Color.White, modifier = Modifier.padding(15.dp), text = "Rhuarhri Cordon")
                    }
                }

            }
        }

    }
}

class InstructionWidgetViewModel : ViewModel() {

    var expanded by mutableStateOf(false)
    var visible by mutableStateOf(true)

    val timer = object: CountDownTimer(30000, 30000) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            visible = false
        }
    }

    fun startTimer() {
        timer.start()
    }

    fun expand() {
        expanded = true
    }

    fun minimise() {
        expanded = false
    }
}