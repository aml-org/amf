package amf.graphql.client.scala

import amf.antlr.internal.plugins.syntax.{
  AntlrSyntaxRenderPlugin,
  GraphQLFederationSyntaxParsePlugin,
  SyamlForJsonLDSyntaxParsePlugin
}
import amf.apicontract.client.scala.{AMFConfiguration, APIConfigurationBuilder}
import amf.apicontract.internal.validation.model.ApiEffectiveValidations.GraphQLFederationEffectiveValidations
import amf.apicontract.internal.validation.model.ApiValidationProfiles.GraphQLFederationValidationProfile
import amf.apicontract.internal.validation.shacl.ShaclModelValidationPlugin
import amf.core.client.common.validation.ProfileNames

object GraphQLFederationConfiguration extends APIConfigurationBuilder {

  def GraphQLFederation(): AMFConfiguration = {
    common()
      .withPlugins(
        List(
          // TODO: replace with federation specific plugins
//          GraphQLParsePlugin,
          GraphQLFederationSyntaxParsePlugin,
//          GraphQLRenderPlugin,
          AntlrSyntaxRenderPlugin,
          ShaclModelValidationPlugin(ProfileNames.GRAPHQL_FEDERATION)
        )
      )
      .withTransformationPipelines(
        List(
          // TODO: replace with federation specific pipelines
//          GraphQLEditingPipeline(),
//          GraphQLCachePipeline()
        )
      )
      .withValidationProfile(GraphQLFederationValidationProfile, GraphQLFederationEffectiveValidations)
      .withPlugin(SyamlForJsonLDSyntaxParsePlugin) // override SYAML
  }
}
