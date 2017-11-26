package amf.plugins.domain.shapes.models

import amf.core.metamodel.Obj
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.metamodel.FileShapeModel._
import amf.plugins.domain.shapes.metamodel.FileShapeModel
import org.yaml.model.YPart

case class FileShape(override val fields: Fields, override val annotations: Annotations) extends AnyShape(fields, annotations) with CommonShapeFields {

  def fileTypes: Seq[String] = fields(FileTypes)

  def withFileTypes(fileTypes: Seq[String]): this.type = set(FileTypes, fileTypes)

  override def adopted(parent: String): this.type = withId(parent + "/" + name)

  override def linkCopy(): FileShape = FileShape().withId(id)

  override def meta: Obj = FileShapeModel
}

object FileShape {
  def apply(): FileShape                         = apply(Annotations())
  def apply(ast: YPart): FileShape               = apply(Annotations(ast))
  def apply(annotations: Annotations): FileShape = FileShape(Fields(), annotations)
}
