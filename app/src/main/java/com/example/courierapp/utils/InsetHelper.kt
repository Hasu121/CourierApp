package com.example.courierapp.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

object InsetHelper {

    fun applySystemBarPadding(
        view: View,
        applyTop: Boolean = true,
        applyBottom: Boolean = true,
        applyLeft: Boolean = false,
        applyRight: Boolean = false
    ) {
        val initialTop = view.paddingTop
        val initialBottom = view.paddingBottom
        val initialLeft = view.paddingLeft
        val initialRight = view.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                left = initialLeft + if (applyLeft) bars.left else 0,
                top = initialTop + if (applyTop) bars.top else 0,
                right = initialRight + if (applyRight) bars.right else 0,
                bottom = initialBottom + if (applyBottom) bars.bottom else 0
            )

            insets
        }
    }
}