package amf.raml

import amf.common.AMFToken
import amf.common.AMFToken._
import amf.parser.{BaseAMFParser, YeastASTBuilder}

class RamlParser(b: YeastASTBuilder) extends BaseAMFParser(b) {

  private def link(linkToken: AMFToken = Link): Boolean = {
    beginTree()
    if (linkToken != LibraryToken) discard()
    consume()
    endTree(linkToken)

    true
  }

  def parseLibrary(): Unit = link(LibraryToken)

  private def library(): Boolean = parseList(MapToken, StartMap, Comma, EndMap, () => entry(parseLibrary))

  override protected def parseValue(): Unit = current match {
    case Tag if currentText equals "!include" => link()
    case _                                    => super.parseValue()
  }

  override protected def parseEntry(): Boolean = currentText match {
    case "uses" => entry(library)
    case _      => super.parseEntry()
  }
}
