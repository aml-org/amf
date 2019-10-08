package amf.plugins.document.webapi.parser

import amf.core.parser.ParsedReference
import amf.core.remote.Vendor
import amf.plugins.document.webapi.JsonSchemaWebApiContext
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

    def appendDefinitionsPrefix(url: String): String =
      if (!url.startsWith(definitionsPrefix)) appendPrefix(definitionsPrefix, url) else url

    def appendParameterDefinitionsPrefix(url: String): String = appendPrefix(parameterDefinitionsPrefix, url)

    def appendResponsesDefinitionsPrefix(url: String): String = appendPrefix(responsesDefinitionsPrefix, url)

    private def appendPrefix(prefix: String, url: String): String = prefix + url
  }

  // TODO oas2? raml10?
  def toOas(ctx: WebApiContext): OasWebApiContext = {
    ctx.vendor match {
      case Vendor.OAS30 =>
        new Oas3WebApiContext(ctx.rootContextDocument,
                              ctx.refs,
                              ctx,
                              Some(toOasDeclarations(ctx.declarations)),
                              Some(ctx.parserCount),
                              ctx.eh)
      case _ =>
        new Oas2WebApiContext(ctx.rootContextDocument,
                              ctx.refs,
                              ctx,
                              Some(toOasDeclarations(ctx.declarations)),
                              Some(ctx.parserCount),
                              ctx.eh)
    }

  }

  def toOas(root: String, refs: Seq[ParsedReference], ctx: WebApiContext): OasWebApiContext = {
    new Oas2WebApiContext(root, refs, ctx, Some(toOasDeclarations(ctx.declarations)), Some(ctx.parserCount), ctx.eh)
  }

  def toRaml(ctx: WebApiContext): RamlWebApiContext = {
    new Raml10WebApiContext(ctx.rootContextDocument,
                            ctx.refs,
                            ctx,
                            Some(toRamlDeclarations(ctx.declarations)),
                            Some(ctx.parserCount),
                            ctx.eh)
  }

  private def toRamlDeclarations(ds: WebApiDeclarations) = {
    ds match {
      case raml: RamlWebApiDeclarations => raml
      case other                        => RamlWebApiDeclarations(other)
    }
  }

  def toOasDeclarations(ds: WebApiDeclarations): OasWebApiDeclarations = {
    ds match {
      case oas: OasWebApiDeclarations => oas
      case other                      => OasWebApiDeclarations(other)
    }
  }

  def toRaml(spec: SpecEmitterContext): RamlSpecEmitterContext = {
    new Raml10SpecEmitterContext(spec.eh, spec.getRefEmitter)
  }

  def toOas(spec: SpecEmitterContext): OasSpecEmitterContext = {
    new Oas2SpecEmitterContext(spec.eh, spec.getRefEmitter)
  }

  def toJsonSchema(ctx: WebApiContext): JsonSchemaWebApiContext = {
    new JsonSchemaWebApiContext(ctx.rootContextDocument,
                                ctx.refs,
                                ctx,
                                Some(toOasDeclarations(ctx.declarations)),
                                Some(ctx.parserCount),
                                ctx.eh)
  }

  def toJsonSchema(root: String, refs: Seq[ParsedReference], ctx: WebApiContext): OasWebApiContext = {
    new JsonSchemaWebApiContext(root,
                                refs,
                                ctx,
                                Some(toOasDeclarations(ctx.declarations)),
                                Some(ctx.parserCount),
                                ctx.eh)
  }
}
