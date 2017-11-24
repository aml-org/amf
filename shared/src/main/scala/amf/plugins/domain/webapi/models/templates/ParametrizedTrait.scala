package amf.plugins.domain.webapi.models.templates

import amf.framework.model.domain.templates.ParametrizedDeclaration
import amf.framework.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.templates.ParametrizedTraitModel
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
