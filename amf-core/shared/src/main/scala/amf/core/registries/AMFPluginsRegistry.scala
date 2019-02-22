package amf.core.registries

import amf.client.plugins._
import amf.core.validation.AMFPayloadValidationPlugin

import scala.collection.mutable

object AMFPluginsRegistry {

  private val syntaxPluginIDRegistry: mutable.HashMap[String, AMFSyntaxPlugin]               = mutable.HashMap()
  private val syntaxPluginRegistry: mutable.HashMap[String, AMFSyntaxPlugin]                 = mutable.HashMap()
  private val documentPluginRegistry: mutable.HashMap[String, Seq[AMFDocumentPlugin]]        = mutable.HashMap()
  private val documentPluginIDRegistry: mutable.HashMap[String, AMFDocumentPlugin]           = mutable.HashMap()
  private val documentPluginVendorsRegistry: mutable.HashMap[String, Seq[AMFDocumentPlugin]] = mutable.HashMap()
  private val domainPluginRegistry: mutable.HashMap[String, AMFDomainPlugin]                 = mutable.HashMap()
  private val featurePluginIDRegistry: mutable.HashMap[String, AMFFeaturePlugin]             = mutable.HashMap()
  private val featurePlugin: mutable.HashMap[String, AMFFeaturePlugin]                       = mutable.HashMap()
  private val payloadValidationPluginRegistry: mutable.HashMap[String, Seq[AMFPayloadValidationPlugin]] =
    mutable.HashMap()
  private val payloadValidationPluginIDRegistry: mutable.HashMap[String, AMFPayloadValidationPlugin] =
    mutable.HashMap()

  def plugins: Iterable[AMFPlugin] =
    syntaxPluginIDRegistry.values ++ documentPluginIDRegistry.values ++ domainPluginRegistry.values ++ featurePluginIDRegistry.values

  def documentPlugins: Iterable[AMFDocumentPlugin] = documentPluginIDRegistry.values

  def registerSyntaxPlugin(syntaxPlugin: AMFSyntaxPlugin): Unit = {
    syntaxPluginIDRegistry.get(syntaxPlugin.ID) match {
      case Some(_) => // ignore
      case None =>
        syntaxPluginIDRegistry.put(syntaxPlugin.ID, syntaxPlugin)
        syntaxPlugin.supportedMediaTypes().foreach { mediaType =>
          syntaxPluginRegistry.get(mediaType) match {
            case Some(plugin) if plugin.ID == syntaxPlugin.ID => // ignore
            case None                                         => syntaxPluginRegistry.put(mediaType, syntaxPlugin)
            case Some(plugin) =>
              throw new Exception(
                s"Cannot register ${syntaxPlugin.ID} for media type $mediaType, ${plugin.ID} already registered")
          }
        }
        registerDependencies(syntaxPlugin)
    }
  }

  def cleanMediaType(mediaType: String): String =
    if (mediaType.contains(";")) {
      mediaType.split(";").head
    } else {
      mediaType
    }

  def syntaxPluginForMediaType(mediaType: String): Option[AMFSyntaxPlugin] = {
    val normalizedMediaType = cleanMediaType(mediaType)
    syntaxPluginRegistry.get(mediaType) match {
      case Some(plugin) => Some(plugin)
      case _            => syntaxPluginRegistry.get(simpleMediaType(normalizedMediaType))
    }
  }

  def registerFeaturePlugin(featurePlugin: AMFFeaturePlugin): Unit = {
    featurePluginIDRegistry.get(featurePlugin.ID) match {
      case Some(_) => // ignore
      case None =>
        featurePluginIDRegistry.put(featurePlugin.ID, featurePlugin)
        registerDependencies(featurePlugin)
    }
  }

  def featurePlugins(): Seq[AMFFeaturePlugin] = featurePluginIDRegistry.values.toSeq

  def registerDocumentPlugin(documentPlugin: AMFDocumentPlugin): Unit = {
    documentPluginIDRegistry.get(documentPlugin.ID) match {
      case Some(_) => // ignore
      case None =>
        documentPluginIDRegistry.put(documentPlugin.ID, documentPlugin)

        documentPlugin.serializableAnnotations().foreach {
          case (name, unloader) =>
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

        documentPlugin.modelEntities.foreach { entity =>
          AMFDomainRegistry.registerModelEntity(entity)
        }
        documentPlugin.modelEntitiesResolver.foreach(resolver =>
          AMFDomainRegistry.registerModelEntityResolver(resolver))

        registerDependencies(documentPlugin)
    }
  }

  def documentPluginForMediaType(mediaType: String): Seq[AMFDocumentPlugin] = {
    documentPluginRegistry.getOrElse(mediaType, Seq()).sortBy(_.priority)
  }

  def dataNodeValidatorPluginForMediaType(mediaType: String): Seq[AMFPayloadValidationPlugin] =
    payloadValidationPluginRegistry.getOrElse(mediaType, Nil)

  def documentPluginForID(ID: String): Option[AMFDocumentPlugin] = {
    documentPluginIDRegistry.get(ID)
  }

  def documentPluginForVendor(vendor: String): Seq[AMFDocumentPlugin] = {
    documentPluginVendorsRegistry.getOrElse(vendor, Seq()).sortBy(_.priority)
  }

  def registerDomainPlugin(domainPlugin: AMFDomainPlugin): Unit = {
    domainPluginRegistry.get(domainPlugin.ID) match {
      case Some(_) => // ignore
      case None =>
        domainPlugin.serializableAnnotations().foreach {
          case (name, unloader) =>
            AMFDomainRegistry.registerAnnotation(name, unloader)
        }
        domainPluginRegistry.put(domainPlugin.ID, domainPlugin)

        domainPlugin.modelEntities.foreach { entity =>
          AMFDomainRegistry.registerModelEntity(entity)
        }

        domainPlugin.modelEntitiesResolver match {
          case Some(resolver) => AMFDomainRegistry.registerModelEntityResolver(resolver)
          case _              => // ignore
        }

        registerDependencies(domainPlugin)
    }
  }

  def unregisterDomainPlugin(domainPlugin: AMFDomainPlugin) = {
    domainPluginRegistry.remove(domainPlugin.ID)
    domainPlugin.serializableAnnotations().foreach {
      case (name, unloader) =>
        AMFDomainRegistry.unregisterAnnotaion(name)
    }
    domainPlugin.modelEntities.foreach { entity =>
      AMFDomainRegistry.unregisterModelEntity(entity)
    }

    domainPlugin.modelEntitiesResolver match {
      case Some(resolver) => AMFDomainRegistry.unregisterModelEntityResolver(resolver)
      case _              => // ignore
    }
  }

  def registerPayloadValidationPlugin(validationPlugin: AMFPayloadValidationPlugin): Unit = {
    payloadValidationPluginIDRegistry.get(validationPlugin.ID) match {
      case Some(_) =>
      case None =>
        validationPlugin.payloadMediaType.foreach { mt =>
          payloadValidationPluginRegistry.get(mt) match {
            case Some(list) if !list.contains(validationPlugin) =>
              payloadValidationPluginRegistry.update(mt, list :+ validationPlugin)
              payloadValidationPluginIDRegistry.update(validationPlugin.ID, validationPlugin)
            case None =>
              payloadValidationPluginRegistry.update(mt, Seq(validationPlugin))
              payloadValidationPluginIDRegistry.update(validationPlugin.ID, validationPlugin)
            case Some(_) => // ignore
          }
        }
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

  protected def unregisterDependencies(plugin: AMFPlugin): Unit = {
    plugin.dependencies().foreach {
      case domainPlugin: AMFDomainPlugin     => unregisterDomainPlugin(domainPlugin)
      case documentPlugin: AMFDocumentPlugin => // ignore
      case syntaxPlugin: AMFSyntaxPlugin     => // ignore
      case _                                 => // ignore
    }
  }
}
