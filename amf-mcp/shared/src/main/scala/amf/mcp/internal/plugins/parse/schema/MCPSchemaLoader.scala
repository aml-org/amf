package amf.mcp.internal.plugins.parse.schema

import amf.core.client.scala.AMFResult
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.validation.AMFValidationResult
import amf.mcp.internal.plugins.parse.SyncJsonSchemaCompiler
import amf.shapes.client.scala.model.document.JsonSchemaDocument

object MCPSchemaLoader {
  val parsed: AMFResult                = SyncJsonSchemaCompiler.compile(MCPSchema.schema)
  val doc: JsonSchemaDocument          = parsed.baseUnit.asInstanceOf[JsonSchemaDocument]
  def schema: Shape                    = doc.encodes
  def errors: Seq[AMFValidationResult] = parsed.results
}
