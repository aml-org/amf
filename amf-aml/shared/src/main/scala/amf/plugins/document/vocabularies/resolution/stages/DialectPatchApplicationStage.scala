package amf.plugins.document.vocabularies.resolution.stages

import amf.core.annotations.LexicalInformation
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{AmfArray, AmfElement, AmfScalar}
import amf.core.parser.{ErrorHandler, Value}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.vocabularies.metamodel.document.DialectInstanceModel
import amf.plugins.document.vocabularies.model.document.{DialectInstance, DialectInstancePatch}
import amf.plugins.document.vocabularies.model.domain._
import amf.plugins.features.validation.ParserSideValidations.InvalidDialectPatch

import scala.language.postfixOps

class DialectPatchApplicationStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage() {

  override def resolve[T <: BaseUnit](model: T): T = {
    model match {
      case patch: DialectInstancePatch => resolvePatch(patch).asInstanceOf[T]
      case _                           => model
    }
  }

  private def resolvePatch(patch: DialectInstancePatch): BaseUnit = {
    findTarget(patch) match {
      case Some(target: DialectInstance) =>
        applyPatch(target, patch)
      case _ =>
        patch
    }
  }

  private def findTarget(patch: DialectInstancePatch): Option[DialectInstance] = {
    patch.extendsModel.option() match {
      case Some(id) if patch.location().isDefined =>
        patch.references.find(u => u.isInstanceOf[DialectInstance] && u.location().get.endsWith(id)) match {
          case Some(d: DialectInstance) if d.location().isDefined => Some(d)
          case _                                                  => None
        }
      case _ => None
    }
  }

  private def applyPatch(target: DialectInstance, patch: DialectInstancePatch): DialectInstance = {
    patchNode(Some(target.encodes.asInstanceOf[DialectDomainElement]),
              target.location().get,
              patch.encodes.asInstanceOf[DialectDomainElement],
              patch.location().get) match {
      case Some(patchedDialectElement) => target.withEncodes(patchedDialectElement)
      case None =>
        target.fields.remove(DialectInstanceModel.Encodes.value.iri())
        target
    }
  }

  private def patchNode(targetNode: Option[DialectDomainElement],
                        targetLocation: String,
                        patchNode: DialectDomainElement,
                        patchLocation: String): Option[DialectDomainElement] = {
    findNodeMergePolicy(patchNode) match {
      case "insert" =>
        patchNodeInsert(targetNode, targetLocation, patchNode, patchLocation)
      case "delete" =>
        patchNodeDelete(targetNode, targetLocation, patchNode, patchLocation)
      case "update" =>
        patchNodeUpdate(targetNode, targetLocation, patchNode, patchLocation)
      case "upsert" =>
        patchNodeUpsert(targetNode, targetLocation, patchNode, patchLocation)
      case "ignore" =>
        targetNode
      case "fail" =>
        errorHandler.violation(
          InvalidDialectPatch,
          patchNode.id,
          None,
          s"Node ${patchNode.meta.`type`.map(_.iri()).mkString(",")} cannot be patched",
          patchNode.annotations.find(classOf[LexicalInformation]),
          None
        )
        None
    }
  }

  private def findNodeMergePolicy(element: DialectDomainElement): String =
    element.definedBy.mergePolicy.option().getOrElse("update")
  private def findPropertyMappingMergePolicy(property: PropertyMapping): String =
    property.mergePolicy.option().getOrElse("update")

  // add or ignore if present
  private def patchNodeInsert(targetNode: Option[DialectDomainElement],
                              targetLocation: String,
                              patchNode: DialectDomainElement,
                              patchLocation: String): Option[DialectDomainElement] = {
    if (targetNode.isEmpty) Some(patchNode) else targetNode
  }

  // delete or ignore if not present
  private def patchNodeDelete(targetNode: Option[DialectDomainElement],
                              targetLocation: String,
                              patchNode: DialectDomainElement,
                              patchLocation: String): Option[DialectDomainElement] = {
    if (targetNode.nonEmpty && sameNodeIdentity(targetNode.get, targetLocation, patchNode, patchLocation)) {
      None
    } else {
      targetNode
    }
  }

  private def patchProperty(targetNode: DialectDomainElement,
                            patchField: Field,
                            patchValue: Value,
                            propertyMapping: PropertyMapping,
                            targetLocation: String,
                            patchLocation: String): Unit = {
    propertyMapping.classification() match {
      case LiteralProperty =>
        patchLiteralProperty(targetNode, patchField, patchValue, propertyMapping, targetLocation, patchLocation)
      case LiteralPropertyCollection =>
        patchLiteralCollectionProperty(targetNode,
                                       patchField,
                                       patchValue,
                                       propertyMapping,
                                       targetLocation,
                                       patchLocation)
      case ObjectProperty =>
        patchObjectProperty(targetNode, patchField, patchValue, propertyMapping, targetLocation, patchLocation)
      case ObjectPropertyCollection | ObjectMapProperty | ObjectPairProperty =>
        patchObjectCollectionProperty(targetNode,
                                      patchField,
                                      patchValue,
                                      propertyMapping,
                                      targetLocation,
                                      patchLocation)
      case _ =>
      // throw new Exception("Unsupported node mapping in patch")

    }
  }

  private def patchLiteralProperty(targetNode: DialectDomainElement,
                                   patchField: Field,
                                   patchValue: Value,
                                   propertyMapping: PropertyMapping,
                                   targetLocation: String,
                                   patchLocation: String): Unit = {
    val targetPropertyValue = targetNode.valueForField(patchField)
    findPropertyMappingMergePolicy(propertyMapping) match {
      case "insert" =>
        if (targetPropertyValue.isEmpty) targetNode.patchLiteralField(patchField, patchValue.value)
      case "delete" =>
        try {
          if (targetPropertyValue.nonEmpty && patchValue.value
                .asInstanceOf[AmfScalar]
                .value == targetPropertyValue.get.value.asInstanceOf[AmfScalar].value)
            targetNode.removeField(patchField)
        } catch {
          case _: Exception => // ignore
        }
      case "update" =>
        if (targetPropertyValue.nonEmpty) targetNode.patchLiteralField(patchField, patchValue.value)
      case "upsert" =>
        targetNode.patchLiteralField(patchField, patchValue.value)
      case "ignore" =>
      // ignore
      case "fail" =>
        errorHandler.violation(
          InvalidDialectPatch,
          targetNode.id,
          None,
          s"Property ${patchField.value.iri()} cannot be patched",
          targetPropertyValue.get.annotations.find(classOf[LexicalInformation]),
          None
        )
      case _ =>
      // ignore
    }
  }

  private def patchLiteralCollectionProperty(targetNode: DialectDomainElement,
                                             patchField: Field,
                                             patchValue: Value,
                                             propertyMapping: PropertyMapping,
                                             targetLocation: String,
                                             patchLocation: String): Unit = {
    val targetPropertyValueSeq: Seq[AmfElement] = targetNode.valueForField(patchField) match {
      case Some(v) if v.value.isInstanceOf[AmfArray] => v.value.asInstanceOf[AmfArray].values
      case Some(v)                                   => Seq(v.value)
      case _                                         => Nil
    }
    val targetPropertyValue = Set[AmfElement](targetPropertyValueSeq: _*)

    val patchPropertyValueSeq: Seq[AmfElement] = patchValue.value match {
      case arr: AmfArray => arr.values
      case elm           => Seq(elm)
    }
    val patchPropertyValue = Set[AmfElement](patchPropertyValueSeq: _*)

    findPropertyMappingMergePolicy(propertyMapping) match {
      case "insert" =>
        targetNode.patchLiteralField(patchField, targetPropertyValue.union(patchPropertyValue).toSeq)
      case "delete" =>
        targetNode.patchLiteralField(patchField, targetPropertyValue.diff(patchPropertyValue).toSeq)
      case "update" =>
        targetNode.patchLiteralField(patchField, patchPropertyValue.toSeq)
      case "upsert" =>
        targetNode.patchLiteralField(patchField, targetPropertyValue.union(patchPropertyValue).toSeq)
      case "ignore" =>
      // ignore
      case "fail" =>
        errorHandler.violation(
          InvalidDialectPatch,
          targetNode.id,
          None,
          s"Property ${patchField.value.iri()} cannot be patched",
          patchValue.annotations.find(classOf[LexicalInformation]),
          None
        )
      case _ =>
      // ignore
    }
  }

  private def neutralId(id: String, location: String): String = {
    id.replace(location, "")
  }

  private def patchObjectCollectionProperty(targetNode: DialectDomainElement,
                                            patchField: Field,
                                            patchValue: Value,
                                            propertyMapping: PropertyMapping,
                                            targetLocation: String,
                                            patchLocation: String): Unit = {
    val targetPropertyValue: Seq[AmfElement] = targetNode.valueForField(patchField) match {
      case Some(v) if v.value.isInstanceOf[AmfArray] => v.value.asInstanceOf[AmfArray].values
      case Some(v)                                   => Seq(v.value)
      case _                                         => Nil
    }
    val targetPropertyValueIds = targetPropertyValue
      .collect { case elm: DialectDomainElement => elm }
      .foldLeft(Map[String, DialectDomainElement]()) {
        (acc: Map[String, DialectDomainElement], elm: DialectDomainElement) =>
          acc + (neutralId(elm.id, targetLocation) -> elm)
      }

    val patchPropertyValue: Seq[AmfElement] = patchValue.value match {
      case arr: AmfArray => arr.values
      case elm           => Seq(elm)
    }
    val patchPropertyValueIds = patchPropertyValue
      .collect { case elm: DialectDomainElement => elm }
      .foldLeft(Map[String, DialectDomainElement]()) {
        (acc: Map[String, DialectDomainElement], elm: DialectDomainElement) =>
          acc + (neutralId(elm.id, patchLocation) -> elm)
      }

    findPropertyMappingMergePolicy(propertyMapping) match {
      case "insert" =>
        val newDialectDomainElements = patchPropertyValueIds.collect {
          case (id, elem) =>
            targetPropertyValueIds.get(neutralId(id, patchLocation)) match {
              case Some(_) => None
              case None    => Some(elem)
            }
        } collect { case Some(elem) => elem } toSeq
        val unionElements = targetPropertyValue.collect { case d: DialectDomainElement => d } union newDialectDomainElements
        targetNode.patchObjectField(patchField, unionElements)
      case "delete" =>
        val newDialectDomainElements = patchPropertyValueIds.collect {
          case (id, _) =>
            targetPropertyValueIds.get(neutralId(id, patchLocation))
        } collect { case Some(elem) => elem } toSeq
        val unionElements = targetPropertyValue.collect { case d: DialectDomainElement => d } diff newDialectDomainElements
        targetNode.patchObjectField(patchField, unionElements)
      case "update" =>
        val computedDomainElements: Seq[(DialectDomainElement, Option[DialectDomainElement])] =
          patchPropertyValueIds.collect {
            case (id, elem) =>
              targetPropertyValueIds.get(neutralId(id, patchLocation)) match {
                case Some(targetElem) => Some((targetElem, elem))
                case None             => None
              }
          } collect { case Some(pair) => pair } map {
            case (targetElem, patchElem) =>
              (targetElem, patchNode(Some(targetElem), targetLocation, patchElem, patchLocation))
          } toSeq

        val newDomainElements = computedDomainElements.foldLeft(targetPropertyValueIds) {
          case (acc, (targetElem, maybePatchedElem)) =>
            maybePatchedElem match {
              case Some(mergedElem) => acc.updated(neutralId(targetElem.id, targetLocation), mergedElem)
              case None             => acc - neutralId(targetElem.id, targetLocation)
            }
        }
        targetNode.patchObjectField(patchField, newDomainElements.values.toSeq)
      case "upsert" =>
        val existingElems = patchPropertyValueIds.toSeq.map {
          case (id, elem) =>
            targetPropertyValueIds.get(neutralId(id, patchLocation)) match {
              case Some(targetElem) => (Some(targetElem), elem)
              case None             => (None, elem)
            }
        }

        val computedDomainElements = existingElems.map {
          case (maybeTargetElem, patchElem) =>
            maybeTargetElem match {
              case Some(targetElem) =>
                (Some(targetElem), patchNode(Some(targetElem), targetLocation, patchElem, patchLocation))
              case None => (None, Some(patchElem))
            }

        }

        val newDomainElements = computedDomainElements.foldLeft(targetPropertyValueIds) {
          case (acc, (maybeTargetElem, maybeMergedElem)) =>
            maybeTargetElem match {
              // these elements were patch elements matching target elements
              // they might have produced a merged or none element
              case Some(targetElem) =>
                maybeMergedElem match {
                  case Some(mergedElement) =>
                    acc.updated(neutralId(targetElem.id, targetLocation), mergedElement)
                  case None =>
                    acc - neutralId(targetElem.id, targetLocation)
                }
              // These are new elements introduced by the patch array not in the original target array, we always add them
              case None =>
                maybeMergedElem match {
                  case Some(mergedElement) =>
                    acc.updated(neutralId(mergedElement.id, patchLocation), mergedElement)
                  case None =>
                    acc // this should never happen
                }
            }
        }
        targetNode.patchObjectField(patchField, newDomainElements.values.toSeq)
      case "ignore" =>
      // ignore
      case "fail" =>
        errorHandler.violation(
          InvalidDialectPatch,
          targetNode.id,
          None,
          s"Property ${patchField.value.iri()} cannot be patched",
          patchValue.annotations.find(classOf[LexicalInformation]),
          None
        )
      case _ =>
      // ignore
    }
  }

  private def patchObjectProperty(targetNode: DialectDomainElement,
                                  patchField: Field,
                                  patchValue: Value,
                                  propertyMapping: PropertyMapping,
                                  targetLocation: String,
                                  patchLocation: String): Unit = {
    patchValue.value match {
      case patchDialectDomainElement: DialectDomainElement =>
        val targetNodeValue = targetNode.valueForField(patchField) match {
          case Some(v) if v.value.isInstanceOf[DialectDomainElement] =>
            Some(v.value.asInstanceOf[DialectDomainElement])
          case _ =>
            None
        }
        patchNode(targetNodeValue, targetLocation, patchDialectDomainElement, patchLocation) match {
          case Some(mergedNode: DialectDomainElement) => targetNode.patchObjectField(patchField, mergedNode)
          case _                                      => targetNode.removeField(patchField)
        }
      case _ => // ignore
    }
  }

  // recursive merge if both present
  private def patchNodeUpdate(targetNode: Option[DialectDomainElement],
                              targetLocation: String,
                              patchNode: DialectDomainElement,
                              patchLocation: String): Option[DialectDomainElement] = {
    val nodeMapping = patchNode.definedBy
    if (targetNode.isDefined && sameNodeIdentity(targetNode.get, targetLocation, patchNode, patchLocation)) {
      patchNode.meta.fields.foreach { patchField =>
        patchNode.valueForField(patchField) match {
          case Some(fieldValue) =>
            nodeMapping
              .propertiesMapping()
              .find(_.nodePropertyMapping().option().getOrElse("") == patchField.value.iri()) match {
              case Some(propertyMapping) =>
                patchProperty(targetNode.get, patchField, fieldValue, propertyMapping, targetLocation, patchLocation)
              case _ => // ignore
            }
          case _ => // ignore
        }
      }
    }
    targetNode
  }

  // recursive merge if both present
  private def patchNodeUpsert(targetNode: Option[DialectDomainElement],
                              targetLocation: String,
                              patchNode: DialectDomainElement,
                              patchLocation: String): Option[DialectDomainElement] = {
    if (targetNode.isEmpty)
      patchNodeInsert(targetNode, targetLocation, patchNode, patchLocation)
    else
      patchNodeUpdate(targetNode, targetLocation, patchNode, patchLocation)
  }

  private def sameNodeIdentity(target: DialectDomainElement,
                               targetLocation: String,
                               patchNode: DialectDomainElement,
                               patchLocation: String): Boolean = {
    neutralId(target.id, targetLocation) == neutralId(patchNode.id, patchLocation)
  }

}
