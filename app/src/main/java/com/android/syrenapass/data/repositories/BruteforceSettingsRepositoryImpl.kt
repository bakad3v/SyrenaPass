package com.android.syrenapass.data.repositories

import android.content.Context
import com.android.syrenapass.data.serializers.BruteforceSettingsSerializer
import com.android.syrenapass.data.serializers.UsbSettingsSerializer
import com.android.syrenapass.datastoreDBA.dataStoreDirectBootAware
import com.android.syrenapass.domain.entities.BruteforceSettings
import com.android.syrenapass.domain.entities.UsbSettings
import com.android.syrenapass.domain.repositories.BruteforceRepository
import com.android.syrenapass.domain.repositories.UsbSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BruteforceSettingsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context, bruteforceSettingsSerializer: BruteforceSettingsSerializer):
    BruteforceRepository {
    private val Context.bruteforceDataStore by dataStoreDirectBootAware(
        DATASTORE_NAME,
        bruteforceSettingsSerializer
    )

    companion object {
        private const val DATASTORE_NAME = "bruteforce_datastore.json"
    }

    override val bruteforceSettings: Flow<BruteforceSettings> = context.bruteforceDataStore.data

    override suspend fun setBruteforceStatus(status: Boolean) {
        context.bruteforceDataStore.updateData {
            it.copy(bruteforceRestricted = status)
        }
    }

    override suspend fun setBruteforceLimit(limit: Int) {
        context.bruteforceDataStore.updateData {
            it.copy(allowedAttempts = limit)
        }
    }

    override suspend fun onWrongPassword(): Boolean {
        context.bruteforceDataStore.updateData {
            it.copy(wrongAttempts = it.wrongAttempts+1)
        }
        val data = context.bruteforceDataStore.data.first()
        return data.wrongAttempts >= data.allowedAttempts
    }

    override suspend fun onRightPassword() {
        context.bruteforceDataStore.updateData {
            it.copy(wrongAttempts = 0)
        }
    }

}