package com.android.syrenapass.domain.usecases.apps

import com.android.syrenapass.domain.entities.AppDomain
import com.android.syrenapass.domain.repositories.AppsRepository
import javax.inject.Inject

class AddApplicationsUseCase @Inject constructor(private val appsRepository: AppsRepository){
    suspend operator fun invoke(apps: List<AppDomain>) {
        return appsRepository.addApplications(apps)
    }
}