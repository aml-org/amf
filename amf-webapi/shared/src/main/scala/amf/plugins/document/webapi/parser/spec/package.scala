package amf.plugins.document.webapi.parser

import amf.core.parser.ParsedReference
import amf.core.remote.Vendor
import amf.plugins.document.webapi.contexts._
import amf.plugins.document.webapi.contexts.emitter.oas.{
  Oas2SpecEmitterContext,
  Oas3SpecEmitterFactory,
  OasSpecEmitterContext
}
import amf.plugins.document.webapi.contexts.emitter.raml.{Raml10SpecEmitterContext, RamlSpecEmitterContext}
import amf.plugins.document.webapi.contexts.parser.oas.{
  Oas2WebApiContext,
  Oas3WebApiContext,
  OasWebApiContext,
  JsonSchemaWebApiContext
}
import amf.plugins.document.webapi.contexts.parser.raml.{RamlWebApiContext, Raml10WebApiContext}

/**
  * Oas package object
  */
package object spec {

  object OasDefinitions {
    val oas2DefinitionsPrefix = "#/definitions/"

    val oas3DefinitionsPrefix = "#/components/schemas/"

    val oas3ComponentsPrefix = "#/components/"

    val parameterDefinitionsPrefix = "#/parameters/"

    val responsesDefinitionsPrefix = "#/responses/"

    def stripDefinitionsPrefix(url: String)(implicit ctx: WebApiContext): String = {
      if (ctx.vendor == Vendor.OAS30) url.stripPrefix(oas3DefinitionsPrefix)
      else url.stripPrefix(oas2DefinitionsPrefix)
    }

    def stripParameterDefinitionsPrefix(url: String)(implicit ctx: WebApiContext): String = {
      if (ctx.vendor == Vendor.OAS30)
        stripOas3ComponentsPrefix(url, "parameters")
      else
        url.stripPrefix(parameterDefinitionsPrefix)
    }

    def stripOas3ComponentsPrefix(url: String, fieldName: String): String =
      url.stripPrefix(oas3ComponentsPrefix + fieldName + "/")

    def stripResponsesDefinitionsPrefix(url: String)(implicit ctx: OasWebApiContext): String = {
      if (ctx.vendor == Vendor.OAS30)
        stripOas3ComponentsPrefix(url, "responses")
      else
        url.stripPrefix(responsesDefinitionsPrefix)
    }

    def appendDefinitionsPrefix(url: String, vendor: Option[Vendor] = None): String = vendor match {
      case Some(Vendor.OAS30) =>
        if (!url.startsWith(oas3DefinitionsPrefix)) appendPrefix(oas3DefinitionsPrefix, url) else url
      case _ =>
        if (!url.startsWith(oas2DefinitionsPrefix)) appendPrefix(oas2DefinitionsPrefix, url) else url
    }

    def appendParameterDefinitionsPrefix(url: String, asHeader: Boolean = false)(
        implicit spec: OasSpecEmitterContext): String = {
      if (spec.factory.isInstanceOf[Oas3SpecEmitterFactory])
        appendOas3ComponentsPrefix(url, "parameters")
      else
        appendPrefix(parameterDefinitionsPrefix, url)
    }

    def appendResponsesDefinitionsPrefix(url: String)(implicit spec: OasSpecEmitterContext): String = {
      if (spec.factory.isInstanceOf[Oas3SpecEmitterFactory])
        appendOas3ComponentsPrefix(url, "responses")
      else
        appendPrefix(responsesDefinitionsPrefix, url)
    }

    def appendOas3ComponentsPrefix(url: String, fieldName: String): String = {
      appendPrefix(oas3ComponentsPrefix + s"$fieldName/", url)
    }

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
                              ctx.options)
      case _ =>
        new Oas2WebApiContext(ctx.rootContextDocument,
                              ctx.refs,
                              ctx,
                              Some(toOasDeclarations(ctx.declarations)),
                              ctx.options)
    }

  }

  def toOas(root: String, refs: Seq[ParsedReference], ctx: WebApiContext): OasWebApiContext = {
    new Oas2WebApiContext(root, refs, ctx, Some(toOasDeclarations(ctx.declarations)), ctx.options)
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
    new JsonSchemaWebApiContext(ctx.rootContextDocument,
                                ctx.refs,
                                ctx,
                                Some(toOasDeclarations(ctx.declarations)),
                                ctx.options)
  }

  def toJsonSchema(root: String, refs: Seq[ParsedReference], ctx: WebApiContext): OasWebApiContext = {
    new JsonSchemaWebApiContext(root, refs, ctx, Some(toOasDeclarations(ctx.declarations)), ctx.options)
  }
}
