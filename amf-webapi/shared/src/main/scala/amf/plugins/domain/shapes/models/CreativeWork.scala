package amf.plugins.domain.shapes.models

import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.core.utils._
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel._
import org.yaml.model.YMap

/**
  * Creative work internal model
  */
case class CreativeWork(fields: Fields, annotations: Annotations) extends DomainElement with Linkable {

  def url: StrField         = fields.field(Url)
  def description: StrField = fields.field(Description)
  def title: StrField       = fields.field(Title)

  def withUrl(url: String): this.type                 = set(Url, url)
  def withDescription(description: String): this.type = set(Description, description)
  def withTitle(title: String): this.type             = set(Title, title)

  override def linkCopy(): Linkable = CreativeWork().withId(id)

  override def meta = CreativeWorkModel

  private def searchIdPart: Option[String] = linkTarget match {
    case Some(target: CreativeWork) => target.title.option().orElse(target.url.option()).map(_.urlEncoded)
    case _                          => title.option().orElse(url.option()).map(_.urlEncoded)
  }

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/creative-work/" + searchIdPart.orNull
}

object CreativeWork {

  def apply(): CreativeWork = apply(Annotations())

  def apply(ast: YMap): CreativeWork = apply(Annotations(ast))

  def apply(annotations: Annotations): CreativeWork = apply(Fields(), annotations)
}
