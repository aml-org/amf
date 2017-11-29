package amf.core.parser

import amf.core.model.document.BaseUnit
import amf.core.remote.{Cache, Context, Platform}
import amf.core.services.RuntimeCompiler
import org.yaml.model.YAggregate

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

case class Reference(url: String, kind: ReferenceKind, ast: YAggregate) {

  def isRemote: Boolean = !url.startsWith("#")

  def resolve(remote: Platform,
              base: Option[Context],
              mediaType: String,
              vendor: String,
              cache: Cache,
              ctx: ParserContext): Future[BaseUnit] = {
    RuntimeCompiler(url, remote, mediaType, vendor, base, kind, cache, Some(ctx))

      .map(root => {
//        target = root
        root
      })
  }
}
