package amf.raml

import amf.common.AMFToken
import amf.common.AMFToken._
import amf.parser.{BaseAMFParser, YeastASTBuilder}
import amf.remote.{Raml, Vendor}

/** [[amf.remote.Raml]] parser */
class RamlParser(b: YeastASTBuilder) extends BaseAMFParser(b) {

  private def link(token: AMFToken): Boolean = {
    beginTree()
    discard(Tag)
    consume()
    endTree(token)
    true
  }

  private def library(): Unit = link(Library)

  private def libraries() = parseList(MapToken, StartMap, Comma, EndMap, () => entry(() => library()))

  override protected def parseValue(): Unit = current match {
    case Tag if currentText equals "!include" => link(Link)
    case _                                    => super.parseValue()
  }

  override protected def parseEntry(): Boolean = currentText match {
    case "uses" => entry(() => libraries())
    case _      => super.parseEntry()
  }

  override def vendor(): Vendor = Raml
}
