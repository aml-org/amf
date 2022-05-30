package org.yaml

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader

/** Created by emilio.gabeiras on 6/4/17.
  */
object YamlFileType extends LanguageFileType(YamlLanguage) {
  override def getName             = "YAML"
  override def getDescription      = DESCRIPTION
  override def getDefaultExtension = "yml"
  override def getIcon             = FILE

  private final val DESCRIPTION = YamlBundle.message("filetype.description.yaml")
  private final val FILE        = IconLoader.getIcon("/org/yaml/icons/yml-24.png")
}
