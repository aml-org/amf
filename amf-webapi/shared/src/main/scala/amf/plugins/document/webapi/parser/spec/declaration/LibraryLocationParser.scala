package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.errorhandling.AMFErrorHandler
import org.yaml.model.{YMapEntry, YScalar, YType}

object LibraryLocationParser {
  def apply(e: YMapEntry)(implicit errorHandler: AMFErrorHandler): Option[String] = {
    e.value.tagType match {
      case YType.Null | YType.Map | YType.Seq => None
      case _                                  => Some(e.value.as[YScalar].text) // TODO should we validate the tag?
    }
  }
}
