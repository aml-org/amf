package amf.framework.parser

import amf.facades.Validation
import amf.framework.model.document.BaseUnit
import amf.framework.remote.{Cache, Context, Platform}
import amf.framework.services.RuntimeCompiler
import amf.plugins.document.vocabularies.core.DialectRegistry
import org.yaml.model.YAggregate

import scala.concurrent.Future

case class Reference(url: String, kind: ReferenceKind, ast: YAggregate) {

  def isRemote: Boolean = !url.startsWith("#")

  def resolve(remote: Platform,
              base: Option[Context],
              mediaType: String,
              vendor: String,
              cache: Cache,
              dialects: DialectRegistry,
              ctx: ParserContext): Future[BaseUnit] = {
    RuntimeCompiler(url, remote, mediaType, vendor, base, kind, cache, Some(ctx))
      .map(root => {
//        target = root
        root
      })
  }
}
