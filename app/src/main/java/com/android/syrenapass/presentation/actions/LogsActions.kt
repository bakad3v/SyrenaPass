package com.android.syrenapass.presentation.actions

import com.android.syrenapass.presentation.utils.DateValidatorAllowed

sealed class LogsActions {
  class ShowUsualDialog(val value: DialogActions): LogsActions()
  class ShowDatePicker(val dateValidator: DateValidatorAllowed, val selection: Long): LogsActions()

}
