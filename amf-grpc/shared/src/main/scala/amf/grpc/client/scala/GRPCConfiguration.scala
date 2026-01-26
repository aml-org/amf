package amf.grpc.client.scala

import amf.antlr.client.scala.parse.AntlrBasedDocumentsFallbackPlugin
import amf.antlr.internal.plugins.syntax.{
  AntlrSyntaxRenderPlugin,
  GrpcSyntaxParsePlugin,
  SyamlForJsonLDSyntaxParsePlugin
}
import amf.apicontract.client.scala.{AMFConfiguration, APIConfigurationBuilder}
import amf.apicontract.internal.validation.model.ApiEffectiveValidations.GrpcEffectiveValidations
import amf.apicontract.internal.validation.model.ApiValidationProfiles.GrpcValidationProfile
import amf.apicontract.internal.validation.shacl.APIShaclModelValidationPlugin
import amf.core.client.common.validation.{ProfileNames, SeverityLevels}
import amf.core.internal.remote.Spec
import amf.grpc.internal.plugins.parse.GrpcParsePlugin
import amf.grpc.internal.plugins.render.GrpcRenderPlugin
import amf.grpc.internal.transformation.{GrpcCachePipeline, GrpcEditingPipeline, GrpcTransformationPipeline}

object GRPCConfiguration extends APIConfigurationBuilder {
  def GRPC(): AMFConfiguration =
    common()
      .withPlugins(
        List(
          GrpcParsePlugin,
          GrpcSyntaxParsePlugin,
          GrpcRenderPlugin,
          AntlrSyntaxRenderPlugin,
          APIShaclModelValidationPlugin(ProfileNames.GRPC)
        )
      )
      .withTransformationPipelines(
        List(
          GrpcTransformationPipeline(),
          GrpcEditingPipeline(),
          GrpcCachePipeline()
        )
      )
      .withValidationProfile(GrpcValidationProfile, GrpcEffectiveValidations)
      .withPlugin(SyamlForJsonLDSyntaxParsePlugin) // override SYAML
      .withFallback(AntlrBasedDocumentsFallbackPlugin(false, false, SeverityLevels.WARNING, Spec.GRPC))
}
