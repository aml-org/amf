package amf.plugins.domain.webapi.models

import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.CreativeWorkModel.{Description, Title, Url}
import amf.plugins.domain.webapi.metamodel.CreativeWorkModel
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
    withId(parent + "/creative-work/" + Option(url).fold(title)(u => u))

  override def linkCopy(): Linkable = CreativeWork().withId(id)

  override def meta = CreativeWorkModel
}

object CreativeWork {

  def apply(): CreativeWork = apply(Annotations())

  def apply(ast: YMap): CreativeWork = apply(Annotations(ast))

  def apply(annotations: Annotations): CreativeWork = apply(Fields(), annotations)
}
