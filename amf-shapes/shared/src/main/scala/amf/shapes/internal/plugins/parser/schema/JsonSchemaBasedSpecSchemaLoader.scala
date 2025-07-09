package amf.shapes.internal.plugins.parser.schema

import amf.core.client.scala.AMFResult
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.AMFValidationResult
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.helper.SyncJsonSchemaCompiler

abstract class JsonSchemaBasedSpecSchemaLoader {

  protected def schemaProvider: JsonSchemaBasedSpecSchema

  lazy val parsed: AMFResult           = SyncJsonSchemaCompiler.compile(schemaProvider.schema)
  lazy val doc: JsonSchemaDocument     = parsed.baseUnit.asInstanceOf[JsonSchemaDocument]
  def schema: Shape                    = doc.encodes
  def errors: Seq[AMFValidationResult] = parsed.results
}
