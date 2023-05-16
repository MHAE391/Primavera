package com.m391.primavera.user.teacher.home

import com.m391.primavera.R
import com.m391.primavera.utils.BaseRecyclerViewAdapter
import com.m391.primavera.utils.models.ServerConversationModel

class ConversationsAdapter(callback: (conversation: ServerConversationModel) -> Unit) :
    BaseRecyclerViewAdapter<ServerConversationModel>(callback) {
    override fun getLayoutRes(viewType: Int) = R.layout.conversation_card_model
}