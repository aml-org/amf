package amf.core

import amf.client.GenerationOptions
import amf.framework.model.document.BaseUnit
import amf.domain.extensions.idCounter
import amf.framework.plugins.AMFSyntaxPlugin
import amf.framework.registries.AMFPluginsRegistry
import amf.framework.services.RuntimeSerializer
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.vocabularies.RAMLExtensionsPlugin
import amf.plugins.document.webapi.{OAS20Plugin, PayloadPlugin, RAML10Plugin}
import amf.plugins.syntax.SYamlSyntaxPlugin
import amf.remote.Platform
import org.yaml.model.YDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AMFSerializer(unit: BaseUnit, mediaType: String, vendor: String, options: GenerationOptions) {

  // temporary
  AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
  AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
  AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
  AMFPluginsRegistry.registerDocumentPlugin(RAMLExtensionsPlugin)
  AMFPluginsRegistry.registerDocumentPlugin(OAS20Plugin)
  AMFPluginsRegistry.registerDocumentPlugin(RAML10Plugin)
  //

  def make(): YDocument = {
    findDomainPlugin() match {
      case Some(domainPlugin) =>  domainPlugin.unparse(unit, options) match {
        case Some(ast) => ast
        case None      => throw new Exception(s"Error unparsing syntax $mediaType with domain plugin ${domainPlugin.ID}")
      }
      case None => throw new Exception(s"Cannot parse domain model for media type $mediaType and vendor $vendor")
    }
  }

  /** Print ast to string. */
  def dumpToString: String = dump()

  /** Print ast to file. */
  def dumpToFile(remote: Platform, path: String): Future[Unit] = remote.write(path, dump())


  protected def dump(): String = {
    // reset data node counter
    idCounter.reset()

    val ast = make()

    // Let's try to find a syntax plugin for the media type and vendor
    val parsed: Option[CharSequence] = AMFPluginsRegistry.syntaxPluginForMediaType(mediaType) match {
      case Some(syntaxPlugin) => syntaxPlugin.unparse(mediaType, ast)
      case None =>
        // media type not directly supported, maybe it is supported by the media types of the accepted domain plugin
        findDomainPlugin() match {
          case Some(domainPlugin) => domainPlugin.documentSyntaxes.collectFirst[(String,Option[AMFSyntaxPlugin])] { case mediaType: String =>
            (mediaType, AMFPluginsRegistry.syntaxPluginForMediaType(mediaType))
          } flatMap  {
            case (effectiveMediaType, Some(syntaxPlugin)) => syntaxPlugin.unparse(effectiveMediaType, ast)
            case _ => None
          }
          case None => None
        }
      case _ => None
    }
    parsed match {
      case Some(doc) => doc.toString
      case _         => throw new Exception(s"Unsupported media type $mediaType and vendor $vendor")
    }
  }

  protected def findDomainPlugin() = {
    AMFPluginsRegistry.documentPluginForVendor(vendor).find { plugin =>
      plugin.documentSyntaxes.contains(mediaType) && plugin.canUnparse(unit)
    } match {
      case Some(domainPlugin) => Some(domainPlugin)
      case None => AMFPluginsRegistry.documentPluginForMediaType(mediaType).find(_.canUnparse(unit))
    }
  }
}

object AMFSerializer {
  def init() = {
    if (RuntimeSerializer.serializer.isEmpty) {
      RuntimeSerializer.register(new RuntimeSerializer {
        override def dump(unit: BaseUnit, mediaType: String, vendor: String, options: GenerationOptions): String = new AMFSerializer(unit, mediaType, vendor, options).dump()
      })
    }
  }
}