package amf.plugins.document.webapi.parser

import amf.core.parser.ParsedReference
import amf.core.remote.Vendor
import amf.plugins.document.webapi.contexts._
import amf.plugins.document.webapi.contexts.emitter.async.Async20SpecEmitterFactory
import amf.plugins.document.webapi.contexts.emitter.oas.{
  Oas2SpecEmitterContext,
  Oas3SpecEmitterFactory,
  OasSpecEmitterContext
}
import amf.plugins.document.webapi.contexts.emitter.raml.{Raml10SpecEmitterContext, RamlSpecEmitterContext}
import amf.plugins.document.webapi.contexts.parser.oas.{
  JsonSchemaWebApiContext,
  Oas2WebApiContext,
  Oas3WebApiContext,
  OasWebApiContext
}
import amf.plugins.document.webapi.contexts.parser.raml.{Raml10WebApiContext, RamlWebApiContext}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{ShapeEmitterContext, SpecAwareEmitterContext}

/**
  * Oas package object
  */
package object spec {

  object OasDefinitions extends OasShapeDefinitions {

    val parameterDefinitionsPrefix = "#/parameters/"

    val responsesDefinitionsPrefix = "#/responses/"

    def stripParameterDefinitionsPrefix(url: String)(implicit ctx: WebApiContext): String = {
      if (ctx.vendor == Vendor.OAS30)
        stripOas3ComponentsPrefix(url, "parameters")
      else
        url.stripPrefix(parameterDefinitionsPrefix)
    }

    def stripResponsesDefinitionsPrefix(url: String)(implicit ctx: OasWebApiContext): String = {
      if (ctx.vendor == Vendor.OAS30)
        stripOas3ComponentsPrefix(url, "responses")
      else
        url.stripPrefix(responsesDefinitionsPrefix)
    }

    def appendParameterDefinitionsPrefix(url: String, asHeader: Boolean = false)(
        implicit spec: SpecAwareEmitterContext): String = {
      if (spec.isOas3 || spec.isAsync)
        appendOas3ComponentsPrefix(url, "parameters")
      else
        appendPrefix(parameterDefinitionsPrefix, url)
    }

    def appendResponsesDefinitionsPrefix(url: String)(implicit spec: SpecAwareEmitterContext): String = {
      if (spec.isOas3)
        appendOas3ComponentsPrefix(url, "responses")
      else
        appendPrefix(responsesDefinitionsPrefix, url)
    }
  }

  // TODO oas2? raml10?
  def toOas(ctx: WebApiContext): OasWebApiContext = {
    val result = ctx.vendor match {
      case Vendor.OAS30 =>
        new Oas3WebApiContext(ctx.rootContextDocument,
                              ctx.refs,
                              ctx,
                              Some(toOasDeclarations(ctx.declarations)),
                              ctx.options)
      case _ =>
        new Oas2WebApiContext(ctx.rootContextDocument,
                              ctx.refs,
                              ctx,
                              Some(toOasDeclarations(ctx.declarations)),
                              ctx.options)
    }
    result.indexCache = ctx.indexCache
    result
  }

  def toOas(root: String, refs: Seq[ParsedReference], ctx: WebApiContext): OasWebApiContext = {
    val result = new Oas2WebApiContext(root, refs, ctx, Some(toOasDeclarations(ctx.declarations)), ctx.options)
    result.indexCache = ctx.indexCache
    result
  }

  def toRaml(ctx: WebApiContext): RamlWebApiContext = {
    new Raml10WebApiContext(ctx.rootContextDocument,
                            ctx.refs,
                            ctx,
                            Some(toRamlDeclarations(ctx.declarations)),
                            options = ctx.options)
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
    val result = new JsonSchemaWebApiContext(ctx.rootContextDocument,
                                             ctx.refs,
                                             ctx,
                                             Some(toOasDeclarations(ctx.declarations)),
                                             ctx.options,
                                             ctx.defaultSchemaVersion)
    result.indexCache = ctx.indexCache
    result
  }

  def toJsonSchema(root: String, refs: Seq[ParsedReference], ctx: WebApiContext): OasWebApiContext = {
    val result = new JsonSchemaWebApiContext(root,
                                             refs,
                                             ctx,
                                             Some(toOasDeclarations(ctx.declarations)),
                                             ctx.options,
                                             ctx.defaultSchemaVersion)
    result.indexCache = ctx.indexCache
    result
  }
}
