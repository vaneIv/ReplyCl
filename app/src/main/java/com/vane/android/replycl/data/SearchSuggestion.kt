package com.vane.android.replycl.data

import androidx.annotation.DrawableRes

data class SearchSuggestion(
    @DrawableRes val iconResId: Int,
    val title: String,
    val subtitle: String
)
