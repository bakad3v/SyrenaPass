package com.android.aftools.data.repositories

import android.content.Context
import com.android.aftools.data.serializers.SettingsSerializer
import com.android.aftools.datastoreDBA.dataStoreDirectBootAware
import com.android.aftools.domain.entities.Settings
import com.android.aftools.domain.entities.Theme
import com.android.aftools.domain.repositories.SettingsRepository
import com.android.aftools.superuser.superuser.SuperUserManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository for changing app settings and enabling/disabling deletion
 */
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    settingsSerializer: SettingsSerializer,
    private val superUserManager: SuperUserManager
) : SettingsRepository {

    private val Context.settingsDatastore by dataStoreDirectBootAware(
        DATASTORE_NAME,
        settingsSerializer
    )

    companion object {
        private const val DATASTORE_NAME = "settings_datastore.json"
    }

    override val settings: Flow<Settings> = context.settingsDatastore.data

    override suspend fun setTheme(theme: Theme) {
        context.settingsDatastore.updateData {
            it.copy(theme = theme)
        }
    }

    override suspend fun setServiceStatus(working: Boolean) {
        context.settingsDatastore.updateData {
            it.copy(serviceWorking = working)
        }
    }

    override suspend fun setRunOnBoot(status: Boolean) {
        context.settingsDatastore.updateData {
            it.copy(runOnBoot = status)
        }
    }

    override suspend fun setDeleteApps(new: Boolean) {
        context.settingsDatastore.updateData {
            it.copy(deleteApps = new)
        }
    }

    override suspend fun setDeleteFiles(new: Boolean) {
        context.settingsDatastore.updateData {
            it.copy(deleteFiles = new)
        }
    }

    override suspend fun setDeleteProfiles(new: Boolean) {
        context.settingsDatastore.updateData {
            it.copy(deleteProfiles = new)
        }
    }

    override suspend fun setTRIM(new: Boolean) {
        context.settingsDatastore.updateData {
            it.copy(trim = new)
        }
    }

    override suspend fun setWipe(new: Boolean) {
        context.settingsDatastore.updateData {
            it.copy(wipe = new)
        }
    }

    override suspend fun runRoot(new: Boolean) {
        context.settingsDatastore.updateData {
            it.copy(runRoot = new)
        }
    }

    override suspend fun sendBroadcast(new: Boolean) {
        context.settingsDatastore.updateData {
            it.copy(sendBroadcast = new)
        }
    }

    override suspend fun setRemoveItself(
        new: Boolean
    ) {
        context.settingsDatastore.updateData {
            it.copy(removeItself = new)
        }
    }

    override suspend fun setLogdOnStart(
        new: Boolean
    ) {
        context.settingsDatastore.updateData {
            it.copy(stopLogdOnStart = new)
        }
    }

    override suspend fun setLogdOnBoot(
        new: Boolean
    ) {
        context.settingsDatastore.updateData {
            it.copy(stopLogdOnBoot = new)
        }
    }

    override suspend fun setClearAndHide(new: Boolean) {
        context.settingsDatastore.updateData {
            it.copy(clearAndHideItself = new)
        }
    }

    override suspend fun setUserLimit(limit: Int) {
        superUserManager.getSuperUser().setUsersLimit(limit)
    }


    override suspend fun enableMultiuserUI() {
        superUserManager.getSuperUser().enableMultiuserUI()
    }


    override suspend fun getUserLimit(): Int? =
        superUserManager.getSuperUser().getUserLimit()

    override suspend fun disableSafeBoot() {
        superUserManager.getSuperUser().disableSafeBoot()
    }

    override suspend fun setRunOnDuressPassword(status: Boolean) {
        context.settingsDatastore.updateData {
            it.copy(runOnDuressPassword = status)
        }
    }

}
