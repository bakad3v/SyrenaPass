package com.android.syrenapass.presentation.services

import android.content.Context
import com.android.syrenapass.R
import com.android.syrenapass.domain.usecases.apps.GetManagedAppsUseCase
import com.android.syrenapass.domain.usecases.apps.RemoveApplicationUseCase
import com.android.syrenapass.domain.usecases.logs.GetLogsDataUseCase
import com.android.syrenapass.domain.usecases.logs.WriteToLogsUseCase
import com.android.syrenapass.domain.usecases.permissions.GetPermissionsUseCase
import com.android.syrenapass.domain.usecases.profiles.DeleteProfilesUseCase
import com.android.syrenapass.domain.usecases.profiles.GetProfilesToDeleteUseCase
import com.android.syrenapass.domain.usecases.profiles.GetProfilesUseCase
import com.android.syrenapass.domain.usecases.settings.GetSettingsUseCase
import com.android.syrenapass.presentation.utils.UIText
import com.android.syrenapass.superuser.superuser.SuperUser
import com.android.syrenapass.superuser.superuser.SuperUserException
import com.android.syrenapass.superuser.superuser.SuperUserManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BFUActivitiesRunner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val getProfilesToDelete: GetProfilesToDeleteUseCase,
    private val getManagedAppsUseCase: GetManagedAppsUseCase,
    private val removeApplicationUseCase: RemoveApplicationUseCase,
    private val deleteProfilesUseCase: DeleteProfilesUseCase,
    private val writeToLogsUseCase: WriteToLogsUseCase,
    private val superUserManager: SuperUserManager,
    private val getPermissionsUseCase: GetPermissionsUseCase,
    private val getLogsDataUseCase: GetLogsDataUseCase
) {

    private var logsAllowed: Boolean? = null
    private val mutex = Mutex()
    private var isRunning = false

    suspend fun runTask() {
        mutex.withLock {
            if (isRunning) {
                return
            }
            isRunning = true
        }
        withContext(Dispatchers.IO) {
            runBFUActivity()
            mutex.withLock {
                isRunning = false
            }
        }
    }

    private suspend fun writeToLogsResource(rId: Int, vararg obj: Any) {
        if (logsAllowed == true)
            writeToLogsUseCase(context.getString(rId, obj))
    }

    private suspend fun writeToLogsUIText(text: UIText.StringResource) {
        if (logsAllowed == true)
            writeToLogsUseCase(text.asString(context))
    }

    private suspend fun runSuperuserAction(startText: Int, successText: Int, failureText: Int, vararg startTextParams: Any, action: suspend () -> Unit): Boolean {
        try {
            writeToLogsResource(startText, startTextParams)
            action()
            writeToLogsResource(successText)
            return true
        } catch (e: SuperUserException) {
            writeToLogsUIText(e.messageForLogs)
        } catch (e: Exception) {
            writeToLogsResource(failureText, e.stackTraceToString())
        }
        return false
    }

    private suspend fun removeProfiles(superUser: SuperUser) {
        writeToLogsResource(R.string.getting_profiles)
        val profiles = try {
            getProfilesToDelete().first()
        } catch (e: Exception) {
            writeToLogsResource(R.string.getting_profiles_failed)
            return
        }
        profiles.forEach {
                runSuperuserAction(R.string.removing_profile,R.string.profile_removed,R.string.profile_not_removed, it) {
                    superUser.removeProfile(it)
                }
        }
        profiles.forEach {
            deleteProfilesUseCase(it)
        }
    }

    private suspend fun deleteApps(
        superUser: SuperUser
    ) {
        val apps = getManagedAppsUseCase().first()
        apps.forEach {
            if (it.toDelete) {
                runSuperuserAction(R.string.uninstalling_app,R.string.app_uninstalled,R.string.app_not_uninstalled, it.packageName) {
                    superUser.uninstallApp(it.packageName)
                }
            }
            if (it.toClearData) {
                runSuperuserAction(R.string.clearing_app_data,R.string.app_data_cleared, R.string.app_data_not_cleared, it.packageName) {
                    superUser.clearAppData(it.packageName)
                }
            }
            if (it.toHide) {
                runSuperuserAction(R.string.hiding_app,R.string.app_hidden, R.string.app_not_hidden, it.packageName) {
                    superUser.hideApp(it.packageName)
                }
            }
        }
        apps.forEach {
            removeApplicationUseCase(it.packageName)
        }
    }

    private suspend fun runBFUActivity() {
        try {
            logsAllowed = getLogsDataUseCase().first().logsEnabled
            writeToLogsResource(R.string.actions_started)
        } catch (e: Exception) {
            return
        }
        writeToLogsResource(R.string.loading_data)
        val (permissions, settings, superUser) = try {
            Triple(
                getPermissionsUseCase().first(),
                getSettingsUseCase().first(),
                superUserManager.getSuperUser()
            )
        } catch (e: Exception) {
            writeToLogsResource(R.string.getting_data_error, e.stackTraceToString())
            return
        }
        writeToLogsResource(R.string.got_data)
        if (permissions.isAdmin && settings.wipe) {
            runSuperuserAction(R.string.wiping_data,R.string.data_wiped,R.string.wiping_data_error) {
                superUser.wipe()
            }
            return
        }
        if (!permissions.isRoot && !permissions.isOwner)
            return
        if (settings.deleteProfiles) {
            removeProfiles(superUser)
        }
        if (settings.deleteApps) {
            deleteApps(superUser)
        }
        if (permissions.isRoot) {
            if (settings.runRoot) {
                superUser.executeRootCommand("TODO")
            }
            if (settings.trim) {
                runSuperuserAction(R.string.running_trim, R.string.trim_runned, R.string.trim_failed) {
                    superUser.runTrim()
                }
            }
        }
        if (settings.removeItself && !settings.deleteFiles && permissions.isRoot) {
            runSuperuserAction(R.string.uninstalling_itself, R.string.uninstallation_complete,R.string.uninstallation_failed) {
                superUser.uninstallApp(context.packageName)
            }
        }
    }
}