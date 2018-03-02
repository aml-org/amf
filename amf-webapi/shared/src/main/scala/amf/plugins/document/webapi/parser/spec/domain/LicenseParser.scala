package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.domain.webapi.metamodel.LicenseModel
import amf.plugins.domain.webapi.models.License
import org.yaml.model.{YMap, YNode}

/**
  *
  */
object LicenseParser {
  def apply(map: YMap)(implicit ctx: WebApiContext): LicenseParser = new LicenseParser(map)(toOas(ctx))

  def parse(node: YNode)(implicit ctx: WebApiContext): License =
    LicenseParser(node.as[YMap]).parse()
}

class LicenseParser(map: YMap)(implicit ctx: WebApiContext) extends SpecParserOps {
  def parse(): License = {
    val license = License(map)

    map.key("url", LicenseModel.Url in license)
    map.key("name", LicenseModel.Name in license)

    AnnotationParser(license, map).parse()

    ctx.closedShape(license.id, map, "license")

    license
  }
}
