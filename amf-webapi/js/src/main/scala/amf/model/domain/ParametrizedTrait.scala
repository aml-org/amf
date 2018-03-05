package amf.model.domain

import amf.plugins.domain.webapi.models.templates.{ParametrizedTrait => CoreParametrizedTrait}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ParametrizedTrait private[model] (private val tr: CoreParametrizedTrait)
    extends ParametrizedDeclaration(tr) {

  @JSExportTopLevel("model.domain.ParametrizedTrait")
  def this() = this(CoreParametrizedTrait())

  override private[amf] def element: CoreParametrizedTrait = tr
}
