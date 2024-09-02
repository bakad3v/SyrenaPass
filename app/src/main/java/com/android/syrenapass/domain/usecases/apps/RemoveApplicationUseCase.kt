package com.android.syrenapass.domain.usecases.apps

import com.android.syrenapass.domain.repositories.AppsRepository
import javax.inject.Inject

class RemoveApplicationUseCase @Inject constructor(private val appsRepository: AppsRepository){
    suspend operator fun invoke(packageName: String) {
        return appsRepository.removeApplication(packageName)
    }
}