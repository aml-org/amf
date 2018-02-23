package amf.plugins.domain.webapi.models.templates

import amf.ProfileNames
import amf.core.metamodel.domain.templates.AbstractDeclarationModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.templates.TraitModel
import amf.plugins.domain.webapi.models.Operation
import amf.plugins.domain.webapi.resolution.ExtendsHelper
import org.yaml.model.YPart

case class Trait(fields: Fields, annotations: Annotations) extends AbstractDeclaration(fields, annotations) {
  override def linkCopy(): Trait = Trait().withId(id)

  override def meta: AbstractDeclarationModel = TraitModel

  /** Get this trait as an operation. No variables will be replaced. Pass the BaseUnit that contains this trait to use its declarations and the profile ProfileNames.RAML08 if this is from a raml08 unit. */
  def asOperation[T <: BaseUnit](unit: T, profile: String = ProfileNames.RAML): Operation =
    ExtendsHelper.asOperation(profile, dataNode, unit)
}

object Trait {
  def apply(): Trait = apply(Annotations())

  def apply(ast: YPart): Trait = apply(Annotations(ast))

  def apply(annotations: Annotations): Trait = Trait(Fields(), annotations)
}
