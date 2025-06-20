package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, ExternalSourceElement, Linkable, Shape}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.internal.annotations.{AVROSchemaType, InlineDefinition, TypePropertyLexicalInfo}
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.domain.metamodel.AnyShapeModel._
import org.yaml.model.YPart

class AnyShape private[amf] (val fields: Fields, val annotations: Annotations = Annotations())
    extends Shape
    with ShapeHelpers
    with ExternalSourceElement
    with InheritanceChain
    with DocumentedElement
    with ExemplifiedDomainElement
    with AvroShapeFields {

  // This is used in ShapeNormalization to know if a Shape should go through the AnyShapeAdjuster
  private[amf] val isConcreteShape: Boolean = false

  // TODO: should return Option has field can be null
  def documentation: CreativeWork              = fields.field(Documentation)
  def xmlSerialization: XMLSerializer          = fields.field(XMLSerialization)
  def comment: StrField                        = fields.field(Comment)
  def semanticContext: Option[SemanticContext] = Option(fields.field(AnyShapeModel.Semantics))
  def schemaVersion: StrField                  = fields.field(SchemaVersion)

  def withDocumentation(documentation: CreativeWork): this.type        = set(Documentation, documentation)
  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type = set(XMLSerialization, xmlSerialization)
  def withComment(comment: String): this.type                          = set(Comment, comment)
  def withSemanticContext(context: SemanticContext): this.type         = set(AnyShapeModel.Semantics, context)
  def withSchemaVersion(version: String): this.type                    = set(SchemaVersion, version)

  override def documentations: Seq[CreativeWork] = Seq(documentation)

  override def linkCopy(): AnyShape = AnyShape().withId(id)

  override def meta: AnyShapeModel = AnyShapeModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/any/" + name.option().getOrElse("default-any").urlComponentEncoded

  protected[amf] def copyAnyShape(fields: Fields = fields, annotations: Annotations = annotations): AnyShape =
    AnyShape(fields, annotations).withId(id)

  /** Aux method to know when the shape is instance only of any shape and it's because was parsed from an empty (or only
    * with example) payload, an not an explicit type def
    */
  def isNotExplicit: Boolean =
    meta.`type`.equals(AnyShapeModel.`type`) &&
      annotations.find(classOf[TypePropertyLexicalInfo]).isEmpty

  protected[amf] def inlined: Boolean = annotations.find(classOf[InlineDefinition]).isDefined

  private[amf] override def ramlSyntaxKey: String = "anyShape"

  protected[amf] def trackedExample(trackId: String): Option[Example] = examples.find(_.isTrackedBy(trackId))

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = AnyShape.apply

  override def copyShape(): this.type = super.copyShape().withId(id)

  protected[amf] def isStrictAnyMeta = meta.`type`.headOption.exists(_.iri() == AnyShapeModel.`type`.head.iri())

  // Method to check that the AnyShape is an any type
  protected[amf] def isAnyType: Boolean =
    this.getClass == classOf[AnyShape] &&
      !isXOne &&
      !isOr &&
      !isAnd &&
      !isNot &&
      !isConditional &&
      !fields.exists(AnyShapeModel.Inherits)

  def avroSchemaType: Option[String] = annotations.find(classOf[AVROSchemaType]).map(_.avroType)

  def isAvroSchema: Boolean = avroSchemaType.nonEmpty

}

object AnyShape {
  def apply(): AnyShape = apply(Annotations())

  def apply(ast: YPart): AnyShape = apply(Annotations(ast))

  def apply(annotations: Annotations): AnyShape = AnyShape(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): AnyShape = new AnyShape(fields, annotations)
}
