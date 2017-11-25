package amf.plugins.domain.webapi.models.templates

import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.templates.TraitModel
import org.yaml.model.YPart


case class Trait(fields: Fields, annotations: Annotations) extends AbstractDeclaration(fields, annotations) {
  override def linkCopy(): Trait = Trait().withId(id)

  override def meta = TraitModel
}

object Trait {
  def apply(): Trait = apply(Annotations())

  def apply(ast: YPart): Trait = apply(Annotations(ast))

  def apply(annotations: Annotations): Trait = Trait(Fields(), annotations)
}
