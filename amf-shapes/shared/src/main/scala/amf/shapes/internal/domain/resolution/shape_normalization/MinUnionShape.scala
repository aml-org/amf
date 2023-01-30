package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, Shape}
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.internal.metamodel.{Field, Obj}
import amf.core.internal.utils.IdCounter
import amf.shapes.client.scala.model.domain.{NodeShape, UnionShape}
import amf.shapes.internal.domain.metamodel.{AnyShapeModel, NodeShapeModel, UnionShapeModel}
import amf.shapes.internal.domain.resolution.shape_normalization.MinShapeAlgorithm.allShapeFields
import org.mulesoft.common.collections.Group

object MinUnionShape {

  type RestrictFields = (List[Field], Shape, Shape, Seq[Field]) => Shape

  object UnionErrorHandler extends AMFErrorHandler {

    override def report(result: AMFValidationResult): Unit =
      throw new Exception("raising exceptions in union processing")

    def wrapContext(ctx: NormalizationContext): NormalizationContext = {
      new NormalizationContext(
        this,
        ctx.keepEditingInfo,
        ctx.profile,
        ctx.cache
      )
    }
  }

  def computeMinUnion(baseUnion: UnionShape, superUnion: UnionShape, computeNarrowRestrictions: RestrictFields)(implicit
      context: NormalizationContext
  ): Shape = {

    val unionContext: NormalizationContext = UnionErrorHandler.wrapContext(context)
    val newUnionItems =
      if (baseUnion.anyOf.isEmpty || superUnion.anyOf.isEmpty) {
        baseUnion.anyOf ++ superUnion.anyOf
      } else {
        val minShapes = for {
          baseUnionElement  <- baseUnion.anyOf
          superUnionElement <- superUnion.anyOf
        } yield {
          try {
            Some(unionContext.minShape(baseUnionElement, superUnionElement))
          } catch {
            case _: Exception => None
          }
        }
        val finalMinShapes = minShapes.collect { case Some(s) => s }
        if (finalMinShapes.isEmpty)
          throw new InheritanceIncompatibleShapeError(
            "Cannot compute inheritance for union",
            None,
            baseUnion.location(),
            baseUnion.position()
          )
        finalMinShapes
      }

    avoidDuplicatedIds(newUnionItems)
    baseUnion.fields.setWithoutId(
      UnionShapeModel.AnyOf,
      AmfArray(newUnionItems),
      baseUnion.fields.getValue(UnionShapeModel.AnyOf).annotations
    )

    computeNarrowRestrictions(
      UnionShapeModel.fields,
      baseUnion,
      superUnion,
      Seq(UnionShapeModel.AnyOf)
    )

    baseUnion
  }

  def computeMinUnionNode(baseUnion: UnionShape, superNode: NodeShape, computeNarrowRestrictions: RestrictFields)(
      implicit context: NormalizationContext
  ): Shape = {
    val unionContext: NormalizationContext = UnionErrorHandler.wrapContext(context)
    val newUnionItems = for {
      baseUnionElement <- baseUnion.anyOf
    } yield {
      unionContext.minShape(baseUnionElement, superNode)
    }

    baseUnion.fields.setWithoutId(
      UnionShapeModel.AnyOf,
      AmfArray(newUnionItems),
      baseUnion.fields.getValue(UnionShapeModel.AnyOf).annotations
    )

    computeNarrowRestrictions(UnionShapeModel.fields, baseUnion, superNode, Seq(UnionShapeModel.AnyOf))

    baseUnion
  }

  def computeMinSuperUnion(baseShape: Shape, superUnion: UnionShape, computeNarrowRestrictions: RestrictFields)(implicit
      context: NormalizationContext
  ): Shape = {
    val unionContext: NormalizationContext = UnionErrorHandler.wrapContext(context)
    val minItems = for {
      superUnionElement <- superUnion.anyOf
    } yield {
      try {
        val newShape = unionContext.minShape(filterBaseShape(baseShape), superUnionElement)
        setValuesOfUnionElement(newShape, superUnionElement)
        Some(newShape)
      } catch {
        case _: Exception => None
      }
    }
    val newUnionItems = minItems collect { case Some(s) => s }
    if (newUnionItems.isEmpty) {
      throw new InheritanceIncompatibleShapeError(
        "Cannot compute inheritance from union",
        None,
        baseShape.location(),
        baseShape.position()
      )
    }

    newUnionItems.zipWithIndex.foreach { case (shape, i) =>
      shape.id = shape.id + s"_$i"
      shape
    }

    superUnion.fields.setWithoutId(
      UnionShapeModel.AnyOf,
      AmfArray(newUnionItems),
      superUnion.fields.getValue(UnionShapeModel.AnyOf).annotations
    )

    computeNarrowRestrictions(allShapeFields, baseShape, superUnion, Seq(UnionShapeModel.AnyOf))
    baseShape.fields foreach { case (field, value) =>
      if (field != UnionShapeModel.AnyOf) {
        superUnion.fields.setWithoutId(field, value.value, value.annotations)
      }
    }

    superUnion.annotations.reject(_ => true) ++= baseShape.annotations
    superUnion.withId(baseShape.id)
  }

  private def filterBaseShape(baseShape: Shape): Shape = {
    // There are some fields of the union that we don't want to propagate to it's members
    val filteredFields =
      Seq(
        AnyShapeModel.Values,
        AnyShapeModel.DefaultValueString,
        AnyShapeModel.Default,
        AnyShapeModel.Examples,
        AnyShapeModel.Description
      )
    val filteredBase = baseShape.copyShape()
    filteredBase.fields.filter(f => !filteredFields.contains(f._1))
    filteredBase
  }

  private def setValuesOfUnionElement(newShape: Shape, superUnionElement: Shape): Unit = {
    superUnionElement.name.option().foreach(n => newShape.withName(n))
    /*
    overrides additionalProperties value of unionElement to newShape to generate consistency with restrictShape method
    that is called when a union type is parsed as AnyShape.
     */
    (newShape, superUnionElement) match {
      case (newShape: NodeShape, superUnion: NodeShape) =>
        newShape.fields
          .getValueAsOption(NodeShapeModel.Closed)
          .map(closedValue =>
            newShape.set(
              NodeShapeModel.Closed,
              AmfScalar(superUnion.closed.value(), closedValue.value.annotations),
              closedValue.annotations
            )
          )
      case _ =>
    }
  }

  private def avoidDuplicatedIds(newUnionItems: Seq[Shape]): Unit =
    newUnionItems.legacyGroupBy(_.id).foreach {
      case (_, shapes) if shapes.size > 1 =>
        val counter = new IdCounter()
        shapes.foreach { shape =>
          shape.id = counter.genId(shape.id)
        }
      case _ =>
    }
}
