package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.parser.ParserContext
import org.yaml.model.{YMapEntry, YScalar, YType}

case class LibraryLocationParser(e: YMapEntry) {

  def parse()(implicit ctx: ParserContext): Option[String] = {
    e.value.tagType match {
      case YType.Null | YType.Map | YType.Seq => None
      case _                                  => Some(e.value.as[YScalar].text) // TODO should we validate the tag?
    }
  }
}

object LibraryLocationParser {
  def apply(e: YMapEntry, ctx: ParserContext): Option[String] = new LibraryLocationParser(e).parse()(ctx)
}
