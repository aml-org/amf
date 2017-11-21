package amf.core

import amf.client.GenerationOptions
import amf.document.BaseUnit
import amf.domain.extensions.idCounter
import amf.framework.plugins.AMFSyntaxPlugin
import amf.plugins.domain.graph.AMFGraphPlugin
import amf.plugins.domain.payload.PayloadPlugin
import amf.plugins.domain.vocabularies.RAMLExtensionsPlugin
import amf.plugins.domain.webapi.{OAS20Plugin, RAML10Plugin}
import amf.plugins.syntax.SYamlSyntaxPlugin
import amf.remote.Platform
import org.yaml.model.YDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AMFSerializer(unit: BaseUnit, mediaType: String, vendor: String, options: GenerationOptions) {

  // temporary
  AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
  AMFPluginsRegistry.registerDomainPlugin(AMFGraphPlugin)
  AMFPluginsRegistry.registerDomainPlugin(PayloadPlugin)
  AMFPluginsRegistry.registerDomainPlugin(RAMLExtensionsPlugin)
  AMFPluginsRegistry.registerDomainPlugin(OAS20Plugin)
  AMFPluginsRegistry.registerDomainPlugin(RAML10Plugin)
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
          case Some(domainPlugin) => domainPlugin.domainSyntaxes.collectFirst[(String,Option[AMFSyntaxPlugin])] { case mediaType: String =>
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
    AMFPluginsRegistry.domainPluginForVendor(vendor).find { plugin =>
      plugin.domainSyntaxes.contains(mediaType) && plugin.canUnparse(unit)
    } match {
      case Some(domainPlugin) => Some(domainPlugin)
      case None => AMFPluginsRegistry.domainPluginForMediaType(mediaType).find(_.canUnparse(unit))
    }
  }
}
