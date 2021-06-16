package amf.apicontract.client.scala.model.domain.templates

import amf.core.client.scala.model.domain.templates.ParametrizedDeclaration
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.templates.ParametrizedTraitModel
import org.yaml.model.YPart

case class ParametrizedTrait(fields: Fields, annotations: Annotations)
    extends ParametrizedDeclaration(fields, annotations) {
  override def meta = ParametrizedTraitModel
}

object ParametrizedTrait {
  def apply(): ParametrizedTrait = apply(Annotations())

  def apply(ast: YPart): ParametrizedTrait = apply(Annotations(ast))

  def apply(annotations: Annotations): ParametrizedTrait = ParametrizedTrait(Fields(), annotations)
}
