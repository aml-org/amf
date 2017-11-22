package amf.core

import amf.framework.plugins.{AMFDomainPlugin, AMFSyntaxPlugin}

import scala.collection.mutable

object AMFPluginsRegistry {

  private val syntaxPluginRegistry: mutable.HashMap[String, AMFSyntaxPlugin] = mutable.HashMap()
  private val domainPluginRegistry: mutable.HashMap[String, Seq[AMFDomainPlugin]] = mutable.HashMap()
  private val domainPluginIDRegistry: mutable.HashMap[String, AMFDomainPlugin] = mutable.HashMap()
  private val domainPluginVendorsRegistry: mutable.HashMap[String, Seq[AMFDomainPlugin]] = mutable.HashMap()

  def domainPlugins = domainPluginIDRegistry.values

  def registerSyntaxPlugin(syntaxPlugin: AMFSyntaxPlugin) = {
    syntaxPlugin.supportedMediaTypes().foreach { mediaType =>
      syntaxPluginRegistry.get(mediaType) match {
        case Some(plugin) if plugin.ID == syntaxPlugin.ID => // ignore
        case None         => syntaxPluginRegistry.put(mediaType, syntaxPlugin)
        case Some(plugin) => throw new Exception(s"Cannot register ${syntaxPlugin.ID} for media type $mediaType, ${plugin.ID} already registered")
      }
    }
  }

  def syntaxPluginForMediaType(mediaType: String): Option[AMFSyntaxPlugin] = {
    syntaxPluginRegistry.get(mediaType) match {
      case Some(plugin) => Some(plugin)
      case _ => syntaxPluginRegistry.get(simpleMediaType(mediaType))
    }
  }

  def registerDomainPlugin(domainPlugin: AMFDomainPlugin) = {
    domainPluginIDRegistry.get(domainPlugin.ID) match {
      case None         => {
        domainPluginIDRegistry.put(domainPlugin.ID, domainPlugin)

        domainPlugin.domainSyntaxes.foreach { mediaType =>
          val plugins = domainPluginRegistry.getOrElse(mediaType, Seq())
          domainPluginRegistry.put(mediaType, plugins ++ Seq(domainPlugin))
        }

        domainPlugin.vendors.foreach { vendor =>
          val plugins = domainPluginVendorsRegistry.getOrElse(vendor, Seq())
          domainPluginVendorsRegistry.put(vendor, plugins ++ Seq(domainPlugin))
        }
      }

      case Some(plugin) => // ignore
    }
  }

  def domainPluginForMediaType(mediaType: String): Seq[AMFDomainPlugin] = {
    domainPluginRegistry.getOrElse(mediaType, Seq())
  }

  def domainPluginForID(ID: String): Option[AMFDomainPlugin] = {
    domainPluginIDRegistry.get(ID)
  }

  def domainPluginForVendor(vendor: String): Seq[AMFDomainPlugin] = {
    domainPluginVendorsRegistry.getOrElse(vendor, Seq())
  }

  protected def simpleMediaType(mediaType: String): String = {
    mediaType.split("/") match {
      case Array(main, sub) if sub.indexOf("+") > -1 => // application/raml+yaml
        main + "/" + sub.split("\\+").last
      case Array(main, sub) if sub.indexOf(".") > -1 => // text/vnd.yaml
        main + "/" + sub.split("\\.").last
      case _ => mediaType
    }
  }
}
