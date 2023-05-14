package com.m391.primavera.utils

import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.m391.primavera.utils.models.BluetoothDeviceModel
import com.m391.primavera.utils.models.ServerMessageModel
import com.m391.primavera.utils.models.ServerTeacherModel


class DataBindingViewHolder<T>(private val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: T) {
        when (item) {
            is ServerTeacherModel -> binding.setVariable(BR.teacher, item)
            is ServerMessageModel -> binding.setVariable(BR.message, item)
        }
        binding.executePendingBindings()
    }
}