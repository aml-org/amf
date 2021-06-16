package amf.apicontract.internal.annotations

import amf.core.client.scala.model.domain.AmfElement
import amf.core.internal.annotations.SourceLocation
import amf.shapes.internal.annotations.{ParsedJSONSchema, SchemaIsJsonSchema}

import scala.language.implicitConversions

object AnnotationSyntax {
  implicit def annotationOpsFor(e: AmfElement): AnnotationOpsFor = AnnotationOpsFor(e)
}

final case class AnnotationOpsFor(e: AmfElement) {
  def sourceLocation: Option[SourceLocation] = e.annotations.find(classOf[SourceLocation])
  def isJsonSchema: Boolean                  = e.annotations.find(classOf[SchemaIsJsonSchema]).isDefined
  def jsonSchemaText: Option[String]         = e.annotations.find(classOf[ParsedJSONSchema]).map(_.rawText)
}
