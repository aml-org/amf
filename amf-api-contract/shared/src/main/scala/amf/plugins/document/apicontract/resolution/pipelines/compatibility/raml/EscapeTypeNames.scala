package amf.plugins.document.apicontract.resolution.pipelines.compatibility.raml
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.{Linkable, Shape}
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.internal.utils.IdCounter
import amf.plugins.document.apicontract.parser.{RamlTypeDefMatcher, TypeName}
import amf.plugins.domain.apicontract.models.api.Api

import scala.collection.mutable

class EscapeTypeNames() extends TransformationStep {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = model match {
    case d: Document if d.encodes.isInstanceOf[Api] =>
      try {
        val replacedNames: mutable.Map[String, String] = mutable.Map.empty

        val expressionLikeNameCounter = new IdCounter()
        d.iterator().foreach {
          case shape: Shape =>
            shape.name.option().map { name =>
              RamlTypeDefMatcher.matchWellKnownType(TypeName(name), default = UndefinedType) match {
                case UndefinedType if name.contains(".") =>
                  val newName = name.replace(".", "_")
                  replacedNames(name) = newName
                  shape.withName(newName)
                case XMLSchemaType | JSONSchemaType | UndefinedType =>
                // Do nothing
                case TypeExpressionType =>
                  val newName = expressionLikeNameCounter.genId("type")
                  replacedNames(name) = newName
                  shape.withName(newName)
                case _ =>
                  val newName = s"${name}_"
                  replacedNames(name) = newName
                  shape.withName(newName)
              }
            }
          case _ => // Nothing
        }

        // Update links
        model.iterator().foreach {
          case l: Linkable if l.isLink =>
            l.linkLabel
              .option()
              // TODO: need to check that the to-be replaced link label is a link to the same shape that changed its name?
              .flatMap(replacedNames.get) match {
              case Some(newLabel) => l.withLinkLabel(newLabel)
              case _              => // Nothing
            }
          case _ => // Nothing
        }
      } catch {
        case _: Throwable => // ignore: we don't want this to break anything
      }
      model
    case _ => model
  }
}
