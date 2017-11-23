package amf.shape

import amf.domain.Fields
import amf.framework.model.domain.DomainElement
import amf.framework.parser.Annotations
import amf.metadata.shape.XMLSerializerModel
import amf.metadata.shape.XMLSerializerModel._
import org.yaml.model.YMap

case class XMLSerializer(fields: Fields, annotations: Annotations) extends DomainElement {
  def attribute: Boolean = fields(Attribute)
  def wrapped: Boolean   = fields(Wrapped)
  def name: String       = fields(Name)
  def namespace: String  = fields(Namespace)
  def prefix: String     = fields(Prefix)

  def withAttribute(attribute: Boolean): this.type = set(Attribute, attribute)
  def withWrapped(wrapped: Boolean): this.type     = set(Wrapped, wrapped)
  def withName(name: String): this.type            = set(Name, name)
  def withNamespace(namespace: String): this.type  = set(Namespace, namespace)
  def withPrefix(prefix: String): this.type        = set(Prefix, prefix)

  /** Call after object has been adopted by specified parent. */
  override def adopted(parent: String): this.type = withId(parent + "/xml")

  override def meta = XMLSerializerModel
}

object XMLSerializer {

  def apply(): XMLSerializer = apply(Annotations())

  def apply(ast: YMap): XMLSerializer = apply(Annotations(ast))

  def apply(annotations: Annotations): XMLSerializer = XMLSerializer(Fields(), annotations)
}
