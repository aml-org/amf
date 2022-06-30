package amf.graphql.client.scala

import amf.antlr.internal.plugins.syntax.{AntlrSyntaxRenderPlugin, GraphQLSyntaxParsePlugin}
import amf.apicontract.client.scala.{AMFConfiguration, APIConfigurationBuilder}
import amf.apicontract.internal.transformation.{GraphQLCachePipeline, GraphQLEditingPipeline}
import amf.apicontract.internal.validation.model.ApiEffectiveValidations.GraphQLEffectiveValidations
import amf.apicontract.internal.validation.model.ApiValidationProfiles.GraphQLValidationProfile
import amf.apicontract.internal.validation.shacl.ShaclModelValidationPlugin
import amf.core.client.common.validation.ProfileNames
import amf.graphql.plugins.parse.GraphQLParsePlugin
import amf.graphql.plugins.render.GraphQLRenderPlugin

object GraphQLConfiguration extends APIConfigurationBuilder {

  def GraphQL(): AMFConfiguration = {
    common()
      .withPlugins(
        List(
          GraphQLParsePlugin,
          GraphQLSyntaxParsePlugin,
          GraphQLRenderPlugin,
          AntlrSyntaxRenderPlugin,
          ShaclModelValidationPlugin(ProfileNames.GRAPHQL)
        )
      )
      .withTransformationPipelines(
        List(
          GraphQLEditingPipeline(),
          GraphQLCachePipeline()
        )
      )
      .withValidationProfile(GraphQLValidationProfile, GraphQLEffectiveValidations)
  }
}
