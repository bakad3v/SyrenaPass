package com.android.syrenapass.presentation.actions

import com.android.syrenapass.presentation.utils.UIText

sealed class DialogActions {
  class ShowQuestionDialog(
    val title: UIText.StringResource,
    val message: UIText.StringResource,
    val requestKey: String
  ) : DialogActions()

  class ShowInfoDialog(val title: UIText.StringResource, val message: UIText.StringResource) :
    DialogActions()

  class ShowInputDigitDialog(
    val title: UIText.StringResource,
    val hint: String,
    val message: UIText.StringResource,
    val range: IntRange
  ) :
    DialogActions()

  class ShowInputPasswordDialog(
    val title: UIText.StringResource,
    val hint: String,
    val message: UIText.StringResource
  ) : DialogActions()
}
