package org.yaml

import com.intellij.psi.TokenType
import com.intellij.psi.tree.{IFileElementType, TokenSet}
import org.yaml.YAMLTokenTypes._

/** Created by emilio.gabeiras on 6/4/17.
  */
object YamlElementTypes {
  def FILE                 = new IFileElementType(YamlLanguage)
  val DOCUMENT             = new YamlElementType("Document ---")
  val KEY_VALUE_PAIR       = new YamlElementType("Key value pair")
  val HASH                 = new YamlElementType("Hash")
  val ARRAY                = new YamlElementType("Array")
  val SEQUENCE_ITEM        = new YamlElementType("Sequence item")
  val COMPOUND_VALUE       = new YamlElementType("Compound value")
  val MAPPING              = new YamlElementType("Mapping")
  val SEQUENCE             = new YamlElementType("Sequence")
  val SCALAR_LIST_VALUE    = new YamlElementType("Scalar list value")
  val SCALAR_TEXT_VALUE    = new YamlElementType("Scalar text value")
  val SCALAR_PLAIN_VALUE   = new YamlElementType("Scalar plain style")
  val SCALAR_QUOTED_STRING = new YamlElementType("Scalar quoted string")
  val SCALAR_VALUES: TokenSet =
    TokenSet.create(SCALAR_TEXT, SCALAR_STRING, SCALAR_DSTRING, SCALAR_LIST, TEXT, SCALAR_LIST_VALUE)
  val BLANK_ELEMENTS: TokenSet = TokenSet.create(WHITESPACE, TokenType.WHITE_SPACE, EOL, INDENT, COMMENT)
}
