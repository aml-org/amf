package amf.core.model.domain.templates

import amf.core.metamodel.domain.templates.AbstractDeclarationModel._
import amf.core.model.domain.{DataNode, DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}


abstract class AbstractDeclaration(fields: Fields, annotations: Annotations) extends DomainElement with Linkable {
  def name: String           = fields(Name)
  def dataNode: DataNode     = fields(DataNode)
  def variables: Seq[String] = fields(Variables)

  def withName(name: String): this.type                = set(Name, name)
  def withDataNode(dataNode: DataNode): this.type      = set(DataNode, dataNode)
  def withVariables(variables: Seq[String]): this.type = set(Variables, variables)

  override def adopted(parent: String): this.type = withId(parent + "/" + name)
}
