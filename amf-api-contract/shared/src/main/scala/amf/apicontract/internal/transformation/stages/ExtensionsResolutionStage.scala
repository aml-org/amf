package amf.apicontract.internal.transformation.stages

import amf.apicontract.client.scala.model.document.{Extension, Overlay}
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.metamodel.domain._
import amf.apicontract.internal.metamodel.domain.api.BaseApiModel
import amf.apicontract.internal.spec.common.WebApiDeclarations
import amf.apicontract.internal.spec.common.transformation.ExtendsHelper
import amf.apicontract.internal.spec.raml.parser.context.{Raml08WebApiContext, Raml10WebApiContext, RamlWebApiContext}
import amf.apicontract.internal.validation.definitions.ResolutionSideValidations.MissingExtensionInReferences
import amf.core.client.common.validation.{ProfileName, Raml08Profile}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document._
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfScalar, DomainElement}
import amf.core.client.scala.parse.document.{EmptyFutureDeclarations, ParserContext}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.transform.stages.ReferenceResolutionStage
import amf.core.internal.annotations.Aliases
import amf.core.internal.metamodel.Type.Scalar
import amf.core.internal.metamodel.document.BaseUnitModel
import amf.core.internal.metamodel.domain.common.{DescriptionField, DisplayNameField, NameFieldSchema, NameFieldShacl}
import amf.core.internal.metamodel.domain.{DomainElementModel, ShapeModel}
import amf.core.internal.metamodel.{Field, Type}
import amf.core.internal.parser.LimitedParseConfig
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.validation.CoreValidations.TransformationValidation
import amf.shapes.internal.domain.metamodel.common.{DocumentationField, ExamplesField}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  */
// todo: refactor to support error handler in all resolution stages
class ExtensionsResolutionStage(val profile: ProfileName, val keepEditingInfo: Boolean)
    extends TransformationStep()
    with PlatformSecrets {
  override def transform(model: BaseUnit,
                         errorHandler: AMFErrorHandler,
                         configuration: AMFGraphConfiguration): BaseUnit = {
    val extendsStage = new ExtendsResolutionStage(profile, keepEditingInfo)
    val resolvedModel = model match {
      case overlay: Overlay =>
        new OverlayResolutionStage(profile, keepEditingInfo)(errorHandler).resolve(model, overlay, configuration)
      case extension: Extension =>
        new ExtensionResolutionStage(profile, keepEditingInfo)(errorHandler).resolve(model, extension, configuration)
      case _ => extendsStage.transform(model, errorHandler, configuration)
    }
    assignNewRoot(resolvedModel)
  }

  private def assignNewRoot[T <: BaseUnit](model: T): T = model.withRoot(true)
}

case class IdTracker() {
  private val knownIds: mutable.Set[String] = mutable.Set()

  def track(id: String): Unit = knownIds += id

  def track(ids: Seq[String]): Unit = knownIds ++= ids

  def notTracking(id: String): Boolean = !knownIds.contains(id)

  def notTracking(ids: Seq[String]): Boolean = ids.forall(id => notTracking(id))
}

abstract class MergingRestrictions() {
  def allowsOverride(field: Field): Boolean
  def allowsNodeInsertionIn(field: Field): Boolean
}

trait DomainElementArrayMergeStrategy {
  def merge(target: DomainElement, field: Field, o: AmfArray, extensionId: String, extensionLocation: Option[String])
}

abstract class ExtensionLikeResolutionStage[T <: ExtensionLike[_ <: DomainElement]](
    val profile: ProfileName,
    val keepEditingInfo: Boolean)(implicit val errorHandler: AMFErrorHandler)
    extends InnerAdoption {

  val restrictions: MergingRestrictions

  private val domainElementArrayMergeStrategy = new DomainElementArrayMergeStrategy {
    override def merge(target: DomainElement,
                       field: Field,
                       o: AmfArray,
                       extensionId: String,
                       extensionLocation: Option[String]): Unit =
      setDomainElementArrayValue(target, field, o, extensionId, extensionLocation)
  }

  /** Default to raml10 context. */
  implicit val ctx: RamlWebApiContext = profile match {
    case Raml08Profile => new Raml08WebApiContext("", Nil, ParserContext(config = LimitedParseConfig(errorHandler)))
    case _             => new Raml10WebApiContext("", Nil, ParserContext(config = LimitedParseConfig(errorHandler)))
  }

  def removeExtends(document: Document): BaseUnit = {
    document.encodes.asInstanceOf[Api].endPoints.foreach { endpoint =>
      if (!keepEditingInfo) {
        endpoint.fields.removeField(DomainElementModel.Extends)
      }
      endpoint.operations.foreach { operation =>
        if (!keepEditingInfo) {
          operation.fields.removeField(DomainElementModel.Extends)
        }
      }
    }
    document
  }

  def mergeReferences(document: Document,
                      extension: ExtensionLike[Api],
                      extensionId: String,
                      extensionLocation: Option[String]): Unit = {
    val existing = document.references.map(_.id) ++ Seq(document.id, extension.id)

    val extensionReferences = extension.references.collect {
      case m: Module   => m
      case f: Fragment => f
    }

    val refs = document.references ++ extensionReferences.filter(unit => !existing.contains(unit.id)).map { unit =>
      unit
    }

    document.withReferences(refs)
  }

  def mergeAliases(document: Document, extension: ExtensionLike[_ <: DomainElement]): Unit = {
    val extensionsAliases = extension.annotations.find(classOf[Aliases]).getOrElse(Aliases(Set())).aliases
    val documentAliases   = document.annotations.find(classOf[Aliases]).getOrElse(Aliases(Set())).aliases

    extensionsAliases.map(_._1).intersect(documentAliases.map(_._1)).foreach { alias =>
      val extensionFullUrl = extensionsAliases.find(_._1 == alias).map(_._2._1).get
      val docFullUrl       = documentAliases.find(_._1 == alias).map(_._2._1).get
      if (extensionFullUrl != docFullUrl)
        errorHandler.violation(
          TransformationValidation,
          s"Conflicting urls for alias '$alias' and libraries: '$extensionFullUrl' - '$docFullUrl'",
          document.location().getOrElse(document.id)
        )
    }

    val totalAliases = extensionsAliases.union(documentAliases)
    if (totalAliases.nonEmpty) {
      document.annotations.reject(_.isInstanceOf[Aliases])
      document.annotations += Aliases(totalAliases)
    }
  }

  def resolve(model: BaseUnit, entryPoint: T, configuration: AMFGraphConfiguration): BaseUnit

  protected def resolveOverlay(model: BaseUnit, entryPoint: T, configuration: AMFGraphConfiguration): BaseUnit = {
    extensionsQueue(ListBuffer[BaseUnit](entryPoint), entryPoint) match {
      case (document: Document) :: extensions =>
        // Don't remove Extends field from the model when traits and resource types are resolved.
        val extendsStage   = new ExtendsResolutionStage(profile, keepEditingInfo, fromOverlay = true) // this false is required to merge overlays with traits/resource types
        val referenceStage = new ReferenceResolutionStage(keepEditingInfo)

        // All includes are resolved and applied for both Master Tree and Extension Tree.
        referenceStage.transform(document, errorHandler, configuration)

        // Current Target Tree Object is set to the Target Tree root (API).
        val masterTree = document.asInstanceOf[EncodesModel].encodes.asInstanceOf[Api]

        // First I need to merge all the declarations and references to Master Tree
        extensions.foreach {
          // Current Extension Tree Object is set to the Extension Tree root (API).
          case extension: ExtensionLike[_] =>
            // Resolve references.
            referenceStage.transform(extension, errorHandler, configuration)

            val iriMerger = IriMerger(document.id + "#", extension.id + "#")

            mergeDeclarations(document,
                              extension.asInstanceOf[ExtensionLike[Api]],
                              iriMerger,
                              extension.id,
                              ExtendsHelper.findUnitLocationOfElement(extension.id, model))

            mergeAliases(document, extension)

            mergeReferences(document,
                            extension.asInstanceOf[ExtensionLike[Api]],
                            extension.id,
                            ExtendsHelper.findUnitLocationOfElement(extension.id, model))
        }

        // Then, with all the declarations and references applied.
        // All Trait and Resource Types applications are applied in the Master Tree.
        extendsStage.transform(document, errorHandler, configuration)

        extensions.foreach {
          // Current Extension Tree Object is set to the Extension Tree root (API).
          case extension: ExtensionLike[_] =>
            val iriMerger = IriMerger(document.id + "#", extension.id + "#")

            new ExtensionDomainElementMerge(
              restrictions,
              domainElementArrayMergeStrategy,
              extension.id,
              ExtendsHelper.findUnitLocationOfElement(extension.id, model),
              new InferredOverlayTypeExampleTransform()
            ).merge(masterTree, extension.encodes, IdTracker())

            adoptIris(iriMerger, masterTree, IdTracker())

            // Traits and Resource Types applications are applied one more time to the Target Tree.
            extendsStage.transform(document, errorHandler, configuration)
        }

        // now we can remove is/type predicates safely if requested by pipeline
        removeExtends(document)
      case _ => model
    }
  }

  case class IriMerger(master: String = "", extension: String = "") {
    def merge(element: DomainElement, field: Field, value: AmfElement): Unit =
      element.set(field, value.asInstanceOf[AmfScalar].toString.replaceFirst(extension, master))
  }

  /** Merge annotation types, types, security schemes, resource types,  */
  def mergeDeclarations(master: Document,
                        extension: ExtensionLike[Api],
                        iriMerger: IriMerger,
                        extensionId: String,
                        extensionLocation: Option[String]): Unit = {
    val declarations = WebApiDeclarations(master.declares, ctx.eh, EmptyFutureDeclarations())

    // Extension declarations will be added to master document. The ones with the same name will be merged.
    val mergingTracker = IdTracker()
    extension.declares.foreach { declaration =>
      declarations.findEquivalent(declaration) match {
        case Some(equivalent) =>
          new ExtensionDomainElementMerge(
            restrictions,
            domainElementArrayMergeStrategy,
            extensionId,
            extensionLocation,
            new InferredOverlayTypeExampleTransform()).merge(equivalent, declaration, mergingTracker)
        case None =>
          val extendedDeclaration =
            adoptInner(master.id + "#/declarations", declaration, mergingTracker).asInstanceOf[DomainElement]
          declarations += extendedDeclaration
      }
    }

    val declarables = declarations.declarables()

    val adoptIrisTracker = IdTracker()
    declarables.foreach(adoptIris(iriMerger, _, adoptIrisTracker))

    master.withDeclares(declarables)
  }

  def setDomainElementArrayValue(target: DomainElement,
                                 field: Field,
                                 other: AmfArray,
                                 extensionId: String,
                                 extensionLocation: Option[String]): Unit

  def adoptIris(iriMerger: IriMerger, target: DomainElement, tracker: IdTracker): Unit = {
    if (tracker.notTracking(target.id)) {
      tracker.track(target.id)
      target.fields.foreach {
        case (field, value) =>
          field.`type` match {
            case Type.Iri => iriMerger.merge(target, field, value.value)
            case Type.ArrayLike(_: DomainElementModel) =>
              value.value.asInstanceOf[AmfArray].values.collect { case d: DomainElement => d }.foreach {
                adoptIris(iriMerger, _, tracker)
              }
            case _: DomainElementModel => adoptIris(iriMerger, value.value.asInstanceOf[DomainElement], tracker)
            case _                     =>
          }
      }
    }
  }

  def extensionsQueue(collector: ListBuffer[BaseUnit], model: BaseUnit): List[BaseUnit] = model match {
    case extension: ExtensionLike[_] =>
      model.findInReferences(extension.extend) match {
        case Some(e) =>
          collector += e
          extensionsQueue(collector, e)
        case None if Option(extension.extend).isDefined =>
          errorHandler.violation(
            MissingExtensionInReferences,
            model,
            Some(extension.extend),
            s"BaseUnit '${extension.extend}' not found in references."
          )
          Nil
        case _ =>
          errorHandler.violation(
            MissingExtensionInReferences,
            model,
            Some(extension.extend),
            s"Missing extend property for model '${model.id}'."
          )
          Nil
      }
    case _ => collector.reverse.toList
  }
}

class ExtensionResolutionStage(override val profile: ProfileName, override val keepEditingInfo: Boolean)(
    override implicit val errorHandler: AMFErrorHandler)
    extends ExtensionLikeResolutionStage[Extension](profile, keepEditingInfo) {
  override def resolve(model: BaseUnit, entryPoint: Extension, configuration: AMFGraphConfiguration): BaseUnit =
    resolveOverlay(model, entryPoint, configuration)

  override def setDomainElementArrayValue(target: DomainElement,
                                          field: Field,
                                          other: AmfArray,
                                          extensionId: String,
                                          extensionLocation: Option[String]): Unit = {
    val targetValues = target.fields.get(field).asInstanceOf[AmfArray].values
    val mergedValues = (targetValues ++ other.values).distinct
    target.set(field, AmfArray(mergedValues))
  }

  override val restrictions: MergingRestrictions = MergingRestrictions.unrestricted
}

class OverlayResolutionStage(override val profile: ProfileName, override val keepEditingInfo: Boolean)(
    override implicit val errorHandler: AMFErrorHandler)
    extends ExtensionLikeResolutionStage[Overlay](profile, keepEditingInfo) {
  override def resolve(model: BaseUnit, entryPoint: Overlay, configuration: AMFGraphConfiguration): BaseUnit =
    resolveOverlay(model, entryPoint, configuration)

  override def setDomainElementArrayValue(target: DomainElement,
                                          field: Field,
                                          other: AmfArray,
                                          extensionId: String,
                                          extensionLocation: Option[String]): Unit = {
    val seq: Seq[AmfElement] = other.values.map { value =>
      value
    }
    target.setArray(field, seq)
  }

  override val restrictions: MergingRestrictions = MergingRestrictions.onlyFunctionalField
}

object MergingRestrictions {

  val unrestricted: MergingRestrictions = new MergingRestrictions {
    override def allowsOverride(field: Field): Boolean        = true
    override def allowsNodeInsertionIn(field: Field): Boolean = true
  }

  val onlyFunctionalField: MergingRestrictions = new MergingRestrictions {

    val allowedFields: Seq[Field] = Seq(
      NameFieldShacl.Name,
      NameFieldSchema.Name,
      DisplayNameField.DisplayName,
      ShapeModel.DisplayName,
      DescriptionField.Description,
      DocumentationField.Documentation,
      TagModel.Documentation,
      BaseApiModel.Documentations,
      BaseUnitModel.Usage,
      ExamplesField.Examples,
      DomainElementModel.CustomDomainProperties
    )

    val allowedScalarFieldsInObject: Seq[Field] = Seq(
      EndPointModel.Path,
      OperationModel.Method,
      ResponseModel.StatusCode,
      PayloadModel.MediaType
    )

    val blockedObjectFields: Seq[Field] = Seq(
      BaseApiModel.Servers
    )

    def isAllowedField(field: Field): Boolean             = allowedFields.contains(field)
    def isAllowedScalarObjectField(field: Field): Boolean = allowedScalarFieldsInObject.contains(field)
    def isAllowedObjectField(field: Field): Boolean       = !blockedObjectFields.contains(field)

    override def allowsOverride(field: Field): Boolean =
      field.`type` match {
        case _: Scalar => isAllowedField(field) || isAllowedScalarObjectField(field)
        case _         => isAllowedField(field) || isAllowedObjectField(field)
      }

    // Can insert new examples/documentation in existing
    override def allowsNodeInsertionIn(field: Field): Boolean = isAllowedField(field)
  }
}
