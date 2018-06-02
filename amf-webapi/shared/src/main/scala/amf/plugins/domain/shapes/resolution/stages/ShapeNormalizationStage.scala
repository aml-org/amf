package amf.plugins.domain.shapes.resolution.stages

import amf.core.metamodel.{MetaModelTypeMapping, Obj}
import amf.core.model.document.BaseUnit
import amf.core.model.domain._
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.resolution.stages.shape_normalization._

/**
  * Computes the canonical form for all the shapes in the model
  * We are assuming certain pre-conditions in the state of the shape:
  *  - All type references have been replaced by their expanded forms
  * @param profile
  */
class ShapeNormalizationStage(profile: String, val keepEditingInfo: Boolean, val errorHandler: ErrorHandler)
    extends ResolutionStage(profile)
    with MetaModelTypeMapping {

  protected val context = new NormalizationContext(errorHandler, keepEditingInfo, profile)

  override def resolve(model: BaseUnit): BaseUnit = model.transform(findShapesPredicate, transform)

  def findShapesPredicate(element: DomainElement): Boolean = {
    val metaModelFound: Obj = metaModel(element)
    val targetIri           = (Namespace.Shapes + "Shape").iri()
    metaModelFound.`type`.exists { t: ValueType =>
      t.iri() == targetIri
    }
  }

  protected def transform(element: DomainElement, isCycle: Boolean): Option[DomainElement] = {
    element match {
      case shape: Shape => Some(ShapeCanonizer(ShapeExpander(shape, context), context))
      case other        => Some(other)
    }
  }

}
