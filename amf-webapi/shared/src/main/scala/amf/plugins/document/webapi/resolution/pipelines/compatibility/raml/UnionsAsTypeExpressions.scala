package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.{DomainElement, NamedDomainElement, Shape}
import amf.core.resolution.stages.ResolutionStage
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.parser.spec.declaration.emitters.raml.RamlUnionEmitterHelper
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression
import amf.plugins.domain.shapes.models.UnionShape

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Transforms unions 'anyOf: [A, B]' into type expressions 'A | B' so we generate valid RAML for the old parser
  * @param errorHandler
  */
class UnionsAsTypeExpressions()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  val counter = new IdCounter()

  override def resolve[T <: BaseUnit](model: T): T = {
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
