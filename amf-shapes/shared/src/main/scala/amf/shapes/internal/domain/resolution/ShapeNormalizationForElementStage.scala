package amf.shapes.internal.domain.resolution

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{AmfObject, DomainElement, Shape}
import amf.core.client.scala.traversal.{
  DomainElementSelectorAdapter,
  DomainElementTransformationAdapter,
  TransformationData,
  TransformationTraversal
}
import amf.core.client.scala.traversal.iterator.DomainElementIterator
import amf.core.internal.transform.stages.elements.resolution.ElementStageTransformer
import amf.core.internal.transform.stages.selectors.ShapeSelector
import amf.shapes.internal.domain.resolution.shape_normalization.{
  NormalizationContext,
  ShapeNormalizationInheritanceResolver,
  ShapeNormalizationRecursionAnalyzer,
  ShapeNormalizationReferencesUpdater
}

class ShapeNormalizationForElementStage(context: NormalizationContext) extends ElementStageTransformer[Shape] {

  override def transform(shape: Shape, configuration: AMFGraphConfiguration): Option[Shape] = {
    // resolve inheritance
    resolveInheritanceInSubTree(shape)

    // update model
    val updater = ShapeNormalizationReferencesUpdater(context)
    val updatedShape = updater.updateShape(shape)
    updateReferencesInSubTree(updatedShape, updater)

    // place recursions
    validateAndPlaceRecursionsInSubTree(updatedShape)

    Some(updatedShape)
  }

  private def validateAndPlaceRecursionsInSubTree(shape: Shape): Unit = {
    val recursionAnalyzer = ShapeNormalizationRecursionAnalyzer(context)

    def placeRecursions(element: DomainElement): Option[DomainElement] = {
      element match {
        case shape: Shape => Some(recursionAnalyzer.analyze(shape))
        case other        => Some(other)
      }
    }

    val domainElementAdapter  = new DomainElementSelectorAdapter(ShapeSelector)
    val transformationAdapter = new DomainElementTransformationAdapter((elem, _) => placeRecursions(elem))
    new TransformationTraversal(TransformationData(domainElementAdapter, transformationAdapter)).traverse(shape)
  }

  def resolveInheritanceInSubTree(shape: Shape): Unit = {
    DomainElementIterator(List(shape)).foreach {
      case s: Shape => ShapeNormalizationInheritanceResolver(context).normalize(s)
      case _        => // nothing
    }
  }

  private def updateReferencesInSubTree(shape: Shape, updater: ShapeNormalizationReferencesUpdater): Unit = {
    val iterator = AmfUpdaterIterator(shape, e => updater.update(e))
    iterator.foreach {
      case o: AmfObject => updater.updateFields(o)
      case _            => // skip
    }
  }
}

object ShapeNormalizationForElementStage {
  def apply(
      errorHandler: AMFErrorHandler,
      keepEditingInfo: Boolean,
      profileName: ProfileName
  ): ShapeNormalizationForElementStage = {
    val context = new NormalizationContext(errorHandler, keepEditingInfo, profileName)
    new ShapeNormalizationForElementStage(context)
  }
}
