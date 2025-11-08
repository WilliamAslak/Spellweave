package com.spellweave

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.spellweave.R
import org.robolectric.Robolectric
import org.robolectric.Shadows

inline fun <reified T : Fragment> launchWithNav(
    crossinline fragmentFactory: () -> T,
    startDestId: Int,
    args: Bundle? = null
): Pair<T, TestNavHostController> {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val navController = TestNavHostController(context).apply {
        setGraph(R.navigation.mobile_navigation)
        if (args != null) setCurrentDestination(startDestId, args) else setCurrentDestination(startDestId)
    }

    val scenario = launchFragmentInContainer(themeResId = R.style.Theme_Spellweave) {
        fragmentFactory().apply { if (args != null) arguments = args }
    }

    lateinit var fragment: T
    scenario.onFragment { f ->
        fragment = f
        Navigation.setViewNavController(f.requireView(), navController)
    }
    return fragment to navController
}

/** Force a RecyclerView to layout so ViewHolders can be found by position in tests. */
fun androidx.recyclerview.widget.RecyclerView.ensureLaidOut(
    width: Int = 1080,
    height: Int = 1920
) {
    if (measuredWidth == 0 || measuredHeight == 0) {
        measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST)
        )
        layout(0, 0, width, height)
    }
}
fun pump() {
    // Run immediate tasks on the main (UI) looper
    Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

    // Flush Robolectric schedulers (foreground/background)
    Robolectric.flushForegroundThreadScheduler()
    Robolectric.flushBackgroundThreadScheduler()

    // Run any delayed tasks that became ready after flushing
    Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
}