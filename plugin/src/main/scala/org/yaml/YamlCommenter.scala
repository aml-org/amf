package org.yaml

import com.intellij.lang.Commenter

class YamlCommenter extends Commenter {
  override def getLineCommentPrefix           = "#"
  override def getBlockCommentPrefix: String  = null
  override def getBlockCommentSuffix          = null
  override def getCommentedBlockCommentPrefix = null
  override def getCommentedBlockCommentSuffix = null
}
