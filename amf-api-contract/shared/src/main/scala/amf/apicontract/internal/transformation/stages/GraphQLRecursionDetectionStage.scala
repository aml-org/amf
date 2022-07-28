package amf.apicontract.internal.transformation.stages

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{DomainElement, RecursiveShape, Shape}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.{Field, Type}
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape, UnionShape}
import org.mulesoft.common.collections.FilterType

case class GraphQLRecursionDetectionStage() extends TransformationStep() with PlatformSecrets {
  def traverse(element: Shape, previous: Seq[Shape] = Nil): Unit = {
    element match {
      case u: UnionShape    => traverseField(u, u.meta.AnyOf, previous :+ element)
      case a: ArrayShape    => traverseField(a, a.meta.Items, previous :+ element)
      case p: PropertyShape => traverseField(p, p.meta.Range, previous :+ element)
      case n: NodeShape =>
        traverseField(n, n.meta.Properties, previous :+ element)
        n.operations.flatMap(_.requests).flatMap(_.queryParameters).foreach { param =>
          traverseField(param, param.meta.Schema, previous :+ element)
        }
        n.operations.flatMap(_.responses).map(_.payload).foreach { payload =>
          traverseField(payload, payload.meta.Schema, previous :+ element)
        }
      case _ => // nothing
    }

  }

  private def traverseField(source: DomainElement, field: Field, previous: Seq[Shape]): Unit = {
    field.`type` match {

      case _: ShapeModel =>
        val target = source.fields(field).asInstanceOf[Shape]
        maybeRecursion(target, previous) match {
          case Some(r) => source.set(field, r)
          case None    => traverse(target, previous)
        }

      case _: Type.ArrayLike =>
        val targets = source.fields(field).asInstanceOf[Seq[Shape]]
        val newTargets = targets.map { target =>
          maybeRecursion(target, previous) match {
            case Some(r) =>
              r
            case None =>
              traverse(target, previous)
              target
          }
        }
        if (newTargets.exists(_.isInstanceOf[RecursiveShape])) source.setArray(field, newTargets)
    }
  }

  private def maybeRecursion(target: Shape, previous: Seq[Shape]): Option[RecursiveShape] = {
    if (previous.contains(target)) {
      Some(RecursiveShape().withFixPoint(target.id))
    } else {
      None
    }
  }

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    model match {
      case d: DeclaresModel =>
        d.declares.filterType[Shape].foreach { shape =>
          traverse(shape)
        }
        model
      case _ => model
    }
  }
}
