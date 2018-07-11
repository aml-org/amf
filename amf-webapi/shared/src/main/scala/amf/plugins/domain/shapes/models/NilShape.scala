package amf.plugins.domain.shapes.models

import amf.core.parser.{Annotations, Fields}
import amf.core.utils.Strings
import amf.plugins.domain.shapes.metamodel.{AnyShapeModel, NilShapeModel}
import org.yaml.model.YPart

case class NilShape(override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations) {

  override def linkCopy(): NilShape = NilShape().withId(id) // todo review with antonio

  override def meta: AnyShapeModel = NilShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/nil/" + name.option().getOrElse("default-nil").urlComponentEncoded

  override def copyShape(): NilShape = NilShape(fields.copy(), annotations.copy()).withId(id)

  override def ramlSyntaxKey: String = "shape"
}

object NilShape {
  def apply(): NilShape = apply(Annotations())

  def apply(ast: YPart): NilShape = apply(Annotations(ast))

  def apply(annotations: Annotations): NilShape = NilShape(Fields(), annotations)
}
