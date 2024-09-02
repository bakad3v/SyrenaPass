package com.android.syrenapass.domain.usecases.apps

import com.android.syrenapass.domain.entities.AppDomain
import com.android.syrenapass.domain.repositories.AppsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInstalledAppsUseCase @Inject constructor(private val appsRepository: AppsRepository){
    operator fun invoke(): List<AppDomain> {
        return appsRepository.getInstalledApplications()
    }
}