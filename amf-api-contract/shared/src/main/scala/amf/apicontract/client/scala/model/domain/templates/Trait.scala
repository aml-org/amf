package amf.apicontract.client.scala.model.domain.templates

import amf.apicontract.client.scala.model.domain.Operation
import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.templates.AbstractDeclaration
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.templates.TraitModel
import amf.apicontract.internal.spec.common.transformation.ExtendsHelper
import org.yaml.model.{YMapEntry, YPart}

class Trait(override val fields: Fields, override val annotations: Annotations)
    extends AbstractDeclaration(fields, annotations) {
  override def linkCopy(): Trait = {
    Trait().withId(id)
  }

  override def meta: TraitModel.type = TraitModel

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Trait.apply

  override protected def declarationComponent: String = "trait"
}

object Trait {
  def apply(): Trait = apply(Annotations())

  def apply(ast: YPart): Trait = apply(Annotations(ast))

  def apply(annotations: Annotations): Trait = Trait(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Trait = new Trait(fields, annotations)
}
