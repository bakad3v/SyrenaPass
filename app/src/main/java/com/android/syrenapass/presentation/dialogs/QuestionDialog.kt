package com.android.syrenapass.presentation.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.android.syrenapass.R
import com.android.syrenapass.databinding.InfoDialogFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


/**
 * Confirmation dialog fragment
 */
class QuestionDialog : DialogFragment() {
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val message = requireArguments().getString(MESSAGE) ?: throw RuntimeException("Message absent in QuestionDialog")
    val title = requireArguments().getString(TITLE) ?: throw RuntimeException("Title absent in QuestionDialog")
    val requestKey =  requireArguments().getString(ARG_REQUEST_KEY) ?: throw RuntimeException("Request key absent in QuestionDialog")
    val infoDialogFragmentBinding = InfoDialogFragmentBinding.inflate(layoutInflater)
    infoDialogFragmentBinding.root.text = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)
    infoDialogFragmentBinding.root.movementMethod = LinkMovementMethod.getInstance()
    return MaterialAlertDialogBuilder(requireActivity())
      .setTitle(title)
      .setView(infoDialogFragmentBinding.root)
      .setNegativeButton(R.string.cancel) { dialog: DialogInterface, i: Int -> dialog.cancel() }
      .setPositiveButton(R.string.ok) { _, _ ->
        parentFragmentManager.setFragmentResult(
          requestKey,
          bundleOf(RESPONSE to true)
        )
      }
      .create()
  }

  companion object {
    const val MESSAGE = "message"
    const val TITLE = "title"
    private val TAG = QuestionDialog::class.simpleName
    private const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"
    const val RESPONSE = "RESPONSE"
    fun show(fragmentManager: FragmentManager, title: String, message: String, requestKey: String) {
      val fragment = QuestionDialog().apply {
        arguments = bundleOf(MESSAGE to message, TITLE to title, ARG_REQUEST_KEY to requestKey)
        }
      fragment.show(fragmentManager, TAG)
    }

    fun setupListener(fragmentManager: FragmentManager, requestKey: String, lifecycleOwner: LifecycleOwner, listener: () -> Unit) {
      fragmentManager.setFragmentResultListener(requestKey,lifecycleOwner
      ) { _, _ -> listener.invoke() }
    }

  }
}
