package com.rhuarhri.wellbeing.screen_drawing_widget

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class DrawWidget {

    @Composable
    fun draw(currentCircle: Circle, circleList: List<Circle>, circleColor : Color, backgroundColor : Color) {

        //val color : Color =
        val radius = currentCircle.radius
        val location = currentCircle.location

        //val circleList = circleList

        /*var backgroundColor = Color(red = 0xcb, green = 0xe1, blue = 0xef, alpha = 0xff)
        if (darkMode == true) {
            backgroundColor = Color(red = 0x95, green = 0x00, blue = 0x00, alpha = 0xff)
        }*/

        val maxHeight = LocalConfiguration.current.screenHeightDp
        val maxWidth = LocalConfiguration.current.screenWidthDp

        Canvas(modifier = Modifier.fillMaxSize().background(backgroundColor),) {

            drawCircle(color = circleColor, center = location, radius = radius)

            if (circleList.isNotEmpty()) {
                for (circle in circleList) {
                    var color = Color(red = 0x00, green = 0x00, blue = 0x00, circle.opacity)
                    color = Color(red = circleColor.red, green = circleColor.green,
                        blue = circleColor.blue, alpha = color.alpha)

                    drawCircle(
                        color = color,//Circle.getColor(circle.opacity, darkMode),
                        center = circle.location,
                        radius = circle.radius
                    )
                }
            }
        }
    }
}

data class Circle(var location: Offset = Offset(0f, 0f),
                  var radius : Float = 0f,
                  var opacity : Int = 0x00) {

    companion object {
        val startRadius : Float = 20f
        fun getColor(opacity: Int, darkMode : Boolean) : Color {
            //dark mode color
            //return Color(red= 0x5e, green = 0xa9, blue = 0xbe, alpha = opacity)

            return if (darkMode == true) {
                Color(red= 0xD5, green = 0x6B, blue = 0x00, alpha = opacity)
            } else {
                Color(red= 0x5e, green = 0xa9, blue = 0xbe, alpha = opacity)
            }
        }
    }

    //var color = Color(red= 0xff, green = 0xff, blue = 0xff, alpha = opacity)

}

class DrawWidgetState(val currentCircle: Circle, val circleList: List<Circle>) {
    /*
    pro tip the state class should not be a data class as android compose will use the classes
    is equal to method. This is problematic as if the data in the class changes android compose
    will not be notified of the change as the class itself has not changed.
    solution 1: over ride the existing is equal to method
    solution 2: don't use a data class to store state
    */
}

class DrawWidgetViewModel : ViewModel() {

    var state by mutableStateOf<DrawWidgetState>(DrawWidgetState(Circle(), listOf()))

    fun moveCircle(x: Float, y: Float) {
        val newCircle : Circle = Circle()
        newCircle.location = Offset(x, y)
        newCircle.opacity = 0xff
        //newCircle.color = Circle.getDefaultColor(0xff)
        newCircle.radius = 20f

        val newState = DrawWidgetState(newCircle, state.circleList)
        state = newState
    }

    private var timer : Timer = Timer()

    private fun addToList() {
        val existingCircle = state.currentCircle
        val existingCircleList = mutableListOf<Circle>()
        for (circle in state.circleList) {
            existingCircleList.add(circle)
        }

        if (existingCircleList.size < 20) {
            if (existingCircleList.isNotEmpty()) {
                val x = existingCircle.location.x
                val y = existingCircle.location.y

                if (x > (existingCircleList.last().location.x + 50) || y > (existingCircleList.last().location.y + 50)) {
                    existingCircleList.add(existingCircle)
                }
            } else {
                existingCircleList.add(existingCircle)
            }
        }

        val newCircleList = mutableListOf<Circle>()

        //the app will have a trailing list of circles which will slowly faded out
        if (existingCircleList.isNotEmpty()) {

            for (i in 0 until existingCircleList.size) {
                val circle = existingCircleList.get(i)

                if (circle.radius >= (Circle.startRadius * 28)) {

                } else {
                    circle.radius = circle.radius + (Circle.startRadius / 2)
                    val opacity = circle.opacity - 4
                    circle.opacity = opacity
                    //circle.color = Circle.getDefaultColor(opacity)
                    newCircleList.add(circle)
                }

            }

            val newCircle : Circle = Circle()
            newCircle.location = Offset(existingCircle.location.x, existingCircle.location.y)
            newCircle.opacity = 0x00
            //newCircle.color = Circle.getDefaultColor(0xff)
            newCircle.radius = 0f

            val newState = DrawWidgetState(newCircle, newCircleList)

            state = newState
        }
    }

    fun startTimer() {
        val delay : Long = 0
        val period : Long = 100

        val action = {
            addToList()
        }

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                action.invoke()
            }

        }, delay, period)
    }

    private fun endTimer() {
        timer.cancel()
    }

    override fun onCleared() {
        endTimer()
        super.onCleared()
    }
}