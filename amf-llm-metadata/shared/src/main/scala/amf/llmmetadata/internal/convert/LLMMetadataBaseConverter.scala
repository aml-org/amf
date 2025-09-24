package amf.llmmetadata.internal.convert

import amf.core.internal.convert.BidirectionalMatcher
import amf.llmmetadata.client.platform.{LLMMetadataConfiguration => ClientLLMMetadataConfiguration}
import amf.llmmetadata.client.scala.LLMMetadataConfiguration
import amf.shapes.internal.convert.ShapesBaseConverter

trait LLMMetadataBaseConverter
    extends ShapesBaseConverter
    with LLMMetadataConfigurationConverter

trait LLMMetadataConfigurationConverter {
  implicit object LLMMetadataConfigurationMatcher
      extends BidirectionalMatcher[LLMMetadataConfiguration, ClientLLMMetadataConfiguration] {
    override def asClient(from: LLMMetadataConfiguration): ClientLLMMetadataConfiguration = new ClientLLMMetadataConfiguration(from)

    override def asInternal(from: ClientLLMMetadataConfiguration): LLMMetadataConfiguration = from._internal
  }
}
