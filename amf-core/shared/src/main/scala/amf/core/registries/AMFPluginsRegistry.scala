package amf.core.registries

import amf.core.plugins.{AMFDocumentPlugin, AMFDomainPlugin, AMFPlugin, AMFSyntaxPlugin}

import scala.collection.mutable

object AMFPluginsRegistry {

  private val syntaxPluginIDRegistry: mutable.HashMap[String, AMFSyntaxPlugin] = mutable.HashMap()
  private val syntaxPluginRegistry: mutable.HashMap[String, AMFSyntaxPlugin] = mutable.HashMap()
  private val documentPluginRegistry: mutable.HashMap[String, Seq[AMFDocumentPlugin]] = mutable.HashMap()
  private val documentPluginIDRegistry: mutable.HashMap[String, AMFDocumentPlugin] = mutable.HashMap()
  private val documentPluginVendorsRegistry: mutable.HashMap[String, Seq[AMFDocumentPlugin]] = mutable.HashMap()
  private val domainPluginRegistry: mutable.HashMap[String, AMFDomainPlugin] = mutable.HashMap()



  def documentPlugins = documentPluginIDRegistry.values

  def registerSyntaxPlugin(syntaxPlugin: AMFSyntaxPlugin) = {
    syntaxPluginIDRegistry.get(syntaxPlugin.ID) match {
      case Some(_) => // ignore
      case None =>
        syntaxPluginIDRegistry.put(syntaxPlugin.ID, syntaxPlugin)
        syntaxPlugin.supportedMediaTypes().foreach { mediaType =>
          syntaxPluginRegistry.get(mediaType) match {
            case Some(plugin) if plugin.ID == syntaxPlugin.ID => // ignore
            case None         => syntaxPluginRegistry.put(mediaType, syntaxPlugin)
            case Some(plugin) => throw new Exception(s"Cannot register ${syntaxPlugin.ID} for media type $mediaType, ${plugin.ID} already registered")
          }
        }
        registerDependencies(syntaxPlugin)
    }
  }

  def syntaxPluginForMediaType(mediaType: String): Option[AMFSyntaxPlugin] = {
    syntaxPluginRegistry.get(mediaType) match {
      case Some(plugin) => Some(plugin)
      case _ => syntaxPluginRegistry.get(simpleMediaType(mediaType))
    }
  }

  def registerDocumentPlugin(documentPlugin: AMFDocumentPlugin) = {
    documentPluginIDRegistry.get(documentPlugin.ID) match {
      case Some(_)  => // ignore
      case None     =>
        documentPluginIDRegistry.put(documentPlugin.ID, documentPlugin)

        documentPlugin.serializableAnnotations().foreach { case (name, unloader) =>
          AMFDomainRegistry.registerAnnotation(name, unloader)
        }

        documentPlugin.documentSyntaxes.foreach { mediaType =>
          val plugins = documentPluginRegistry.getOrElse(mediaType, Seq())
          documentPluginRegistry.put(mediaType, plugins ++ Seq(documentPlugin))
        }

        documentPlugin.vendors.foreach { vendor =>
          val plugins = documentPluginVendorsRegistry.getOrElse(vendor, Seq())
          documentPluginVendorsRegistry.put(vendor, plugins ++ Seq(documentPlugin))
        }

        documentPlugin.modelEntities.foreach{ entity => AMFDomainRegistry.registerModelEntity(entity) }

        registerDependencies(documentPlugin)
    }
  }

  def documentPluginForMediaType(mediaType: String): Seq[AMFDocumentPlugin] = {
    documentPluginRegistry.getOrElse(mediaType, Seq())
  }

  def documentPluginForID(ID: String): Option[AMFDocumentPlugin] = {
    documentPluginIDRegistry.get(ID)
  }

  def documentPluginForVendor(vendor: String): Seq[AMFDocumentPlugin] = {
    documentPluginVendorsRegistry.getOrElse(vendor, Seq())
  }

  def registerDomainPlugin(domainPlugin: AMFDomainPlugin) = {
    domainPluginRegistry.get(domainPlugin.ID) match {
      case Some(_) => // ignore
      case None =>
        domainPlugin.serializableAnnotations().foreach { case (name, unloader) =>
          AMFDomainRegistry.registerAnnotation(name, unloader)
        }
        domainPluginRegistry.put(domainPlugin.ID, domainPlugin)

        domainPlugin.modelEntities.foreach{ entity => AMFDomainRegistry.registerModelEntity(entity) }

        registerDependencies(domainPlugin)
    }
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

  protected def registerDependencies(plugin: AMFPlugin): Unit = {
    plugin.dependencies().foreach {
      case domainPlugin: AMFDomainPlugin     => registerDomainPlugin(domainPlugin)
      case documentPlugin: AMFDocumentPlugin => registerDocumentPlugin(documentPlugin)
      case syntaxPlugin: AMFSyntaxPlugin     => registerSyntaxPlugin(syntaxPlugin)
      case _                                 => // ignore
    }
  }
}
