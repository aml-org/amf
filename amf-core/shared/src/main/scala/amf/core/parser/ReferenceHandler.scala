package amf.core.parser

import amf.core.remote.Context

import scala.concurrent.Future

trait ReferenceHandler {

  /** Collect references on given document. */
  def collect(document: ParsedDocument, ctx: ParserContext): Seq[Reference] = Nil

  /** Update parsed reference if needed. */
  def update(reference: ParsedReference, ctx: ParserContext, context: Context): Future[ParsedReference] =
    Future.successful(reference)
}

object SimpleReferenceHandler extends ReferenceHandler
