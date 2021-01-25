package amf.client.`new`.builder

import amf.client.`new`.amfcore.AmfParsePlugin

import scala.collection.mutable.ListBuffer

class WebApiInstanceBuilder extends AmfInstanceBuilder {

  override val registry: AmfRegistryBuilder = AmfWebApiRegistryBuilder
}

// compose with different interfaces?
object AmfWebApiRegistryBuilder extends AmfRegistryBuilder {

  override protected val parsePlugins: ListBuffer[AmfParsePlugin] = ListBuffer() //ramlplug, oas plugin etc)
  override protected val entityBuilder: AmfEntityIndexer          = AmfCompleteEntityIndexer
}

object RamlWebApiRegistryBuilder extends AmfRegistryBuilder {
  override protected val parsePlugins: ListBuffer[AmfParsePlugin] = ListBuffer() // only raml plugin
}
