package amf.grpc.client.scala

import amf.antlr.internal.plugins.syntax.AntlrSyntaxParsePlugin
import amf.apicontract.client.scala.{AMFConfiguration, APIConfigurationBuilder}
import amf.grpc.plugins.parse.GrpcParsePlugin
import amf.grpc.plugins.render.GrpcRenderPlugin

object GRPCConfiguration extends APIConfigurationBuilder {
  def GRPC(): AMFConfiguration =
    common()
      .withPlugins(List(GrpcParsePlugin, AntlrSyntaxParsePlugin, GrpcRenderPlugin))
}