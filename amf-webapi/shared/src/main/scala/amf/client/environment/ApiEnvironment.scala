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
      .withPlugins(List(ExternalJsonYamlRefsParsePlugin, PayloadParsePlugin, PayloadRenderPlugin))

  def raml10(): AMFEnvironment = common().withPlugins(List(Raml10ParsePlugin, Raml10RenderPlugin))
  def raml08(): AMFEnvironment = common().withPlugins(List(Raml08ParsePlugin, Raml08RenderPlugin))
  def raml(): AMFEnvironment   = raml08().merge(raml10())

  def oas20(): AMFEnvironment = common().withPlugins(List(Oas20ParsePlugin, Oas20RenderPlugin))
  def oas30(): AMFEnvironment = common().withPlugins(List(Oas30ParsePlugin, Oas30RenderPlugin))
  def oas(): AMFEnvironment   = oas20().merge(oas30())

  def webApi(): AMFEnvironment = raml().merge(oas())

  def async20(): AMFEnvironment = common().withPlugins(List(Async20ParsePlugin, Async20RenderPlugin))

  def api(): AMFEnvironment = webApi().merge(async20())

}
