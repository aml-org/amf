package amf.graphqlfederation.internal.spec.context

import amf.core.client.scala.model.domain.extensions.PropertyShapePath
import amf.graphqlfederation.internal.spec.context.linking.LinkEvaluation
import amf.graphqlfederation.internal.spec.context.linking.fieldset.{
  PropertyShapePathLinker,
  PropertyShapePathExpression
}

case class GraphQLFederationLinkingActions() {
  implicit private val propertyShapePathLinker: PropertyShapePathLinker = PropertyShapePathLinker()

  private var propertyPaths
      : Seq[LinkEvaluation[Seq, PropertyShapePathExpression, PropertyShapePath, GraphQLFederationWebApiContext]] = Nil

  def +(a: LinkEvaluation[Seq, PropertyShapePathExpression, PropertyShapePath, GraphQLFederationWebApiContext]): Unit =
    propertyPaths = propertyPaths :+ a

  def executeAll()(implicit ctx: GraphQLFederationWebApiContext): Unit = {
    propertyPaths.foreach(_.eval())
  }
}
