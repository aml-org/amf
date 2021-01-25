package amf.client.`new`.builder

import amf.client.`new`.AmfResolvers
import amf.internal.reference.ReferenceResolver
import amf.internal.resource.ResourceLoader

import scala.collection.mutable.ListBuffer

class AmfResolversBuilder {
  private val resourceLoaders: ListBuffer[ResourceLoader]  = ListBuffer.empty
  private val referenceResolver: Option[ReferenceResolver] = None
  def build(): AmfResolvers                                = new AmfResolvers(resourceLoaders.toSeq, referenceResolver)

  def withResourceLoader(rl: ResourceLoader) = {
    resourceLoaders += rl
    this
  }

  def withDefaultResourceLoaders() = {
    // add defaults
    this
  }
}
