package amf.raml

import amf.common.AMFToken._
import amf.parser.{BaseAMFParser, YeastASTBuilder}

class RamlParser(b: YeastASTBuilder) extends BaseAMFParser(b) {

  private def link(): Boolean = {
    beginTree()
    consume()
    endTree(Link)
    true
  }

  override protected def parseValue(): Boolean = currentText match {
    case text if text.startsWith("!include ") => link()
    case _                                    => super.parseValue()
  }
}
