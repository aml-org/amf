package amf.apicontract.internal.spec.async.parser.context

import amf.apicontract.internal.spec.async.parser.context.syntax._
import amf.apicontract.internal.spec.common.AsyncWebApiDeclarations
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote._
import amf.shapes.internal.spec.async.parser.Async2Settings

import scala.collection.mutable

object Async2WebApiContext {

  def apply(
      location: String,
      refs: Seq[ParsedReference],
      wrapped: ParserContext,
      declarations: Option[AsyncWebApiDeclarations],
      options: ParsingOptions,
      spec: Spec
  ): Async2WebApiContext = spec match {
    case AsyncApi20 => async20(location, refs, wrapped, declarations, options)
    case AsyncApi21 => async21(location, refs, wrapped, declarations, options)
    case AsyncApi22 => async22(location, refs, wrapped, declarations, options)
    case AsyncApi23 => async23(location, refs, wrapped, declarations, options)
    case AsyncApi24 => async24(location, refs, wrapped, declarations, options)
    case AsyncApi25 => async25(location, refs, wrapped, declarations, options)
    case AsyncApi26 => async26(location, refs, wrapped, declarations, options)
  }

  def async20(
      location: String,
      refs: Seq[ParsedReference],
      wrapped: ParserContext,
      declarations: Option[AsyncWebApiDeclarations],
      options: ParsingOptions
  ): Async2WebApiContext = {
    context(location, refs, wrapped, declarations, options)(AsyncApi20)
  }

  def async21(
      location: String,
      refs: Seq[ParsedReference],
      wrapped: ParserContext,
      declarations: Option[AsyncWebApiDeclarations],
      options: ParsingOptions
  ): Async2WebApiContext = {
    context(location, refs, wrapped, declarations, options)(AsyncApi21)
  }

  def async22(
      location: String,
      refs: Seq[ParsedReference],
      wrapped: ParserContext,
      declarations: Option[AsyncWebApiDeclarations],
      options: ParsingOptions
  ): Async2WebApiContext = {
    context(location, refs, wrapped, declarations, options)(AsyncApi22)
  }

  def async23(
      location: String,
      refs: Seq[ParsedReference],
      wrapped: ParserContext,
      declarations: Option[AsyncWebApiDeclarations],
      options: ParsingOptions
  ): Async2WebApiContext = {
    context(location, refs, wrapped, declarations, options)(AsyncApi23)
  }

  def async24(
      location: String,
      refs: Seq[ParsedReference],
      wrapped: ParserContext,
      declarations: Option[AsyncWebApiDeclarations],
      options: ParsingOptions
  ): Async2WebApiContext = {
    context(location, refs, wrapped, declarations, options)(AsyncApi24)
  }

  def async25(
      location: String,
      refs: Seq[ParsedReference],
      wrapped: ParserContext,
      declarations: Option[AsyncWebApiDeclarations],
      options: ParsingOptions
  ): Async2WebApiContext = {
    context(location, refs, wrapped, declarations, options)(AsyncApi25)
  }

  def async26(
      location: String,
      refs: Seq[ParsedReference],
      wrapped: ParserContext,
      declarations: Option[AsyncWebApiDeclarations],
      options: ParsingOptions
  ): Async2WebApiContext = {
    context(location, refs, wrapped, declarations, options)(AsyncApi26)
  }

  private def context(
      location: String,
      refs: Seq[ParsedReference],
      wrapped: ParserContext,
      declarations: Option[AsyncWebApiDeclarations],
      options: ParsingOptions
  )(spec: Spec): Async2WebApiContext = {
    new Async2WebApiContext(
      location,
      refs,
      wrapped,
      declarations,
      mutable.HashSet.empty,
      options,
      settings(spec),
      bindingSet(spec),
      factory(spec)
    )
  }

  private def settings(spec: Spec) = spec match {
    case AsyncApi20 => Async2Settings(Async20Syntax, AsyncApi20)
    case AsyncApi21 => Async2Settings(Async21Syntax, AsyncApi21)
    case AsyncApi22 => Async2Settings(Async22Syntax, AsyncApi22)
    case AsyncApi23 => Async2Settings(Async23Syntax, AsyncApi23)
    case AsyncApi24 => Async2Settings(Async24Syntax, AsyncApi24)
    case AsyncApi25 => Async2Settings(Async25Syntax, AsyncApi25)
    case AsyncApi26 => Async2Settings(Async26Syntax, AsyncApi26)
  }

  private def factory(spec: Spec): Async2WebApiContext => AsyncSpecVersionFactory = spec match {
    case AsyncApi20 => ctx => Async20VersionFactory()(ctx)
    case AsyncApi21 => ctx => Async21VersionFactory()(ctx)
    case AsyncApi22 => ctx => Async21VersionFactory()(ctx)
    case AsyncApi23 => ctx => Async23VersionFactory()(ctx)
    case AsyncApi24 => ctx => Async23VersionFactory()(ctx)
    case AsyncApi25 => ctx => Async23VersionFactory()(ctx)
    case AsyncApi26 => ctx => Async23VersionFactory()(ctx)
  }

  private def bindingSet(spec: Spec): AsyncValidBindingSet = spec match {
    case AsyncApi20 => AsyncValidBindingSet.async20
    case AsyncApi21 => AsyncValidBindingSet.async21
    case AsyncApi22 => AsyncValidBindingSet.async22
    case AsyncApi23 => AsyncValidBindingSet.async23
    case AsyncApi24 => AsyncValidBindingSet.async24
    case AsyncApi25 => AsyncValidBindingSet.async25
    case AsyncApi26 => AsyncValidBindingSet.async26
  }
}

class Async2WebApiContext private (
    loc: String,
    refs: Seq[ParsedReference],
    private val wrapped: ParserContext,
    private val ds: Option[AsyncWebApiDeclarations] = None,
    private val operationIds: mutable.Set[String] = mutable.HashSet(),
    options: ParsingOptions = ParsingOptions(),
    settings: Async2Settings,
    bindings: AsyncValidBindingSet,
    factoryFactory: Async2WebApiContext => AsyncSpecVersionFactory
) extends AsyncWebApiContext(
      loc,
      refs,
      options,
      wrapped,
      ds,
      operationIds,
      settings,
      bindings
    ) {

  override val factory: AsyncSpecVersionFactory = factoryFactory(this)
  override def makeCopy(): Async2WebApiContext =
    new Async2WebApiContext(
      rootContextDocument,
      refs,
      this,
      Some(declarations),
      operationIds,
      options,
      settings,
      bindings,
      factoryFactory
    )
}
