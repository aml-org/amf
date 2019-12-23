package amf.plugins.domain.shapes.resolution.stages

import amf.ProfileName
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.MetaModelTypeMapping
import amf.core.model.document.BaseUnit
import amf.core.model.domain._
import amf.core.resolution.stages.ResolutionStage
import amf.core.resolution.stages.elements.resolution.{ElementResolutionStage, ElementStageTransformer}
import amf.core.resolution.stages.selectors.ShapeSelector
import amf.core.traversal.ModelTraversalRegistry
import amf.plugins.domain.shapes.resolution.stages.shape_normalization._
import amf.plugins.features.validation.CoreValidations.RecursiveShapeSpecification

import scala.collection.mutable.ListBuffer

/**
  * Computes the canonical form for all the shapes in the model
  * We are assuming certain pre-conditions in the state of the shape:
  *  - All type references have been replaced by their expanded forms
  */
class ShapeNormalizationStage(profile: ProfileName, val keepEditingInfo: Boolean)(
    override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage()
    with MetaModelTypeMapping
    with ElementResolutionStage[Shape] {

  protected var m: Option[BaseUnit] = None
  protected val context             = new NormalizationContext(errorHandler, keepEditingInfo, profile)

  override def resolve[T <: BaseUnit](model: T): T = {
    m = Some(model)
    model.transform(ShapeSelector, transform).asInstanceOf[T]
  }

  protected def transform(element: DomainElement, isCycle: Boolean): Option[DomainElement] = {
    element match {
      case shape: Shape => transformer.transform(shape)
      case other        => Some(other)
    }
  }

  override def transformer: ElementStageTransformer[Shape] = new ShapeTransformer(context)
}

private[stages] case class RecursionErrorRegister() {
  private val avoidRegister = ListBuffer[String]()

  private def buildRecursion(base: Option[String], s: Shape): RecursiveShape = {
    val fixPointId = base.getOrElse(s.id)
    val r          = RecursiveShape(s).withFixPoint(fixPointId)
    r
  }

  def recursionAndError(root: Shape, base: Option[String], s: Shape, traversal: ModelTraversalRegistry)(
      implicit context: NormalizationContext): RecursiveShape = {
    val recursion = buildRecursion(base, s)
    recursionError(root, recursion, traversal: ModelTraversalRegistry, Some(root.id))
  }

  def recursionError(original: Shape,
                     r: RecursiveShape,
                     traversal: ModelTraversalRegistry,
                     checkId: Option[String] = None)(implicit context: NormalizationContext): RecursiveShape = {

    val canRegister = !avoidRegister.contains(r.id)
    if (!r.supportsRecursion
          .option()
          .getOrElse(false) && !traversal.avoidError(r, checkId) && canRegister) {
      context.errorHandler.violation(
        RecursiveShapeSpecification,
        original.id,
        None,
        "Error recursive shape",
        original.position(),
        original.location()
      )
      avoidRegister += r.id
    } else if (traversal.avoidError(r, checkId)) {
      r.withSupportsRecursion(true)
    }
    r
  }
}
