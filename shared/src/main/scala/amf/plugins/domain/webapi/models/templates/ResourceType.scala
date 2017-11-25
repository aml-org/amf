package amf.plugins.domain.webapi.models.templates

import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.templates.ResourceTypeModel
import org.yaml.model.YPart

case class ResourceType(fields: Fields, annotations: Annotations) extends AbstractDeclaration(fields, annotations) {
  override def linkCopy(): ResourceType = ResourceType().withId(id)

  override def meta = ResourceTypeModel
}

object ResourceType {
  def apply(): ResourceType = apply(Annotations())

  def apply(ast: YPart): ResourceType = apply(Annotations(ast))

  def apply(annotations: Annotations): ResourceType = ResourceType(Fields(), annotations)
}