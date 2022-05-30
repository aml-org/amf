/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ /*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yaml

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import org.yaml.YamlHighlighter.SIGN
import org.yaml.YAMLTokenTypes._
import org.yaml.lexer.YAMLFlexLexer

class YamlSyntaxHighlighter extends SyntaxHighlighterBase with YAMLTokenTypes {
  private val map: Map[IElementType, TextAttributesKey] = Map(
    SCALAR_KEY      -> YamlHighlighter.SCALAR_KEY,
    SCALAR_STRING   -> YamlHighlighter.SCALAR_STRING,
    SCALAR_DSTRING  -> YamlHighlighter.SCALAR_DSTRING,
    SCALAR_TEXT     -> YamlHighlighter.SCALAR_TEXT,
    SCALAR_LIST     -> YamlHighlighter.SCALAR_LIST,
    COMMENT         -> YamlHighlighter.COMMENT,
    TEXT            -> YamlHighlighter.TEXT,
    LBRACE          -> SIGN,
    RBRACE          -> SIGN,
    LBRACKET        -> SIGN,
    RBRACKET        -> SIGN,
    COMMA           -> SIGN,
    QUESTION        -> SIGN,
    COLON           -> SIGN,
    DOCUMENT_MARKER -> SIGN,
    SEQUENCE_MARKER -> SIGN
  )

  override def getTokenHighlights(tokenType: IElementType): Array[TextAttributesKey] = {
    SyntaxHighlighterBase.pack(map.getOrElse(tokenType, null))
  }

  override def getHighlightingLexer = new YAMLFlexLexer
}
