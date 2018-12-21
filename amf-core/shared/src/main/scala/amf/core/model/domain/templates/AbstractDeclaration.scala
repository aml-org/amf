package amf.core.model.domain.templates

import amf.core.metamodel.Field
import amf.core.metamodel.domain.templates.AbstractDeclarationModel._
import amf.core.model.StrField
import amf.core.model.domain.{DataNode, DomainElement, Linkable, NamedDomainElement}
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.Strings

abstract class AbstractDeclaration(fields: Fields, annotations: Annotations)
    extends DomainElement
    with Linkable
    with NamedDomainElement {

  def description: StrField    = fields.field(Description)
  def dataNode: DataNode       = fields.field(DataNode)
  def variables: Seq[StrField] = fields.field(Variables)

  def withDataNode(dataNode: DataNode): this.type      = set(DataNode, dataNode)
  def withVariables(variables: Seq[String]): this.type = set(Variables, variables)
  def withDescription(description: String): this.type  = set(Description, description)

  override def componentId: String        = "/" + name.option().getOrElse("default-abstract").urlComponentEncoded
  override protected def nameField: Field = Name
}
