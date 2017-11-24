package amf.model

import amf.plugins.domain.webapi.models.templates

case class Trait private[model] (private val tr: templates.Trait) extends AbstractDeclaration(tr) {
  def this() = this(templates.Trait())

  override private[amf] def element = tr

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: templates.Trait => Trait(l) })

  override def linkCopy(): DomainElement with Linkable = Trait(element.linkCopy())
}
