package amf.plugins.document.apicontract.parser.spec.domain

import amf.core.internal.parser.YMapOps
import amf.shapes.internal.spec.contexts.WebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec._
import amf.plugins.document.apicontract.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.domain.apicontract.metamodel.OrganizationModel
import amf.plugins.domain.apicontract.models.Organization
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

    AnnotationParser(organization, map)(WebApiShapeParserContextAdapter(ctx)).parse()

    ctx.closedShape(organization.id, map, "contact")

    organization
  }
}

class RamlCompatibleOrganizationParser(node: YNode)(implicit ctx: WebApiContext) extends SpecParserOps {}
