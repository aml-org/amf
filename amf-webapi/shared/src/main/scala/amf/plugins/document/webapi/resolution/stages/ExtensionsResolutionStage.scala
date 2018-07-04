package amf.plugins.document.webapi.resolution.stages

import amf.core.annotations.{Aliases, SynthesizedField}
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.metamodel.domain.DomainElementModel._
import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel, ShapeModel}
import amf.core.metamodel.{Field, Type}
import amf.core.model.document._
import amf.core.model.domain.DataNodeOps.adoptTree
import amf.core.model.domain._
import amf.core.parser.{Annotations, EmptyFutureDeclarations, ErrorHandler, FieldEntry, ParserContext, Value}
import amf.core.resolution.stages.{ReferenceResolutionStage, ResolutionStage}
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.annotations.ExtensionProvenance
import amf.plugins.document.webapi.contexts.{Raml08WebApiContext, Raml10WebApiContext, RamlWebApiContext}
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel
import amf.plugins.domain.webapi.metamodel.templates.ParametrizedTraitModel
import amf.plugins.domain.webapi.models.WebApi
import amf.plugins.domain.webapi.resolution.ExtendsHelper
import amf.plugins.domain.webapi.resolution.stages.DataNodeMerging
import amf.plugins.features.validation.ParserSideValidations
import amf.{ProfileName, RAML08Profile}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  */
// todo: refactor to support error handler in all resolution stages
class ExtensionsResolutionStage(val profile: ProfileName, val keepEditingInfo: Boolean)(
    override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage()
    with PlatformSecrets {
  override def resolve[T <: BaseUnit](model: T): T = {
    val extendsStage = new ExtendsResolutionStage(profile, keepEditingInfo)
    model match {
      case overlay: Overlay =>
        new OverlayResolutionStage(profile, keepEditingInfo).resolve(model, overlay).asInstanceOf[T]
      case extension: Extension =>
        new ExtensionResolutionStage(profile, keepEditingInfo).resolve(model, extension).asInstanceOf[T]
      case _ => extendsStage.resolve(model)
    }
  }
}

abstract class ExtensionLikeResolutionStage[T <: ExtensionLike[_ <: DomainElement]](
    val profile: ProfileName,
    val keepEditingInfo: Boolean)(implicit val errorHandler: ErrorHandler) {

  /** Default to raml10 context. */
  implicit val ctx: RamlWebApiContext = profile match {
    case RAML08Profile => new Raml08WebApiContext("", Nil, ParserContext())
    case _             => new Raml10WebApiContext("", Nil, ParserContext())
  }

  def removeExtends(document: Document): BaseUnit = {
    document.encodes.asInstanceOf[WebApi].endPoints.foreach { endpoint =>
      if (!keepEditingInfo)
        endpoint.fields.removeField(DomainElementModel.Extends)
      endpoint.operations.foreach { operation =>
        if (!keepEditingInfo)
          operation.fields.removeField(DomainElementModel.Extends)
      }
    }
    document
  }

  def mergeReferences(document: Document,
                      extension: ExtensionLike[WebApi],
                      extensionId: String,
                      extensionLocation: Option[String]): Unit = {
    val existing = document.references.map(_.id) ++ Seq(document.id, extension.id)
    val libs     = extension.references.collect { case m: Module => m }

    val refs = document.references ++ libs.filter(unit => !existing.contains(unit.id)).map { unit =>
      unit.annotations += ExtensionProvenance(extensionId, extensionLocation)
      unit
    }

    document.withReferences(refs)
  }

  def mergeAliases(document: Document, extension: ExtensionLike[_ <: DomainElement]) = {
    val extensionsAliases = extension.annotations.find(classOf[Aliases]).getOrElse(Aliases(Set())).aliases
    val documentAliases   = document.annotations.find(classOf[Aliases]).getOrElse(Aliases(Set())).aliases

    extensionsAliases.map(_._1).intersect(documentAliases.map(_._1)).foreach { alias =>
      val extensionFullUrl = extensionsAliases.find(_._1 == alias).map(_._2._1).get
      val docFullUrl       = documentAliases.find(_._1 == alias).map(_._2._1).get
      if (extensionFullUrl != docFullUrl)
        throw new Exception(s"Conflicting urls for alias '$alias' and libraries: '$extensionFullUrl' - '$docFullUrl'")
    }

    val totalAliases = extensionsAliases.union(documentAliases)
    if (totalAliases.nonEmpty) {
      document.annotations.reject(_.isInstanceOf[Aliases])
      document.annotations += Aliases(totalAliases)
    }
  }

  def resolve(model: BaseUnit, entryPoint: T): BaseUnit

  protected def resolveOverlay(model: BaseUnit, entryPoint: T): BaseUnit = {
    extensionsQueue(ListBuffer[BaseUnit](entryPoint), entryPoint) match {
      case (document: Document) :: extensions =>
        // Don't remove Extends field from the model when traits and resource types are resolved.
        val extendsStage   = new ExtendsResolutionStage(profile, keepEditingInfo, fromOverlay = true) // this false is required to merge overlays with traits/resource types
        val referenceStage = new ReferenceResolutionStage(keepEditingInfo)

        // All includes are resolved and applied for both Master Tree and Extension Tree.
        referenceStage.resolve(document)
        // All Trait and Resource Types applications are applied in the Master Tree.
        extendsStage.resolve(document)

        // Current Target Tree Object is set to the Target Tree root (API).
        val masterTree = document.asInstanceOf[EncodesModel].encodes.asInstanceOf[WebApi]

        extensions.foreach {
          // Current Extension Tree Object is set to the Extension Tree root (API).
          case extension: ExtensionLike[_] =>
            // Resolve references.
            referenceStage.resolve(extension)

            val iriMerger = IriMerger(document.id + "#", extension.id + "#")

            mergeDeclarations(document,
                              extension.asInstanceOf[ExtensionLike[WebApi]],
                              iriMerger,
                              extension.id,
                              ExtendsHelper.findUnitLocationOfElement(extension.id, model))

            mergeAliases(document, extension)

            mergeReferences(document,
                            extension.asInstanceOf[ExtensionLike[WebApi]],
                            extension.id,
                            ExtendsHelper.findUnitLocationOfElement(extension.id, model))

            merge(masterTree,
                  extension.encodes,
                  extension.id,
                  ExtendsHelper.findUnitLocationOfElement(extension.id, model))

            adoptIris(iriMerger, masterTree)

            // Traits and Resource Types applications are applied one more time to the Target Tree.
            extendsStage.resolve(document)
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

  def merge(master: DomainElement,
            overlay: DomainElement,
            extensionId: String,
            extensionLocation: Option[String]): DomainElement = {
    cleanSynthesizedFacets(master)
    overlay.fields.fields().filter(ignored).foreach {
      case entry @ FieldEntry(field, value) =>
        master.fields.entry(field) match {
          case None =>
            val newValue = adoptInner(master.id, value.value)
            if (keepEditingInfo) newValue.annotations += ExtensionProvenance(extensionId, extensionLocation)
            master.set(field, newValue) // Set field if it doesn't exist.
          case Some(existing) =>
            field.`type` match {
              case _: Type.Scalar =>
                if (keepEditingInfo) value.value.annotations += ExtensionProvenance(extensionId, extensionLocation)
                master.set(field, value.value)
              case Type.ArrayLike(element) =>
                mergeByValue(master, field, element, existing.value, value, extensionId, extensionLocation)
              case DataNodeModel =>
                mergeDataNode(master,
                              field,
                              existing.value.value.asInstanceOf[DomainElement],
                              value.value.asInstanceOf[DomainElement],
                              extensionId,
                              extensionLocation)
              case _: ShapeModel if incompatibleType(existing.domainElement, entry.domainElement) =>
                master.set(field, entry.domainElement, Annotations(ExtensionProvenance(overlay.id, extensionLocation)))
              case _: DomainElementModel =>
                merge(existing.domainElement, entry.domainElement, extensionId, extensionLocation)
              case _ => throw new Exception(s"Cannot merge '${field.`type`}':not a (Scalar|Array|Object)")
            }
        }
    }
    master
  }

  def cleanSynthesizedFacets(domain: DomainElement): Unit = {
    domain match {
      case shape: Shape =>
        shape.annotations.reject(_.isInstanceOf[SynthesizedField])
      case _ => //
    }
  }

  private def incompatibleType(master: DomainElement, overlay: DomainElement): Boolean = {
    if (master.isInstanceOf[Shape] && overlay.isInstanceOf[Shape]) {
      master.getClass != overlay.getClass
    } else {
      false
    }
  }

  def mergeDataNode(master: DomainElement,
                    field: Field,
                    existing: DomainElement,
                    overlay: DomainElement,
                    extensionId: String,
                    extensionLocation: Option[String]): Unit = {
    (existing, overlay) match {
      case (e: DataNode, o: DataNode) if existing.getClass == overlay.getClass =>
        DataNodeMerging.merge(e, o)
      case _ =>
        // Different types of nodes means the overlay has redefined this extension, so replace it
        if (keepEditingInfo) overlay.annotations += ExtensionProvenance(extensionId, extensionLocation)
        master.set(field, overlay)
    }
  }

  /** Merge annotation types, types, security schemes, resource types,  */
  def mergeDeclarations(master: Document,
                        extension: ExtensionLike[WebApi],
                        iriMerger: IriMerger,
                        extensionId: String,
                        extensionLocation: Option[String]): Unit = {
    val declarations = WebApiDeclarations(master.declares, Some(ctx), EmptyFutureDeclarations())

    // Extension declarations will be added to master document. The ones with the same name will be merged.
    extension.declares.foreach { declaration =>
      declarations.findEquivalent(declaration) match {
        case Some(equivalent) => merge(equivalent, declaration, extensionId, extensionLocation)
        case None =>
          val extendedDeclaration = adoptInner(master.id + "#/declarations", declaration).asInstanceOf[DomainElement]
          if (keepEditingInfo) extendedDeclaration.annotations += ExtensionProvenance(extensionId, extensionLocation)
          declarations += extendedDeclaration
      }
    }

    val declarables = declarations.declarables()

    declarables.foreach(adoptIris(iriMerger, _))

    master.withDeclares(declarables)
  }

  def setDomainElementArrayValue(target: DomainElement,
                                 field: Field,
                                 other: AmfArray,
                                 extensionId: String,
                                 extensionLocation: Option[String]): Unit

  private def mergeByValue(target: DomainElement,
                           field: Field,
                           element: Type,
                           main: Value,
                           other: Value,
                           extensionId: String,
                           extensionLocation: Option[String]): Unit = {
    val m = main.value.asInstanceOf[AmfArray]
    val o = other.value.asInstanceOf[AmfArray]

    element match {
      case _: Type.Scalar        => mergeByValue(target, field, m, o, extensionId, extensionLocation)
      case key: KeyField         => mergeByKeyValue(target, field, element, key, m, o, extensionId, extensionLocation)
      case _: DomainElementModel => setDomainElementArrayValue(target, field, o, extensionId, extensionLocation)
      case _                     => throw new Exception(s"Cannot merge '$element': not a KeyField nor a Scalar")
    }
  }

  private def mergeByValue(target: DomainElement,
                           field: Field,
                           main: AmfArray,
                           other: AmfArray,
                           extensionId: String,
                           extensionLocation: Option[String]): Unit = {
    val existing = main.values.map(_.asInstanceOf[AmfScalar].value).toSet
    other.values.foreach { value =>
      val scalar = value.asInstanceOf[AmfScalar].value
      if (!existing.contains(scalar)) {
        val scalarValue = AmfScalar(scalar)
        if (keepEditingInfo) scalarValue.annotations += ExtensionProvenance(extensionId, extensionLocation)
        target.add(field, scalarValue)
      }
    }
  }

  private def mergeByKeyValue(target: DomainElement,
                              field: Field,
                              element: Type,
                              key: KeyField,
                              master: AmfArray,
                              extension: AmfArray,
                              extensionId: String,
                              extensionLocation: Option[String]): Unit = {

    val asSimpleProperty = key == ExampleModel || key == DomainExtensionModel || key == ParametrizedTraitModel || key == ParametrizedSecuritySchemeModel

    val existing = mutable.Map(master.values.flatMap { m =>
      val obj = m.asInstanceOf[DomainElement]
      obj.fields.entry(key.key).map(_.scalar.value -> obj)
    }: _*)

    var nullKey = master.values
      .find {
        case o: DomainElement => o.fields.entry(key.key).isEmpty
      }
      .map(_.asInstanceOf[DomainElement])

    extension.values.foreach {
      case obj: DomainElement =>
        obj.fields.entry(key.key) match {
          case Some(value) =>
            val keyValue = value.scalar.value
            existing += keyValue -> mergeByKeyResult(target,
                                                     asSimpleProperty,
                                                     existing.get(keyValue),
                                                     obj,
                                                     extensionId,
                                                     extensionLocation)

          case _ => // If key is null and nullKey exists, merge if it is not a simpleProperty. Else just override.
            nullKey = Some(mergeByKeyResult(target, asSimpleProperty, nullKey, obj, extensionId, extensionLocation))
        }
    }

    target.setArray(field, existing.values.toSeq ++ nullKey)
  }

  private def mergeByKeyResult(target: DomainElement,
                               asSimpleProperty: Boolean,
                               existing: Option[DomainElement],
                               obj: DomainElement,
                               extensionId: String,
                               extensionLocation: Option[String]) = {
    existing match {
      case Some(e) if !asSimpleProperty => merge(e, obj.adopted(target.id), extensionId, extensionLocation)
      case _                            => adoptInner(target.id, obj).asInstanceOf[DomainElement]
    }
  }

  private def ignored(entry: FieldEntry) = entry.field match {
    case Sources | BaseUnitModel.Usage | ExtensionLikeModel.Extends => false
    case _                                                          => true
  }

  def adoptInner(id: String, target: AmfElement): AmfElement = target match {
    case array: AmfArray =>
      AmfArray(array.values.map(adoptInner(id, _)), array.annotations)
    case dataNode: DataNode =>
      adoptTree(id, dataNode)
    case element: DomainElement =>
      element.adopted(id)

      element.fields.foreach {
        case (_, value) => adoptInner(element.id, value.value)
      }

      element
    case _ => target
  }

  def adoptIris(iriMerger: IriMerger, target: DomainElement): Unit = {
    target.fields.foreach {
      case (field, value) =>
        field.`type` match {
          case Type.Iri => iriMerger.merge(target, field, value.value)
          case Type.ArrayLike(_: DomainElementModel) =>
            value.value.asInstanceOf[AmfArray].values.collect { case d: DomainElement => d }.foreach {
              adoptIris(iriMerger, _)
            }
          case _: DomainElementModel => adoptIris(iriMerger, value.value.asInstanceOf[DomainElement])
          case _                     =>
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
            ParserSideValidations.MissingExtensionInReferences.id,
            model,
            Some(extension.extend),
            s"BaseUnit '${extension.extend}' not found in references."
          )
          Nil
        case _ =>
          errorHandler.violation(
            ParserSideValidations.MissingExtensionInReferences.id,
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
    override implicit val errorHandler: ErrorHandler)
    extends ExtensionLikeResolutionStage[Extension](profile, keepEditingInfo) {
  override def resolve(model: BaseUnit, entryPoint: Extension): BaseUnit = resolveOverlay(model, entryPoint)

  override def setDomainElementArrayValue(target: DomainElement,
                                          field: Field,
                                          other: AmfArray,
                                          extensionId: String,
                                          extensionLocation: Option[String]): Unit = {
    other.values.foreach { value =>
      if (keepEditingInfo) value.annotations += ExtensionProvenance(extensionId, extensionLocation)
      target.add(field, value)
    }
  }
}

class OverlayResolutionStage(override val profile: ProfileName, override val keepEditingInfo: Boolean)(
    override implicit val errorHandler: ErrorHandler)
    extends ExtensionLikeResolutionStage[Overlay](profile, keepEditingInfo) {
  override def resolve(model: BaseUnit, entryPoint: Overlay): BaseUnit = resolveOverlay(model, entryPoint)

  override def setDomainElementArrayValue(target: DomainElement,
                                          field: Field,
                                          other: AmfArray,
                                          extensionId: String,
                                          extensionLocation: Option[String]): Unit = {
    val seq: Seq[AmfElement] = other.values.map { value =>
      if (keepEditingInfo) value.annotations += ExtensionProvenance(extensionId, extensionLocation)
      value
    }
    target.setArray(field, seq)
  }
}
