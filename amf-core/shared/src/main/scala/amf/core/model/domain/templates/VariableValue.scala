package amf.core.model.domain.templates

import amf.client.model.StrField
import amf.core.metamodel.Obj
import amf.core.metamodel.domain.templates.VariableValueModel
import amf.core.metamodel.domain.templates.VariableValueModel.{Name, Value}
import amf.core.model.domain.{DataNode, DomainElement}
import amf.core.parser.{Annotations, Fields}
import org.yaml.model.YPart

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

case class VariableValue(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: StrField  = fields.field(Name)
  def value: DataNode = fields.field(Value)

  def withName(name: String): this.type     = set(Name, name)
  def withValue(value: DataNode): this.type = set(Value, value)

  override def adopted(parent: String): this.type = withId(parent + "/" + name.value())

  override def meta: Obj = VariableValueModel
}

object VariableValue {

  def apply(): VariableValue = apply(Annotations())

  def apply(ast: YPart): VariableValue = apply(Annotations(ast))

  def apply(annotations: Annotations): VariableValue = apply(Fields(), annotations)
}

@JSExportTopLevel("model.domain.Variable")
@JSExportAll
case class Variable(name: String, value: DataNode)
