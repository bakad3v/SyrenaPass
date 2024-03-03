package com.android.syrenapass.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_SEND
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.android.syrenapass.R
import com.android.syrenapass.TopLevelFunctions.launchLifecycleAwareCoroutine
import com.android.syrenapass.databinding.PassFragmentBinding
import com.android.syrenapass.presentation.activities.MainActivity
import com.android.syrenapass.presentation.states.ActivityState
import com.android.syrenapass.presentation.states.PasswordState
import com.android.syrenapass.presentation.viewmodels.PasswordsVM
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for entering password
 */
@AndroidEntryPoint
class PassFragment: Fragment() {
  private val viewModel: PasswordsVM by viewModels()
  private var _binding: PassFragmentBinding? = null
  private val controller by lazy { findNavController() }
  private val binding
    get() = _binding ?: throw RuntimeException("MainFragmentBinding == null")

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding =
      PassFragmentBinding.inflate(inflater, container, false)
    binding.viewmodel = viewModel
    binding.lifecycleOwner = viewLifecycleOwner
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    (activity as MainActivity).setActivityState(ActivityState.PasswordActivityState)
    binding.password.imeOptions = IME_FLAG_NO_PERSONALIZED_LEARNING + IME_ACTION_SEND
    observePasswordState()
    observeTextEntered()
  }

  /**
   * Handling send action
   */
  private fun observeTextEntered() {
    binding.password.setOnEditorActionListener {
        textView, actionId, _ ->
        var handled = false
        if (actionId == IME_ACTION_SEND) {
          viewModel.passwordEntered(textView.text.toList().toCharArray())
          handled = true
        }
        return@setOnEditorActionListener handled
    }
  }

  /**
   * Moving to next screen after database unlocking
   */
  private fun observePasswordState() {
    viewLifecycleOwner.launchLifecycleAwareCoroutine {
      viewModel.passwordStatus.collect{
        if (it is PasswordState.CheckPasswordResults && it.rightPassword) {
          viewModel.writeToLogs(getString(R.string.app_entered))
          hideKeyboard()
          controller.navigate(R.id.setupFilesFragment)
        }
      }
    }
  }

  private fun hideKeyboard() {
    val inputMethodManager =
      getSystemService(requireContext(), InputMethodManager::class.java) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
  }

  override fun onDestroy() {
    _binding = null
    super.onDestroy()
  }
}
