package amf.plugins.domain.shapes.models

import amf.core.model.{BoolField, StrField}
import amf.core.model.domain.DomainElement
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.XMLSerializerModel
import amf.plugins.domain.shapes.metamodel.XMLSerializerModel._
import org.yaml.model.{YMap, YNode}

case class XMLSerializer(fields: Fields, annotations: Annotations) extends DomainElement {

  def attribute: BoolField = fields.field(Attribute)
  def wrapped: BoolField   = fields.field(Wrapped)
  def name: StrField       = fields.field(Name)
  def namespace: StrField  = fields.field(Namespace)
  def prefix: StrField     = fields.field(Prefix)

  def withAttribute(attribute: Boolean): this.type = set(Attribute, attribute)
  def withWrapped(wrapped: Boolean): this.type     = set(Wrapped, wrapped)
  def withName(name: String): this.type            = set(Name, name)
  def withNamespace(namespace: String): this.type  = set(Namespace, namespace)
  def withPrefix(prefix: String): this.type        = set(Prefix, prefix)

  override def meta = XMLSerializerModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/xml"
}

object XMLSerializer {

  def apply(): XMLSerializer = apply(Annotations())

  def apply(ast: YMap): XMLSerializer = apply(Annotations(ast))

  def apply(node: YNode): XMLSerializer = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): XMLSerializer = XMLSerializer(Fields(), annotations)
}
