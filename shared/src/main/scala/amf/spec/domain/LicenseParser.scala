package amf.spec.domain

import amf.domain.{Annotations, License}
import amf.metadata.domain.LicenseModel
import amf.parser.YMapOps
import amf.spec.common.{AnnotationParser, ValueNode}
import amf.spec.oas.OasSyntax
import amf.validation.Validation
import org.yaml.model.YMap

/**
  *
  */
case class LicenseParser(map: YMap, currentValidation: Validation) extends OasSyntax {
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

    validateClosedShape(currentValidation, license.id, map, "license")

    license
  }
}