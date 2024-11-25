package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.License
import amf.apicontract.internal.metamodel.domain.LicenseModel
import amf.apicontract.internal.spec.common.parser.{SpecParserOps, WebApiContext}
import amf.apicontract.internal.spec.spec.toOas
import amf.apicontract.internal.validation.definitions.ParserSideValidations.ExclusiveLicenseIdentifierError
import amf.core.internal.parser.YMapOps
import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.common.parser.AnnotationParser
import org.yaml.model.{YMap, YNode}

/** */
object LicenseParser {
  def apply(node: YNode)(implicit ctx: WebApiContext): LicenseParser = new LicenseParser(node)(toOas(ctx))

  def parse(node: YNode)(implicit ctx: WebApiContext): License =
    LicenseParser(node).parse()
}

class LicenseParser(node: YNode)(implicit ctx: WebApiContext) extends SpecParserOps {
  def parse(): License = {
    val license = License(node)

    val map = node.as[YMap]
    map.key("url", LicenseModel.Url in license)
    map.key("name", LicenseModel.Name in license)

    if (ctx.spec == Spec.OAS31) {
      map.key("identifier", LicenseModel.Identifier in license)

      if (license.identifier.option().isDefined && license.url.option().isDefined) {
        ctx.eh.violation(
          ExclusiveLicenseIdentifierError,
          license.id,
          ExclusiveLicenseIdentifierError.message,
          license.annotations
        )
      }
    }

    AnnotationParser(license, map).parse()

    ctx.closedShape(license, map, "license")

    license
  }
}
