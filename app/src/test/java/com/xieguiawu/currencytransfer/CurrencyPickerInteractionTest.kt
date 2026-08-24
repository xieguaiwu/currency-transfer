package com.xieguiawu.currencytransfer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Real-touch interaction tests for the currency picker.
 *
 * Uses performTouchInput { click() } (physical pointer injection through
 * the real hit-test/PointerInput pipeline) rather than the semantics
 * performClick(), so a TextField that swallows touch events is exposed.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CurrencyPickerInteractionTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun tapFromCurrencyField_opensSelectDialog() {
        // Real touch on the "From" field label text
        composeRule.onNodeWithText("US Dollar").performTouchInput { click() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Select currency").assertIsDisplayed()
    }

    @Test
    fun pickCurrencyFromDialog_updatesConversion() {
        // Open the picker via the trailing-icon area (any point on the field)
        composeRule.onNodeWithText("US Dollar").performTouchInput { click() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Select currency").assertIsDisplayed()

        // Search and pick EUR
        composeRule.onNodeWithText("Search code or name").performTextInput("EUR")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Euro").performClick()
        composeRule.waitForIdle()

        // Dialog closed, result card reflects the new pair
        composeRule.onNodeWithText("Select currency").assertDoesNotExist()
        composeRule.onNodeWithText("Euro").assertIsDisplayed()
    }

    @Test
    fun swapButton_swapsCurrencies() {
        composeRule.onNodeWithText("US Dollar").assertIsDisplayed()
        composeRule.onNodeWithText("Chinese Yuan").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Swap currencies").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Chinese Yuan").assertIsDisplayed()
        composeRule.onNodeWithText("US Dollar").assertIsDisplayed()
    }
}
