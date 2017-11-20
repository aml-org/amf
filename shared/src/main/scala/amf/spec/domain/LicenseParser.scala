package amf.spec.domain

import amf.domain.{Annotations, License}
import amf.metadata.domain.LicenseModel
import amf.parser.YMapOps
import amf.plugins.domain.webapi.contexts.WebApiContext
import amf.spec.ParserContext
import amf.spec.common.{AnnotationParser, ValueNode}
import org.yaml.model.YMap

/**
  *
  */
object LicenseParser {
  def apply(map: YMap)(implicit ctx: ParserContext): LicenseParser = new LicenseParser(map)(ctx.toOas)
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

    AnnotationParser(() => license, map).parse()

    ctx.closedShape(license.id, map, "license")

    license
  }
}
