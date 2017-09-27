package amf.domain.`abstract`

import amf.domain.extensions.DataNode
import amf.metadata.domain.`abstract`.AbstractDeclarationModel._
import amf.domain.{Annotations, DomainElement, Fields}
import org.yaml.model.YPart

abstract class AbstractDeclaration(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: String             = fields(Name)
  def dataNode: DataNode       = fields(DataNode)
  def variables: Seq[Variable] = fields(Variables)

  def withName(name: String): this.type                  = set(Name, name)
  def withDataNode(dataNode: DataNode): this.type        = set(DataNode, dataNode)
  def withVariables(variables: Seq[Variable]): this.type = setArray(Variables, variables)

  def withVariable(name: String): Variable = {
    val result = Variable().withName(name)
    add(Variables, result)
    result
  }

  override def adopted(parent: String): this.type = withId(parent + "/" + name)
}

case class ResourceType(fields: Fields, annotations: Annotations) extends AbstractDeclaration(fields, annotations)

object ResourceType {
  def apply(): ResourceType = apply(Annotations())

  def apply(ast: YPart): ResourceType = apply(Annotations(ast))

  def apply(annotations: Annotations): ResourceType = ResourceType(Fields(), annotations)
}

case class Trait(fields: Fields, annotations: Annotations) extends AbstractDeclaration(fields, annotations)

object Trait {
  def apply(): Trait = apply(Annotations())

  def apply(ast: YPart): Trait = apply(Annotations(ast))

  def apply(annotations: Annotations): Trait = Trait(Fields(), annotations)
}
