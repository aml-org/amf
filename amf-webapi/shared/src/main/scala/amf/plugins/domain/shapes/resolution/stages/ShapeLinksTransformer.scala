package amf.plugins.domain.shapes.resolution.stages

import amf.core.metamodel.domain.ShapeModel
import amf.core.metamodel.domain.extensions.PropertyShapeModel
import amf.core.model.domain.{AmfArray, Shape}
import amf.core.parser.Value
import amf.core.resolution.stages.elements.resolution.ElementStageTransformer
import amf.plugins.domain.shapes.metamodel.{ArrayShapeModel, UnionShapeModel}
import amf.plugins.domain.shapes.models.{ArrayShape, NodeShape, UnionShape}

class ShapeLinksTransformer extends ElementStageTransformer[Shape] {
  override def transform(element: Shape): Option[Shape] = {
    Some(resolveLink(element, Seq.empty))
  }

  private def resolveInherits(s: Shape) = {
    val newInherits = s.inherits.map { i =>
      if (i.isLink) i.effectiveLinkTarget()
      else i
    }
    s.fields.getValueAsOption(ShapeModel.Inherits) match {
      case Some(Value(arr: AmfArray, ann)) =>
        val newInhetirs = arr.values.collect({ case s: Shape => s }).map { i =>
          if (i.isLink) i.effectiveLinkTarget()
          else i
        }
        s.set(ShapeModel.Inherits, AmfArray(newInhetirs, arr.annotations), ann)
      case _ => // ignore
    }
  }

  private def resolveLink(s: Shape, traversed: Seq[String]): Shape = {
    if (traversed.contains(s.id)) s
    else {
      val newS =
        if (s.isLink) s.effectiveLinkTarget().asInstanceOf[Shape]
        else s

      resolveInherits(newS)
      newS match {
        case a: ArrayShape =>
          a.fields.getValueAsOption(ArrayShapeModel.Items) match {
            case Some(Value(s: Shape, ann)) =>
              a.set(ArrayShapeModel.Items, resolveLink(a.items, traversed :+ newS.id), ann)
            case _ => // ignore
          }
          a
        case o: NodeShape =>
          o.properties.foreach { ps =>
            ps.fields.getValueAsOption(PropertyShapeModel.Range) match {
              case Some(Value(e: Shape, ann)) =>
                ps.set(PropertyShapeModel.Range, resolveLink(e, traversed :+ o.id), ann)
              case _ => // ignore
            }
          }
          o
        case u: UnionShape =>
          s.fields.getValueAsOption(UnionShapeModel.AnyOf) match {
            case Some(Value(arr: AmfArray, ann)) =>
              val newAnyOf = arr.values.collect({ case s: Shape => s }).map { i =>
                resolveLink(i, traversed :+ u.id)
              }
              s.set(UnionShapeModel.AnyOf, AmfArray(newAnyOf, arr.annotations), ann)
            case _ => // ignore
          }
          u
        case other => other
      }
    }
  }
}
