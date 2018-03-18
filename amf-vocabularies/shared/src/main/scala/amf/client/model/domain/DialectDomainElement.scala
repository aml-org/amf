package amf.client.model.domain

import amf.client.convert.VocabulariesClientConverter._
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies.model.domain.{DialectDomainElement => InternalDialectDomainElement}
import org.yaml.model.YNode

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class DialectDomainElement(override private[amf] val _internal: InternalDialectDomainElement) extends DomainElement {

  @JSExportTopLevel("model.domain.DialectDomainElement")
  def this() = this(InternalDialectDomainElement())

  def withInstanceTypes(types: ClientList[String]) = {
    _internal.withInstanceTypes(types.asInternal)
    this
  }

  def dynamicTypes(): ClientList[String] = _internal.dynamicType.map(_.iri()).asClient

  def withDefinedby(nodeMapping: NodeMapping) = {
    _internal.withDefinedBy(nodeMapping._internal)
    this
  }

  def definedBy(): NodeMapping = NodeMapping(_internal.definedBy)

  def localRefName(): String = _internal.localRefName

  def includeName(): String = _internal.includeName

  def setObjectProperty(propertyId: String, value: DialectDomainElement) = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setObjectField(mapping, value._internal, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setObjectCollectionProperty(propertyId: String, value: ClientList[DialectDomainElement]) = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setObjectField(mapping, value.asInternal, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def getObjectProperty(propertyId: String): DialectDomainElement = {
    val expanded = Namespace.expand(propertyId).iri()
    val mapped = _internal.objectProperties.get(expanded) map { elem =>
      DialectDomainElement(elem)
    }
    mapped.orNull
  }

  def getObjectCollectionProperty(propertyId: String): ClientList[DialectDomainElement] = {
    val expanded = Namespace.expand(propertyId).iri()
    val mapped = _internal.objectCollectionProperties.get(expanded) map { elems =>
      elems.asClient
    }
    mapped.orNull
  }

  def setLiteralProperty(propertyId: String, value: String) = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setLiteralField(mapping, value, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setLiteralProperty(propertyId: String, value: Int) = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setLiteralField(mapping, value, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setLiteralProperty(propertyId: String, value: Double) = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setLiteralField(mapping, value, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setLiteralProperty(propertyId: String, value: Float) = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setLiteralField(mapping, value, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setLiteralProperty(propertyId: String, value: Boolean) = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setLiteralField(mapping, value, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setLiteralProperty(propertyId: String, value: ClientList[Any]) = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setLiteralField(mapping, value.asInternal, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setMapKeyProperty(propertyId: String, value: String) = {
    _internal.setMapKeyField(propertyId, value, YNode.Empty)
  }

  def getLiteralProperty(propertyId: String): Any = {
    val expanded = Namespace.expand(propertyId).iri()
    val mapped = _internal.objectProperties.get(expanded)
    mapped.orNull
  }

  def getLiteralCollection(propertyId: String): ClientList[Any] = {
    val expanded = Namespace.expand(propertyId).iri()
    val mapped = _internal.objectCollectionProperties.get(expanded) map { elems =>
      elems.asInstanceOf[Seq[Any]].asClient
    }
    mapped.orNull
  }

}
