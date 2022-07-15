package amf.graphqlfederation.internal.spec.context.linking.fieldset

import amf.core.client.scala.model.domain.Shape

case class UnresolvedPropertyShapePath(root: Shape, path: Seq[String]) {
  def +(name: String): UnresolvedPropertyShapePath = UnresolvedPropertyShapePath(root, path :+ name)
}
