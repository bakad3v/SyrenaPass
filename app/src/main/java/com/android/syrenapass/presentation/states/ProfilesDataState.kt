package com.android.syrenapass.presentation.states

import com.android.syrenapass.domain.entities.ProfileDomain


sealed class ProfilesDataState {
  data object Loading : ProfilesDataState()
  data object SuperUserAbsent: ProfilesDataState()
  class ViewData(val items: List<ProfileDomain>) : ProfilesDataState()
}
