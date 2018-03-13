package amf.plugins.document.webapi.parser

import amf.plugins.document.webapi.contexts._

/**
  * Oas package object
  */
package object spec {

  object OasDefinitions {
    val definitionsPrefix = "#/definitions/"

    val parameterDefinitionsPrefix = "#/parameters/"

    val responsesDefinitionsPrefix = "#/responses/"

    def stripDefinitionsPrefix(url: String): String = url.stripPrefix(definitionsPrefix)

    def stripParameterDefinitionsPrefix(url: String): String = url.stripPrefix(parameterDefinitionsPrefix)

    def stripResponsesDefinitionsPrefix(url: String): String = url.stripPrefix(responsesDefinitionsPrefix)

    def appendDefinitionsPrefix(url: String): String = appendPrefix(definitionsPrefix, url)

    def appendParameterDefinitionsPrefix(url: String): String = appendPrefix(parameterDefinitionsPrefix, url)

    def appendResponsesDefinitionsPrefix(url: String): String = appendPrefix(responsesDefinitionsPrefix, url)

    private def appendPrefix(prefix: String, url: String): String = prefix + url
  }

  def toOas(ctx: WebApiContext): OasWebApiContext = {
    new OasWebApiContext(ctx, Some(ctx.declarations))
  }

  def toRaml(ctx: WebApiContext): RamlWebApiContext = {
    new Raml10WebApiContext(ctx, Some(ctx.declarations))
  }

  def toRaml(spec: SpecEmitterContext): RamlSpecEmitterContext = {
    new Raml10SpecEmitterContext(spec.getRefEmitter)
  }

  def toOas(spec: SpecEmitterContext): OasSpecEmitterContext = {
    new Oas2SpecEmitterContext(spec.getRefEmitter)
  }
}
