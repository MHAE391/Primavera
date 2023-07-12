package com.m391.primavera.utils

import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.utils.models.*
import kotlinx.coroutines.runBlocking


class DataBindingViewHolder<T>(private val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: T) {
        when (item) {
            is ServerTeacherModel -> binding.setVariable(BR.teacher, item)
            is ServerMessageModel -> binding.setVariable(BR.message, item)
            is ServerConversationModel -> binding.setVariable(BR.conversation, item)
            is ServerChildModel -> binding.setVariable(BR.serverChild, item)
            is HealthHistoryModel -> binding.setVariable(BR.health, item)
        }
        binding.executePendingBindings()
    }
}