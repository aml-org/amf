package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.domain.webapi.metamodel.LicenseModel
import amf.plugins.domain.webapi.models.License
import org.yaml.model.YMap

/**
  *
  */
object LicenseParser {
  def apply(map: YMap)(implicit ctx: WebApiContext): LicenseParser = new LicenseParser(map)(toOas(ctx))
}

class LicenseParser(map: YMap)(implicit ctx: WebApiContext) {
  def parse(): License = {
    val license = License(map)

    map.key("url", entry => {
      val value = ValueNode(entry.value)
      license.set(LicenseModel.Url, value.string(), Annotations(entry))
    })

    map.key("name", entry => {
      val value = ValueNode(entry.value)
      license.set(LicenseModel.Name, value.string(), Annotations(entry))
    })

    AnnotationParser(license, map).parse()

    ctx.closedShape(license.id, map, "license")

    license
  }
}
