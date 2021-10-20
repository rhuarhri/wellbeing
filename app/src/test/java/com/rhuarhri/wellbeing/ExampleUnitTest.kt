package com.rhuarhri.wellbeing

import android.content.Context
import com.rhuarhri.wellbeing.information_widget.Information
import com.rhuarhri.wellbeing.information_widget.InformationType
import com.rhuarhri.wellbeing.information_widget.InformationWidgetLogic
import com.rhuarhri.wellbeing.information_widget.InformationWidgetRepository
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.runners.MockitoJUnitRunner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


}

@RunWith(MockitoJUnitRunner::class)
class InformationWidgetLogicTests {

    /*
    How the information widget logic work
    1 each instance of the information class has a type provided by InformationType
    2 The information widget will display a list of the information class to the user
    3 if the user see an instance of the information class but does not interact with it
    then the information widget logic class will assume any information with the same type as the
    one being displayed does not interest the user. As a result the information widget logic class
    will priorise information with a different type
     */

    @Mock
    private lateinit var mockRepo : InformationWidgetRepository

    fun testData() : MutableList<Information> {
        val dummyData = mutableListOf<Information>()
        var testInformation = Information("url", "anxiety title", "anxiety description", InformationType.ANXIETY)
        dummyData.add(testInformation)
        testInformation = Information("url", "depression title", "depression description", InformationType.DEPRESSION)
        dummyData.add(testInformation)
        testInformation = Information("url", "stress title", "stress description", InformationType.STRESS)
        dummyData.add(testInformation)
        testInformation = Information("url", "panic attack title", "panic attack description", InformationType.PANIC_ATTACK)
        dummyData.add(testInformation)
        testInformation = Information("url", "mindfulness title", "mindfulness description", InformationType.MINDFULNESS)
        dummyData.add(testInformation)
        testInformation = Information("url", "helpful title", "helpful description", InformationType.HELPFUL)
        dummyData.add(testInformation)
        return dummyData
    }

    @Test
    fun unorderedList() {
        val testList = testData()

        `when`(mockRepo.getAll()).thenReturn(testList)

        val informationWidgetLogic = InformationWidgetLogic(mockRepo)

        val result = informationWidgetLogic.get()

        /*
        No history so the list should no change from what the repo provides
         */
        assertEquals(testList, result)
    }

    @Test
    fun orderedList() {
        val testList = testData()

        `when`(mockRepo.getAll()).thenReturn(testList)

        val informationWidgetLogic = InformationWidgetLogic(mockRepo)

        /*
        The user first gets an unordered list to look through
         */
        informationWidgetLogic.get()

        /*at this point the user will have seen all information that don't have the helpful type*/
        val updatedList = mutableListOf<Information>()
        for (information in testList) {

            if (information.type != InformationType.HELPFUL) {
                val current = information
                current.viewed += 1
                updatedList.add(current)
            } else {
                updatedList.add(information)
            }
        }

        informationWidgetLogic.update(updatedList)

        val orderedResult = informationWidgetLogic.get()

        val result = orderedResult.first().type

        /*
        the top result should have the type of helpful
        this is because the user will have seen and then decided to ignore
        all information that does not have the type helpful. This ensures
        that unseen information is prioritised
         */

        assertEquals(InformationType.HELPFUL, result)
    }

    @Test
    fun orderListByType() {
        /*
        An type that has a small amount of total views should have priority over a type
        that has a lot of views.
        This is because the app expects every view a type has was the point in which the user
        has been given the option to view a some information which they ignore
         */

        val testList = testData()

        `when`(mockRepo.getAll()).thenReturn(testList)

        val informationWidgetLogic = InformationWidgetLogic(mockRepo)

        /*
        The user first gets an unordered list to look through
         */
        informationWidgetLogic.get()

        /*
        The user views the information to different amounts
         */
        val updatedList = mutableListOf<Information>()
        for (information in testList) {

            val current = information
            when(information.type) {
                InformationType.HELPFUL -> {
                    current.viewed = 0
                }
                InformationType.MINDFULNESS -> {
                    current.viewed = 1
                }
                InformationType.PANIC_ATTACK -> {
                    current.viewed = 2
                }
                InformationType.STRESS -> {
                    current.viewed = 3
                }
                InformationType.DEPRESSION -> {
                    current.viewed = 4
                }
                InformationType.ANXIETY -> {
                    current.viewed = 5
                }
                else -> {

                }
            }

            updatedList.add(current)
        }

        //even with adding new information should not change the order of the information types
        updatedList.addAll(testData())

        informationWidgetLogic.update(updatedList)


        val orderList = informationWidgetLogic.get()

        val first = orderList.first().type

        val last = orderList.last().type

        assertEquals(InformationType.HELPFUL, first)

        assertEquals(InformationType.ANXIETY, last)
    }

    @Test
    fun orderListByViewAmount() {
        /*
        Any un viewed instance of information should have priority over viewed information of the same type
         */

        val testData = mutableListOf<Information>()
        var testInformation = Information("url", "1", "description", InformationType.ANXIETY)
        testData.add(testInformation)
        testInformation = Information("url", "2", "description", InformationType.ANXIETY)
        testData.add(testInformation)
        testInformation = Information("url", "3", "description", InformationType.ANXIETY)
        testData.add(testInformation)

        `when`(mockRepo.getAll()).thenReturn(testData)

        val informationWidgetLogic = InformationWidgetLogic(mockRepo)

        /*
        The user first gets an unordered list to look through
         */
        informationWidgetLogic.get()

        val updatedList = mutableListOf<Information>()
        for (information in testData) {
            when(information.title) {
                "1" -> {
                    information.viewed = 1
                }
                "2" -> {
                    information.viewed = 2
                }
                "3" -> {
                    information.viewed = 3
                }

            }
            updatedList.add(information)
        }

        informationWidgetLogic.update(updatedList)

        val orderedList = informationWidgetLogic.get()

        val first = orderedList.first().title
        val last = orderedList.last().title

        assertEquals("1", first)
        assertEquals("3", last)

    }
}
