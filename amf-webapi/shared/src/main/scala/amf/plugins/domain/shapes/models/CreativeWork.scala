package amf.plugins.domain.shapes.models

import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.core.utils._
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel._
import org.yaml.model.YNode

/**
  * Creative work internal model
  */
class CreativeWork(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with Linkable {

  def url: StrField         = fields.field(Url)
  def description: StrField = fields.field(Description)
  def title: StrField       = fields.field(Title)

  def withUrl(url: String): this.type                 = set(Url, url)
  def withDescription(description: String): this.type = set(Description, description)
  def withTitle(title: String): this.type             = set(Title, title)

  override def linkCopy(): Linkable = CreativeWork().withId(id)

  override def meta: Obj = CreativeWorkModel

  private def searchIdPart: Option[String] = linkTarget match {
    case Some(target: CreativeWork) => target.title.option().orElse(target.url.option()).map(_.urlComponentEncoded)
    case _                          => title.option().orElse(url.option()).map(_.urlComponentEncoded)
  }

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/creative-work/" + searchIdPart.orNull

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = CreativeWork.apply
}

object CreativeWork {

  def apply(): CreativeWork = apply(Annotations())

  def apply(node: YNode): CreativeWork = apply(Annotations.valueNode(node))

  def apply(annotations: Annotations): CreativeWork = apply(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): CreativeWork = new CreativeWork(fields, annotations)
}
