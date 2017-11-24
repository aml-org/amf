package amf.model

import amf.plugins.domain.webapi.models.templates

case class ParametrizedTrait private[model] (private val tr: templates.ParametrizedTrait)
    extends ParametrizedDeclaration(tr) {
  def this() = this(templates.ParametrizedTrait())

  override private[amf] def element: templates.ParametrizedTrait = tr
}
