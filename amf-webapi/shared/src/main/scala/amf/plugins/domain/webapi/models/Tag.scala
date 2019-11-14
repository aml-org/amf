package amf.plugins.domain.webapi.models

import amf.core.metamodel.Field
import amf.core.model.StrField
import amf.core.model.domain.NamedDomainElement
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.AmfStrings
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.metamodel.TagModel
import amf.plugins.domain.webapi.metamodel.TagModel._
import org.yaml.model.YNode

/**
  * Tag internal model
  */
case class Tag(fields: Fields, annotations: Annotations) extends NamedDomainElement {

  def description: StrField       = fields.field(Description)
  def documentation: CreativeWork = fields(Documentation)

  def withDescription(description: String): this.type           = set(Description, description)
  def withDocumentation(documentation: CreativeWork): this.type = set(Documentation, documentation)

  override def meta: TagModel.type = TagModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String        = "/tag/" + name.option().getOrElse("default-type").urlComponentEncoded
  override protected def nameField: Field = Name
}

object Tag {

  def apply(): Tag = apply(Annotations())

  def apply(node: YNode): Tag = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): Tag = new Tag(Fields(), annotations)
}
