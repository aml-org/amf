package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.domain.{DomainElement, Linkable, Shape}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.domain.metamodel.UnionShapeModel._
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, UnionShapeModel}
import org.yaml.model.YPart

case class UnionShape private[amf] (override val fields: Fields, override val annotations: Annotations)
    extends AnyShape(fields, annotations) {

  def anyOf: Seq[Shape] = fields.field(AnyOf)

  def withAnyOf(elements: Seq[Shape], annotations: Annotations = Annotations()): this.type =
    this.setArray(AnyOf, elements, annotations)

  override def linkCopy(): AnyShape = UnionShape().withId(id)

  override val meta: UnionShapeModel.type = UnionShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String =
    "/union/" + name.option().getOrElse("default-union").urlComponentEncoded

  private[amf] override def ramlSyntaxKey: String = "unionShape"

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = UnionShape.apply
}

object UnionShape {

  def apply(): UnionShape = apply(Annotations())

  def apply(ast: YPart): UnionShape = apply(Annotations(ast))

  def apply(annotations: Annotations): UnionShape = UnionShape(Fields(), annotations)

}
