package com.m391.primavera

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.Authentication
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.user.father.child.addition.AddNewChildViewModel
import com.m391.primavera.user.father.child.profile.display.ChildProfileViewModel
import com.m391.primavera.user.father.child.profile.edit.EditChildProfileViewModel
import com.m391.primavera.user.father.conversations.FatherConversationsViewModel
import com.m391.primavera.user.father.home.FatherHomeViewModel
import com.m391.primavera.user.father.home.switjha.FatherSwitchViewModel
import com.m391.primavera.user.father.profile.FatherProfileViewModel
import com.m391.primavera.user.father.search.FatherTeacherSearchFragment
import com.m391.primavera.user.father.search.FatherTeacherSearchViewModel
import com.m391.primavera.user.father.teacher.TeacherProfileViewModel
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.Constants.TYPE
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import kotlin.properties.Delegates

class App : Application() {
    /*
        private val appModule = module {
            single {
                DataStoreManager.getInstance(androidContext())
            }

            single {
                ServerDatabase(androidContext(), get())
            }
            viewModel {
                EditChildProfileViewModel(get(), get(), get())

            }
            viewModel {
                AddNewChildViewModel(get(), get(), get())
            }
            viewModel {
                FatherSwitchViewModel(get(), get(), get())
            }
            viewModel {
                ChildProfileViewModel(get(), get(), get())
            }
            viewModel {
                FatherHomeViewModel(get(), get(), get())
            }
            viewModel {
                FatherProfileViewModel(get(), get(), get())
            }
            viewModel {
                FatherTeacherSearchViewModel(get(), get(), get())
            }
            viewModel {
                TeacherProfileViewModel(get(), get(), get())
            }
            viewModel {
                FatherConversationsViewModel(get(), get(), get())
            }
        }*/

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

    }

}