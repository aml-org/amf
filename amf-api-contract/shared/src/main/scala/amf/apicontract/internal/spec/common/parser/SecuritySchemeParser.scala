package amf.apicontract.internal.spec.common.parser

import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.core.internal.parser.YScalarYRead
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
