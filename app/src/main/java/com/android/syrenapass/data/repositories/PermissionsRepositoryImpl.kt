package com.android.syrenapass.data.repositories

import android.content.Context
import com.android.syrenapass.data.serializers.PermissionsSerializer
import com.android.syrenapass.data.serializers.SettingsSerializer
import com.android.syrenapass.datastoreDBA.dataStoreDirectBootAware
import com.android.syrenapass.domain.entities.Permissions
import com.android.syrenapass.domain.entities.Settings
import com.android.syrenapass.domain.repositories.PermissionsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PermissionsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context, permissionsSerializer: PermissionsSerializer): PermissionsRepository {
    private val Context.permissionsDatastore by dataStoreDirectBootAware(
        DATASTORE_NAME,
        permissionsSerializer
    )

    companion object {
        private const val DATASTORE_NAME = "permissions_datastore.json"
    }

    override val permissions: Flow<Permissions> = context.permissionsDatastore.data

    override suspend fun setAdminStatus(status: Boolean) {
        context.permissionsDatastore.updateData {
            it.copy(isAdmin = status)
        }
    }

    override suspend fun setOwnerStatus(status: Boolean) {
        context.permissionsDatastore.updateData {
            it.copy(isOwner = status)
        }
    }

    override suspend fun setRootStatus(status: Boolean) {
        context.permissionsDatastore.updateData {
            it.copy(isRoot = status)
        }
    }
}