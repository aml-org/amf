package amf.llmmetadata.client.platform

import amf.llmmetadata.client.scala.{LLMMetadataBaseUnitClient => InternalLLMMetadataBaseUnitClient}
import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class LLMMetadataBaseUnitClient private[amf] (private val _internal: InternalLLMMetadataBaseUnitClient)
    extends JsonSchemaBasedSpecBaseUnitClient(_internal)
