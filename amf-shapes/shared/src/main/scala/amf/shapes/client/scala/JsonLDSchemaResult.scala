package amf.shapes.client.scala

import amf.core.client.scala.AMFParseResult
import amf.core.client.scala.validation.AMFValidationResult
import amf.shapes.client.scala.model.document.{JsonLDInstanceDocument, JsonSchemaDocument}

class JsonLDSchemaResult(val jsonDocument: JsonSchemaDocument, override val results: Seq[AMFValidationResult])
    extends AMFParseResult(jsonDocument, results)

class JsonLDInstanceResult(val instance: JsonLDInstanceDocument, override val results: Seq[AMFValidationResult])
    extends AMFParseResult(instance, results)
