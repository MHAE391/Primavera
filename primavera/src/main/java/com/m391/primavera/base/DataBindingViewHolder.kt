package com.m391.primavera.base

import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.m391.primavera.ServerMessageModel
import kotlinx.coroutines.runBlocking


class DataBindingViewHolder<T>(private val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: T) {
        when (item) {
            is ServerMessageModel -> binding.setVariable(BR.voiceMessage, item)
        }

        binding.executePendingBindings()
    }
}