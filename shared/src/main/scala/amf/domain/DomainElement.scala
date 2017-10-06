package amf.domain

import amf.domain.`abstract`.{ParametrizedDeclaration, ParametrizedResourceType, ParametrizedTrait}
import amf.domain.extensions.DomainExtension
import amf.metadata.Field
import amf.metadata.domain.DomainElementModel._
import amf.metadata.domain.LinkableElementModel
import amf.model.{AmfElement, AmfObject}
import amf.vocabulary.ValueType

trait Linkable extends AmfObject { this: DomainElement with Linkable =>
  var linkTarget: Option[DomainElement]    = None
  var linkAnnotations: Option[Annotations] = None

  def isLink: Boolean           = linkTarget.isDefined
  def linkLabel: Option[String] = Option(fields(LinkableElementModel.Label))

  def linkCopy(): Linkable

  def withLinkTarget(target: DomainElement): this.type = {
    linkTarget = Some(target)
    set(LinkableElementModel.TargetId, target.id)
  }

  def withLinkLabel(label: String): this.type = set(LinkableElementModel.Label, label)

  def link[T](label: Option[String] = None, annotations: Option[Annotations] = None): T = {
    val href = linkCopy()
    href.withLinkTarget(this)
    label.map(href.withLinkLabel)

    href.asInstanceOf[T]
  }
}

/**
  * Internal model for any domain element
  */
trait DomainElement extends AmfObject {
  def customDomainProperties: Seq[DomainExtension] = fields(CustomDomainProperties)
  def extend: Seq[ParametrizedDeclaration]         = fields(Extends)

  def withCustomDomainProperties(customProperties: Seq[DomainExtension]): this.type =
    setArray(CustomDomainProperties, customProperties)

  def withExtends(extend: Seq[ParametrizedDeclaration]): this.type = setArray(Extends, extend)

  def withResourceType(name: String): ParametrizedResourceType = {
    val result = ParametrizedResourceType().withName(name)
    add(Extends, result)
    result
  }

  def withTrait(name: String): ParametrizedTrait = {
    val result = ParametrizedTrait().withName(name)
    add(Extends, result)
    result
  }
}

trait DynamicDomainElement extends DomainElement {
  def dynamicFields: List[Field]
  def dynamicType: List[ValueType]

  // this is used to generate the graph
  def valueForField(f: Field): Option[AmfElement]
}
