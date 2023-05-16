package com.m391.primavera

import com.m391.primavera.base.BaseRecyclerViewAdapter

class ChatAdapter(callback: (teacher: ServerMessageModel) -> Unit) :
    BaseRecyclerViewAdapter<ServerMessageModel>(callback) {
    override fun getLayoutRes(viewType: Int) = R.layout.voice_message_model
}