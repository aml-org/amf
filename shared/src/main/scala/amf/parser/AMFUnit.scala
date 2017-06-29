package amf.parser

import amf.common.AMFToken
import amf.common.AMFToken.{Library, Link}
import amf.lexer.Token

/**
  * Created by pedro.colunga on 5/19/17.
  */
case class AMFUnit(root: ASTNode[_ <: Token], url: String, `type`: AMFUnitType)

case class AMFUnitType(name: String)

object AMFUnitType {

  def apply(token: AMFToken): AMFUnitType = token match {
    case Link    => Document
    case Library => Module
    case _       => Document // Fragment(?
  }
}

object Document extends AMFUnitType("document")

object Module extends AMFUnitType("module")

object Fragment extends AMFUnitType("fragment")
