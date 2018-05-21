package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.parser.ParserContext
import org.yaml.model.{YMapEntry, YScalar, YType}

case class LibraryLocationParser(e: YMapEntry, ctx: ParserContext) {

  def parse(): Option[String] = {
    e.value.tagType match {
      case YType.Include                      => Some(e.value.as[YScalar].text)
      case YType.Null | YType.Map | YType.Seq => None
      case _                                  => Some(e.value)
    }
  }
}

object LibraryLocationParser {
  def apply(e: YMapEntry, ctx: ParserContext): Option[String] = new LibraryLocationParser(e, ctx).parse()
}
