package com.android.syrenapass.superuser.superuser

import com.android.syrenapass.presentation.utils.UIText

class SuperUserException(message: String, val messageForLogs: UIText.StringResource): Exception(message)