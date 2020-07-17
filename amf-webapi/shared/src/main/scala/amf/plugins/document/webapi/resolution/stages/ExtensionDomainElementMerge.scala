package amf.plugins.document.webapi.resolution.stages

import amf.core.annotations.{LexicalInformation, SourceLocation, SynthesizedField}
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.metamodel.domain.DomainElementModel.Sources
import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.{Field, Type}
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel, ShapeModel}
import amf.core.model.domain.{AmfArray, AmfObject, AmfScalar, DataNode, DomainElement, Shape}
import amf.core.parser.{Annotations, FieldEntry, Value}
import amf.plugins.document.webapi.annotations.{ExtensionProvenance, Inferred}
import amf.plugins.domain.shapes.metamodel.{ExampleModel, ScalarShapeModel}
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel
import amf.plugins.domain.webapi.metamodel.templates.ParametrizedTraitModel
import amf.plugins.domain.webapi.resolution.stages.DataNodeMerging
import amf.plugins.features.validation.CoreValidations.ResolutionValidation

import scala.collection.mutable

trait DomainElementArrayValueMerge {
  def merge(target: DomainElement, field: Field, o: AmfArray, extensionId: String, extensionLocation: Option[String])
}

class ExtensionDomainElementMerge(
    restrictions: MergingRestrictions,
    keepEditingInfo: Boolean,
    domainElementArrayValueMerge: DomainElementArrayValueMerge)(implicit val errorHandler: ErrorHandler)
    extends InnerAdoption {
  def merge(master: DomainElement,
            overlay: DomainElement,
            extensionId: String,
            extensionLocation: Option[String],
            idTracker: IdTracker): DomainElement = {
    val ids = master.id :: overlay.id :: Nil
    if (idTracker.notTracking(ids)) {
      idTracker.track(ids)
      cleanSynthesizedFacets(master)
      overlay.fields.fields().filter(f => ignored(f, master)).foreach {
        case entry @ FieldEntry(field, value) =>
          master.fields.entry(field) match {
            case None if restrictions allowsNodeInsertionIn field =>
              val newValue = adoptInner(master.id, value.value, idTracker)
              if (keepEditingInfo) newValue.annotations += ExtensionProvenance(extensionId, extensionLocation)
              master.set(field, newValue) // Set field if it doesn't exist.
            case None if field == ScalarShapeModel.DataType && value.annotations.contains(classOf[Inferred]) =>
            // If the overlay field is a datatype and the type is inferred it must be a type that add only an example
            // Nothing to do
            case None => // not allowed insert a new obj node. Not exists node in master.
              val (node, annotations) = value.value match {
                case amfObject: AmfObject => (amfObject.id, amfObject.annotations)
                case array: AmfArray =>
                  val ann =
                    if (value.annotations.nonEmpty) value.annotations
                    else array.values.headOption.map(_.annotations).getOrElse(Annotations())
                  (field.value.toString, ann)
                case _ => (field.value.toString, value.value.annotations)
              }

              errorHandler.violation(
                ResolutionValidation,
                node,
                s"Property '$node' of type '${value.value.getClass.getSimpleName}' is not allowed to be overriden or added in overlays",
                annotations
              )

            case Some(existing) if restrictions allowsOverride field =>
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
                  master
                    .set(field, entry.domainElement, Annotations(ExtensionProvenance(overlay.id, extensionLocation)))
                case _: DomainElementModel =>
                  merge(existing.domainElement, entry.domainElement, extensionId, extensionLocation, idTracker)
                case _ =>
                  errorHandler.violation(
                    ResolutionValidation,
                    field.toString,
                    None,
                    s"Cannot merge '${field.`type`}':not a (Scalar|Array|Object)",
                    value.value.annotations.find(classOf[LexicalInformation]),
                    value.value.annotations.find(classOf[SourceLocation]).map(_.location)
                  )
              }
            case Some(existing) => // cannot be override
              if (!isSameValue(existing, entry))
                errorHandler.violation(
                  ResolutionValidation,
                  field.toString,
                  s"Property '${existing.field.toString}' in '${master.getClass.getSimpleName}' is not allowed to be overriden or added in overlays",
                  value.annotations
                )
          }
      }
    }
    master
  }

  def isSameValue(existing: FieldEntry, master: FieldEntry): Boolean = existing.value.toString == master.value.toString

  def cleanSynthesizedFacets(domain: DomainElement): Unit = domain match {
    case shape: Shape =>
      shape.annotations.reject(_.isInstanceOf[SynthesizedField])
    case _ => //
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
      case _: Type.Scalar => mergeByValue(target, field, m, o, extensionId, extensionLocation)
      case key: KeyField  => mergeByKeyValue(target, field, element, key, m, o, extensionId, extensionLocation)
      case _: DomainElementModel =>
        domainElementArrayValueMerge.merge(target, field, o, extensionId, extensionLocation)
      case _ =>
        errorHandler.violation(ResolutionValidation,
                               extensionId,
                               None,
                               s"Cannot merge '$element': not a KeyField nor a Scalar",
                               target.position(),
                               target.location())
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
        val tracker = IdTracker()
        obj.fields.entry(key.key) match {
          case Some(value) =>
            val keyValue = value.scalar.value
            existing += keyValue -> mergeByKeyResult(target,
                                                     asSimpleProperty,
                                                     existing.get(keyValue),
                                                     obj,
                                                     extensionId,
                                                     extensionLocation,
                                                     field,
                                                     tracker)

          case _ => // If key is null and nullKey exists, merge if it is not a simpleProperty. Else just override.
            nullKey = Some(
              mergeByKeyResult(target, asSimpleProperty, nullKey, obj, extensionId, extensionLocation, field, tracker))
        }
    }

    target.setArray(field, existing.values.toSeq ++ nullKey)
  }

  private def mergeByKeyResult(target: DomainElement,
                               asSimpleProperty: Boolean,
                               existing: Option[DomainElement],
                               obj: DomainElement,
                               extensionId: String,
                               extensionLocation: Option[String],
                               field: Field,
                               idTracker: IdTracker) = {
    existing match {
      case Some(e) if !asSimpleProperty => merge(e, obj.adopted(target.id), extensionId, extensionLocation, idTracker)
      case None if !(restrictions allowsNodeInsertionIn field) =>
        errorHandler.violation(
          ResolutionValidation,
          obj.id,
          s"Property of key '${obj.id}' of class '${obj.getClass.getSimpleName}' is not allowed to be overriden or added in overlays",
          obj.annotations
        )
        obj
      case _ =>
        adoptInner(target.id, obj, idTracker).asInstanceOf[DomainElement]
    }
  }

  private def ignored(entry: FieldEntry, domainElement: DomainElement) = entry.field match {
    case Sources | BaseUnitModel.Usage                                                => false
    case ExtensionLikeModel.Extends if domainElement.isInstanceOf[ExtensionLikeModel] => false
    case _                                                                            => true
  }

}
