package com.m391.primavera.user.father.home.child

import com.m391.primavera.R
import com.m391.primavera.utils.BaseRecyclerViewAdapter
import com.m391.primavera.utils.models.HealthHistoryModel
import com.m391.primavera.utils.models.ServerChildModel

class HealthAdapter(callback: (health: HealthHistoryModel) -> Unit) :
    BaseRecyclerViewAdapter<HealthHistoryModel>(callback) {
    override fun getLayoutRes(viewType: Int) = R.layout.health_history_card
}