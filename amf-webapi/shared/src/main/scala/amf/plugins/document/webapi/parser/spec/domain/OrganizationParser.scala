package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser._
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.webapi.metamodel.OrganizationModel
import amf.plugins.domain.webapi.models.Organization
import org.yaml.model.{YMap, YNode}

/**
  *
  */
object OrganizationParser {
  def apply(node: YNode)(implicit ctx: WebApiContext): OrganizationParser = new OrganizationParser(node)(toOas(ctx))

  def parse(node: YNode)(implicit ctx: WebApiContext): Organization =
    OrganizationParser(node).parse()
}

class OrganizationParser(node: YNode)(implicit ctx: WebApiContext) extends SpecParserOps {
  def parse(): Organization = {

    val organization = Organization(node)
    val map          = node.as[YMap]

    map.key("url", OrganizationModel.Url in organization)
    map.key("name", OrganizationModel.Name in organization)
    map.key("email", OrganizationModel.Email in organization)

    AnnotationParser(organization, map).parse()

    ctx.closedShape(organization.id, map, "contact")

    organization
  }
}

class RamlCompatibleOrganizationParser(node: YNode)(implicit ctx: WebApiContext) extends SpecParserOps {}
