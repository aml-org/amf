package amf.apicontract.internal.spec.common.parser

import amf.aml.internal.semantic.{AnnotationSchemaValidator, IgnoreAnnotationSchemaValidator}
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{
  AnnotationSchemaMustBeAny,
  MissingAnnotationSchema
}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import org.mulesoft.lexer.SourceLocation
import org.yaml.model.YNode

case class DeclaredAnnotationSchemaValidator(override val annotationIndex: Map[String, CustomDomainProperty])
    extends AnnotationSchemaValidator {
  override def validate(name: String, key: YNode, eh: AMFErrorHandler): Unit = {
    annotationIndex.get(name).fold(addMissingAnnotationViolation(name, key, eh))(checkAnnotationSchema(_, eh))
  }

  private def checkAnnotationSchema(annotation: CustomDomainProperty, eh: AMFErrorHandler): Unit = {
    annotation.schema match {
      case any: AnyShape if any.meta != AnyShapeModel =>
        eh.violation(AnnotationSchemaMustBeAny, annotation, AnnotationSchemaMustBeAny.message, annotation.annotations)
      case _ => // ignore
    }
  }

  private def addMissingAnnotationViolation(name: String, key: YNode, eh: AMFErrorHandler): Unit = {
    eh.violation(MissingAnnotationSchema,
                 name,
                 MissingAnnotationSchema.message,
                 SourceLocation(key.sourceName, key.range))
  }
}

trait AnnotationSchemaValidatorBuilder {
  def build(index: Map[String, CustomDomainProperty]): AnnotationSchemaValidator
}

object IgnoreAnnotationSchemaValidatorBuilder extends AnnotationSchemaValidatorBuilder {
  override def build(index: Map[String, CustomDomainProperty]): AnnotationSchemaValidator =
    IgnoreAnnotationSchemaValidator
}

object DeclaredAnnotationSchemaValidatorBuilder extends AnnotationSchemaValidatorBuilder {
  override def build(index: Map[String, CustomDomainProperty]): AnnotationSchemaValidator =
    DeclaredAnnotationSchemaValidator(index)
}
