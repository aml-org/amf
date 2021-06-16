package amf.shapes.internal.domain.resolution

import amf.core.client.scala.model.domain.{AmfArray, Shape}
import amf.core.client.scala.transform.stages.elements.resolution.ElementStageTransformer
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.Value
import amf.shapes.internal.domain.metamodel.{ArrayShapeModel, UnionShapeModel}
import amf.shapes.client.scala.model.domain.UnionShape
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape, UnionShape}

abstract class ShapeLinksTransformer extends ElementStageTransformer[Shape] {
  override def transform(element: Shape): Option[Shape] = {
    Some(resolveLink(element, Seq.empty))
  }

  protected def applies(element: Shape): Boolean

  private def resolveInherits(s: Shape, traversed: Seq[String]) = {
    s.fields.getValueAsOption(ShapeModel.Inherits) match {
      case Some(Value(arr: AmfArray, ann)) =>
        val newInhetirs: Seq[Shape] = arr.values.collect({ case s: Shape => s }).map { i =>
          resolveLink(i, s.id +: traversed)
        }
        s.set(ShapeModel.Inherits, AmfArray(newInhetirs, arr.annotations), ann)
      case _ => // ignore
    }
  }

  private def resolveLink(s: Shape, traversed: Seq[String]): Shape = {
    if (traversed.contains(s.id)) s
    else if (applies(s)) {
      val newS =
        if (s.isLink) s.effectiveLinkTarget().asInstanceOf[Shape]
        else s

      resolveInherits(newS, traversed)
      newS match {
        case a: ArrayShape =>
          a.fields.getValueAsOption(ArrayShapeModel.Items) match {
            case Some(Value(s: Shape, ann)) =>
              a.set(ArrayShapeModel.Items, resolveLink(a.items, newS.id +: traversed), ann)
            case _ => // ignore
          }
          a
        case o: NodeShape =>
          o.properties.foreach { ps =>
            ps.fields.getValueAsOption(PropertyShapeModel.Range) match {
              case Some(Value(e: Shape, ann)) =>
                ps.set(PropertyShapeModel.Range, resolveLink(e, o.id +: traversed), ann)
              case _ => // ignore
            }
          }
          o
        case u: UnionShape =>
          s.fields.getValueAsOption(UnionShapeModel.AnyOf) match {
            case Some(Value(arr: AmfArray, ann)) =>
              val newAnyOf = arr.values.collect({ case s: Shape => s }).map { i =>
                resolveLink(i, u.id +: traversed)
              }
              s.set(UnionShapeModel.AnyOf, AmfArray(newAnyOf, arr.annotations), ann)
            case _ => // ignore
          }
          u
        case other => other
      }
    } else s
  }
}
