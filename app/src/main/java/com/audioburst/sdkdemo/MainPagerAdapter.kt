package com.audioburst.sdkdemo

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.audioburst.sdkdemo.custom_views.CustomParamsView
import com.audioburst.sdkdemo.custom_views.SdkKeysView

class MainPagerAdapter(context: Context) : PagerAdapter() {

    private val sdkKeysView: SdkKeysView = SdkKeysView(context)
    private val customParamsView: CustomParamsView = CustomParamsView(context)

    private val views by lazy {
        listOf(sdkKeysView, customParamsView)
    }

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun getCount() = views.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return views[position].also(container::addView)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    }

    fun render(configuration: SdkKeysView.Configuration) {
        sdkKeysView.render(configuration)
    }

    fun render(configuration: CustomParamsView.Configuration) {
        customParamsView.render(configuration)
    }
}
