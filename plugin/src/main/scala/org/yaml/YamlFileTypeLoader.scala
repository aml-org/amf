package org.yaml

import com.intellij.openapi.fileTypes.{FileTypeConsumer, FileTypeFactory}

class YamlFileTypeLoader extends FileTypeFactory {
  override def createFileTypes(consumer: FileTypeConsumer): Unit =
    consumer.consume(YamlFileType, YamlFileType.getDefaultExtension + ";yaml")
}
