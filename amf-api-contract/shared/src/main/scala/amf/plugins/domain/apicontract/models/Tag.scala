package amf.plugins.domain.apicontract.models

import amf.core.metamodel.Field
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.NamedDomainElement
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.AmfStrings
import amf.plugins.domain.shapes.models.{CreativeWork, DocumentedElement}
import amf.plugins.domain.apicontract.metamodel.TagModel
import amf.plugins.domain.apicontract.metamodel.TagModel._
import org.yaml.model.YNode

/**
  * Tag internal model
  */
case class Tag(fields: Fields, annotations: Annotations) extends NamedDomainElement with DocumentedElement {

  def description: StrField = fields.field(Description)

  // TODO: should return Option has field can be null
  def documentation: CreativeWork = fields(Documentation)

  override def documentations: Seq[CreativeWork] = Seq(documentation)

  def withDescription(description: String): this.type           = set(Description, description)
  def withDocumentation(documentation: CreativeWork): this.type = set(Documentation, documentation)

  override def meta: TagModel.type = TagModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/tag/" + name.option().getOrElse("default-type").urlComponentEncoded
  override def nameField: Field    = Name
}

object Tag {

  def apply(): Tag = apply(Annotations())

  def apply(node: YNode): Tag = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): Tag = new Tag(Fields(), annotations)
}
