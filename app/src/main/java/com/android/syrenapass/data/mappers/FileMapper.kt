package com.android.syrenapass.data.mappers

import android.net.Uri
import com.android.syrenapass.data.db.FileDbModel
import com.android.syrenapass.domain.entities.FileDomain
import javax.inject.Inject

class FileMapper @Inject constructor() {
  private fun mapDbToDtModel(fileDbModel: FileDbModel) =
    FileDomain(
      size = fileDbModel.size,
      name = fileDbModel.name,
      priority = fileDbModel.priority,
      uri = Uri.parse(fileDbModel.uri),
      fileType = fileDbModel.fileType,
      sizeFormatted = fileDbModel.sizeFormatted
    )

  fun mapDbListToDtList(dbList: List<FileDbModel>): List<FileDomain> =
    dbList.map { mapDbToDtModel(it) }

}
