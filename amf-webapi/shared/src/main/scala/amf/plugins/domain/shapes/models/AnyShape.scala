package amf.plugins.domain.shapes.models

import amf.core.model.document.PayloadFragment
import amf.core.model.domain.{ExternalSourceElement, Shape}
import amf.core.parser.{Annotations, Fields}
import amf.core.services.PayloadValidator
import amf.core.utils.Strings
import amf.core.validation.{AMFValidationReport, SeverityLevels}
import amf.plugins.document.webapi.annotations.{DeclaredElement, InlineDefinition}
import amf.plugins.document.webapi.parser.spec.common.JsonSchemaSerializer
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.shapes.metamodel.AnyShapeModel._
import amf.plugins.domain.webapi.annotations.TypePropertyLexicalInfo
import org.yaml.model.YPart

import scala.collection.mutable
import scala.concurrent.Future

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

    def linkSubType(shape: AnyShape) = {
    addSubType(shape)
    shape.addSuperType(this)
  }

  def supportsInheritance: Boolean =
    isInstanceOf[NodeShape] && asInstanceOf[NodeShape].discriminator.option().isDefined

  def effectiveStructuralShapes: Seq[Shape] = {
    val acc =
      if (annotations
            .contains(classOf[DeclaredElement])) { // problem with inlined types extending types with discriminator
        computeSubtypesClosure()
      } else {
        superTypes.find(_.isInstanceOf[AnyShape]) match { // which one if multiple inheritance?
          case Some(superType: AnyShape) => superType.effectiveStructuralShapes
          case None                      => Nil
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

class AnyShape(val fields: Fields, val annotations: Annotations)
    extends Shape
    with ShapeHelpers
    with JsonSchemaSerializer
    with ExternalSourceElement
    with InheritanceChain {

  def documentation: CreativeWork     = fields.field(Documentation)
  def xmlSerialization: XMLSerializer = fields.field(XMLSerialization)
  def examples: Seq[Example]          = fields.field(Examples)

  def withDocumentation(documentation: CreativeWork): this.type        = set(Documentation, documentation)
  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type = set(XMLSerialization, xmlSerialization)
  def withExamples(examples: Seq[Example]): this.type                  = setArray(Examples, examples)

  def withExample(name: Option[String]): Example = {
    val example = Example()
    name.foreach { example.withName }
    add(Examples, example)
    example
  }

  override def linkCopy(): AnyShape = AnyShape().withId(id)

  override def meta: AnyShapeModel = AnyShapeModel

  def toJsonSchema: String = toJsonSchema(this)

  def copyAnyShape(fields: Fields = fields, annotations: Annotations = annotations): AnyShape =
    AnyShape(fields, annotations).withId(id)

  def validate(payload: String): Future[AMFValidationReport] =
    PayloadValidator.validate(this, payload, SeverityLevels.VIOLATION)

  def validate(fragment: PayloadFragment): Future[AMFValidationReport] =
    PayloadValidator.validate(this, fragment, SeverityLevels.VIOLATION)

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/any/" + name.option().getOrElse("default-any").urlComponentEncoded

  /** Aux method to know when the shape is instance only of any shape
    * and it's because was parsed from
    * an empty (or only with example) payload, an not an explicit type def */
  def isDefaultEmpty: Boolean =
    meta.`type`.equals(AnyShapeModel.`type`) &&
      fields.filter(fe => fe._1 != AnyShapeModel.Examples).nonEmpty &&
      annotations.find(classOf[TypePropertyLexicalInfo]).isEmpty

  override def copyShape(): AnyShape = AnyShape(fields.copy(), annotations.copy()).withId(id)

  protected def inlined: Boolean = annotations.find(classOf[InlineDefinition]).isDefined

  override def ramlSyntaxKey: String = "anyShape"
}

object AnyShape {
  def apply(): AnyShape = apply(Annotations())

  def apply(ast: YPart): AnyShape = apply(Annotations(ast))

  def apply(annotations: Annotations): AnyShape = AnyShape(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): AnyShape = new AnyShape(fields, annotations)
}
