package amf.graphqlfederation.client.scala

import amf.antlr.internal.plugins.syntax.{
  AntlrSyntaxRenderPlugin,
  GraphQLFederationSyntaxParsePlugin,
  SyamlForJsonLDSyntaxParsePlugin
}
import amf.apicontract.client.scala.{AMFConfiguration, APIConfigurationBuilder}
import amf.apicontract.internal.validation.model.ApiEffectiveValidations.GraphQLFederationEffectiveValidations
import amf.apicontract.internal.validation.model.ApiValidationProfiles.GraphQLFederationValidationProfile
import amf.apicontract.internal.validation.shacl.APIShaclModelValidationPlugin
import amf.core.client.common.validation.ProfileNames
import amf.graphqlfederation.internal.plugins.GraphQLFederationParsePlugin
import amf.graphqlfederation.internal.spec.transformation.GraphQLFederationIntrospectionPipeline

object GraphQLFederationConfiguration extends APIConfigurationBuilder {

  def GraphQLFederation(): AMFConfiguration = {
    common()
      .withPlugins(
        List(
          // TODO: replace with federation specific plugins
          GraphQLFederationParsePlugin,
          GraphQLFederationSyntaxParsePlugin,
//          GraphQLRenderPlugin,
          AntlrSyntaxRenderPlugin,
          APIShaclModelValidationPlugin(ProfileNames.GRAPHQL_FEDERATION)
        )
      )
      .withTransformationPipelines(
        List(
          // TODO: replace with federation specific pipelines
//          GraphQLEditingPipeline(),
//          GraphQLCachePipeline()
          GraphQLFederationIntrospectionPipeline
        )
      )
      .withValidationProfile(GraphQLFederationValidationProfile, GraphQLFederationEffectiveValidations)
      .withPlugin(SyamlForJsonLDSyntaxParsePlugin) // override SYAML
  }
}
