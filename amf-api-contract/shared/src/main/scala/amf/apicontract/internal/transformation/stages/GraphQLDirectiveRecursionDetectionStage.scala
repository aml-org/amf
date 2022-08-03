package amf.apicontract.internal.transformation.stages

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel}
import amf.core.client.scala.model.domain.{DataNode, NamedDomainElement}
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, PropertyShape}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.validation.CoreValidations.RecursiveShapeSpecification
import amf.shapes.client.scala.model.domain.operations._
import amf.shapes.client.scala.model.domain.{ArrayShape, NodeShape, ScalarShape, UnionShape}
import org.mulesoft.common.collections.FilterType

case class GraphQLDirectiveRecursionDetectionStage() extends TransformationStep() with PlatformSecrets {
  def traverse(element: NamedDomainElement)(implicit
      eh: AMFErrorHandler,
      source: CustomDomainProperty
  ): Unit = {
    if (element.customDomainProperties.exists(_.definedBy == source)) {
      val message = {
        val kind = element match {
          case _: PropertyShape                      => "field"
          case _: ShapeOperation                     => "field"
          case s: ScalarShape if s.values.nonEmpty   => "enum"
          case _: ScalarShape                        => "scalar"
          case n: NodeShape if n.isAbstract.value()  => "interface"
          case n: NodeShape if n.isInputOnly.value() => "input object"
          case _: NodeShape                          => "object"
          case _: UnionShape                         => "union"
          case _: DataNode                           => "value"
          case _                                     => "type" // should be unreachable
        }
        s"Directive definition '${source.name
            .value()}' cannot reference itself indirectly thorough $kind '${element.name.value()}'"
      }
      eh.violation(RecursiveShapeSpecification, source.id, None, message, source.position(), source.location())
    }
    element match {
      case op: ShapeOperation =>
        op.requests.foreach(traverse)
        op.responses.foreach(traverse)
      case req: ShapeRequest   => req.queryParameters.foreach(traverse)
      case resp: ShapeResponse => traverse(resp.payload)
      case param: ShapeParameter =>
        traverse(param.schema)
        traverse(param.defaultValue)
      case payload: ShapePayload => traverse(payload.schema)
      case u: UnionShape         => u.anyOf.foreach(traverse)
      case s: ScalarShape =>
        s.values.foreach(traverse)
      case a: ArrayShape    => traverse(a.items)
      case p: PropertyShape => traverse(p.range)
      case n: NodeShape =>
        n.properties.foreach(traverse)
        n.operations.foreach(traverse)
      case _ => // nothing
    }

  }

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    model match {
      case d: DeclaresModel =>
        d.declares.filterType[CustomDomainProperty].foreach { cdp =>
          traverse(cdp.schema)(errorHandler, cdp)
        }
        model
      case _ => model
    }
  }
}
