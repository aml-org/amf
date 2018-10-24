package amf.client.model.domain

import amf.client.convert.VocabulariesClientConverter
import amf.client.convert.VocabulariesClientConverter._
import amf.core.model.BoolField
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies.model.domain.{DialectDomainElement => InternalDialectDomainElement}
import org.yaml.model.YNode

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class DialectDomainElement(override private[amf] val _internal: InternalDialectDomainElement) extends DomainElement {

  @JSExportTopLevel("model.domain.DialectDomainElement")
  def this() = this(InternalDialectDomainElement())

  def isAbstract(): BoolField = _internal.isAbstract
  def withAbstract(isAbstract: Boolean): DialectDomainElement = {
    _internal.withAbstract(isAbstract)
    this
  }

  def withInstanceTypes(types: ClientList[String]): DialectDomainElement = {
    _internal.withInstanceTypes(types.asInternal)
    this
  }

  def withDefinedby(nodeMapping: NodeMapping): DialectDomainElement = {
    _internal.withDefinedBy(nodeMapping._internal)
    this
  }

  def definedBy(): NodeMapping = NodeMapping(_internal.definedBy)

  def localRefName(): String = _internal.localRefName

  def includeName(): String = _internal.includeName

  def setObjectProperty(propertyId: String, value: DialectDomainElement): InternalDialectDomainElement = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setObjectField(mapping, value._internal, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setObjectCollectionProperty(propertyId: String, value: ClientList[DialectDomainElement]): InternalDialectDomainElement = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setObjectField(mapping, value.asInternal, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def getTypeUris(): ClientList[String] = _internal.dynamicType.map(_.iri()).asClient

  def getPropertyUris(): ClientList[String] = _internal.dynamicFields.map(_.value.iri()).asClient

  def getScalarByPropertyUri(propertyId: String): ClientList[Any] = {
    val expanded = Namespace.expand(propertyId).iri()
    val res: Option[Seq[Any]] = _internal.findPropertyMappingByTermPropertyId(expanded) match {
      case Some(mapping) =>
        _internal.literalProperties.get(mapping.id) flatMap {
          case Some(res: Seq[_]) => Some(res)
          case Some(value)       => Some(Seq(value))
          case None              =>
            _internal.mapKeyProperties.get(mapping.id) flatMap  {
              case Some(value) => Some(Seq(value))
              case _           => None
            }
        }
      case _ =>
        None
    }
    res.getOrElse(Nil).asClient
  }


  def getObjectPropertyUri(propertyId: String): ClientList[DialectDomainElement] = {
    val expanded = Namespace.expand(propertyId).iri()
    val res: Option[Seq[InternalDialectDomainElement]] = _internal.findPropertyMappingByTermPropertyId(expanded) match {
      case Some(mapping) =>
        _internal.objectProperties.get(mapping.id) match {
          case Some(value: InternalDialectDomainElement) => Some(Seq(DialectDomainElement(value)))
          case None                                      => None
            _internal.objectCollectionProperties.get(mapping.id).map { elems =>
              elems.map(elem => elem)
            }
        }
      case _ =>
        None
    }
    res.getOrElse(Nil).asClient
  }


  def setLiteralProperty(propertyId: String, value: String): InternalDialectDomainElement = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setLiteralField(mapping, value, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setLiteralProperty(propertyId: String, value: Int): InternalDialectDomainElement = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setLiteralField(mapping, value, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setLiteralProperty(propertyId: String, value: Double): InternalDialectDomainElement = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setLiteralField(mapping, value, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setLiteralProperty(propertyId: String, value: Float): InternalDialectDomainElement = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setLiteralField(mapping, value, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setLiteralProperty(propertyId: String, value: Boolean): InternalDialectDomainElement = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setLiteralField(mapping, value, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setLiteralProperty(propertyId: String, value: ClientList[Any]): InternalDialectDomainElement = {
    _internal.findPropertyMappingByTermPropertyId(Namespace.expand(propertyId).iri()) match {
      case Some(mapping) =>
        _internal.setLiteralField(mapping, value.asInternal, YNode.Empty)
      case None =>
        throw new Exception(s"Cannot find node mapping for propertyId $propertyId")
    }
  }

  def setMapKeyProperty(propertyId: String, value: String): InternalDialectDomainElement = {
    _internal.setMapKeyField(propertyId, value, YNode.Empty)
  }

}
