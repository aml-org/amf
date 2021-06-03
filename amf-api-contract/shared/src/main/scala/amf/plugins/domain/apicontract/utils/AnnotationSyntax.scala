package amf.plugins.domain.apicontract.utils
import amf.core.annotations.SourceLocation
import amf.core.model.domain.AmfElement
import amf.plugins.document.apicontract.annotations.{ParsedJSONSchema, SchemaIsJsonSchema}
import scala.language.implicitConversions

object AnnotationSyntax {
  implicit def annotationOpsFor(e: AmfElement): AnnotationOpsFor = AnnotationOpsFor(e)
}

final case class AnnotationOpsFor(e: AmfElement) {
  def sourceLocation: Option[SourceLocation] = e.annotations.find(classOf[SourceLocation])
  def isJsonSchema: Boolean                  = e.annotations.find(classOf[SchemaIsJsonSchema]).isDefined
  def jsonSchemaText: Option[String]         = e.annotations.find(classOf[ParsedJSONSchema]).map(_.rawText)
}
