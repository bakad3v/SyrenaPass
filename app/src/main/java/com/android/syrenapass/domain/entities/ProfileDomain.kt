package com.android.syrenapass.domain.entities

data class ProfileDomain(val id: Int, val name: String, val main: Boolean, val toDelete: Boolean=false)