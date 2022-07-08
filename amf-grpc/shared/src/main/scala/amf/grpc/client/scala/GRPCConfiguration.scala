package amf.grpc.client.scala

import amf.antlr.internal.plugins.syntax.{AntlrSyntaxRenderPlugin, GrpcSyntaxParsePlugin, SyamlForJsonLDSyntaxParsePlugin}
import amf.apicontract.client.scala.{AMFConfiguration, APIConfigurationBuilder}
import amf.grpc.internal.plugins.parse.GrpcParsePlugin
import amf.grpc.internal.plugins.render.GrpcRenderPlugin

object GRPCConfiguration extends APIConfigurationBuilder {
  def GRPC(): AMFConfiguration =
    common()
      .withPlugins(List(GrpcParsePlugin, GrpcSyntaxParsePlugin, GrpcRenderPlugin, AntlrSyntaxRenderPlugin))
      .withPlugin(SyamlForJsonLDSyntaxParsePlugin) // override SYAML
}
