package amf.core.model.domain

import amf.core.metamodel.Field
import amf.core.metamodel.domain.ShapeModel.Name
import amf.core.model.StrField
import amf.core.parser.Annotations
import org.yaml.model.YNode

/**
  * All DomainElements supporting name
  */
trait NamedDomainElement extends DomainElement {

  protected def nameField: Field

  /** Return [[DomainElement]] name. */
  def name: StrField = fields.field(nameField)

  /** Update [[DomainElement]] name. */
  def withName(name: String, a: Annotations = Annotations()): this.type = set(nameField, AmfScalar(name, a))
}
