package amf.parser

import amf.common.AMFToken
import amf.common.AMFToken.{LibraryToken, Link}
import amf.lexer.Token

/**
  * Created by pedro.colunga on 5/19/17.
  */
case class Container(root: ASTNode[_ <: Token], url: String, `type`: ContainerType)

case class ContainerType(name: String)
object ContainerType {
  def apply(token: AMFToken): ContainerType = token match {
    case Link         => Document
    case LibraryToken => Library
    case _            => Document // Fragment(?
  }
}

object Document extends ContainerType("document")
object Library  extends ContainerType("library")
object Fragment extends ContainerType("fragment")
