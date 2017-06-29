package amf.parser

import amf.common.AMFToken
import amf.common.AMFToken.{Library, Link}
import amf.lexer.Token
import amf.remote.Vendor

/**
  * raml:unit from the document model.
  */
case class AMFUnit(root: ASTNode[_ <: Token], url: String, `type`: AMFUnitType, vendor: Vendor)

class AMFUnitType(name: String)
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
