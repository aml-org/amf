package amf.sfdc.plugins.parse

import amf.apicontract.internal.transformation.compatibility.oas3.{
  AddItemsToArrayType,
  CleanNullSecurity,
  CleanRepeatedOperationIds,
  CleanSchemes,
  MandatoryDocumentationUrl,
  MandatoryPathParameters,
  MandatoryResponses,
  Oas30SecuritySettingsMapper
}
import amf.core.client.scala.transform.{TransformationPipeline, TransformationStep}

object SfdcToOas30TransformationPipeline extends TransformationPipeline {
  override val name: String = this.getClass.getSimpleName

  override def steps: Seq[TransformationStep] = Seq(
    new CleanNullSecurity(),
    new CleanSchemes(),
    new MandatoryDocumentationUrl(),
    new MandatoryResponses(),
    new Oas30SecuritySettingsMapper(),
    new MandatoryPathParameters(),
    new AddItemsToArrayType(),
    new CleanRepeatedOperationIds()
  )
}
