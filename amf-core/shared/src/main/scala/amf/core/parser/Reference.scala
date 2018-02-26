package amf.core.parser

import amf.core.model.document.BaseUnit
import amf.core.remote.{Cache, Context}
import amf.core.services.RuntimeCompiler
import org.yaml.model.YNode

import scala.concurrent.Future

case class Reference(url: String, kind: ReferenceKind, ast: YNode) {

  def isRemote: Boolean = !url.startsWith("#")

  def resolve(base: Context,
              mediaType: Option[String],
              vendor: String,
              cache: Cache,
              ctx: ParserContext): Future[BaseUnit] = {
    RuntimeCompiler(url, mediaType, vendor, base, kind, cache, Some(ctx))
  }
}
