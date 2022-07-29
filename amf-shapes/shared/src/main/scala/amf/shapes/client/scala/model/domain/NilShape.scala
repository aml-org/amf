package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, NilShapeModel}
import org.yaml.model.YPart

case class NilShape private[amf] (override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations) {

  override def linkCopy(): NilShape = NilShape().withId(id) // todo review with antonio

  override val meta: NilShapeModel.type = NilShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = "/nil/" + name.option().getOrElse("default-nil").urlComponentEncoded

  private[amf] override def ramlSyntaxKey: String = "shape"

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = NilShape.apply
}

object NilShape {
  def apply(): NilShape = apply(Annotations())

  def apply(ast: YPart): NilShape = apply(Annotations(ast))

  def apply(annotations: Annotations): NilShape = NilShape(Fields(), annotations)
}
