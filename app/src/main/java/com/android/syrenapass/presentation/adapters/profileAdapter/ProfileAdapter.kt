package com.android.syrenapass.presentation.adapters.profileAdapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.ListAdapter
import com.android.syrenapass.R
import com.android.syrenapass.databinding.ProfileCardviewBinding
import com.android.syrenapass.domain.entities.ProfileDomain
import com.google.android.material.button.MaterialButton

import javax.inject.Inject

/**
 * Recycler view adapter for usual files
 */
class ProfileAdapter @Inject constructor(
  diffCallback: MyProfileAdapterDiffCallback,
) : ListAdapter<ProfileDomain, MyProfileViewHolder>(diffCallback) {

  /**
   * Callbacks which can be set up from activity
   */
  var onDeleteItemClickListener: ((Int, Boolean) -> Unit)? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyProfileViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = ProfileCardviewBinding.inflate(inflater, parent, false)
    return MyProfileViewHolder(binding)
  }

  /**
   * Function for selecting image to display depending on the type of file
   */
  private fun MaterialButton.setStyle(delete: Boolean) {
      if (delete) {
        setText(R.string.not_delete)
        setIconResource(R.drawable.baseline_block_24)
        val typedValue = TypedValue()
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorTertiary, typedValue, true)
        setTextColor(typedValue.data)
        setIconTintResource(typedValue.data)
        setStrokeColorResource(typedValue.data)
        return
      }
      setText(R.string.delete)
      setIconResource(R.drawable.ic_baseline_delete_24)
      val typedValue = TypedValue()
      context.theme.resolveAttribute(com.google.android.material.R.attr.colorError, typedValue, true)
      setTextColor(typedValue.data)
      setIconTintResource(typedValue.data)
      setStrokeColorResource(typedValue.data)
  }

  /**
   * Function for setting up text in recyclerview item
   */
  override fun onBindViewHolder(holder: MyProfileViewHolder, position: Int) {
    val profile = getItem(position)
    with(holder.binding) {
      name.text = profile.name
      delete.setStyle(profile.toDelete)
      delete.setOnClickListener {
        onDeleteItemClickListener?.invoke(profile.id,!profile.toDelete)
      }
    }
  }

}
