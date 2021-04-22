package amf.plugins.domain.shapes.models

import amf.client.execution.BaseExecutionEnvironment
import amf.client.plugins.{ScalarRelaxedValidationMode, StrictValidationMode}
import amf.core.annotations.DeclaredElement
import amf.core.emitter.ShapeRenderOptions
import amf.core.model.StrField
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.{DomainElement, ExternalSourceElement, Linkable, Shape, ValidatorAware}
import amf.core.parser.{Annotations, Fields}
import amf.core.utils.AmfStrings
import amf.core.validation.{AMFValidationReport, PayloadValidator, SeverityLevels}
import amf.internal.environment.Environment
import amf.plugins.document.webapi.annotations.InlineDefinition
import amf.plugins.document.webapi.parser.spec.common.{JsonSchemaSerializer, RamlDatatypeSerializer}
import amf.plugins.domain.shapes.metamodel.AnyShapeModel
import amf.plugins.domain.shapes.metamodel.AnyShapeModel._
import amf.plugins.domain.shapes.validation.PayloadValidationPluginsHandler
import amf.plugins.domain.webapi.annotations.TypePropertyLexicalInfo
import amf.plugins.domain.webapi.models.DocumentedElement
import amf.plugins.domain.webapi.unsafe.JsonSchemaSecrets
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
    with JsonSchemaSecrets
    with ShapeHelpers
    with JsonSchemaSerializer
    with RamlDatatypeSerializer
    with ExternalSourceElement
    with InheritanceChain
    with DocumentedElement
    with ExemplifiedDomainElement
    with ValidatorAware {

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

  def toJsonSchema(exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): String =
    // TODO: THIS SHOULD NOT BE CALLED FROM DOMAIN MODEL!
    toJsonSchema(this, exec)

  def buildJsonSchema(options: ShapeRenderOptions = ShapeRenderOptions(),
                      exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): String =
    // TODO: THIS SHOULD NOT BE CALLED FROM DOMAIN MODEL!
    generateJsonSchema(this, options, exec)

  /** Delegates generation of a new RAML Data Type or returns cached
    * one if it was generated before.
    */
  def toRamlDatatype(exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): String =
    toRamlDatatype(this, exec)

  /** Generates a new RAML Data Type. */
  def buildRamlDatatype(exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): String =
    generateRamlDatatype(this, exec)

  def copyAnyShape(fields: Fields = fields, annotations: Annotations = annotations): AnyShape =
    AnyShape(fields, annotations).withId(id)

  def validateParameter(
      payload: String,
      env: Environment = Environment(),
      exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): Future[AMFValidationReport] =
    PayloadValidationPluginsHandler.validateWithGuessing(this,
                                                         payload,
                                                         SeverityLevels.VIOLATION,
                                                         env,
                                                         ScalarRelaxedValidationMode,
                                                         exec)

  def validateParameter(payload: String, exec: BaseExecutionEnvironment): Future[AMFValidationReport] =
    validateParameter(payload, Environment(exec), exec)

  def validate(payload: String, env: Environment, exec: BaseExecutionEnvironment): Future[AMFValidationReport] =
    PayloadValidationPluginsHandler.validateWithGuessing(this,
                                                         payload,
                                                         SeverityLevels.VIOLATION,
                                                         env,
                                                         StrictValidationMode,
                                                         exec)

  def validate(payload: String): Future[AMFValidationReport] =
    validate(payload, Environment(), platform.defaultExecutionEnvironment)

  def validate(payload: String, env: Environment): Future[AMFValidationReport] =
    validate(payload, env, platform.defaultExecutionEnvironment)

  def validate(payload: String, exec: BaseExecutionEnvironment): Future[AMFValidationReport] =
    validate(payload, Environment(exec), exec)

  def validate(fragment: PayloadFragment,
               env: Environment,
               exec: BaseExecutionEnvironment): Future[AMFValidationReport] =
    PayloadValidationPluginsHandler.validateFragment(this, fragment, SeverityLevels.VIOLATION, env, exec = exec)

  def validate(fragment: PayloadFragment): Future[AMFValidationReport] =
    validate(fragment, Environment(), platform.defaultExecutionEnvironment)

  def validate(fragment: PayloadFragment, env: Environment): Future[AMFValidationReport] =
    validate(fragment, env, platform.defaultExecutionEnvironment)

  def validate(fragment: PayloadFragment, exec: BaseExecutionEnvironment): Future[AMFValidationReport] =
    validate(fragment, Environment(exec), exec)

  def payloadValidator(mediaType: String, env: Environment = Environment()): Option[PayloadValidator] =
    PayloadValidationPluginsHandler.payloadValidator(this, mediaType, env, StrictValidationMode)

  def payloadValidator(mediaType: String, exec: BaseExecutionEnvironment): Option[PayloadValidator] =
    payloadValidator(mediaType, Environment(exec))

  def parameterValidator(mediaType: String, env: Environment = Environment()): Option[PayloadValidator] =
    PayloadValidationPluginsHandler.payloadValidator(this, mediaType, env, ScalarRelaxedValidationMode)

  def parameterValidator(mediaType: String, exec: BaseExecutionEnvironment): Option[PayloadValidator] =
    parameterValidator(mediaType, Environment(exec))

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
