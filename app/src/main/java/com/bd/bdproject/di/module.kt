package com.bd.bdproject.di

import com.bd.bdproject.data.AppDatabase
import com.bd.bdproject.data.repository.LightRepository
import com.bd.bdproject.data.repository.LightTagRelationRepository
import com.bd.bdproject.data.repository.TagRepository
import com.bd.bdproject.viewmodel.CalendarViewModel
import com.bd.bdproject.viewmodel.SplashViewModel
import com.bd.bdproject.viewmodel.StatisticDetailViewModel
import com.bd.bdproject.viewmodel.StatisticViewModel
import com.bd.bdproject.viewmodel.common.LightTagRelationViewModel
import com.bd.bdproject.viewmodel.common.LightViewModel
import com.bd.bdproject.viewmodel.common.TagViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getInstance(androidApplication()) }
    single { get<AppDatabase>().lightDao() }
    single { get<AppDatabase>().tagDao() }
    single { get<AppDatabase>().lightTagRelation() }
    single { LightRepository(get()) }
    single { TagRepository(get()) }
    single { LightTagRelationRepository(get()) }

    viewModel { LightViewModel(get()) }
    viewModel { TagViewModel(get()) }
    viewModel { LightTagRelationViewModel(get()) }
    viewModel { SplashViewModel(get()) }
    viewModel { CalendarViewModel(get()) }
    viewModel { StatisticViewModel(get()) }
    viewModel { StatisticDetailViewModel(get()) }
}