package amf.plugins.document.webapi.resolution.stages

import amf.ProfileNames
import amf.framework.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.framework.metamodel.domain.DomainElementModel._
import amf.framework.metamodel.domain.templates.KeyField
import amf.framework.metamodel.domain.{DataNodeModel, DomainElementModel}
import amf.framework.metamodel.{Field, Type}
import amf.framework.model.document._
import amf.framework.model.domain._
import amf.framework.parser.{FieldEntry, ParserContext, Value}
import amf.framework.remote.Raml
import amf.framework.resolution.stages.{ReferenceResolutionStage, ResolutionStage}
import amf.framework.unsafe.PlatformSecrets
import amf.plugins.document.webapi.annotations.SynthesizedField
import amf.plugins.document.webapi.contexts.{RamlSpecAwareContext, WebApiContext}
import amf.plugins.document.webapi.parser.spec.Declarations
import amf.plugins.document.webapi.parser.spec.raml.RamlSyntax
import amf.plugins.domain.shapes.metamodel.{ExampleModel, ShapeModel}
import amf.plugins.domain.shapes.models.Shape
import amf.plugins.domain.webapi.metamodel.extensions.DomainExtensionModel
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel
import amf.plugins.domain.webapi.metamodel.templates.ParametrizedTraitModel
import amf.plugins.domain.webapi.models.WebApi
import amf.plugins.domain.webapi.resolution.stages.DataNodeMerging

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  */
class ExtensionsResolutionStage(profile: String)
    extends ResolutionStage(profile)
    with PlatformSecrets {


  implicit val ctx: WebApiContext = new WebApiContext(Raml, ParserContext(), RamlSpecAwareContext, RamlSyntax)

  override def resolve(model: BaseUnit): BaseUnit = {
    val extendsStage = new ExtendsResolutionStage(ProfileNames.AMF)
    model match {
      case overlay: ExtensionLike => resolveOverlay(model, overlay)
      case _                      => extendsStage.resolve(model)
    }
  }

  def removeExtends(document: Document): BaseUnit = {
    document.encodes.asInstanceOf[WebApi].endPoints.foreach { endpoint =>
      endpoint.fields.remove(DomainElementModel.Extends)
      endpoint.operations.foreach { operation =>
        operation.fields.remove(DomainElementModel.Extends)
      }
    }
    document
  }

  def mergeReferences(document: Document, extension: ExtensionLike): Unit = {
    val existing = document.references.map(_.id) ++ Seq(document.id, extension.id)
    val libs     = extension.references.collect { case m: Module => m }

    val refs = document.references ++ libs.filter(unit => !existing.contains(unit.id))

    document.withReferences(refs)
  }

  private def resolveOverlay(model: BaseUnit, entryPoint: ExtensionLike): BaseUnit = {
    val (document: Document) :: extensions = extensionsQueue(ListBuffer[BaseUnit](entryPoint), entryPoint)
    // Don't remove Extends field from the model when traits and resource types are resolved.
    val extendsStage   = new ExtendsResolutionStage(ProfileNames.AMF, removeFromModel = false)
    val referenceStage = new ReferenceResolutionStage(ProfileNames.AMF)

    // All includes are resolved and applied for both Master Tree and Extension Tree.
    referenceStage.resolve(document)
    // All Trait and Resource Types applications are applied in the Master Tree.
    extendsStage.resolve(document)

    // Current Target Tree Object is set to the Target Tree root (API).
    val masterTree = document.asInstanceOf[EncodesModel].encodes.asInstanceOf[WebApi]

    extensions.foreach {
      // Current Extension Tree Object is set to the Extension Tree root (API).
      case extension: ExtensionLike =>
        // Resolve references.
        referenceStage.resolve(extension)

        val iriMerger = IriMerger(document.id + "#", extension.id + "#")

        mergeDeclarations(document, extension, iriMerger)

        mergeReferences(document, extension)

        merge(masterTree, extension.encodes)

        adoptIris(iriMerger, masterTree)

        // Traits and Resource Types applications are applied one more time to the Target Tree.
        extendsStage.resolve(document)
    }

    removeExtends(document)
  }

  case class IriMerger(master: String = "", extension: String = "") {
    def merge(element: DomainElement, field: Field, value: AmfElement): Unit =
      element.set(field, value.asInstanceOf[AmfScalar].toString.replaceFirst(extension, master))
  }

  def merge(master: DomainElement, overlay: DomainElement): DomainElement = {
    cleanSynthesizedFacets(master)
    overlay.fields.fields().filter(ignored).foreach {
      case entry@FieldEntry(field, value) =>

        master.fields.entry(field) match {
          case None =>
            master.set(field, adoptInner(master.id, value.value)) // Set field if it doesn't exist.
          case Some(existing) =>
            field.`type` match {
              case _: Type.Scalar => master.set(field, value.value)
              case Type.ArrayLike(element) => mergeByValue(master, field, element, existing.value, value)
              case DataNodeModel =>
                mergeDataNode(master,
                  field,
                  existing.value.value.asInstanceOf[DomainElement],
                  value.value.asInstanceOf[DomainElement])
              case _: ShapeModel if incompatibleType(existing.domainElement, entry.domainElement) =>
                master.set(field, entry.domainElement)
              case _: DomainElementModel => merge(existing.domainElement, entry.domainElement)
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
      case _            => //
    }
  }


  private def incompatibleType(master: DomainElement, overlay: DomainElement): Boolean = {
    if (master.isInstanceOf[Shape] && overlay.isInstanceOf[Shape]) {
      master.getClass != overlay.getClass
    } else {
      false
    }
  }

  def mergeDataNode(master: DomainElement, field: Field, existing: DomainElement, overlay: DomainElement): Unit =
    (existing, overlay) match {
      case (e: DataNode, o: DataNode) if existing.getClass == overlay.getClass =>
        DataNodeMerging.merge(e, o)
      case _ =>
        // Different types of nodes means the overlay has redefined this extension, so replace it
        master.set(field, overlay)
    }

  /** Merge annotation types, types, security schemes, resource types,  */
  def mergeDeclarations(master: Document, extension: ExtensionLike, iriMerger: IriMerger): Unit = {

    val declarations = Declarations(master.declares, Some(ctx))

    // Extension declarations will be added to master document. The ones with the same name will be merged.
    extension.declares.foreach { declaration =>
      declarations.findEquivalent(declaration) match {
        case Some(equivalent) => merge(equivalent, declaration)
        case None =>
          declarations += adoptInner(master.id + "#/declarations", declaration)
            .asInstanceOf[DomainElement]
      }
    }

    val declarables = declarations.declarables()

    declarables.foreach(adoptIris(iriMerger, _))

    master.withDeclares(declarables)
  }

  def addAll(target: DomainElement, field: Field, other: AmfArray): Unit = other.values.foreach(target.add(field, _))

  private def mergeByValue(target: DomainElement, field: Field, element: Type, main: Value, other: Value): Unit = {
    val m = main.value.asInstanceOf[AmfArray]
    val o = other.value.asInstanceOf[AmfArray]

    element match {
      case _: Type.Scalar        => mergeByValue(target, field, m, o)
      case key: KeyField         => mergeByKeyValue(target, field, element, key, m, o)
      case _: DomainElementModel => addAll(target, field, o)
      case _                     => throw new Exception(s"Cannot merge '$element': not a KeyField nor a Scalar")
    }
  }

  private def mergeByValue(target: DomainElement, field: Field, main: AmfArray, other: AmfArray): Unit = {
    val existing = main.values.map(_.asInstanceOf[AmfScalar].value).toSet
    other.values.foreach { value =>
      val scalar = value.asInstanceOf[AmfScalar].value
      if (!existing.contains(scalar)) {
        target.add(field, AmfScalar(scalar))
      }
    }
  }

  private def mergeByKeyValue(target: DomainElement,
                              field: Field,
                              element: Type,
                              key: KeyField,
                              master: AmfArray,
                              extension: AmfArray): Unit = {

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
            existing += keyValue -> mergeByKeyResult(target, asSimpleProperty, existing.get(keyValue), obj)

          case _ => // If key is null and nullKey exists, merge if it is not a simpleProperty. Else just override.
            nullKey = Some(mergeByKeyResult(target, asSimpleProperty, nullKey, obj))
        }
    }

    target.setArray(field, existing.values.toSeq ++ nullKey)
  }

  private def mergeByKeyResult(target: DomainElement,
                               asSimpleProperty: Boolean,
                               existing: Option[DomainElement],
                               obj: DomainElement) = {
    existing match {
      case Some(e) if !asSimpleProperty => merge(e, obj.adopted(target.id))
      case _                            => adoptInner(target.id, obj).asInstanceOf[DomainElement]
    }
  }

  private def ignored(entry: FieldEntry) = entry.field match {
    case Includes | Sources | BaseUnitModel.Usage | ExtensionLikeModel.Extends => false
    case _                                                                     => true
  }

  def adoptInner(id: String, target: AmfElement): AmfElement = target match {
    case array: AmfArray =>
      AmfArray(array.values.map(adoptInner(id, _)), array.annotations)
    case dataNode: DataNode =>
      DataNodeMerging.adoptInner(id, dataNode)
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
    case extension: ExtensionLike =>
      model.findInReferences(extension.extend) match {
        case Some(e) =>
          collector += e
          extensionsQueue(collector, e)
        case None => throw new Exception(s"BaseUnit '${extension.extend}' not found in references.")
      }
    case _ => collector.reverse.toList
  }
}
