package amf.apicontract.internal.spec.async.parser.context

import amf.apicontract.internal.spec.common.AsyncWebApiDeclarations
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote.{AsyncApi20, Spec}
import amf.shapes.internal.spec.common.parser.SpecSyntax

import scala.collection.mutable

class Async20WebApiContext(loc: String,
                           refs: Seq[ParsedReference],
                           private val wrapped: ParserContext,
                           private val ds: Option[AsyncWebApiDeclarations] = None,
                           private val operationIds: mutable.Set[String] = mutable.HashSet(),
                           options: ParsingOptions = ParsingOptions())
    extends AsyncWebApiContext(loc, refs, options, wrapped, ds, operationIds) {
  override val factory: Async20VersionFactory = Async20VersionFactory()(this)
  override val spec: Spec                     = AsyncApi20
  override val syntax: SpecSyntax             = Async20Syntax

  override def makeCopy(): Async20WebApiContext =
    new Async20WebApiContext(rootContextDocument, refs, this, Some(declarations), operationIds, options)
}
