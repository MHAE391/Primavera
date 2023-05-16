package com.m391.primavera.user.father.home.switjha

import com.m391.primavera.R
import com.m391.primavera.utils.BaseRecyclerViewAdapter
import com.m391.primavera.utils.models.ServerChildModel
import com.m391.primavera.utils.models.ServerConversationModel

class ChildrenAdapter(callback: (serverChild: ServerChildModel) -> Unit) :
    BaseRecyclerViewAdapter<ServerChildModel>(callback) {
    override fun getLayoutRes(viewType: Int) = R.layout.child_card
}