package amf.core.parser

import amf.core.remote.{Cache, Context}
import amf.internal.environment.Environment

import scala.concurrent.Future

trait ReferenceHandler {

  /** Collect references on given document. */
  def collect(document: ParsedDocument, ctx: ParserContext): ReferenceCollector

  /** Update parsed reference if needed. */
  def update(reference: ParsedReference,
             ctx: ParserContext,
             context: Context,
             environment: Environment,
             cache: Cache): Future[ParsedReference] =
    Future.successful(reference)
}

object SimpleReferenceHandler extends ReferenceHandler {

  /** Collect references on given document. */
  override def collect(document: ParsedDocument, ctx: ParserContext): ReferenceCollector = EmptyReferenceCollector
}
