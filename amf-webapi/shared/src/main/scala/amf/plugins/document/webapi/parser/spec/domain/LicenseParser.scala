package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser._
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.webapi.metamodel.LicenseModel
import amf.plugins.domain.webapi.models.License
import org.yaml.model.{YMap, YNode}

/**
  *
  */
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

    AnnotationParser(license, map)(WebApiShapeParserContextAdapter(ctx)).parse()

    ctx.closedShape(license.id, map, "license")

    license
  }
}
