package amf.core

import amf.client.plugins.{AMFDocumentPlugin, AMFSyntaxPlugin}
import amf.core.benchmark.ExecutionLog
import amf.core.emitter.{RenderOptions, YDocumentBuilder}
import amf.core.model.document.{BaseUnit, ExternalFragment}
import amf.core.parser.{ParsedDocument, SyamlParsedDocument}
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Platform
import amf.core.services.RuntimeSerializer
import org.mulesoft.common.io.Output
import org.mulesoft.common.io.Output._

import scala.concurrent.{ExecutionContext, Future}

class AMFSerializer(unit: BaseUnit, mediaType: String, vendor: String, options: RenderOptions) {

  def make(): ParsedDocument = {
    val domainPlugin = getDomainPlugin
    domainPlugin.unparse(unit, options) match {
      case Some(ast) => ast
      case None      => throw new Exception(s"Error unparsing syntax $mediaType with domain plugin ${domainPlugin.ID}")
    }
  }

  def renderAsYDocument(): SyamlParsedDocument = {
    val domainPlugin = getDomainPlugin
    val builder      = new YDocumentBuilder
    if (domainPlugin.emit(unit, builder, options)) builder.result
    else throw new Exception(s"Error unparsing syntax $mediaType with domain plugin ${domainPlugin.ID}")
  }

  /** Print ast to writer. */
  def renderToWriter[W: Output](writer: W)(implicit executor: ExecutionContext): Future[Unit] = Future(render(writer))

  /** Print ast to string. */
  def renderToString: Future[String] = {
    Future(render())(scala.concurrent.ExecutionContext.Implicits.global)
  }

  /** Print ast to file. */
  def renderToFile(remote: Platform, path: String): Future[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Future(render()).map(remote.write(path, _))
  }

  private def render[W: Output](writer: W): Unit =
    parsed { (syntaxPlugin, mediaType, ast) => syntaxPlugin.unparse(mediaType, ast, writer)
    } match {
      case Some(_) =>
      case None if unit.isInstanceOf[ExternalFragment] =>
        writer.append(unit.asInstanceOf[ExternalFragment].encodes.raw.value())
      case _ => throw new Exception(s"Unsupported media type $mediaType and vendor $vendor")
    }

  private def parsed[T](unparse: (AMFSyntaxPlugin, String, ParsedDocument) => Option[T]): Option[T] = {
    ExecutionLog.log(s"AMFSerializer#render: Rendering to $mediaType ($vendor file) ${unit.location()}")
    val ast = make()

    // Let's try to find a syntax plugin for the media type and vendor
    AMFPluginsRegistry.syntaxPluginForMediaType(mediaType) match {
      case Some(syntaxPlugin) => unparse(syntaxPlugin, mediaType, ast)
      case None               =>
        // media type not directly supported, maybe it is supported by the media types of the accepted domain plugin
        findDomainPlugin() match {
          case Some(domainPlugin) =>
            domainPlugin.documentSyntaxes.collectFirst[(String, Option[AMFSyntaxPlugin])] {
              case mediaType: String =>
                (mediaType, AMFPluginsRegistry.syntaxPluginForMediaType(mediaType))
            } flatMap {
              case (effectiveMediaType, Some(syntaxPlugin)) => unparse(syntaxPlugin, effectiveMediaType, ast)
              case _                                        => None
            }
          case None => None
        }
      case _ => None
    }
  }

  private def render(): String =
    parsed { (syntaxPlugin, mediaType, ast) => syntaxPlugin.unparse(mediaType, ast)
    } match {
      case Some(doc)                                   => doc.toString
      case None if unit.isInstanceOf[ExternalFragment] => unit.asInstanceOf[ExternalFragment].encodes.raw.value()
      case _                                           => throw new Exception(s"Unsupported media type $mediaType and vendor $vendor")
    }

  protected def findDomainPlugin(): Option[AMFDocumentPlugin] =
    AMFPluginsRegistry.documentPluginForVendor(vendor).find { plugin =>
      plugin.documentSyntaxes.contains(mediaType) && plugin.canUnparse(unit)
    } match {
      case Some(domainPlugin) => Some(domainPlugin)
      case None               => AMFPluginsRegistry.documentPluginForMediaType(mediaType).find(_.canUnparse(unit))
    }

  private def getDomainPlugin: AMFDocumentPlugin =
    findDomainPlugin().getOrElse {
      throw new Exception(
        s"Cannot serialize domain model '${unit.location()}' for detected media type $mediaType and vendor $vendor")
    }
}

object AMFSerializer {
  def init(): Unit = {
    if (RuntimeSerializer.serializer.isEmpty) {
      RuntimeSerializer.register(new RuntimeSerializer {
        override def dump(unit: BaseUnit, mediaType: String, vendor: String, options: RenderOptions): String =
          new AMFSerializer(unit, mediaType, vendor, options).render()

        override def dumpToFile(platform: Platform,
                                file: String,
                                unit: BaseUnit,
                                mediaType: String,
                                vendor: String,
                                options: RenderOptions): Future[Unit] =
          new AMFSerializer(unit, mediaType, vendor, options).renderToFile(platform, file)
      })
    }
  }
}
