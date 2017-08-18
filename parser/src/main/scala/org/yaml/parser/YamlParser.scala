package org.yaml.parser

import java.io.File

import org.mulesoft.lexer.TokenData
import org.yaml.lexer.YamlToken._
import org.yaml.lexer.{YamlLexer, YamlToken, YeastToken}
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by emilio.gabeiras on 8/16/17.
  */
class YamlParser(val lexer: YamlLexer) {
  private var stack    = List(new Builder)
  private def current  = stack.head
  private val elements = new ArrayBuffer[YamlPart]
  private val aliases  = mutable.Map.empty[String, YNode]

  class Builder {
    var metaText = ""

    val tokens      = new ArrayBuffer[YeastToken]
    val parts       = new ArrayBuffer[YamlPart]
    val textBuilder = new StringBuilder
  }

  def parse(): IndexedSeq[YamlPart] = {
    while (lexer.token != EndStream) {
      process(lexer.tokenData)
      lexer.advance()
    }
    elements.toArray[YamlPart]
  }

  def push(): Unit = {
    val nc = createNonContent()
    if (nc != null)
      current.parts += nc
    stack = new Builder :: stack
  }

  private def pop(part: YamlPart): Unit = {
    stack = stack.tail
    current.parts += part
  }

  private def getTokens: IndexedSeq[YeastToken] = {
    val tks = current.tokens
    if (tks.isEmpty) IndexedSeq.empty
    else {
      val r = tks.toArray[YeastToken]
      tks.clear()
      r
    }
  }
  private def getText: String = {
    val tb = current.textBuilder
    if (tb.isEmpty) ""
    else {
      val r = tb.toString()
      tb.clear()
      r
    }
  }
  private def getParts: IndexedSeq[YamlPart] = {
    val ps = current.parts
    if (ps.isEmpty) IndexedSeq.empty
    else {
      val r = ps.toArray[YamlPart]
      ps.clear()
      r
    }
  }

  private def createNonContent() = {
    val tks = getTokens
    if (tks.isEmpty) null else new YNonContent(tks)
  }

  private def process(td: TokenData[YamlToken]): Unit = {
    td.token match {
      case BeginDocument =>
        val nc = createNonContent()
        if (nc != null) elements += nc
      case EndDocument =>
        elements += new YDocument(getParts)
        current.parts.clear()
      case BeginNode | BeginSequence | BeginScalar | BeginMapping | BeginPair | BeginProperties | BeginAlias |
          BeginAnchor =>
        push()
      case EndSequence =>
        pop(new YSequence(getParts))
      case EndNode =>
        pop(YNode(getParts, aliases))
      case EndScalar =>
        pop(new YScalar(getText, getTokens))
      case EndMapping =>
        pop(new YMap(getParts))
      case EndPair =>
        pop(YMapEntry(getParts))
      case EndAlias =>
        pop(new YAlias(current.metaText, getTokens))
      case EndAnchor =>
        pop(new YAnchor(current.metaText, getTokens))
      case EndProperties =>
        pop(YProperties(getParts))
      case MetaText =>
        current.metaText = lexer.tokenString
      case Text =>
        current.textBuilder.append(lexer.tokenText)
      case LineFold =>
        current.textBuilder.append(' ')
      case LineFeed =>
        current.textBuilder.append('\n')
      case _ =>
        addToken(td)
    }
  }

  private def addToken(td: TokenData[YamlToken]) = {
    current.tokens += YeastToken(td.token, td.start, td.end)
  }
}
object YamlParser {
  def apply(lexer: YamlLexer): YamlParser = new YamlParser(lexer)
  def apply(file: File): YamlParser       = new YamlParser(YamlLexer(file))
  def apply(s: String): YamlParser        = new YamlParser(YamlLexer(s))
}
