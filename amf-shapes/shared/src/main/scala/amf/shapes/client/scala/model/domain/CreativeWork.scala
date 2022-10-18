package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.metamodel.domain.ShapeModel.Description
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.domain.metamodel.CreativeWorkModel
import amf.shapes.internal.domain.metamodel.CreativeWorkModel._
import org.yaml.model.YNode

/** Creative work internal model
  */
class CreativeWork private[amf] (override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with Linkable {

  def url: StrField         = fields.field(Url)
  def description: StrField = fields.field(Description)
  def title: StrField       = fields.field(Title)

  def withUrl(url: String): this.type                 = set(Url, url)
  def withDescription(description: String): this.type = set(Description, description)
  def withTitle(title: String): this.type             = set(Title, title)

  override def linkCopy(): Linkable = CreativeWork().withId(id)

  override def meta: CreativeWorkModel.type = CreativeWorkModel

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
