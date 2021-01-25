package amf.client.`new`

import java.util.EventListener

import amf.ProfileName
import amf.client.`new`.amfcore.{AmfLogger, AmfParsePlugin, AmfResolvePlugin, AmfValidatePlugin}
import amf.client.remote.Content
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.{Platform, UnsupportedUrlScheme, Vendor}
import amf.internal.environment.Environment
import amf.internal.reference.ReferenceResolver
import amf.internal.resource.ResourceLoader
import org.mulesoft.common.io.FileSystem
import org.yaml.model.YDocument

import scala.concurrent.{ExecutionContext, Future}

// all constructors only visible from amf. Users should always use builders or defaults
class AmfEnvironment(val resolvers: AmfResolvers,
                     val logger: AmfLogger,
                     val listeners: List[EventListener],
                     val platform: Platform,
                     val executionContext: ExecutionContext,
                     val idGenerator: AmfIdGenerator) { // break platform into more specific classes?

}

// environment class
class AmfResolvers(val resourceLoader: Seq[ResourceLoader], val referencesResolver: Option[ReferenceResolver]) {

  def resolveContent(url: String)(implicit executionContext: ExecutionContext): Future[Content] = {
    loaderConcat(url, resourceLoader.filter(_.accepts(url)))
  }

  private def loaderConcat(url: String, loaders: Seq[ResourceLoader])(
      implicit executionContext: ExecutionContext): Future[Content] = loaders.toList match {
    case Nil         => throw new UnsupportedUrlScheme(url)
    case head :: Nil => head.fetch(url)
    case head :: tail =>
      head.fetch(url).recoverWith {
        case _ => loaderConcat(url, tail)
      }
  }

}
