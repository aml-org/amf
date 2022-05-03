package amf.apicontract.internal.spec

import amf.apicontract.internal.spec.common.{OasWebApiDeclarations, RamlWebApiDeclarations, WebApiDeclarations}
import amf.apicontract.internal.spec.common.emitter.SpecEmitterContext
import amf.apicontract.internal.spec.common.parser.WebApiContext
import amf.apicontract.internal.spec.jsonschema.JsonSchemaWebApiContext
import amf.apicontract.internal.spec.oas.emitter.context.{Oas2SpecEmitterContext, OasSpecEmitterContext}
import amf.apicontract.internal.spec.oas.parser.context.{Oas2WebApiContext, Oas3WebApiContext, OasWebApiContext}
import amf.apicontract.internal.spec.raml.emitter.context.{Raml10SpecEmitterContext, RamlSpecEmitterContext}
import amf.apicontract.internal.spec.raml.parser.context.{Raml10WebApiContext, RamlWebApiContext}
import amf.core.client.scala.parse.document.ParsedReference
import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.common.emitter.SpecAwareEmitterContext
import amf.shapes.internal.spec.oas.OasShapeDefinitions

/** Oas package object
  */
package object spec {

  object OasDefinitions extends OasShapeDefinitions {

    val parameterDefinitionsPrefix = "#/parameters/"

    val responsesDefinitionsPrefix = "#/responses/"

    def stripParameterDefinitionsPrefix(url: String)(implicit ctx: WebApiContext): String = {
      if (ctx.spec == Spec.OAS30)
        stripOas3ComponentsPrefix(url, "parameters")
      else
        url.stripPrefix(parameterDefinitionsPrefix)
    }

    def stripResponsesDefinitionsPrefix(url: String)(implicit ctx: OasWebApiContext): String = {
      if (ctx.spec == Spec.OAS30)
        stripOas3ComponentsPrefix(url, "responses")
      else
        url.stripPrefix(responsesDefinitionsPrefix)
    }

    def appendParameterDefinitionsPrefix(url: String, asHeader: Boolean = false)(implicit
        spec: SpecAwareEmitterContext
    ): String = {
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
    val result = ctx.spec match {
      case Spec.OAS30 =>
        new Oas3WebApiContext(
          ctx.rootContextDocument,
          ctx.refs,
          ctx,
          Some(toOasDeclarations(ctx.declarations)),
          ctx.options
        )
      case _ =>
        new Oas2WebApiContext(
          ctx.rootContextDocument,
          ctx.refs,
          ctx,
          Some(toOasDeclarations(ctx.declarations)),
          ctx.options
        )
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
    new Raml10WebApiContext(
      ctx.rootContextDocument,
      ctx.refs,
      ctx,
      Some(toRamlDeclarations(ctx.declarations)),
      options = ctx.options
    )
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
    new Raml10SpecEmitterContext(spec.eh, spec.getRefEmitter, spec.renderConfig)
  }

  def toOas(spec: SpecEmitterContext): OasSpecEmitterContext = {
    new Oas2SpecEmitterContext(spec.eh, spec.getRefEmitter, spec.renderConfig)
  }

  def toJsonSchema(ctx: WebApiContext): JsonSchemaWebApiContext = {
    val result = new JsonSchemaWebApiContext(
      ctx.rootContextDocument,
      ctx.refs,
      ctx,
      Some(toOasDeclarations(ctx.declarations)),
      ctx.options,
      ctx.defaultSchemaVersion
    )
    result.indexCache = ctx.indexCache
    result
  }

  def toJsonSchema(root: String, refs: Seq[ParsedReference], ctx: WebApiContext): OasWebApiContext = {
    val result = new JsonSchemaWebApiContext(
      root,
      refs,
      ctx,
      Some(toOasDeclarations(ctx.declarations)),
      ctx.options,
      ctx.defaultSchemaVersion
    )
    result.indexCache = ctx.indexCache
    result
  }
}
