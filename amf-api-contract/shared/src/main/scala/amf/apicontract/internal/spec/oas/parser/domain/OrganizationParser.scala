package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.Organization
import amf.apicontract.internal.metamodel.domain.OrganizationModel
import amf.apicontract.internal.spec.common.parser.{SpecParserOps, WebApiContext, WebApiShapeParserContextAdapter}
import amf.apicontract.internal.spec.spec.toOas
import amf.core.internal.parser.YMapOps
import amf.shapes.internal.spec.common.parser.AnnotationParser
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

    ctx.closedShape(organization, map, "contact")

    organization
  }
}

class RamlCompatibleOrganizationParser(node: YNode)(implicit ctx: WebApiContext) extends SpecParserOps {}
