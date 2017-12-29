package amf.plugins.document.webapi.parser

import amf.plugins.document.webapi.contexts._

/**
  * Oas package object
  */
package object spec {

  object OasDefinitions {
    val definitionsPrefix = "#/definitions/"

    val parameterDefinitionsPrefix = "#/parameters/"

    def stripDefinitionsPrefix(url: String): String = url.stripPrefix(definitionsPrefix)

    def stripParameterDefinitionsPrefix(url: String): String = url.stripPrefix(parameterDefinitionsPrefix)

    def appendDefinitionsPrefix(url: String): String = definitionsPrefix + url

    def appendParameterDefinitionsPrefix(url: String): String = parameterDefinitionsPrefix + url
  }

  def toOas(ctx: WebApiContext): WebApiContext = {
    new OasWebApiContext(ctx, Some(ctx.declarations))
  }

  def toRaml(ctx: WebApiContext): RamlWebApiContext = {
    new Raml10WebApiContext(ctx, Some(ctx.declarations))
  }

  def toRaml(spec: OasSpecEmitterContext): RamlSpecEmitterContext = {
    new Raml10SpecEmitterContext(spec.getRefEmitter)
  }

  def toOas(spec: RamlSpecEmitterContext): OasSpecEmitterContext = {
    new OasSpecEmitterContext(spec.getRefEmitter)
  }
}
