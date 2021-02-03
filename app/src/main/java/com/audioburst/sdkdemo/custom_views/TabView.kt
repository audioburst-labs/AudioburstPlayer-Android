package com.audioburst.sdkdemo.custom_views

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.tabs.TabLayout

class TabView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TabLayout(context, attrs, defStyleAttr) {

    fun render(configuration: Configuration) {
        when {
            areTabsTheSame(configuration.tabTitles) -> getTabAt(configuration.selectedIndex)?.let(this::selectTab)
            else -> addTabs(configuration)
        }
    }

    private fun currentTabsTitles(): List<String> {
        val tabTitles = mutableListOf<String?>()
        for (i in 0 until tabCount) {
            tabTitles.add(getTabAt(i)?.text?.toString())
        }
        return tabTitles.filterNotNull()
    }

    private fun areTabsTheSame(tabItems: List<String>): Boolean =
        currentTabsTitles() == tabItems

    private fun addTabs(configuration: Configuration) {
        configuration.tabTitles.forEachIndexed { index, tabItem ->
            val tab = newTab().apply {
                text = tabItem
            }
            addTab(tab)
            if (index == configuration.selectedIndex) {
                selectTab(tab)
            }
        }
    }

    data class Configuration(
        val tabTitles: List<String> = emptyList(),
        val selectedIndex: Int = -1,
    )
}