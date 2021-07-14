package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, ExternalSourceElement, Linkable}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.domain.metamodel.SchemaShapeModel
import amf.shapes.internal.domain.metamodel.SchemaShapeModel._
import org.yaml.model.YPart

case class SchemaShape private[amf] (override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations)
    with ExternalSourceElement {

  def mediaType: StrField = fields.field(MediaType)

  def withRaw(text: String): SchemaShape            = set(Raw, text)
  def withMediaType(mediaType: String): SchemaShape = set(MediaType, mediaType)

  override def linkCopy(): SchemaShape = SchemaShape().withId(id)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String =
    "/schema/" + name.option().getOrElse("default-schema").urlComponentEncoded

  private[amf] override def ramlSyntaxKey: String = "schemaShape" // same that any shape
  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = SchemaShape.apply

  override def meta: SchemaShapeModel = SchemaShapeModel
}

object SchemaShape {

  def apply(): SchemaShape = apply(Annotations())

  def apply(ast: YPart): SchemaShape = apply(Annotations(ast))

  def apply(annotations: Annotations): SchemaShape = SchemaShape(Fields(), annotations)
}
