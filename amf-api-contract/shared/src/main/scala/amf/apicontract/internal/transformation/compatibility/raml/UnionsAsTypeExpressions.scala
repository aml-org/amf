package amf.apicontract.internal.transformation.compatibility.raml

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel}
import amf.core.client.scala.model.domain.{DomainElement, NamedDomainElement, Shape}
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.internal.utils.IdCounter
import amf.shapes.client.scala.annotations.ParsedFromTypeExpression
import amf.shapes.client.scala.domain.models.UnionShape
import amf.shapes.internal.spec.raml.emitter.RamlUnionEmitterHelper

import scala.collection.mutable

/**
  * Transforms unions 'anyOf: [A, B]' into type expressions 'A | B' so we generate valid RAML for the old parser
  * @param errorHandler
  */
class UnionsAsTypeExpressions() extends TransformationStep {

  val counter = new IdCounter()

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    try {
      val document                                        = model.asInstanceOf[DeclaresModel]
      val declarations: mutable.ListBuffer[DomainElement] = mutable.ListBuffer()
      document.declares.foreach { declarations += _ }

      model.iterator().foreach {
        case union: UnionShape =>
          val names = union.anyOf.map { shape =>
            RamlUnionEmitterHelper
              .shapeAsSingleType(shape)
              .getOrElse {
                declarations.find(_.id == shape.id) match {
                  case Some(declared: Shape) if declared.name.option().isDefined => declared.name.value()
                  case _ =>
                    ensureValidShapeName(shape, declarations)
                    declarations += shape
                    shape.name.value()
                }
              }
          }
          union.annotations += ParsedFromTypeExpression(names.mkString(" | "))
        case _ => // ignore
      }

      document.withDeclares(declarations)
    } catch {
      case _: Throwable => // ignore: we don't want this to break anything
    }
    model
  }

  private def ensureValidShapeName(shape: Shape, declarations: Seq[DomainElement]) = {
    if (shape.name.option().isEmpty || nameIsUsed(shape, declarations)) {
      val namedShape = counter.genId("GenShape")
      shape.withName(namedShape)
    }
  }

  private def nameIsUsed(shape: Shape, declarations: Seq[DomainElement]): Boolean = {
    declarations.exists {
      case n: NamedDomainElement if !n.name.isNullOrEmpty => n.name.value() == shape.name.value()
      case _                                              => false
    }
  }
}
