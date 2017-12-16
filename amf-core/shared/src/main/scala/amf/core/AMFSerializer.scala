package amf.core

import amf.core.client.GenerationOptions
import amf.core.model.document.BaseUnit
import amf.core.plugins.AMFSyntaxPlugin
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote.Platform
import amf.core.services.RuntimeSerializer
import org.yaml.model.YDocument

import scala.concurrent.Future

class AMFSerializer(unit: BaseUnit, mediaType: String, vendor: String, options: GenerationOptions) {


  def make(): YDocument = {
    findDomainPlugin() match {
      case Some(domainPlugin) =>  domainPlugin.unparse(unit, options) match {
        case Some(ast) => ast
        case None      => throw new Exception(s"Error unparsing syntax $mediaType with domain plugin ${domainPlugin.ID}")
      }
      case None => throw new Exception(s"Cannot serialize domain model '${unit.location}' for detected media type $mediaType and vendor $vendor")
    }
  }

  /** Print ast to string. */
  def dumpToString: String = dump()

  /** Print ast to file. */
  def dumpToFile(remote: Platform, path: String): Future[Unit] = remote.write(path, dump())


  protected def dump(): String = {
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
        override def dump(unit: BaseUnit, mediaType: String, vendor: String, options: GenerationOptions): String =
          new AMFSerializer(unit, mediaType, vendor, options).dump()

        override def dumpToFile(platform: Platform, file: String, unit: BaseUnit, mediaType: String, vendor: String, options: GenerationOptions) =
          new AMFSerializer(unit, mediaType, vendor, options).dumpToFile(platform, file)
      })
    }
  }
}