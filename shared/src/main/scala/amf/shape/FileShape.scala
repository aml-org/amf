package amf.shape

import amf.domain.Fields
import amf.framework.parser.Annotations
import amf.metadata.shape.FileShapeModel
import amf.metadata.shape.FileShapeModel.FileTypes
import org.yaml.model.YPart

case class FileShape(fields: Fields, annotations: Annotations) extends Shape with CommonOASFields {

  def fileTypes: Seq[String] = fields(FileTypes)

  def withFileTypes(fileTypes: Seq[String]): this.type = set(FileTypes, fileTypes)

  override def adopted(parent: String): this.type = withId(parent + "/" + name)

  override def linkCopy(): FileShape = FileShape().withId(id)

  override def meta = FileShapeModel
}

object FileShape {
  def apply(): FileShape                         = apply(Annotations())
  def apply(ast: YPart): FileShape               = apply(Annotations(ast))
  def apply(annotations: Annotations): FileShape = FileShape(Fields(), annotations)
}
