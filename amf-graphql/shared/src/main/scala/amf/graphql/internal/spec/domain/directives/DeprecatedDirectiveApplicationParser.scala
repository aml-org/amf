package amf.graphql.internal.spec.domain.directives

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape}
import amf.core.internal.metamodel.domain.extensions.DomainExtensionModel.DefinedBy
import amf.core.internal.parser.domain.Annotations.{inferred, synthesized, virtual}
import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.apicontract.internal.validation.shacl.graphql.GraphQLLocationHelper.toLocationIri
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.metamodel.domain.extensions.{CustomDomainPropertyModel, PropertyShapeModel}
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.{NodeShapeModel, ScalarShapeModel}
import org.mulesoft.antlrast.ast.Node

class DeprecatedDirectiveApplicationParser(override implicit val ctx: GraphQLBaseWebApiContext)
    extends RegularDirectiveApplicationParser()(ctx) {
  override def appliesTo(node: Node): Boolean = isName("deprecated", node)

  override protected def parseDefinedBy(directiveApplication: DomainExtension, node: Node): Unit = {
    findDeclaration() match {
      case Some(declaration) =>
        directiveApplication.setWithoutId(DefinedBy, declaration, inferred())
      case None =>
        val declaration = generateDeclaration()
        ctx.declarations += declaration // is this really necessary?
        directiveApplication.setWithoutId(DefinedBy, declaration, inferred())
    }
  }

  private def findDeclaration(): Option[CustomDomainProperty] = ctx.findAnnotation("deprecated", SearchScope.All)

  private def generateDeclaration(): CustomDomainProperty = {
    val customDomainProperty = CustomDomainProperty(virtual())
    customDomainProperty.withName("deprecated", synthesized())
    // TODO change with to a set
    customDomainProperty.withDomain(Seq("FIELD_DEFINITION", "ENUM_VALUE").flatMap(toLocationIri))
    val schema = NodeShape(virtual())
    val scalar = ScalarShape(virtual()).set(ScalarShapeModel.DataType, AmfScalar(DataType.String, virtual()))
    val argument = PropertyShape(virtual())
      .withName("reason", synthesized())
      .set(PropertyShapeModel.Range, scalar, synthesized())
    schema.set(NodeShapeModel.Properties, AmfArray(Seq(argument)), synthesized())
    customDomainProperty.set(CustomDomainPropertyModel.Schema, schema, synthesized())
  }
}
