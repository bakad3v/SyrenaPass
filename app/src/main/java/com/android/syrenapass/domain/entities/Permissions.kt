package com.android.syrenapass.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class Permissions(val isAdmin: Boolean=false, val isOwner: Boolean=false, val isRoot: Boolean=false)