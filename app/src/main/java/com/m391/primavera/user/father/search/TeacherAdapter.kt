package com.m391.primavera.user.father.search

import com.m391.primavera.R
import com.m391.primavera.utils.BaseRecyclerViewAdapter
import com.m391.primavera.utils.models.ServerTeacherModel

class TeacherAdapter(callback: (teacher: ServerTeacherModel) -> Unit) :
    BaseRecyclerViewAdapter<ServerTeacherModel>(callback) {
    override fun getLayoutRes(viewType: Int) = R.layout.teacher_search_card
}