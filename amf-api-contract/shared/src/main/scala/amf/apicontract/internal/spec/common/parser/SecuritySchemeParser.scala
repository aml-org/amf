package amf.apicontract.internal.spec.common.parser

import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.core.internal.parser.YScalarYRead
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model._

abstract class SecuritySchemeParser(entry: YMapEntryLike)(implicit ctx: WebApiContext) extends SpecParserOps {
  def parse(): SecurityScheme

  def getName: (String, Option[YNode]) = {
    entry.key
      .map { keyNode =>
        (keyNode.as[YScalar].text, Some(keyNode))
      }
      .getOrElse(("securityDefinitions", None))
  }
}
