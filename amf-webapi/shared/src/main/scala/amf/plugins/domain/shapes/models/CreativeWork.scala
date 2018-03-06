package amf.plugins.domain.shapes.models

import amf.core.utils._
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel._
import org.yaml.model.YMap

/**
  * Creative work internal model
  */
case class CreativeWork(fields: Fields, annotations: Annotations) extends DomainElement with Linkable {

  def url: String         = fields(Url)
  def description: String = fields(Description)
  def title: String       = fields(Title)

  def withUrl(url: String): this.type                 = set(Url, url)
  def withDescription(description: String): this.type = set(Description, description)
  def withTitle(title: String): this.type             = set(Title, title)

  override def adopted(parent: String): this.type =
    withId(parent + "/creative-work/" + Option(title).fold(url)(u => u.urlEncoded))

  override def linkCopy(): Linkable = CreativeWork().withId(id)

  override def meta = CreativeWorkModel
}

object CreativeWork {

  def apply(): CreativeWork = apply(Annotations())

  def apply(ast: YMap): CreativeWork = apply(Annotations(ast))

  def apply(annotations: Annotations): CreativeWork = apply(Fields(), annotations)
}
