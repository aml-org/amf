package amf.parser

import amf.common.AMFToken
import amf.common.AMFToken.{LibraryToken, Link}
import amf.lexer.Token

/**
  * Created by pedro.colunga on 5/19/17.
  */
case class AMFUnit(root: ASTNode[_ <: Token], url: String, `type`: AMFUnitType)

case class AMFUnitType(name: String)
object AMFUnitType {
  def apply(token: AMFToken): AMFUnitType = token match {
    case Link         => Document
    case LibraryToken => Library
    case _            => Document // Fragment(?
  }
}

object Document extends AMFUnitType("document")
object Library  extends AMFUnitType("library")
object Fragment extends AMFUnitType("fragment")
