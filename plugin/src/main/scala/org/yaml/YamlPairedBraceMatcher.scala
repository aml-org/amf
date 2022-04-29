package org.yaml

import com.intellij.lang.{BracePair, PairedBraceMatcher}
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import org.yaml.YAMLTokenTypes._

class YamlPairedBraceMatcher extends PairedBraceMatcher {
  private final val _pairs: Array[BracePair] =
    Array(new BracePair(LBRACE, RBRACE, true), new BracePair(LBRACKET, RBRACKET, true))

  override def getPairs: Array[BracePair] = _pairs

  override def isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType): Boolean = true

  override def getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int = openingBraceOffset
}
