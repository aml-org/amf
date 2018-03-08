package amf.core.model.domain.templates

import amf.client.model.StrField
import amf.core.metamodel.domain.templates.AbstractDeclarationModel._
import amf.core.model.domain.{DataNode, DomainElement, Linkable, NamedDomainElement}
import amf.core.parser.{Annotations, Fields}

abstract class AbstractDeclaration(fields: Fields, annotations: Annotations)
    extends DomainElement
    with Linkable
    with NamedDomainElement {

  def name: StrField           = fields.field(Name)
  def dataNode: DataNode       = fields.field(DataNode)
  def variables: Seq[StrField] = fields.field(Variables)

  def withName(name: String): this.type                = set(Name, name)
  def withDataNode(dataNode: DataNode): this.type      = set(DataNode, dataNode)
  def withVariables(variables: Seq[String]): this.type = set(Variables, variables)

  override def adopted(parent: String): this.type = withId(parent + "/" + name.value())
}
