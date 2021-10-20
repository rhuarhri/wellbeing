package com.rhuarhri.wellbeing

import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.ViewModelProvider
import com.rhuarhri.wellbeing.information_widget.Information
import com.rhuarhri.wellbeing.information_widget.InformationType
import com.rhuarhri.wellbeing.information_widget.InformationWidget
import com.rhuarhri.wellbeing.information_widget.InformationWidgetViewModel
import com.rhuarhri.wellbeing.instruction_widget.InstructionWidget
import com.rhuarhri.wellbeing.instruction_widget.InstructionWidgetViewModel
import com.rhuarhri.wellbeing.screen_drawing_widget.Circle
import com.rhuarhri.wellbeing.screen_drawing_widget.DrawWidget
import com.rhuarhri.wellbeing.screen_drawing_widget.DrawWidgetViewModel
import com.rhuarhri.wellbeing.ui.theme.WellbeingTheme

class MainActivity : ComponentActivity() {

    lateinit var mainViewModel: MainScreenViewModel
    lateinit var drawViewModel : DrawWidgetViewModel
    lateinit var informationViewModel : InformationWidgetViewModel
    lateinit var instructionViewModel : InstructionWidgetViewModel

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        Why so many view models?
        The idea is that each widget has it's own logic which is kept separate in its own view model
         */

        mainViewModel = ViewModelProvider(this).get(MainScreenViewModel::class.java)
        mainViewModel.stepUpMusic(this)

        //this handles the logic of the draw widget
        drawViewModel = ViewModelProvider(this).get(DrawWidgetViewModel::class.java)
        drawViewModel.startTimer()

        informationViewModel = ViewModelProvider(this).get(InformationWidgetViewModel::class.java)
        informationViewModel.setup()

        instructionViewModel = ViewModelProvider(this).get(InstructionWidgetViewModel::class.java)
        instructionViewModel.startTimer()

        setContent {

            /*var darkMode = false //LocalConfiguration.current.uiMode == Configuration.UI_MODE_NIGHT_YES
            when (LocalConfiguration.current.uiMode) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    println("dark mode set")
                    darkMode = true
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    println("dark mode not set")
                    darkMode = false
                }
                Configuration.UI_MODE_NIGHT_MASK -> {
                    println("dark mode mask")
                    darkMode = false
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    println("dark mode undefined")
                    darkMode = false
                }
            }*/

            WellbeingTheme {
                window.statusBarColor = MaterialTheme.colors.primary.toArgb()

                val drawState = drawViewModel.state
                val informationState = informationViewModel.state

                val circleColor = MaterialTheme.colors.secondary
                val backgroundColor = MaterialTheme.colors.primary
                draw(drawState.currentCircle, drawState.circleList, circleColor, backgroundColor)

                InformationWidget().widget(
                    informationState,
                    {informationViewModel.viewInformation(information = it)},
                    {informationViewModel.viewSelectedInformation(this)},
                    {informationViewModel.hidePopup()}
                )

                Box(Modifier.fillMaxSize(), Alignment.BottomCenter) {
                    InstructionWidget().widget(
                        visible = instructionViewModel.visible,
                        expanded = instructionViewModel.expanded,
                        expand = {instructionViewModel.expand()},
                        minimise = {instructionViewModel.minimise()})
                }

            }
        }
    }

    override fun onPause() {
        mainViewModel.pauseBackgroundMusic()
        super.onPause()
    }

    override fun onResume() {
        mainViewModel.restartBackgroundMusic()
        super.onResume()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //super.onTouchEvent(event)
        if (event != null) {
            if (event.action == MotionEvent.ACTION_MOVE) {
                val x = event.x
                val y = event.y

                drawViewModel.moveCircle(x, y)

            }
        }
        return true
    }

    @Composable
    fun draw(currentCircle : Circle, circleList : List<Circle>, circleColor : Color, backgroundColor : Color) {

        DrawWidget().draw(currentCircle, circleList, circleColor, backgroundColor)
    }

}