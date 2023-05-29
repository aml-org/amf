package amf.apicontract.internal.transformation.stages

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{NamedDomainElement, RecursiveShape, Shape}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.adoption.IdAdopter
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.{Field, Type}
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.validation.CoreValidations.RecursiveShapeSpecification
import amf.shapes.client.scala.model.domain.operations._
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape, UnionShape}
import org.mulesoft.common.collections.FilterType

case class GraphQLTypeRecursionDetectionStage() extends TransformationStep() with PlatformSecrets {
  var visited: Set[String] = Set.empty
  def traverse(element: NamedDomainElement, previous: Seq[NamedDomainElement] = Nil)(implicit
      eh: AMFErrorHandler
  ): Unit = {
    if (!visited.contains(element.id)) {
      visited = visited + element.id
      visit(element, previous)
    }
  }

  private def visit(element: NamedDomainElement, previous: Seq[NamedDomainElement])(implicit
      eh: AMFErrorHandler
  ): Unit = {
    element match {
      // we do not traverse fields which are not part of the Shape hierarchy because we cannot place a RecursiveShape there
      case op: ShapeOperation =>
        op.requests.foreach { req =>
          traverse(req, previous :+ element)
        }

        op.responses.foreach { resp =>
          traverse(resp, previous :+ element)
        }

      case req: ShapeRequest =>
        req.queryParameters.foreach { param =>
          traverse(param, previous :+ element)
        }

      case resp: ShapeResponse =>
        traverse(resp.payload, previous :+ element)

      case param: ShapeParameter => visitFieldTarget(param, param.meta.Schema, previous :+ element)
      case payload: ShapePayload => visitFieldTarget(payload, payload.meta.Schema, previous :+ element)
      case u: UnionShape         => visitFieldTarget(u, u.meta.AnyOf, previous :+ element)
      case a: ArrayShape         => visitFieldTarget(a, a.meta.Items, previous :+ element)
      case p: PropertyShape      => visitFieldTarget(p, p.meta.Range, previous :+ element)
      case n: NodeShape =>
        visitFieldTarget(n, n.meta.Properties, previous :+ element)

        n.operations.foreach { op =>
          traverse(op, previous :+ element)
        }
      case _ => // nothing
    }
  }

  private def visitFieldTarget(source: NamedDomainElement, field: Field, previous: Seq[NamedDomainElement])(implicit
      eh: AMFErrorHandler
  ): Unit = {
    field.`type` match {

      case _: ShapeModel =>
        val target = source.fields(field).asInstanceOf[Shape]
        if (previous.contains(target)) {
          val r = handleRecursion(target, previous)
          source.set(field, r)
        } else {
          traverse(target, previous)
        }
      case _: Type.ArrayLike =>
        val targets = source.fields(field).asInstanceOf[Seq[Shape]]
        val newTargets = targets.map {
          case target if previous.contains(target) =>
            val r = handleRecursion(target, previous)
            r.adopted(source.id)
          case target =>
            traverse(target, previous)
            target
        }
        if (newTargets.exists(_.isInstanceOf[RecursiveShape])) source.setArrayWithoutId(field, newTargets)
    }
  }

  private def handleRecursion(target: Shape, previous: Seq[NamedDomainElement])(implicit
      eh: AMFErrorHandler
  ): RecursiveShape = {
    val all = previous :+ target
    all
      .filterType[NodeShape]
      .filter(_.isInputOnly.value())
      .foreach { inputType =>
        eh.violation(
          RecursiveShapeSpecification,
          inputType.id,
          None,
          s"Input type ${inputType.name
              .value()} cannot be part of cyclic references ${buildChainName(all)}",
          inputType.position(),
          inputType.location()
        )
      }
    RecursiveShape().withFixPoint(target.id)
  }

  private def buildChainName(all: Seq[NamedDomainElement]) = {
    all
      .filter(_.name.nonEmpty)
      .foldLeft("") { (acc, next) =>
        next match {
          case p: PropertyShape      => acc + "." + p.name.value()
          case op: ShapeOperation    => acc + "." + op.name.value()
          case param: ShapeParameter => s"$acc(${param.name.value()})"
          case n: NodeShape          => s"$acc > ${n.name.value()}"
          case _: ArrayShape         => acc
          case _: ShapeRequest       => acc
          case _: ShapePayload       => acc
          case _                     => acc
        }
      }
      .stripPrefix(" > ")
  }

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    model match {
      case d: DeclaresModel =>
        d.declares.filterType[Shape].foreach { shape =>
          traverse(shape)(errorHandler)
        }
        new IdAdopter(model, model.id).adoptFromRoot()
        model
      case _ => model
    }
  }
}
