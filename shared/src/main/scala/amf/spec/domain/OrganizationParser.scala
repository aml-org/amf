package amf.spec.domain

import amf.domain.Organization
import amf.framework.parser.Annotations
import amf.metadata.domain.OrganizationModel
import amf.parser.YMapOps
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.spec.ParserContext
import amf.spec.common.{AnnotationParser, ValueNode}
import org.yaml.model.YMap

/**
  *
  */
object OrganizationParser {
  def apply(map: YMap)(implicit ctx: ParserContext): OrganizationParser = new OrganizationParser(map)(ctx.toOas)
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
