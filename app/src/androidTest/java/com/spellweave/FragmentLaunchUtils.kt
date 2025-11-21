package com.spellweave

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario

fun <T : Fragment> launchFragmentInTestActivity(
    fragment: T,
    args: Bundle? = null
): ActivityScenario<TestFragmentActivity> {

    val scenario = ActivityScenario.launch(TestFragmentActivity::class.java)

    scenario.onActivity { activity ->
        fragment.arguments = args
        activity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitNow()
    }

    return scenario
}
