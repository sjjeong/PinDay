package com.dino.pinday.di

import com.dino.pinday.data.db.DriverFactory
import com.dino.pinday.data.db.PinDayDatabase
import com.dino.pinday.data.repository.AnniversaryRepository
import com.dino.pinday.domain.usecase.CalculateDDayUseCase
import com.dino.pinday.domain.usecase.GetMilestonesUseCase
import com.dino.pinday.ui.add.AddEditViewModel
import com.dino.pinday.ui.detail.DetailViewModel
import com.dino.pinday.ui.home.HomeViewModel
import com.dino.pinday.ui.onboarding.OnboardingViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

expect val platformModule: Module

val appModule = module {
    single { get<DriverFactory>().createDriver() }
    single { PinDayDatabase(get()) }
    single { AnniversaryRepository(get()) }
    single { CalculateDDayUseCase() }
    single { GetMilestonesUseCase() }

    viewModel { HomeViewModel(get(), get()) }
    viewModel { params -> AddEditViewModel(params.getOrNull(), get(), get()) }
    viewModel { params -> DetailViewModel(params.get(), get(), get(), get()) }
    viewModel { OnboardingViewModel(get()) }
}
