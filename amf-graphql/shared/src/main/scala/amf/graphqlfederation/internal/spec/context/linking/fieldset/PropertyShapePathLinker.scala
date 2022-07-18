package amf.graphqlfederation.internal.spec.context.linking.fieldset

import amf.apicontract.internal.validation.definitions.ParserSideValidations.UnmatchedFieldInFieldSet
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.domain.extensions.{PropertyShape, PropertyShapePath}
import amf.graphqlfederation.internal.spec.context.GraphQLFederationWebApiContext
import amf.graphqlfederation.internal.spec.context.linking.Linker
import amf.shapes.client.scala.model.domain.NodeShape

case class PropertyShapePathLinker()
    extends Linker[Seq, PropertyShapePathExpression, PropertyShapePath, GraphQLFederationWebApiContext] {

  override def link(source: Seq[PropertyShapePathExpression])(implicit
                                                              ctx: GraphQLFederationWebApiContext
  ): Seq[PropertyShapePath] = source.map(link)

  private def link(
      source: PropertyShapePathExpression
  )(implicit ctx: GraphQLFederationWebApiContext): PropertyShapePath = {
    var current = source.root match {
      case n: NodeShape     => n
      case p: PropertyShape => followRange(p)
    }

    val path = source.expressionComponents.map { case PropertyShapePathExpression.Component(propName, annotations) =>
      current match {
        case n: NodeShape =>
          n.properties.find(_.name.value() == propName) match {
            case Some(matchedProperty) =>
              current = followRange(matchedProperty)
              matchedProperty.link(propName)
            case None =>
              ctx.eh.violation(
                UnmatchedFieldInFieldSet,
                source.root,
                s"Cannot find property with name $propName when resolving fieldSet"
              )
              PropertyShape().withName("error")
          }
        case _ =>
          ctx.eh.violation(
            UnmatchedFieldInFieldSet,
            source.root,
            s"Cannot obtain property $propName from type ${current.name.value()} when resolving fieldSet",
            annotations
          )
          PropertyShape().withName("error")
      }
    }
    PropertyShapePath().withPath(path)
  }

  private def followRange(p: PropertyShape): Shape = {
    if (p.range.isLink) {
      p.range.effectiveLinkTarget().asInstanceOf[Shape]
    } else {
      p.range
    }
  }
}
