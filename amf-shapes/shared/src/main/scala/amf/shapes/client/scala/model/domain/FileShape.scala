package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.domain.metamodel.FileShapeModel._
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, FileShapeModel}
import org.yaml.model.YPart

case class FileShape private[amf] (override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations)
    with CommonShapeFields {

  def fileTypes: Seq[StrField] = fields.field(FileTypes)

  def withFileTypes(fileTypes: Seq[String]): this.type = set(FileTypes, fileTypes)

  override def linkCopy(): FileShape = FileShape().withId(id)

  override val meta: AnyShapeModel = FileShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = "/" + name.option().getOrElse("default-file").urlComponentEncoded

  private[amf] override val ramlSyntaxKey: String = "fileShape"

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = FileShape.apply
}

object FileShape {
  def apply(): FileShape                         = apply(Annotations())
  def apply(ast: YPart): FileShape               = apply(Annotations(ast))
  def apply(annotations: Annotations): FileShape = FileShape(Fields(), annotations)
}
