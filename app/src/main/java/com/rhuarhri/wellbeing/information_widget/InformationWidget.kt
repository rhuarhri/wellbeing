package com.rhuarhri.wellbeing.information_widget


import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.rhuarhri.wellbeing.screen_drawing_widget.Circle
import androidx.core.content.ContextCompat.startActivity

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.math.log


class InformationWidget {

    @ExperimentalAnimationApi
    @Composable
    fun widget(state : InformationWidgetState, showPopup: (information : Information) -> Unit, popupAccept: () -> Unit, popupDismiss: () -> Unit) {

        //var darkMode = LocalConfiguration.current.uiMode == Configuration.UI_MODE_NIGHT_YES

        val backgroundColor = MaterialTheme.colors.primaryVariant

        AnimatedVisibility(visible = state.showing,) {
            Box(modifier = Modifier.fillMaxSize(), Alignment.TopCenter) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(backgroundColor)
                        .clip(RoundedCornerShape(15.dp)),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Card(
                        Modifier
                            .height(200.dp)
                            .weight(1f)
                            .padding(15.dp)
                            .clickable {
                                showPopup.invoke(state.option1)
                            }
                    ) {
                        Text(state.option1.title)
                    }
                    Card(
                        Modifier
                            .height(200.dp)
                            .weight(1f)
                            .padding(15.dp)
                            .clickable {
                                showPopup.invoke(state.option2)
                            }
                    ) {
                        Text(state.option2.title)
                    }
                }
            }

            showAlert(state.showPopup, state.selectedInformation, popupAccept, popupDismiss)
        }

    }

    @Composable
    private fun showAlert(showing: Boolean, information: Information, accept: () -> Unit, dismiss: () -> Unit) {

        if (showing == true) {
            AlertDialog(
                onDismissRequest = {dismiss.invoke()},
                title = {
                    Text(text = information.title)
                },
                text = {
                    Text(text = information.description, maxLines = 5)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            accept.invoke()
                        }) {
                        Text("Accept")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            dismiss.invoke()
                        }) {
                        Text("dismiss")
                    }
                }
            )
        }
    }
}

class InformationWidgetState(var option1 : Information, var option2 : Information) {
    var showing = false
    var showPopup = false
    var selectedInformation = Information("", "", "", InformationType.ANXIETY)
}

class InformationWidgetViewModel : ViewModel() {

    var state by mutableStateOf(InformationWidgetState(
        Information("", "", "", InformationType.HELPFUL),
        Information("", "", "", InformationType.HELPFUL),
    ))

    var repo : InformationWidgetRepository = InformationWidgetRepository()
    var logic : InformationWidgetLogic = InformationWidgetLogic(repo)

    private var informationList = mutableListOf<Information>()

    fun setup() {
        informationList.addAll(logic.get())
        if (informationList.size > 1) {
            state.option1 = informationList.first()
            state.option2 = informationList[1]
        }

        startTimer()
    }

    fun viewSelectedInformation(context: Context) {
        val url = state.selectedInformation.url
        if (url.isNotBlank()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(context, intent, null)
        }
    }

    fun viewInformation(information: Information) {
        state.selectedInformation = information
        state.showPopup = true
    }

    fun hidePopup() {
        state.showPopup = false
    }

    private fun show() {
        if (informationList.size > 1) {
            state.option1 = informationList.first()
            state.option2 = informationList[1]
        }
        state.showing = true
    }

    private fun hide() {
        state.showing = false

        //removing the options displayed to the user
        informationList.removeAt(0)
        informationList.removeAt(0)

        //adding updated versions of the options
        var updatedInformation : Information = state.option1
        updatedInformation.viewed += 1
        informationList.add(updatedInformation)

        updatedInformation = state.option2
        updatedInformation.viewed += 1
        informationList.add(updatedInformation)

        //update entire list
        logic.update(informationList)

        informationList.clear()
        informationList.addAll(logic.get())
    }

    private var timer : Timer = Timer()


    private fun startTimer() {
        val delay : Long = 30000
        val period : Long = 30000

        val action = {
            if (state.showing == false) {
                show()
            } else {
                hide()
            }
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

class InformationWidgetLogic(private var repo : InformationWidgetRepository) {
    //this is the business logic not the UI login which is in the view model

    private val informationList : MutableList<Information> = mutableListOf<Information>()

    data class History(var type: InformationType) {
        var amount : Int = 0
    }
    private val historyList : MutableList<History> = mutableListOf<History>()

    fun get() : List<Information> {

        if (informationList.isEmpty() == true) {
            val foundList = repo.getAll()
            for(information in foundList) {
                informationList.add(information)
            }
        }

        if (historyList.isEmpty() == true) {
            return informationList
        }

        historyList.sortBy { it.amount }

        val orderedList = mutableListOf<Information>()

        for (history in historyList) {
            val informationByType = mutableListOf<Information>()
            for (information in informationList) {
                if (information.type == history.type) {
                    informationByType.add(information)
                }
            }
            informationByType.sortBy { it.viewed }

            orderedList.addAll(informationByType)
        }

        return orderedList
    }

    fun update(newInformationList : List<Information>) {
        informationList.clear()
        historyList.clear()

        informationList.addAll(newInformationList)

        for (information in informationList) {
            updateHistory(information.viewed, information.type)
        }
    }

    private fun updateHistory(amount: Int, type : InformationType) {
        var typeExists = false
        for (history in historyList) {
            if (history.type == type) {
                history.amount += amount
                typeExists = true
            }
        }

        if (typeExists == false) {
            val newHistory = History(type)
            newHistory.amount = amount
            historyList.add(newHistory)
        }
    }



}

class InformationWidgetRepository {

    fun getAll() : List<Information> {
        return DummyData().get()
    }
}

enum class InformationType {
    DEPRESSION,
    ANXIETY,
    STRESS,
    PANIC_ATTACK,
    MINDFULNESS,

    /*Helpful information is anything about mental health in general */
    HELPFUL
}

class Information(val url : String, val title : String, val description : String, val type : InformationType) {
    /*viewed means the user has been given the option to view this information*/
    var viewed: Int = 0
}

class DummyData {
    fun get() : MutableList<Information> {
        //this would come from an online data base which might be added in the future
        var informationList = mutableListOf<Information>()

        informationList.addAll(getDepressionInformation())
        informationList.addAll(getAnxietyInformation())
        informationList.addAll(getStressInformation())
        informationList.addAll(getPanicAttachInformation())
        informationList.addAll(getMindfulnessInformation())
        informationList.addAll(getHelpfulInformation())

        return informationList
    }

    private fun getDepressionInformation() : MutableList<Information> {
        val informationList = mutableListOf<Information>()

        var url = "https://www.nhs.uk/mental-health/conditions/clinical-depression/overview/"
        var title = "What is depression?"
        var description = "Depression is more than just feeling sad it is a persistent feeling."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.DEPRESSION))

        url = "https://www.nhs.uk/mental-health/conditions/clinical-depression/causes/"
        title = "What causes depression?"
        description = "There are a number of causes of depression."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.DEPRESSION))

        url = "https://www.nhs.uk/mental-health/conditions/clinical-depression/living-with/"
        title = "Things you can do for depression"
        description = "There are some simple things you can do deal with depression."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.DEPRESSION))

        return informationList
    }

    private fun getAnxietyInformation() : MutableList<Information> {
        val informationList = mutableListOf<Information>()

        var url = "https://www.nhs.uk/mental-health/conditions/generalised-anxiety-disorder/overview/"
        var title = "What is anxiety?"
        var description = "Anxiety is a feeling of unease that can become severe."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.ANXIETY))

        url = "https://www.nhs.uk/mental-health/conditions/generalised-anxiety-disorder/self-help/"
        title = "Who to deal with anxiety?"
        description = "If you have some form of anxiety there are some things you can try to reduce your anxiety."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.ANXIETY))

        return informationList
    }

    private fun getStressInformation() : MutableList<Information> {
        val informationList = mutableListOf<Information>()

        var url = "https://www.nhs.uk/every-mind-matters/mental-health-issues/stress/"
        var title = "What is stress?"
        var description = "What is stress, the signs of stress and how to deal with stress."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.STRESS))

        url = "https://www.nhs.uk/mental-health/self-help/guides-tools-and-activities/tips-to-reduce-stress/"
        title = "Ways to prevent stress"
        description = "Stress can be a serious problem if not managed, but stress can be managed."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.STRESS))

        return informationList
    }

    private fun getPanicAttachInformation() : MutableList<Information> {
        val informationList = mutableListOf<Information>()

        val url = "https://www.nhsinform.scot/healthy-living/mental-wellbeing/anxiety-and-panic/how-to-deal-with-panic-attacks"
        val title = "What are panic attacks?"
        val description = "What are panic attacks and how to deal with them."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.PANIC_ATTACK))

        return informationList
    }

    private fun getMindfulnessInformation() : MutableList<Information> {
        val informationList = mutableListOf<Information>()

        var url = "https://www.mindful.org/meditation/mindfulness-getting-started/"
        var title = "What is mindfulness?"
        var description = "Mindfulness is the ability to be aware of where you are and what you are doing and ignore what is going on around you."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.MINDFULNESS))

        url = "https://www.mind.org.uk/information-support/drugs-and-treatments/mindfulness/mindfulness-exercises-tips/"
        title = "Mindfulness exercises"
        description = "Practicing mindfulness can help improve your overall mental health."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.MINDFULNESS))

        url = "https://www.youtube.com/watch?v=ZToicYcHIOU"
        title = "Guided mindfulness session"
        description = "10 minute guided mindfulness session."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.MINDFULNESS))

        return informationList
    }

    private fun getHelpfulInformation() : MutableList<Information> {
        val informationList = mutableListOf<Information>()

        var url = "https://www.youtube.com/watch?v=zt4sOjWwV3M"
        var title = "Common warning signs"
        var description = "Identifying a mental illness can be difficult, however there are some common warning signs that could suggest a mental illness."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.HELPFUL))

        url = "https://www.mind.org.uk/information-support/types-of-mental-health-problems/mental-health-problems-introduction/treatment-options/"
        title = "How to treat a mental health condition"
        description = "There are a verity of mental health problems that someone can be effect by. Each will have it's own form of treatment."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.HELPFUL))

        url = "https://www.mind.org.uk/information-support/types-of-mental-health-problems/mental-health-problems-introduction/types-of-mental-health-problems/"
        title = "Types of mental health problems"
        description = "These are some of the most common forms of mental health problems."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.HELPFUL))

        url = "https://www.nhs.uk/mental-health/self-help/guides-tools-and-activities/five-steps-to-mental-wellbeing/"
        title = "Ways to improve your mental health"
        description = "A good diet and exercise helps improve your physical health. Following these steps can help improve your mental heath."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.HELPFUL))

        url = "https://www.nhs.uk/every-mind-matters/mental-wellbeing-tips/your-mind-plan-quiz/"
        title = "Tips and advice"
        description = "Create a mind plan to get tips and advice that work for you."

        informationList.add(Information(url = url, title = title, description = description, type = InformationType.HELPFUL))

        return informationList
    }
}