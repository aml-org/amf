package amf.apicontract.internal.transformation.stages

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{NamedDomainElement, RecursiveShape, Shape}
import amf.core.internal.adoption.IdAdopter
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.{Field, Type}
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.validation.CoreValidations.RecursiveShapeSpecification
import amf.shapes.client.scala.model.domain.operations._
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape, UnionShape}
import org.mulesoft.common.collections.FilterType

case class GraphQLTypeRecursionDetectionStage() extends TransformationStep() with PlatformSecrets {
  def traverse(element: NamedDomainElement, previous: Seq[NamedDomainElement] = Nil)(implicit
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

      case param: ShapeParameter => traverseField(param, param.meta.Schema, previous :+ element)
      case payload: ShapePayload => traverseField(payload, payload.meta.Schema, previous :+ element)
      case u: UnionShape         => traverseField(u, u.meta.AnyOf, previous :+ element)
      case a: ArrayShape         => traverseField(a, a.meta.Items, previous :+ element)
      case p: PropertyShape      => traverseField(p, p.meta.Range, previous :+ element)
      case n: NodeShape =>
        traverseField(n, n.meta.Properties, previous :+ element)

        n.operations.foreach { op =>
          traverse(op, previous :+ element)
        }
      case _ => // nothing
    }

  }

  private def traverseField(source: NamedDomainElement, field: Field, previous: Seq[NamedDomainElement])(implicit
      eh: AMFErrorHandler
  ): Unit = {
    field.`type` match {

      case _: ShapeModel =>
        val target = source.fields(field).asInstanceOf[Shape]
        handleRecursion(target, previous) match {
          case Some(r) => source.set(field, r)
          case None    => traverse(target, previous)
        }

      case _: Type.ArrayLike =>
        val targets = source.fields(field).asInstanceOf[Seq[Shape]]
        val newTargets = targets.map { target =>
          handleRecursion(target, previous) match {
            case Some(r) =>
              r.adopted(source.id)
              r
            case None =>
              traverse(target, previous)
              target
          }
        }
        if (newTargets.exists(_.isInstanceOf[RecursiveShape])) source.setArrayWithoutId(field, newTargets)
    }
  }

  private def handleRecursion(target: Shape, previous: Seq[NamedDomainElement])(implicit
      eh: AMFErrorHandler
  ): Option[RecursiveShape] = {
    if (previous.contains(target)) {
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
      Some(RecursiveShape().withFixPoint(target.id))
    } else {
      None
    }
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
