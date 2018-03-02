package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.webapi.metamodel.OrganizationModel
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.domain.webapi.models.Organization
import org.yaml.model.{YMap, YMapEntry, YNode}

/**
  *
  */
object OrganizationParser {
  def apply(map: YMap)(implicit ctx: WebApiContext): OrganizationParser = new OrganizationParser(map)(toOas(ctx))

  def parse(node: YNode)(implicit ctx: WebApiContext): Organization =
    OrganizationParser(node.as[YMap]).parse()
}

class OrganizationParser(map: YMap)(implicit ctx: WebApiContext) extends SpecParserOps {
  def parse(): Organization = {

    val organization = Organization(map)

    map.key("url", OrganizationModel.Url in organization)
    map.key("name", OrganizationModel.Name in organization)
    map.key("email", OrganizationModel.Email in organization)

    AnnotationParser(organization, map).parse()

    ctx.closedShape(organization.id, map, "contact")

    organization
  }
}
