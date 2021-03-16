package amf.client.environment

import amf.client.remod.AMFEnvironment
import amf.plugins.document.graph.AMFGraphParsePlugin
import amf.plugins.document.webapi._

// defined private unit mayor version is released
private[amf] object ApiEnvironment {

//  will also define APIDomainPlugin, DataShapesDomainPlugin
  private def common(): AMFEnvironment =
    AMFEnvironment
      .default()
      .withPlugins(List(ExternalJsonYamlRefsParsePlugin, PayloadParsePlugin, JsonSchemaParsePlugin))

  def raml10(): AMFEnvironment = common().withPlugin(Raml10ParsePlugin)
  def raml08(): AMFEnvironment = common().withPlugin(Raml08ParsePlugin)
  def raml(): AMFEnvironment   = raml08().merge(raml10())

  def oas20(): AMFEnvironment = common().withPlugin(Oas20ParsePlugin)
  def oas30(): AMFEnvironment = common().withPlugin(Oas30ParsePlugin)
  def oas(): AMFEnvironment   = oas20().merge(oas30())

  def webApi(): AMFEnvironment = raml().merge(oas())

  def async20(): AMFEnvironment = common().withPlugin(Async20ParsePlugin)

  def api(): AMFEnvironment = webApi().merge(async20())

}
