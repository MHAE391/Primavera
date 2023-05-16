package com.m391.primavera.chat.child

import com.google.firebase.auth.FirebaseAuth
import com.m391.primavera.R
import com.m391.primavera.utils.BaseRecyclerViewAdapter
import com.m391.primavera.utils.Constants.TEXT_MESSAGE
import com.m391.primavera.utils.Constants.VOICE_MESSAGE
import com.m391.primavera.utils.models.ServerMessageModel

class ChildChatAdapter(callback: (teacher: ServerMessageModel) -> Unit) :
    BaseRecyclerViewAdapter<ServerMessageModel>(callback) {
    override fun getLayoutRes(viewType: Int) =
        when (viewType) {
            1 -> R.layout.send_text_message_model
            2 -> R.layout.receive_text_message_model
            3 -> R.layout.send_audio_message_model
            4 -> R.layout.receive_audio_message_model
            5 -> R.layout.send_image_message_model
            else -> R.layout.receive_image_message_model
        }

    override fun getItemViewType(position: Int): Int {
        val message = _items[position]
        return if (message.senderUID == FirebaseAuth.getInstance().currentUser!!.uid) {
            when (message.messageType) {
                TEXT_MESSAGE -> 1
                VOICE_MESSAGE -> 3
                else -> 5
            }
        } else {
            when (message.messageType) {
                TEXT_MESSAGE -> 2
                VOICE_MESSAGE -> 4
                else -> 6
            }

        }
    }
}
