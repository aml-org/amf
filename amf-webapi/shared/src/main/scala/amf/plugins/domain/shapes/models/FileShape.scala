package amf.plugins.domain.shapes.models

import amf.core.metamodel.Obj
import amf.core.model.StrField
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.FileShapeModel._
import amf.plugins.domain.shapes.metamodel.FileShapeModel
import org.yaml.model.YPart
import amf.core.utils.Strings

case class FileShape(override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations)
    with CommonShapeFields {

  def fileTypes: Seq[StrField] = fields.field(FileTypes)

  def withFileTypes(fileTypes: Seq[String]): this.type = set(FileTypes, fileTypes)

  override def linkCopy(): FileShape = FileShape().withId(id)

  override def meta: Obj = FileShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/" + name.option().getOrElse("default-file").urlComponentEncoded
}

object FileShape {
  def apply(): FileShape                         = apply(Annotations())
  def apply(ast: YPart): FileShape               = apply(Annotations(ast))
  def apply(annotations: Annotations): FileShape = FileShape(Fields(), annotations)
}
