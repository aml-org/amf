package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.parser._
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common._
import amf.plugins.domain.webapi.models.security.SecurityScheme
import org.yaml.model._

abstract class SecuritySchemeParser(part: YPart, adopt: SecurityScheme => SecurityScheme)(implicit ctx: WebApiContext)
    extends SpecParserOps {
  def parse(): SecurityScheme
  def getNode: YNode = {
    part match {
      case entry: YMapEntry => entry.value
      case map: YMap        => map
      case node: YNode      => node
    }
  }

  def getName: (String, Option[YNode]) = {
    part match {
      case entry: YMapEntry => (entry.key.as[YScalar].text, Some(entry.key))
      case _: YMap          => ("securityDefinitions", None)
    }
  }
}
