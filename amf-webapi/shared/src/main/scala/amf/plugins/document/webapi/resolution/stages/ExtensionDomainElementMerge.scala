package amf.plugins.document.webapi.resolution.stages

import amf.core.annotations.SynthesizedField
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.metamodel.domain.DomainElementModel.Sources
import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel, ShapeModel}
import amf.core.metamodel.{Field, Type}
import amf.core.model.domain._
import amf.core.parser.{Annotations, FieldEntry, Value}
import amf.plugins.document.webapi.annotations.{ExtensionProvenance, Inferred}
import amf.plugins.domain.shapes.metamodel.{ExampleModel, ScalarShapeModel}
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel
import amf.plugins.domain.webapi.metamodel.templates.ParametrizedTraitModel
import amf.plugins.domain.webapi.resolution.stages.DataNodeMerging
import amf.plugins.features.validation.CoreValidations.ResolutionValidation

import scala.collection.mutable

trait DomainElementArrayMergeStrategy {
  def merge(target: DomainElement, field: Field, o: AmfArray, extensionId: String, extensionLocation: Option[String])
}

class ExtensionDomainElementMerge(restrictions: MergingRestrictions,
                                  keepEditingInfo: Boolean,
                                  domainElemdomainElementArrayMergeStrategy: DomainElementArrayMergeStrategy)(
    implicit val errorHandler: ErrorHandler)
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
      overlay.fields
        .fields()
        .filter(f => ignored(f, master))
        .foreach(mergeField(_, master, overlay, idTracker, extensionId, extensionLocation))
    }
    master
  }

  private def skipNode(): Unit = Unit

  private def mergeField(entry: FieldEntry,
                         master: DomainElement,
                         overlay: DomainElement,
                         idTracker: IdTracker,
                         extensionId: String,
                         extensionLocation: Option[String]): Unit = {
    val FieldEntry(field, value) = entry
    master.fields.entry(field) match {
      case None if restrictions allowsNodeInsertionIn field =>
        insertNode(master, idTracker, extensionId, extensionLocation, entry)
      case None if field == ScalarShapeModel.DataType && isInferred(value) => skipNode()
      // If the overlay field is a datatype and the type is inferred it must be a type that add only an example
      // Nothing to do
      case None => forbiddenInsertionError(entry)

      case Some(existing) if restrictions allowsOverride field =>
        field.`type` match {
          case _: Type.Scalar =>
            if (keepEditingInfo) entry.element.annotations += ExtensionProvenance(extensionId, extensionLocation)
            master.set(field, entry.element)
          case Type.ArrayLike(element) =>
            mergeArrays(master, field, element, existing.array, entry.array, extensionId, extensionLocation)
          case DataNodeModel =>
            mergeDataNode(master,
                          field,
                          existing.element.asInstanceOf[DomainElement],
                          entry.element.asInstanceOf[DomainElement],
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
              s"Cannot merge '${field.`type`}':not a (Scalar|Array|Object)",
              entry.element.annotations
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

  private def insertNode(master: DomainElement,
                         idTracker: IdTracker,
                         extensionId: String,
                         extensionLocation: Option[String],
                         entry: FieldEntry): Unit = {
    val FieldEntry(field, value) = entry
    val newValue                 = adoptInner(master.id, value.value, idTracker)
    if (keepEditingInfo) newValue.annotations += ExtensionProvenance(extensionId, extensionLocation)
    master.set(field, newValue)
  }

  private def forbiddenInsertionError(entry: FieldEntry): Unit = {
    val FieldEntry(field, value) = entry
    val (node, annotations) = entry.element match {
      case amfObject: AmfObject => (amfObject.id, amfObject.annotations)
      case array: AmfArray =>
        val ann =
          if (value.annotations.nonEmpty) value.annotations
          else array.values.headOption.map(_.annotations).getOrElse(Annotations())
        (field.value.toString, ann)
      case _ => (field.value.toString, entry.element.annotations)
    }

    errorHandler.violation(
      ResolutionValidation,
      node,
      s"Property '$node' of type '${entry.element.getClass.getSimpleName}' is not allowed to be overriden or added in overlays",
      annotations
    )
  }

  private def isInferred(value: Value) = value.annotations.contains(classOf[Inferred])

  private def isSameValue(existing: FieldEntry, master: FieldEntry): Boolean =
    existing.value.toString == master.value.toString

  private def cleanSynthesizedFacets(domain: DomainElement): Unit = domain match {
    case shape: Shape => shape.annotations.reject(_.isInstanceOf[SynthesizedField])
    case _            => // ignore
  }

  private def incompatibleType(master: DomainElement, overlay: DomainElement): Boolean = (master, overlay) match {
    case (_: Shape, _: Shape) => master.getClass != overlay.getClass
    case _                    => false
  }

  private def mergeDataNode(master: DomainElement,
                            field: Field,
                            existing: DomainElement,
                            overlay: DomainElement,
                            extensionId: String,
                            extensionLocation: Option[String]): Unit = {
    (existing, overlay) match {
      case (e: DataNode, o: DataNode) if existing.getClass == overlay.getClass => DataNodeMerging.merge(e, o)
      case _                                                                   =>
        // Different types of nodes means the overlay has redefined this extension, so replace it
        if (keepEditingInfo) overlay.annotations += ExtensionProvenance(extensionId, extensionLocation)
        master.set(field, overlay)
    }
  }

  private def mergeArrays(target: DomainElement,
                          field: Field,
                          element: Type,
                          main: AmfArray,
                          other: AmfArray,
                          extensionId: String,
                          extensionLocation: Option[String]): Unit = {
    element match {
      case _: Type.Scalar => mergeScalarArrays(target, field, main, other, extensionId, extensionLocation)
      case key: KeyField  => mergeByKeyValue(target, field, key, main, other, extensionId, extensionLocation)
      case _: DomainElementModel =>
        domainElemdomainElementArrayMergeStrategy.merge(target, field, other, extensionId, extensionLocation)
      case _ =>
        errorHandler.violation(ResolutionValidation,
                               extensionId,
                               None,
                               s"Cannot merge '$element': not a KeyField nor a Scalar",
                               target.position(),
                               target.location())
    }
  }

  private def mergeScalarArrays(target: DomainElement,
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
                              key: KeyField,
                              master: AmfArray,
                              extension: AmfArray,
                              extensionId: String,
                              extensionLocation: Option[String]): Unit = {

    val asSimpleProperty                  = isSimpleProperty(key)
    var existing: Map[Any, DomainElement] = buildElementByKeyMap(key, master)
    var nullKey: Option[DomainElement]    = findElementWithNullKey(key, master)

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

  private def isSimpleProperty(key: KeyField) = {
    key == ExampleModel || key == DomainExtensionModel || key == ParametrizedTraitModel || key == ParametrizedSecuritySchemeModel
  }

  private def findElementWithNullKey(key: KeyField, master: AmfArray): Option[DomainElement] = {
    master.values
      .find {
        case o: DomainElement => o.fields.entry(key.key).isEmpty
      }
      .map(_.asInstanceOf[DomainElement])
  }

  private def buildElementByKeyMap(key: KeyField, master: AmfArray) =
    master.values
      .map(_.asInstanceOf[DomainElement])
      .flatMap { element =>
        element.fields.entry(key.key).map(_.scalar.value -> element)
      }
      .toMap

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
