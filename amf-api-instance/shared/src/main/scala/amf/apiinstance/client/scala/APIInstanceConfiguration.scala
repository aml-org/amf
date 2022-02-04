package amf.apiinstance.client.scala

import amf.apicontract.client.scala.{AMFConfiguration, APIConfigurationBuilder}
import amf.apiinstance.internal.plugins.{AWSAPIGatewayConfigParsePlugin, EnvoyConfigParsePlugin, KongDeclarativeConfigParsePlugin}

object APIInstanceConfiguration extends APIConfigurationBuilder {
  def APIInstance(): AMFConfiguration = common().withPlugins(List(EnvoyConfigParsePlugin, AWSAPIGatewayConfigParsePlugin, KongDeclarativeConfigParsePlugin))
}
