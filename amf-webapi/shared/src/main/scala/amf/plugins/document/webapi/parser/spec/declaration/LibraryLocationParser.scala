package amf.plugins.document.webapi.parser.spec.declaration

import org.yaml.model.{YMapEntry, YScalar, YType}

case class LibraryLocationParser(e: YMapEntry) {

  def parse(): Option[String] = {
    e.value.tagType match {
      case YType.Include => Some(e.value.as[YScalar].text)
      case YType.Null =>
        None
      case _ => Some(e.value)
    }
  }
}

object LibraryLocationParser {
  def apply(e: YMapEntry): Option[String] = new LibraryLocationParser(e).parse()
}
