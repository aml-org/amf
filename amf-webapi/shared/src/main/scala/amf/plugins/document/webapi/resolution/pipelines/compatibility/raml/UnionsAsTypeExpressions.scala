package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression
import amf.plugins.domain.shapes.models.UnionShape

import scala.collection.mutable

/**
  * Transforms unions 'anyOf: [A, B]' into type expressions 'A | B' so we generate valid RAML for the old parser
  * @param errorHandler
  */
class UnionsAsTypeExpressions()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {
  override def resolve[T <: BaseUnit](model: T): T = {
    try {
      var document                                          = model.asInstanceOf[DeclaresModel]
      var collectedDecls: mutable.ListBuffer[DomainElement] = mutable.ListBuffer()
      document.declares.foreach { decl =>
        collectedDecls += decl
      }

      var counter = 1

      model.iterator().foreach {
        case union: UnionShape =>
          val names = union.anyOf.map { shape =>
            collectedDecls.find(_.id == shape.id) match {
              case Some(declared: Shape) if declared.name.option().isDefined =>
                declared.name.value()
              case _ =>
                if (shape.name.option().isEmpty) {
                  val namedShape = s"GenShape$counter"
                  counter += 1
                  shape.withName(namedShape)
                }
                collectedDecls += shape
                shape.name
            }
          }
          union.annotations += ParsedFromTypeExpression(names.mkString(" | "))
        case _ => // ignore
      }

      document.withDeclares(collectedDecls)
    } catch {
      case _: Throwable => // ignore: we don't want this to break anything
    }
    model
  }
}
