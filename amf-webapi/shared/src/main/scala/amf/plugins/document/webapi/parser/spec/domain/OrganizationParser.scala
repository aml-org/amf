package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, ValueNode}
import amf.plugins.domain.webapi.metamodel.OrganizationModel
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.domain.webapi.models.Organization
import org.yaml.model.YMap

/**
  *
  */
object OrganizationParser {
  def apply(map: YMap)(implicit ctx: WebApiContext): OrganizationParser = new OrganizationParser(map)(toOas(ctx))
}

class OrganizationParser(map: YMap)(implicit ctx: WebApiContext) {
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

    ctx.closedShape(organization.id, map, "contact")

    organization
  }
}
