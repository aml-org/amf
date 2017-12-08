package amf.core.validation

import amf.core.annotations.LexicalInformation
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{AmfArray, DomainElement}
import amf.core.validation.core.ValidationResult

case class AMFValidationResult(message: String,
                               level: String,
                               targetNode: String,
                               targetProperty: Option[String],
                               validationId: String,
                               position: Option[LexicalInformation],
                               source: Any) {
  override def toString: String = {
    var str = s"\n- Source: $validationId\n"
    str += s"  Message: $message\n"
    str += s"  Level: $level\n"
    str += s"  Target: $targetNode\n"
    str += s"  Property: ${targetProperty.getOrElse("")}\n"
    str += s"  Position: $position\n"
    str
  }
}

object AMFValidationResult {

  def fromSHACLValidation(model: BaseUnit,
                          message: String,
                          level: String,
                          validation: ValidationResult): AMFValidationResult = {
    model.findById(validation.focusNode) match {
      case None       => throw new Exception(s"Cannot find node with validation error ${validation.focusNode}")
      case Some(node) =>
        val position = findPosition(node, validation)
        AMFValidationResult(
          message = message,
          level = level,
          targetNode = node.id,
          targetProperty = Option(validation.path),
          validation.sourceShape,
          position = position,
          source = validation
        )
    }
  }

  def withShapeId(shapeId: String, validation: AMFValidationResult): AMFValidationResult =
    AMFValidationResult(validation.message,
                        validation.level,
                        validation.targetNode,
                        validation.targetProperty,
                        shapeId,
                        validation.position,
                        validation.source)

  def findPosition(node: DomainElement, validation: ValidationResult): Option[LexicalInformation] = {
    if (Option(validation.path).isDefined && validation.path != "") {
      val foundPosition = node.fields.fields().find(f => f.field.value.iri() == validation.path) match {
        case Some(f) =>
          f.element.annotations.find(classOf[LexicalInformation]).orElse {
            f.value.annotations.find(classOf[LexicalInformation]).orElse {
              f.element match {
                case arr: AmfArray if arr.values.nonEmpty =>
                  arr.values.head.annotations.find(classOf[LexicalInformation])
                case _ => node.annotations.find(classOf[LexicalInformation])
              }
            }
          }
        case _ => node.annotations.find(classOf[LexicalInformation])
      }
      foundPosition
    } else {
      node.annotations.find(classOf[LexicalInformation])
    }
  }

}
