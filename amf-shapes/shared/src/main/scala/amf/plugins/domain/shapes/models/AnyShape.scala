package amf.plugins.domain.shapes.models

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.{DomainElement, ExternalSourceElement, Linkable, Shape}
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.annotations.InlineDefinition
import amf.plugins.domain.apicontract.annotations.TypePropertyLexicalInfo
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.shapes.metamodel.AnyShapeModel._
import org.yaml.model.YPart

import scala.collection.mutable

// Support to track during resolution the
// inheritance chain between shapes as loosely
// defined in RAML
trait InheritanceChain { this: AnyShape =>
  // Array of subtypes to compute
  var subTypes: mutable.Seq[Shape]   = mutable.Seq()
  var superTypes: mutable.Seq[Shape] = mutable.Seq()
  // Any ID, not only the ones with discriminator, just keep the ID ref
  var inheritedIds: mutable.Seq[String] = mutable.Seq()

  def addSubType(shape: Shape): Unit = {
    subTypes.find(_.id == shape.id) match {
      case Some(_) => // duplicated
      case _       => subTypes ++= Seq(shape)
    }
  }

  def addSuperType(shape: Shape): Unit = {
    superTypes.find(_.id == shape.id) match {
      case Some(_) => // duplicated
      case _       => superTypes ++= Seq(shape)
    }
  }

  def linkSubType(shape: AnyShape): Unit = {
    addSubType(shape)
    shape.addSuperType(this)
  }

  def effectiveStructuralShapes: Seq[Shape] = {
    val acc =
      if (annotations
            .contains(classOf[DeclaredElement])) { // problem with inlined types extending types with discriminator
        computeSubtypesClosure()
      } else {
        superTypes.find(_.isInstanceOf[AnyShape]) match { // which one if multiple inheritance?
          case Some(superType: AnyShape) => superType.effectiveStructuralShapes
          case _                         => Nil
        }
      }
    (Seq(this) ++ acc).distinct
  }

  protected def computeSubtypesClosure(): Seq[Shape] = {
    val res =
      if (subTypes.isEmpty) Nil
      else
        subTypes.foldLeft(Seq[Shape]()) {
          case (acc, nextShape) =>
            nextShape match {
              case nestedNode: NodeShape => acc ++ Seq(nestedNode) ++ nestedNode.computeSubtypesClosure
              case _                     => acc
            }
        }
    res.distinct
  }
}

class AnyShape(val fields: Fields, val annotations: Annotations = Annotations())
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

  def copyAnyShape(fields: Fields = fields, annotations: Annotations = annotations): AnyShape =
    AnyShape(fields, annotations).withId(id)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/any/" + name.option().getOrElse("default-any").urlComponentEncoded

  /** Aux method to know when the shape is instance only of any shape
    * and it's because was parsed from
    * an empty (or only with example) payload, an not an explicit type def */
  def isDefaultEmpty: Boolean =
    meta.`type`.equals(AnyShapeModel.`type`) &&
      annotations.find(classOf[TypePropertyLexicalInfo]).isEmpty

  def inlined: Boolean = annotations.find(classOf[InlineDefinition]).isDefined

  override def ramlSyntaxKey: String = "anyShape"

  def trackedExample(trackId: String): Option[Example] = examples.find(_.isTrackedBy(trackId))

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = AnyShape.apply

  override def copyShape(): this.type = super.copyShape().withId(id)

  // Method to check that the AnyShape is an any type
  def isAnyType: Boolean =
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
