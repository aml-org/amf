package amf.graphqlfederation.internal.spec.context

import amf.core.client.scala.model.domain.extensions.PropertyShapePath
import amf.graphqlfederation.internal.spec.context.linking.LinkAction
import amf.graphqlfederation.internal.spec.context.linking.fieldset.{
  PropertyShapePathLinker,
  UnresolvedPropertyShapePath
}

case class GraphQLFederationLinkingActions() {
  implicit private val propertyShapePathLinker: PropertyShapePathLinker = PropertyShapePathLinker()

  private var propertyPaths
      : Seq[LinkAction[Seq, UnresolvedPropertyShapePath, PropertyShapePath, GraphQLFederationWebApiContext]] = Nil

  def +(a: LinkAction[Seq, UnresolvedPropertyShapePath, PropertyShapePath, GraphQLFederationWebApiContext]): Unit =
    propertyPaths = propertyPaths :+ a

  def executeAll()(implicit ctx: GraphQLFederationWebApiContext): Unit = {
    propertyPaths.foreach(_.execute())
  }
}
