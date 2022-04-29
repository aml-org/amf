package org.yaml

import java.util

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFileFactory, TokenType}
import com.intellij.util.LocalTimeCounter
import org.yaml.psi.{YAMLFile, YAMLKeyValue}
import org.yaml.psi.impl.YAMLQuotedTextImpl

object YamlElementGenerator {
  def apply(project: Project): YamlElementGenerator = ServiceManager.getService(project, classOf[YamlElementGenerator])

  def createChainedKey(keyComponents: util.List[String], indentAddition: Int): String = {
    val sb = new StringBuilder
    for (i <- 0 until keyComponents.size) {
      if (i > 0) sb.append('\n').append(StringUtil.repeatSymbol(' ', indentAddition + 2 * i))
      sb.append(keyComponents.get(i)).append(":")
    }
    sb.toString
  }
}

class YamlElementGenerator(val myProject: Project) {
  def createYamlKeyValue(keyName: String, valueText: String): YAMLKeyValue = {
    val tempFile = createDummyYamlWithText(keyName + ": " + valueText)
    PsiTreeUtil.collectElementsOfType(tempFile, classOf[YAMLKeyValue]).iterator.next
  }

  def createYamlDoubleQuotedString: YAMLQuotedTextImpl = {
    val tempFile = createDummyYamlWithText("\"foo\"")
    PsiTreeUtil.collectElementsOfType(tempFile, classOf[YAMLQuotedTextImpl]).iterator.next
  }

  def createDummyYamlWithText(text: String): YAMLFile =
    PsiFileFactory
      .getInstance(myProject)
      .createFileFromText(
        "temp." + YamlFileType.getDefaultExtension,
        YamlFileType,
        text,
        LocalTimeCounter.currentTime,
        true
      )
      .asInstanceOf[YAMLFile]

  def createEol: PsiElement = {
    val file = createDummyYamlWithText("\n")
    PsiTreeUtil.getDeepestFirst(file)
  }

  def createSpace: PsiElement = {
    val keyValue       = createYamlKeyValue("foo", "bar")
    val whitespaceNode = keyValue.getNode.findChildByType(TokenType.WHITE_SPACE)
    assert(whitespaceNode != null)
    whitespaceNode.getPsi
  }

  def createIndent(size: Int): PsiElement = {
    val file = createDummyYamlWithText(StringUtil.repeatSymbol(' ', size))
    PsiTreeUtil.getDeepestFirst(file)
  }

  def createColon: PsiElement = {
    val file = createDummyYamlWithText("? foo : bar")
    val at   = file.findElementAt("? foo ".length)
    assert(at != null && (at.getNode.getElementType eq YAMLTokenTypes.COLON))
    at
  }
}
