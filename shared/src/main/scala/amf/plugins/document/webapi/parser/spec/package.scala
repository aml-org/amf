package amf.plugins.document.webapi.parser

import amf.framework.parser.ParserContext
import amf.plugins.document.webapi.contexts.{OasSpecAwareContext, RamlSpecAwareContext, WebApiContext}
import amf.plugins.document.webapi.parser.spec.oas.OasSyntax
import amf.plugins.document.webapi.parser.spec.raml.RamlSyntax
import amf.remote.{Oas, Raml}

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

  def toOas(ctx: WebApiContext) = {
    new WebApiContext(Oas, ctx, OasSpecAwareContext, OasSyntax)
  }

  def toRaml(ctx: ParserContext) = {
    new WebApiContext(Raml, ctx, RamlSpecAwareContext, RamlSyntax)
  }
}
