package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, ExternalSourceElement, Linkable, Shape}
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.annotations.{InlineDefinition, TypePropertyLexicalInfo}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.domain.metamodel.AnyShapeModel._
import org.yaml.model.YPart

import scala.collection.mutable

class AnyShape private[amf] (val fields: Fields, val annotations: Annotations = Annotations())
    extends Shape
    with ShapeHelpers
    with ExternalSourceElement
    with InheritanceChain
    with DocumentedElement
    with ExemplifiedDomainElement {

  // TODO: should return Option has field can be null
  def documentation: CreativeWork     = fields.field(Documentation)
  def xmlSerialization: XMLSerializer = fields.field(XMLSerialization)
  def comment: StrField               = fields.field(Comment)

  override def documentations: Seq[CreativeWork] = Seq(documentation)

  def withDocumentation(documentation: CreativeWork): this.type        = set(Documentation, documentation)
  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type = set(XMLSerialization, xmlSerialization)
  def withComment(comment: String): this.type                          = set(Comment, comment)

  override def linkCopy(): AnyShape = AnyShape().withId(id)

  override def meta: AnyShapeModel = AnyShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String = "/any/" + name.option().getOrElse("default-any").urlComponentEncoded

  protected[amf] def copyAnyShape(fields: Fields = fields, annotations: Annotations = annotations): AnyShape =
    AnyShape(fields, annotations).withId(id)

  /** Aux method to know when the shape is instance only of any shape
    * and it's because was parsed from
    * an empty (or only with example) payload, an not an explicit type def */
  def isNotExplicit: Boolean =
    meta.`type`.equals(AnyShapeModel.`type`) &&
      annotations.find(classOf[TypePropertyLexicalInfo]).isEmpty

  protected[amf] def inlined: Boolean = annotations.find(classOf[InlineDefinition]).isDefined

  private[amf] override def ramlSyntaxKey: String = "anyShape"

  protected[amf] def trackedExample(trackId: String): Option[Example] = examples.find(_.isTrackedBy(trackId))

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = AnyShape.apply

  override def copyShape(): this.type = super.copyShape().setId(id)

  // Method to check that the AnyShape is an any type
  protected[amf] def isAnyType: Boolean =
    this.getClass == classOf[AnyShape] &&
      !fields.exists(AnyShapeModel.Xone) &&
      !fields.exists(AnyShapeModel.Or) &&
      !fields.exists(AnyShapeModel.And) &&
      !fields.exists(AnyShapeModel.Not) &&
      !fields.exists(AnyShapeModel.If) &&
      !fields.exists(AnyShapeModel.Else) &&
      !fields.exists(AnyShapeModel.Then) &&
      !fields.exists(AnyShapeModel.Inherits)
}

object AnyShape {
  def apply(): AnyShape = apply(Annotations())

  def apply(ast: YPart): AnyShape = apply(Annotations(ast))

  def apply(annotations: Annotations): AnyShape = AnyShape(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): AnyShape = new AnyShape(fields, annotations)
}
