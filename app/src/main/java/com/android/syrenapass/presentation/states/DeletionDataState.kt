package com.android.syrenapass.presentation.states

import com.android.syrenapass.domain.entities.FileDomain


sealed class DeletionDataState {
  data object Loading : DeletionDataState()
  class ViewData(val items: List<FileDomain>) : DeletionDataState()
}
