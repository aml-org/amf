package amf.xml.internal.spec.context

import amf.aml.client.scala.model.domain.{NodeMapping, PropertyMapping}
import amf.core.client.scala.parse.document.ParserContext
import amf.core.client.scala.vocabulary.Namespace

import scala.collection.mutable
import scala.xml.Elem

class XMLDocContext(parserContext: ParserContext, namespaces: mutable.Map[String,String] = mutable.HashMap[String,String]()) extends ParserContext(parserContext.rootContextDocument, parserContext.refs, parserContext.futureDeclarations, parserContext.config) {

  val nodeMappingCache = mutable.Map[String,NodeMapping]()
  val TEXT_PROPERTY_IRI = (Namespace.Data + "text").iri()

  def nodeMappingFor(instanceClass: String): NodeMapping = {
    nodeMappingCache.get(instanceClass) match {
      case Some(mapping) => mapping
      case None          =>
        val textProperty = PropertyMapping().withId(TEXT_PROPERTY_IRI).withNodePropertyMapping(TEXT_PROPERTY_IRI).withName("xmlText").withLiteralRange((Namespace.Xsd + "string").iri())
        val nodeMapping = NodeMapping().withPropertiesMapping(Seq(textProperty))
        nodeMappingCache.put(instanceClass, nodeMapping)
        nodeMapping
    }
  }


  def registerNamespaces(elem: Elem): Unit = {
    val prefix = elem.prefix
    if (!namespaces.keySet.contains(prefix)) {
      namespaces.put(elem.prefix, elem.namespace)
    }

    var binding = elem.scope
    while (binding != null) {
      if (!namespaces.keySet.contains(binding.prefix)) {
        namespaces.put(binding.prefix, binding.uri)
      }
      binding = binding.parent
    }
  }

  def resolveNamespace(key: String): String = namespaces.getOrElse(key, "")

}
