package amf.framework.model.domain

import amf.framework.metamodel.domain.LinkableElementModel
import amf.framework.parser.Annotations

trait Linkable extends AmfObject { this: DomainElement with Linkable =>
  var linkTarget: Option[DomainElement]    = None
  var linkAnnotations: Option[Annotations] = None

  def isLink: Boolean           = linkTarget.isDefined
  def linkLabel: Option[String] = Option(fields(LinkableElementModel.Label))

  def linkCopy(): Linkable

  def withLinkTarget(target: DomainElement): this.type = {
    linkTarget = Some(target)
    set(LinkableElementModel.TargetId, target.id)
  }

  def withLinkLabel(label: String): this.type = set(LinkableElementModel.Label, label)

  def link[T](label: String, annotations: Annotations = Annotations()): T = {
    linkCopy()
      .withLinkTarget(this)
      .withLinkLabel(label)
      .add(annotations)
      .asInstanceOf[T]
  }
}
