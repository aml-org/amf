package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.parser.{Annotations, _}
import amf.core.remote.{Oas, Raml}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common._
import amf.plugins.domain.webapi.models.security.{SecurityScheme, _}
import amf.validations.ParserSideValidations._
import org.yaml.model._

object SecuritySchemeParser {
  def apply(entry: YMapEntry, adopt: SecurityScheme => SecurityScheme)(
      implicit ctx: WebApiContext): SecuritySchemeParser = // todo factory for oas too?
    ctx.vendor match {
      case _: Raml => RamlSecuritySchemeParser(entry, adopt)(toRaml(ctx))
      case _: Oas =>
        OasSecuritySchemeParser(entry.value, scheme => {
          scheme.add(Annotations(entry))
          adopt(scheme)
        })(toOas(ctx))
      case other =>
        ctx.violation(UnexpectedVendor, "", s"Unsupported vendor $other in security scheme parsers", entry)
        RamlSecuritySchemeParser(entry, adopt)(toRaml(ctx)) // use raml as default?
    }
}

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

  def getName: String = {
    part match {
      case entry: YMapEntry => entry.key.as[YScalar].text
      case _: YMap          => "securityDefinitions"
    }
  }
}
