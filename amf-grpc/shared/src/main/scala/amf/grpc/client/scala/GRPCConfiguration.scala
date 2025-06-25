package amf.grpc.client.scala

import amf.antlr.internal.plugins.syntax.{
  AntlrSyntaxRenderPlugin,
  GrpcSyntaxParsePlugin,
  SyamlForJsonLDSyntaxParsePlugin
}
import amf.apicontract.client.scala.{AMFConfiguration, APIConfigurationBuilder}
import amf.grpc.internal.plugins.parse.GrpcParsePlugin
import amf.grpc.internal.plugins.render.GrpcRenderPlugin
import amf.grpc.internal.transformation.{GrpcCachePipeline, GrpcEditingPipeline, GrpcTransformationPipeline}

object GRPCConfiguration extends APIConfigurationBuilder {
  def GRPC(): AMFConfiguration =
    common()
      .withPlugins(List(GrpcParsePlugin, GrpcSyntaxParsePlugin, GrpcRenderPlugin, AntlrSyntaxRenderPlugin))
      .withPlugin(SyamlForJsonLDSyntaxParsePlugin) // override SYAML
      .withTransformationPipelines(
        List(
          GrpcTransformationPipeline(),
          GrpcEditingPipeline(),
          GrpcCachePipeline()
        )
      )
}
