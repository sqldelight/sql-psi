package com.alecstrong.sql.psi.sample.plugin

import com.alecstrong.sql.psi.sample.core.SampleFileType
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory

class SampleFileTypeFactory : FileTypeFactory() {
  override fun createFileTypes(consumer: FileTypeConsumer) {
    consumer.consume(SampleFileType, SampleFileType.defaultExtension)
  }
}
