package com.android.syrenapass.presentation.fragments

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.android.syrenapass.R
import com.android.syrenapass.TopLevelFunctions.launchLifecycleAwareCoroutine
import com.android.syrenapass.databinding.SettingsFragmentBinding
import com.android.syrenapass.domain.entities.Theme
import com.android.syrenapass.presentation.activities.MainActivity
import com.android.syrenapass.presentation.dialogs.DialogLauncher
import com.android.syrenapass.presentation.dialogs.PasswordInputDialog
import com.android.syrenapass.presentation.dialogs.QuestionDialog
import com.android.syrenapass.presentation.receivers.DeviceAdminReceiver
import com.android.syrenapass.presentation.states.ActivityState
import com.android.syrenapass.presentation.viewmodels.SettingsVM
import com.android.syrenapass.presentation.viewmodels.SettingsVM.Companion.CONFIRM_AUTODELETION_REQUEST
import com.android.syrenapass.presentation.viewmodels.SettingsVM.Companion.MOVE_TO_ACCESSIBILITY_SERVICE
import com.android.syrenapass.presentation.viewmodels.SettingsVM.Companion.MOVE_TO_ADMIN_SETTINGS
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for managing app settings
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

  private val viewModel: SettingsVM by viewModels()
  private var _binding: SettingsFragmentBinding? = null
  private val binding
    get() = _binding ?: throw RuntimeException("SettingsFragmentBinding == null")

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding =
      SettingsFragmentBinding.inflate(inflater, container, false)
    binding.viewmodel = viewModel
    binding.lifecycleOwner = viewLifecycleOwner
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupActivity()
    setupThemesMenu()
    listenDialogResults()
    setupDialogs()
    setupMenu()
    setupButtons()
  }

  /**
   * Setting up faq icon in action bar
   */
  private fun setupMenu() {
    requireActivity().addMenuProvider(object : MenuProvider {
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.faq_menu, menu)
      }

      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
          R.id.help -> viewModel.showFaq()
        }
        return true
      }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
  }

  private fun startAccessibilityService() {
    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
  }

  private fun requestAdminRights() {
    startActivity(viewModel.adminRightsIntent())
  }

  /**
   * Setting up buttons
   */
  private fun setupButtons() {
    binding.setupPassword.setOnClickListener {
      viewModel.showPasswordInput()
    }
//    binding.startAccessibilityService.setOnClickListener {
//      viewModel.showAccessibilityServiceDialog()
//    }
//    binding.grantAdminRights.setOnClickListener {
//      // viewModel.showDeviceAdminRightsDialog()
//      val dpm = requireContext().getSystemService(DevicePolicyManager::class.java)
//      val deviceAdmin by lazy { ComponentName(requireContext(), DeviceAdminReceiver::class.java) }
//      Log.w("dpm",dpm.isDeviceOwnerApp("com.android.syrenapass").toString())
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//        Log.w("dpm",dpm.isAffiliatedUser.toString())
//      }
//    }
  }


  private fun setupThemesMenu() {
    binding.showMenu.setOnClickListener {
      showThemesMenu()
    }
  }

  /**
   * Listening for dialog result
   */
  private fun listenDialogResults() {
    PasswordInputDialog.setupListener(
      parentFragmentManager,
      viewLifecycleOwner
    ) {
      viewModel.setPassword(it)
    }
    QuestionDialog.setupListener(
      parentFragmentManager,
      MOVE_TO_ACCESSIBILITY_SERVICE,
      viewLifecycleOwner
    ) {
      startAccessibilityService()
    }
    QuestionDialog.setupListener(
      parentFragmentManager,
      MOVE_TO_ADMIN_SETTINGS,
      viewLifecycleOwner
    ) {
      requestAdminRights()
    }
  }

  /**
   * Setting up dialog launcher
   */
  private fun setupDialogs() {
    val dialogLauncher = DialogLauncher(parentFragmentManager, context)
    viewLifecycleOwner.launchLifecycleAwareCoroutine {
      viewModel.settingsActionsFlow.collect {
        dialogLauncher.launchDialogFromAction(it)
      }
    }
  }

  private fun setupActivity() {
    (activity as MainActivity).setActivityState(
      ActivityState.NormalActivityState(
        getString(R.string.settings)
      )
    )
  }

  /**
   * Changing app's theme
   */
  private fun showThemesMenu() {
    val popup = PopupMenu(context, binding.showMenu)
    popup.menuInflater.inflate(R.menu.themes_menu, popup.menu)
    popup.setOnMenuItemClickListener {
      val theme = when (it.itemId) {
        R.id.dark_theme -> {
          Theme.DARK_THEME
        }

        R.id.light_theme -> {
          Theme.LIGHT_THEME
        }

        R.id.system_theme -> {
          Theme.SYSTEM_THEME
        }

        else -> throw RuntimeException("Wrong priority in priority sorting")
      }
      viewModel.setTheme(theme)
      return@setOnMenuItemClickListener true
    }
    popup.show()
  }

  override fun onDestroyView() {
    _binding = null
    super.onDestroyView()
  }
}
