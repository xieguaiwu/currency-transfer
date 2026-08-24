package com.xieguiawu.currencytransfer

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Diagnostic: dump the full semantics tree to see what actually renders. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UiTreeDiagnosticTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun dumpTree() {
        composeRule.waitForIdle()
        val tree = composeRule.onRoot().printToString()
        println("===SEMANTIC TREE START===")
        println(tree)
        println("===SEMANTIC TREE END===")
    }
}
