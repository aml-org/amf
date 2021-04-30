package amf.plugins.domain.shapes.models

import amf.core.metamodel.Field
import amf.core.model.StrField
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.AmfStrings
import amf.plugins.domain.shapes.metamodel.FileShapeModel._
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, FileShapeModel, SchemaShapeModel}
import org.yaml.model.YPart

case class FileShape(override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations)
    with CommonShapeFields {

  def fileTypes: Seq[StrField] = fields.field(FileTypes)

  def withFileTypes(fileTypes: Seq[String]): this.type = set(FileTypes, fileTypes)

  override def linkCopy(): FileShape = FileShape().withId(id)

  override val meta: AnyShapeModel = FileShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + name.option().getOrElse("default-file").urlComponentEncoded

  override val ramlSyntaxKey: String = "fileShape"

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = FileShape.apply
}

object FileShape {
  def apply(): FileShape                         = apply(Annotations())
  def apply(ast: YPart): FileShape               = apply(Annotations(ast))
  def apply(annotations: Annotations): FileShape = FileShape(Fields(), annotations)
}
