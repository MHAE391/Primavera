package com.m391.primavera.user.teacher.search

import com.m391.primavera.R
import com.m391.primavera.utils.BaseRecyclerViewAdapter
import com.m391.primavera.utils.models.ServerChildModel

class ChildSearchAdapter(callback: (serverChild: ServerChildModel) -> Unit) :
    BaseRecyclerViewAdapter<ServerChildModel>(callback) {
    override fun getLayoutRes(viewType: Int) = R.layout.child_search_card
}