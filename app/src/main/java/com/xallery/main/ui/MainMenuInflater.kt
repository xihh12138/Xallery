package com.xallery.main.ui

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.xihh.xallery.R

class MainMenuInflater(onClick: (itemId: Int) -> Unit) {

    fun inflate(toolbar: Toolbar) {
        toolbar.menu.clear()
        toolbar.menu.add(GROUP_ID_SEARCH, ITEM_ID_SEARCH, ORDER_SEARCH, R.string.menu_search)
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            .setIcon(com.xallery.common.R.drawable.ic_search)

        toolbar.menu.addSubMenu(GROUP_ID_MORE, ITEM_ID_FILTER, ORDER_FILTER, R.string.menu_filter)
            .setIcon(com.xallery.common.R.drawable.ic_write_hover)

        toolbar.menu.addSubMenu(GROUP_ID_MORE, ITEM_ID_SELECT, ORDER_SELECT, R.string.menu_select)
            .setIcon(com.xallery.common.R.drawable.ic_arrow_down_black)
    }

    companion object {
        const val GROUP_ID_SEARCH = 0
        const val GROUP_ID_MORE = 1

        const val ITEM_ID_SEARCH = 0
        const val ITEM_ID_FILTER = 2
        const val ITEM_ID_SELECT = 3

        const val ORDER_SEARCH = 0
        const val ORDER_FILTER = 2
        const val ORDER_SELECT = 3
    }
}