package amf.core

import amf.client.render.RenderOptions
import amf.core.model.document.{BaseUnit, ExternalFragment}
import amf.core.model.domain.AmfElement
import amf.core.plugins.{AMFDocumentPlugin, AMFSyntaxPlugin}
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Platform
import amf.core.services.RuntimeSerializer
import org.yaml.model.YDocument

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait ASTMaker[T <: AmfElement] {
  val mediaType: String

  def make(): YDocument
  val domainPlugin: Option[AMFDocumentPlugin]
  val element: T
  val vendor: String
  val options: RenderOptions
}

case class CommonASTMaker(element: BaseUnit,
                          override val options: RenderOptions,
                          override val vendor: String,
                          override val mediaType: String)
    extends ASTMaker[BaseUnit] {
  def make(): YDocument = {
    domainPlugin match {
      case Some(dp) =>
        dp.unparse(element, options) match {
          case Some(ast) => ast
          case None      => throw new Exception(s"Error unparsing syntax $mediaType with domain plugin ${dp.ID}")
        }
      case None =>
        throw new Exception(
          s"Cannot serialize domain model '${element.location}' for detected media type $mediaType and vendor $vendor")
    }
  }

  override val domainPlugin: Option[AMFDocumentPlugin] = {
    AMFPluginsRegistry.documentPluginForVendor(vendor).find { plugin =>
      plugin.documentSyntaxes.contains(mediaType) && plugin.canUnparse(element)
    } match {
      case Some(plugin) => Some(plugin)
      case None         => AMFPluginsRegistry.documentPluginForMediaType(mediaType).find(_.canUnparse(element))
    }
  }

}

class AMFSerializer(maker: ASTMaker[_ <: AmfElement]) {

  /** Print ast to string. */
  def renderToString: Future[String] = Future { dump() }

  /** Print ast to file. */
  def renderToFile(remote: Platform, path: String): Future[Unit] = remote.write(path, dump())

  protected def dump(): String = {
    val ast = maker.make()

    // Let's try to find a syntax plugin for the media type and vendor
    val parsed: Option[CharSequence] = AMFPluginsRegistry.syntaxPluginForMediaType(maker.mediaType) match {
      case Some(syntaxPlugin) => syntaxPlugin.unparse(maker.mediaType, ast)
      case None               =>
        // media type not directly supported, maybe it is supported by the media types of the accepted domain plugin
        maker.domainPlugin match {
          case Some(dp) =>
            dp.documentSyntaxes.collectFirst[(String, Option[AMFSyntaxPlugin])] {
              case mediaType: String =>
                (mediaType, AMFPluginsRegistry.syntaxPluginForMediaType(mediaType))
            } flatMap {
              case (effectiveMediaType, Some(syntaxPlugin)) => syntaxPlugin.unparse(effectiveMediaType, ast)
              case _                                        => None
            }
          case None => None
        }
      case _ => None
    }
    parsed match {
      case Some(doc) => doc.toString
      case None if maker.element.isInstanceOf[ExternalFragment] =>
        maker.element.asInstanceOf[ExternalFragment].encodes.raw.value()
      case _ => throw new Exception(s"Unsupported media type ${maker.mediaType} and vendor ${maker.vendor}")
    }
  }
}

object AMFSerializer {
  def init() = {
    if (RuntimeSerializer.serializer.isEmpty) {
      RuntimeSerializer.register(new RuntimeSerializer {
        override def dump(unit: BaseUnit, mediaType: String, vendor: String, options: RenderOptions): String =
          apply(unit, mediaType, vendor, options).dump()

        override def dumpToFile(platform: Platform,
                                file: String,
                                unit: BaseUnit,
                                mediaType: String,
                                vendor: String,
                                options: RenderOptions) =
          apply(unit, mediaType, vendor, options).renderToFile(platform, file)
      })
    }
  }

  def apply(unit: BaseUnit, mediaType: String, vendor: String, options: RenderOptions): AMFSerializer =
    new AMFSerializer(CommonASTMaker(unit, options, vendor, mediaType))
}
