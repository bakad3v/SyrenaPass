package com.android.syrenapass

import android.util.Log
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.syrenapass.data.mappers.ProfilesMapper
import com.android.syrenapass.presentation.utils.UIText
import com.android.syrenapass.superuser.superuser.SuperUserException
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit
import kotlin.time.measureTime

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class RootTest {
    private val profilesMapper = ProfilesMapper()

    private fun executeRootCommand(command: String): Shell.Result {
        val result = Shell.cmd(command).exec()
        if (!result.isSuccess) {
            if (!askSuperUserRights()) {
                throw SuperUserException("", UIText.StringResource(R.string.no_root_rights))
            }
            val resultText = result.out.joinToString(";")
            val errorText = result.err.joinToString(";")
            Log.w("fail", resultText)
            Log.w("fail", errorText)
            throw SuperUserException(
                resultText,
                UIText.StringResource(R.string.unknow_root_error, resultText)
            )
        }
        return result
    }

    private fun askSuperUserRights(): Boolean {
        val result = Shell.cmd("id").exec()
        return result.isSuccess
    }

    @Test
    fun testAskSuperUserRights() {
        assert(askSuperUserRights())
    }

    @Test
    fun testUsersCommands() {
        val oldUsers = executeRootCommand("pm list users").out
        Log.w("time", "start")
        executeRootCommand("pm create-user \"test\"")
        val users = executeRootCommand("pm list users").out
        val id = users.drop(1).map { profilesMapper.mapRootOutputToProfile(it) }
            .find { it.name == "test" }!!.id
        Log.w("ident", id.toString())
        executeRootCommand("pm remove-user $id")
        val usersComparison = executeRootCommand("pm list users").out
        assertEquals(oldUsers, usersComparison)
        Log.w("time", "finish")
    }

    @Test
    fun testAppManagement() {
        val packageName = "com.android.syrenapass"
        if (executeRootCommand("dpm list-owners").out.any { packageName in it })
            executeRootCommand("dpm remove-active-admin $packageName/.presentation.services.DeviceAdminReceiver")
        executeRootCommand("pm disable $packageName")
        assert(packageName !in executeRootCommand("pm list packages").out)
        executeRootCommand("pm enable $packageName")
        assert(packageName in executeRootCommand("pm list packages").out)
        executeRootCommand("pm uninstall $packageName")
    }

    /**
     * Conclusion: app uninstallation operations seems to not be parallellised, it's better to execute operations sequentially
     */
    @Test
    fun runAppsDeletionConcurrently() = runTest(timeout = 1.hours) {
        askSuperUserRights()
        val time = measureTime {
            val packages = listOf("me.lucky.silence", "me.lucky.wasted", "me.lucky.duress")
            val coroutines = packages.map {
                launch(Dispatchers.IO) {
                    Log.w("uninstallation","$it uninstalling")
                    executeRootCommand("pm uninstall $it")
                    Log.w("uninstallation","$it uninstalled")
                }
            }
            coroutines.joinAll()
        }
        Log.w("uninstallation_time", time.toString(DurationUnit.MILLISECONDS))
    }

    /**
     * Conclusion: app disabling can't be parallelized too, it's significantly faster than app uninstallation
     */
    @Test
    fun runAppsDisableConcurrently() = runTest(timeout = 1.hours) {
        askSuperUserRights()
        val time = measureTime {
            val packages = listOf("me.lucky.silence", "me.lucky.wasted", "me.lucky.duress")
            val coroutines = packages.map {
                launch(Dispatchers.IO) {
                    Log.w("uninstallation","$it disabling")
                    executeRootCommand("pm disable $it")
                    Log.w("uninstallation","$it disabled")
                }
            }
            coroutines.joinAll()
        }
        Log.w("uninstallation_time", time.toString(DurationUnit.MILLISECONDS))
    }

    /**
     * Conclusion: app's data clearing can't be parallelized too, it's slower than app hiding but faster than uninstallation.
     */
    @Test
    fun runRemoveAppsDataConcurrently() = runTest(timeout = 1.hours) {
        askSuperUserRights()
        val time = measureTime {
            val packages = listOf("me.lucky.silence", "me.lucky.wasted", "me.lucky.duress")
            val coroutines = packages.map {
                launch(Dispatchers.IO) {
                    Log.w("uninstallation","$it clearing")
                    executeRootCommand("pm clear $it")
                    Log.w("uninstallation","$it cleared")
                }
            }
            coroutines.joinAll()
        }
        Log.w("uninstallation_time", time.toString(DurationUnit.MILLISECONDS))
    }


    @Test
    fun runAppsDeletionSequence() {
        askSuperUserRights()
        val time = measureTime {
            val packages = listOf("me.lucky.silence", "me.lucky.wasted", "me.lucky.duress")
            packages.forEach {
                executeRootCommand("pm uninstall $it")
                Log.w("uninstallation","$it uninstalled")
            }
        }
        Log.w("uninstallation_time", time.toString(DurationUnit.MILLISECONDS))
    }

    @Test
    fun runTrim() {
        val result = executeRootCommand("sm fstrim")
        assert(result.isSuccess)
    }
}