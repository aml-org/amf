package org.yaml

import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.{ASTNode, ParserDefinition}
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.{FileViewProvider, PsiElement}
import org.yaml.YamlElementTypes._
import org.yaml.lexer.YAMLFlexLexer
import org.yaml.parser.YAMLParser
import org.yaml.psi.impl._
import org.yaml.YAMLTokenTypes._

class YamlParserDefinition extends ParserDefinition {
  private val myCommentTokens = TokenSet.create(YAMLTokenTypes.COMMENT)

  override def createLexer(project: Project) = new YAMLFlexLexer

  override def createParser(project: Project) = new YAMLParser

  override def getFileNodeType = FILE

  override def getWhitespaceTokens: TokenSet = TokenSet.create(YAMLTokenTypes.WHITESPACE)

  override def getCommentTokens: TokenSet = myCommentTokens

  override def getStringLiteralElements: TokenSet = TokenSet.create(SCALAR_STRING, SCALAR_DSTRING, TEXT)

  override def createElement(node: ASTNode): PsiElement = {
    node.getElementType match {
      case DOCUMENT             => new YAMLDocumentImpl(node)
      case KEY_VALUE_PAIR       => new YAMLKeyValueImpl(node)
      case COMPOUND_VALUE       => new YAMLCompoundValueImpl(node)
      case SEQUENCE             => new YAMLBlockSequenceImpl(node)
      case MAPPING              => new YAMLBlockMappingImpl(node)
      case SEQUENCE_ITEM        => new YAMLSequenceItemImpl(node)
      case HASH                 => new YAMLHashImpl(node)
      case ARRAY                => new YAMLArrayImpl(node)
      case SCALAR_LIST_VALUE    => new YAMLScalarListImpl(node)
      case SCALAR_TEXT_VALUE    => new YAMLScalarTextImpl(node)
      case SCALAR_PLAIN_VALUE   => new YAMLPlainTextImpl(node)
      case SCALAR_QUOTED_STRING => new YAMLQuotedTextImpl(node)
      case _                    => new YAMLPsiElementImpl(node)
    }
  }

  override def createFile(viewProvider: FileViewProvider) = new YAMLFileImpl(viewProvider)

  override def spaceExistanceTypeBetweenTokens(left: ASTNode, right: ASTNode) = SpaceRequirements.MAY
}
