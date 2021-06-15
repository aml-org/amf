package amf.shapes.internal.spec.contexts.parser.async

import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote.{AsyncApi20, Vendor}
import amf.plugins.document.apicontract.parser.spec.async.Async20Syntax
import amf.plugins.document.apicontract.parser.spec.{AsyncWebApiDeclarations, SpecSyntax}

import scala.collection.mutable

class Async20WebApiContext(loc: String,
                           refs: Seq[ParsedReference],
                           private val wrapped: ParserContext,
                           private val ds: Option[AsyncWebApiDeclarations] = None,
                           private val operationIds: mutable.Set[String] = mutable.HashSet(),
                           options: ParsingOptions = ParsingOptions())
    extends AsyncWebApiContext(loc, refs, options, wrapped, ds, operationIds) {
  override val factory: Async20VersionFactory = Async20VersionFactory()(this)
  override val vendor: Vendor                 = AsyncApi20
  override val syntax: SpecSyntax             = Async20Syntax

  override def makeCopy(): Async20WebApiContext =
    new Async20WebApiContext(rootContextDocument, refs, this, Some(declarations), operationIds, options)
}
