package amf.graphql.internal.spec.domain.directives

import amf.apicontract.internal.validation.shacl.graphql.GraphQLLocationHelper.toLocationIri
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape}
import amf.core.internal.metamodel.domain.extensions.DomainExtensionModel.DefinedBy
import amf.core.internal.metamodel.domain.extensions.{CustomDomainPropertyModel, PropertyShapeModel}
import amf.core.internal.parser.domain.Annotations.synthesized
import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.document._
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.{NodeShapeModel, ScalarShapeModel}
import org.mulesoft.antlrast.ast.Node

class DeprecatedDirectiveApplicationParser(override implicit val ctx: GraphQLBaseWebApiContext)
    extends RegularDirectiveApplicationParser()(ctx) {
  override def appliesTo(node: Node): Boolean = isName("deprecated", node)

  override protected def parseDefinedBy(directiveApplication: DomainExtension, node: Node): Unit = {
    findDeclaration() match {
      case Some(declaration) =>
        directiveApplication withoutId () set declaration as DefinedBy
      case None =>
        val declaration = generateDeclaration()
        ctx.declarations += declaration // is this really necessary?
        directiveApplication withoutId () set declaration as DefinedBy
    }
  }

  private def findDeclaration(): Option[CustomDomainProperty] = ctx.findAnnotation("deprecated", SearchScope.All)

  private def generateDeclaration(): CustomDomainProperty = {
    val customDomainProperty = CustomDomainProperty(synthesized())
    customDomainProperty.withName("deprecated")
    val domains: Seq[AmfScalar] = Seq("FIELD_DEFINITION", "ENUM_VALUE").flatMap(toLocationIri).map(AmfScalar(_))
    customDomainProperty set domains as CustomDomainPropertyModel.Domain
    val schema = NodeShape()
    val scalar = ScalarShape()
    scalar set AmfScalar(DataType.String) as ScalarShapeModel.DataType
    val argument = PropertyShape().withName("reason")
    argument set scalar as PropertyShapeModel.Range
    schema set Seq(argument) as NodeShapeModel.Properties
    customDomainProperty set schema as CustomDomainPropertyModel.Schema
  }
}
