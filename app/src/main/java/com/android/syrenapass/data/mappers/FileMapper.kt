package com.android.syrenapass.data.mappers

import android.net.Uri
import com.android.syrenapass.data.entities.FileDatastore
import com.android.syrenapass.data.entities.FilesList
import com.android.syrenapass.domain.entities.FileDomain
import javax.inject.Inject

class FileMapper @Inject constructor() {
  private fun mapDatastoreToDtModel(fileDatastore: FileDatastore) =
    FileDomain(
      size = fileDatastore.size,
      name = fileDatastore.name,
      priority = fileDatastore.priority,
      uri = Uri.parse(fileDatastore.uri),
      fileType = fileDatastore.fileType,
      sizeFormatted = fileDatastore.sizeFormatted
    )

  fun mapDatastoreListToDtList(list: FilesList): List<FileDomain> =
    list.list.map { mapDatastoreToDtModel(it) }
}
