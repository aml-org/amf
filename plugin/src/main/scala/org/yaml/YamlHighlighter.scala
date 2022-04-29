package org.yaml

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.{DOC_COMMENT, KEYWORD, OPERATION_SIGN, STRING}
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey

/** Text attributes keys
  */
object YamlHighlighter {
  val SCALAR_KEY: TextAttributesKey     = createTextAttributesKey("YAML_SCALAR_KEY", KEYWORD)
  val SCALAR_TEXT: TextAttributesKey    = createTextAttributesKey("YAML_SCALAR_VALUE", HighlighterColors.TEXT)
  val SCALAR_STRING: TextAttributesKey  = createTextAttributesKey("YAML_SCALAR_STRING", STRING)
  val SCALAR_DSTRING: TextAttributesKey = createTextAttributesKey("YAML_SCALAR_DSTRING", STRING)
  val SCALAR_LIST: TextAttributesKey    = createTextAttributesKey("YAML_SCALAR_LIST", HighlighterColors.TEXT)
  val COMMENT: TextAttributesKey        = createTextAttributesKey("YAML_COMMENT", DOC_COMMENT)
  val TEXT: TextAttributesKey           = createTextAttributesKey("YAML_TEXT", HighlighterColors.TEXT)
  val SIGN: TextAttributesKey           = createTextAttributesKey("YAML_SIGN", OPERATION_SIGN)
}
