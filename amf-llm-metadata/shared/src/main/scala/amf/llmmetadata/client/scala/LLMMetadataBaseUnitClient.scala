package amf.llmmetadata.client.scala

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.AMFParser
import amf.llmmetadata.internal.plugins.parse.schema.LLMMetadataSchemaLoader
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.shapes.client.scala.JsonSchemaBasedSpecBaseUnitClient

/** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
  * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
class LLMMetadataBaseUnitClient private[amf] (override protected val configuration: LLMMetadataConfiguration)
    extends JsonSchemaBasedSpecBaseUnitClient(configuration) {

  override protected def schemaShape: Shape = LLMMetadataSchemaLoader.schema

  override protected def profile: ProfileName = ProfileNames.LLM_METADATA
}
