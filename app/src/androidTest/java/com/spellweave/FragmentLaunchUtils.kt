package com.spellweave

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle

inline fun <reified F : Fragment> launchFragmentInTestContainer(
    fragmentArgs: android.os.Bundle? = null,
    initialState: Lifecycle.State = Lifecycle.State.RESUMED
): FragmentScenario<F> {
    return launchFragmentInContainer<F>(
        fragmentArgs = fragmentArgs,
        themeResId = R.style.Theme_Spellweave,
        initialState = initialState
    )
}
