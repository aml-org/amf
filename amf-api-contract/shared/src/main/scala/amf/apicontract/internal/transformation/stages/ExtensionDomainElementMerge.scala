package amf.apicontract.internal.transformation.stages

import amf.apicontract.internal.metamodel.domain.security.ParametrizedSecuritySchemeModel
import amf.apicontract.internal.metamodel.domain.templates.ParametrizedTraitModel
import amf.apicontract.internal.spec.common.transformation.stage.DataNodeMerging
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain._
import amf.core.internal.annotations.{Inferred, SynthesizedField, VirtualNode}
import amf.core.internal.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.internal.metamodel.domain.ShapeModel.Sources
import amf.core.internal.metamodel.domain.extensions.DomainExtensionModel
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{DataNodeModel, DomainElementModel, ShapeModel}
import amf.core.internal.metamodel.{Field, Obj, Type}
import amf.core.internal.parser.domain.{Annotations, FieldEntry, Value}
import amf.core.internal.validation.CoreValidations.TransformationValidation
import amf.shapes.internal.domain.metamodel.{ExampleModel, ScalarShapeModel}

import scala.language.postfixOps

class ExtensionDomainElementMerge(
    restrictions: MergingRestrictions,
    domainElementArrayMergeStrategy: DomainElementArrayMergeStrategy,
    extensionId: String,
    extensionLocation: Option[String],
    preMergeTransform: PreMergeTransform
)(implicit val errorHandler: AMFErrorHandler)
    extends InnerAdoption {

  def merge(main: DomainElement, overlay: DomainElement, idTracker: IdTracker): DomainElement = {
    val ids = main.id :: overlay.id :: Nil
    if (idTracker.notTracking(ids)) {
      idTracker.track(ids)
      cleanSynthesizedFacets(main)
      preMergeTransform
        .transform(main, overlay)
        .fields
        .fields()
        .filter(f => ignored(f, main))
        .foreach(mergeField(_, main, overlay, idTracker))
    }
    main
  }

  private def mergeField(
      entry: FieldEntry,
      main: DomainElement,
      overlay: DomainElement,
      idTracker: IdTracker
  ): Unit = {
    val FieldEntry(field, value) = entry
    main.fields.entry(field) match {
      case None if restrictions allowsNodeInsertionIn field                  => insertNode(main, idTracker, entry)
      case None if field == ScalarShapeModel.DataType && value.isSynthesized => skipNode()
      // If the overlay field is a datatype and the type is inferred it must be a type that add only an example
      // Nothing to do
      case None => forbiddenInsertionError(entry)

      case Some(existing) if restrictions allowsOverride field =>
        field.`type` match {
          case _: Type.Scalar =>
            main.set(field, entry.element)
          case Type.ArrayLike(element) =>
            mergeArrays(main, field, element, existing.array, entry.array)
          case DataNodeModel =>
            mergeDataNode(
              main,
              field,
              existing.element.asInstanceOf[DomainElement],
              entry.element.asInstanceOf[DomainElement]
            )
          case _: ShapeModel if incompatibleType(existing.domainElement, entry.domainElement) =>
            main
              .set(field, entry.domainElement)
          case _: DomainElementModel =>
            merge(existing.domainElement, entry.domainElement, idTracker)
          case _ =>
            errorHandler.violation(
              TransformationValidation,
              field.toString,
              s"Cannot merge '${field.`type`}':not a (Scalar|Array|Object)",
              entry.element.annotations
            )
        }
      case Some(existing) => // cannot be override
        if (!isInferred(value) && !isSameValue(existing, entry))
          errorHandler.violation(
            TransformationValidation,
            field.toString,
            s"Property '${existing.field.toString}' in '${main.getClass.getSimpleName}' is not allowed to be overriden or added in overlays",
            value.annotations
          )
    }

  }

  private def insertNode(main: DomainElement, idTracker: IdTracker, entry: FieldEntry): Unit = {
    val FieldEntry(field, value) = entry
    val newValue                 = adoptInner(main.id, value.value, idTracker)
    main.set(field, newValue)
  }

  private def skipNode(): Unit = Unit

  private def forbiddenInsertionError(entry: FieldEntry): Unit = {
    val FieldEntry(field, value) = entry
    val (node, annotations) = entry.element match {
      case amfObject: AmfObject => (amfObject.id, amfObject.annotations)
      case array: AmfArray =>
        val ann =
          if (value.annotations.nonEmpty && !value.annotations.contains(classOf[VirtualNode])) value.annotations
          else array.values.headOption.map(_.annotations).getOrElse(Annotations())
        (field.value.toString, ann)
      case _ => (field.value.toString, entry.element.annotations)
    }

    errorHandler.violation(
      TransformationValidation,
      node,
      s"Property '$node' of type '${entry.element.getClass.getSimpleName}' is not allowed to be overriden or added in overlays",
      annotations
    )
  }
  private def isInferred(value: Value) = value.annotations.contains(classOf[Inferred])

  private def isSameValue(existing: FieldEntry, main: FieldEntry): Boolean =
    existing.value.toString == main.value.toString

  private def cleanSynthesizedFacets(domain: DomainElement): Unit = domain match {
    case shape: Shape => shape.annotations.reject(_.isInstanceOf[SynthesizedField])
    case _            => // ignore
  }

  private def incompatibleType(main: DomainElement, overlay: DomainElement): Boolean = (main, overlay) match {
    case (_: Shape, _: Shape) => !areSameType(main, overlay)
    case _                    => false
  }

  private def areSameType(main: DomainElement, overlay: DomainElement) = main.getClass == overlay.getClass

  private def mergeDataNode(
      main: DomainElement,
      field: Field,
      existing: DomainElement,
      overlay: DomainElement
  ): Unit = {
    (existing, overlay) match {
      case (e: DataNode, o: DataNode) if areSameType(existing, overlay) => DataNodeMerging.merge(e, o)
      case _                                                            =>
        // Different types of nodes means the overlay has redefined this extension, so replace it
        main.set(field, overlay)
    }
  }

  private def mergeArrays(target: DomainElement, field: Field, element: Type, main: AmfArray, other: AmfArray): Unit = {
    element match {
      case _: Type.Scalar => mergeScalarArrays(target, field, main, other)
      case key: KeyField  => mergeArraysByKey(target, field, key, main, other)
      case _: DomainElementModel =>
        domainElementArrayMergeStrategy.merge(target, field, other, extensionId, extensionLocation)
      case _ =>
        errorHandler.violation(
          TransformationValidation,
          extensionId,
          s"Cannot merge '$element': not a KeyField nor a Scalar",
          target.annotations
        )
    }
  }

  private def mergeScalarArrays(target: DomainElement, field: Field, main: AmfArray, other: AmfArray): Unit = {
    val existing = main.values.map(_.asInstanceOf[AmfScalar].value).toSet
    other.scalars.foreach { scalarValue =>
      val scalar = scalarValue.value
      if (!existing.contains(scalar)) {
        val scalarValue = AmfScalar(scalar)
        target.add(field, scalarValue)
      }
    }
  }

  private def mergeArraysByKey(
      target: DomainElement,
      field: Field,
      key: KeyField,
      main: AmfArray,
      extension: AmfArray
  ): Unit = {

    val asSimpleProperty                          = isSimpleProperty(key)
    var existingElements: Map[Any, DomainElement] = buildElementByKeyMap(key, main)
    // if we have multiple elements with null key we merge by meta Obj
    var existingNullKeyElements: Map[Obj, DomainElement] = findElementsWithNullKey(key, main)

    extension.values.foreach { case obj: DomainElement =>
      val tracker = IdTracker()
      obj.fields.entry(key.key) match {
        case Some(value) =>
          val keyValue = value.scalar.value
          existingElements += keyValue -> mergeByKeyResult(
            target,
            asSimpleProperty,
            existingElements.get(keyValue),
            obj,
            field,
            tracker
          )

        case _ => // If key is null and nullKey exists, merge if it is not a simpleProperty. Else just override.
          val element =
            mergeByKeyResult(target, asSimpleProperty, existingNullKeyElements.get(obj.meta), obj, field, tracker)

          existingNullKeyElements = existingNullKeyElements + (element.meta -> element)
      }
    }

    target.setArray(field, existingElements.values.toSeq ++ existingNullKeyElements.values.toSeq)
  }

  private def isSimpleProperty(key: KeyField) = {
    key == ExampleModel || key == DomainExtensionModel || key == ParametrizedTraitModel || key == ParametrizedSecuritySchemeModel
  }

  private def findElementsWithNullKey(key: KeyField, main: AmfArray): Map[Obj, DomainElement] = {
    main.values.iterator
      .map(_.asInstanceOf[DomainElement])
      .filter(_.fields.entry(key.key).isEmpty)
      .map(e => e.meta -> e) toMap
  }

  private def buildElementByKeyMap(key: KeyField, main: AmfArray) =
    main.values
      .map(_.asInstanceOf[DomainElement])
      .flatMap { element =>
        element.fields.entry(key.key).map(_.scalar.value -> element)
      }
      .toMap

  private def mergeByKeyResult(
      target: DomainElement,
      asSimpleProperty: Boolean,
      existing: Option[DomainElement],
      obj: DomainElement,
      field: Field,
      idTracker: IdTracker
  ) = {
    existing match {
      case Some(e) if !asSimpleProperty => merge(e, obj.adopted(target.id), idTracker)
      case None if !(restrictions allowsNodeInsertionIn field) =>
        errorHandler.violation(
          TransformationValidation,
          obj.id,
          s"Property of key '${obj.id}' of class '${obj.getClass.getSimpleName}' is not allowed to be overriden or added in overlays",
          obj.annotations
        )
        obj
      case _ => adoptInner(target.id, obj, idTracker).asInstanceOf[DomainElement]
    }
  }

  private def ignored(entry: FieldEntry, domainElement: DomainElement) = entry.field match {
    case Sources | BaseUnitModel.Usage                                                => false
    case ExtensionLikeModel.Extends if domainElement.isInstanceOf[ExtensionLikeModel] => false
    case _                                                                            => true
  }
}
