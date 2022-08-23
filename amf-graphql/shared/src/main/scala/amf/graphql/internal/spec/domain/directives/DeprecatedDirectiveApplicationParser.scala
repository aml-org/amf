package amf.graphql.internal.spec.domain.directives

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape}
import amf.core.internal.metamodel.domain.extensions.DomainExtensionModel.DefinedBy
import amf.core.internal.parser.domain.Annotations.inferred
import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.Locations.locationToDomain
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
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
    val customDomainProperty = CustomDomainProperty(inferred())
    customDomainProperty.withName("deprecated", inferred())
    customDomainProperty.withDomain(Seq("FIELD_DEFINITION", "ENUM_VALUE").flatMap(locationToDomain))
    val schema = NodeShape()
    val scalar = ScalarShape(inferred()).withDataType(DataType.String)
    val argument = PropertyShape()
      .withName("reason")
      .withRange(scalar)
    schema.withProperties(Seq(argument))
    customDomainProperty.withSchema(schema)
  }
}
