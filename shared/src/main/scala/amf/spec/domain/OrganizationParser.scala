package amf.spec.domain

import amf.domain.{Annotations, Organization}
import amf.metadata.domain.OrganizationModel
import amf.parser.YMapOps
import amf.spec.common.{AnnotationParser, ValueNode}
import amf.spec.oas.OasSyntax
import amf.validation.Validation
import org.yaml.model.YMap

/**
  *
  */
case class OrganizationParser(map: YMap, currentValidation: Validation) extends OasSyntax {
  def parse(): Organization = {

    val organization = Organization(map)

    map.key("url", entry => {
      val value = ValueNode(entry.value)
      organization.set(OrganizationModel.Url, value.string(), Annotations(entry))
    })

    map.key("name", entry => {
      val value = ValueNode(entry.value)
      organization.set(OrganizationModel.Name, value.string(), Annotations(entry))
    })

    map.key("email", entry => {
      val value = ValueNode(entry.value)
      organization.set(OrganizationModel.Email, value.string(), Annotations(entry))
    })

    AnnotationParser(() => organization, map).parse()

    validateClosedShape(currentValidation, organization.id, map, "contact")

    organization
  }
}