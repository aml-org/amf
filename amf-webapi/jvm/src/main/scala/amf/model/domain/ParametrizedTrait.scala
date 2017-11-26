package amf.model.domain

import amf.plugins.domain.webapi.models.templates.{ParametrizedTrait => CoreParametrizedTrait}

case class ParametrizedTrait private[model] (private val tr: CoreParametrizedTrait)
    extends ParametrizedDeclaration(tr) {
  def this() = this(CoreParametrizedTrait())

  override private[amf] def element: CoreParametrizedTrait = tr
}
