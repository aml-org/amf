package amf.apicontract.client.scala.configuration

import amf.apicontract.client.scala.{AMFConfiguration, APIConfigurationBuilder}
import amf.apicontract.internal.spec.oas.{Oas30ComponentParsePlugin, Oas30ComponentRenderPlugin, Oas30ElementRenderPlugin}
import amf.apicontract.internal.transformation.{Oas30TransformationPipeline, Oas3CachePipeline, Oas3EditingPipeline}
import amf.apicontract.internal.validation.model.ApiEffectiveValidations.Oas30EffectiveValidations
import amf.apicontract.internal.validation.model.ApiValidationProfiles.Oas30ValidationProfile
import amf.apicontract.internal.validation.payload.APIPayloadValidationPlugin
import amf.apicontract.internal.validation.shacl.APIShaclModelValidationPlugin
import amf.core.client.common.validation.ProfileNames

object OasComponentConfiguration extends APIConfigurationBuilder {

  def OAS30Component(): AMFConfiguration =
    common()
      .withPlugins(
        List(
          Oas30ComponentParsePlugin,
          Oas30ComponentRenderPlugin,
          Oas30ElementRenderPlugin,
          APIShaclModelValidationPlugin(ProfileNames.OAS30),
          APIPayloadValidationPlugin(ProfileNames.OAS30)
        )
      )
      .withValidationProfile(Oas30ValidationProfile, Oas30EffectiveValidations)
      .withTransformationPipelines(
        List(
          Oas30TransformationPipeline(),
          Oas3EditingPipeline(),
          Oas3CachePipeline()
        )
      )

}
