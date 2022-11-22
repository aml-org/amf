package amf.shapes.internal.domain.resolution.shape_normalization2

import amf.core.client.scala.model.domain._
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.annotations._
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.validation.CoreValidations.{RecursiveShapeSpecification, TransformationValidation}
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.domain.metamodel._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

private[resolution] object ReferencesFixer {
  def apply(s: Shape, context: NormalizationContext2): Shape = ReferencesFixer()(context).normalize(s)
}

sealed case class ReferencesFixer()(implicit val context: NormalizationContext2) {
  val recursionAnalyzer = new RecursionAnalyzer(context.errorHandler)

  protected def normalize(shape: Shape): Shape = {
    val lastVersion = retrieveLastVersionOf(shape)
    if (recursionAnalyzer.recursionDetected(lastVersion)) handleRecursion(lastVersion) else fixReferences(lastVersion)
  }

  private def fixReferences(shape: Shape): Shape =
    recursionAnalyzer.executeWhileDetectingRecursion(shape, fixReferencesIn)

  private def handleRecursion(shape: Shape): RecursiveShape = {
    recursionAnalyzer.detectRecursionsFromUnions(shape)
    if (recursionAnalyzer.isInvalidRecursion(shape)) reportInvalidRecursionError(shape)
    RecursiveShape(shape)
  }

  private def retrieveLastVersionOf(shape: Shape) = {
    context.resolvedInheritanceCache.get(shape.id) match {
      case Some(resolvedInheritance) => resolvedInheritance
      case _                         => shape
    }
  }

  private def fixReferencesIn(shape: Shape) = {
    UnnecessaryAnnotationsRemover(shape) // Shouldn't be here
    ExplicitFieldAnnotationSetter(shape) // Shouldn't be here

    shape match {
      case union: UnionShape         => fixReferencesInUnion(union)
      case scalar: ScalarShape       => fixReferencesInScalar(scalar)
      case array: ArrayShape         => fixReferencesInArray(array)
      case matrix: MatrixShape       => fixReferencesInMatrix(matrix)
      case tuple: TupleShape         => fixReferencesInTuple(tuple)
      case property: PropertyShape   => fixReferencesInProperty(property)
      case fileShape: FileShape      => fixReferencesInShape(fileShape)
      case nil: NilShape             => fixReferencesInShape(nil)
      case node: NodeShape           => fixReferencesInNode(node)
      case any: AnyShape             => fixReferencesInAny(any)
      case recursive: RecursiveShape => recursive
    }
  }

  private def fixReferencesInShape(any: Shape) = {
    fixReferencesInLogicalConstraints(any)
    any
  }

  private def fixReferencesInAny(any: AnyShape) = {
    val adjusted = AnyShapeAdjuster2(any) // Analyze if it's possible to remove this case
    fixReferencesInLogicalConstraints(adjusted)
    adjusted
  }

  protected def fixReferencesInNode(node: NodeShape): Shape = {
    fixReferencesInLogicalConstraints(node)
    fixReferencesInProperties(node)
    fixReferencesInAdditionalPropertiesSchema(node)
    node
  }

  protected def fixReferencesInProperty(property: PropertyShape): Shape = {
    val fixedRange = normalize(property.range)
    val fixedProperty = fixedRange match {
      case _: RecursiveShape =>
        // If id is not changed, JSONLD will only render one version of this property (may exists one with Recursive shape as range and the original)
        // Cannot clone because property is recursive
        property.copyShape().withId(property.id + "/recursiveProp")
      case _ => property
    }

    fixedProperty.fields.setWithoutId(
      PropertyShapeModel.Range,
      fixedRange,
      property.fields.getValue(PropertyShapeModel.Range).annotations
    )

    fixedProperty
  }

  protected def fixReferencesInScalar(scalar: ScalarShape): Shape = {
    fixReferencesInLogicalConstraints(scalar)
    scalar
  }

  protected def fixReferencesInArray(array: ArrayShape): Shape = {
    fixReferencesInLogicalConstraints(array)
    array.items match {
      case items: Shape =>
        val newItems = normalizeAllowingRecursionIn(items, canBeEmpty(array))
        array.fields.setWithoutId(ArrayShapeModel.Items, newItems)
        array
      case _ => array
    }
  }

  protected def fixReferencesInMatrix(matrix: MatrixShape): Shape = {
    fixReferencesInLogicalConstraints(matrix)
    matrix.items match {
      case items: Shape =>
        val newItems = normalize(items)
        matrix.fields.setWithoutId(ArrayShapeModel.Items, newItems)
        matrix
      case _ => matrix
    }
  }

  protected def fixReferencesInTuple(tuple: TupleShape): Shape = {
    fixReferencesInLogicalConstraints(tuple)
    val newItems = tuple.items.map(shape => normalize(shape))
    tuple.fields.setWithoutId(
      TupleShapeModel.TupleItems,
      AmfArray(newItems),
      Option(tuple.fields.getValue(TupleShapeModel.TupleItems)).map(_.annotations).getOrElse(Annotations())
    )
    tuple
  }

  protected def fixReferencesInUnion(union: UnionShape): Shape = {
    val fixedAnyOf = fixReferencesInUnionMembers(union)
    val anyOfAnnotations =
      union.fields.getValueAsOption(UnionShapeModel.AnyOf).map(_.annotations).getOrElse(Annotations())
    union.fields.setWithoutId(UnionShapeModel.AnyOf, AmfArray(fixedAnyOf), anyOfAnnotations)
    union

  }

  protected def fixReferencesInLogicalConstraints(shape: Shape): Unit = {
    fixReferencesInLogicalConstraint(shape, ShapeModel.And)
    fixReferencesInLogicalConstraint(shape, ShapeModel.Or)
    fixReferencesInLogicalConstraint(shape, ShapeModel.Xone)
    fixReferencesInLogicalConstraint(shape, ShapeModel.Not)
  }

  protected def fixReferencesInLogicalConstraint(shape: Shape, constraintField: Field): Unit = {
    shape.fields.getValueAsOption(constraintField) match {
      case Some(constraint) =>
        constraint.value match {
          case array: AmfArray =>
            val fixedLogicalConstraint = array.values.map(c => normalize(c.asInstanceOf[Shape]))
            shape.setArrayWithoutId(constraintField, fixedLogicalConstraint, constraint.annotations)
          case s: Shape =>
            shape.setWithoutId(constraintField, normalize(s), constraint.annotations)
          case _ =>
        }
      case _ =>
    }
  }

  private def fixReferencesInProperties(node: NodeShape): Unit = {
    val fixedProperties = node.properties.map { prop =>
      normalizeAllowingRecursionIn(prop, isOptionalProperty(prop))
    }
    node.setArrayWithoutId(NodeShapeModel.Properties, fixedProperties)
  }

  private def fixReferencesInAdditionalPropertiesSchema(node: NodeShape): Unit = {
    Option(node.additionalPropertiesSchema).foreach { schema =>
      val fixed = normalizeAllowingRecursionIn(schema)
      node.setWithoutId(NodeShapeModel.AdditionalPropertiesSchema, fixed)
    }
  }

  private def fixReferencesInUnionMembers(union: UnionShape) = {
    recursionAnalyzer.executeAndAnalyzeRecursionInUnion(
      union, { union =>
        val flattenedAnyOf: ListBuffer[Shape] = ListBuffer()

        union.anyOf.foreach { unionMember: Shape =>
          recursionAnalyzer.traversedUnionMembers.append(unionMember.id)
          val fixedUnionMember = normalize(unionMember)
          recursionAnalyzer.traversedUnionMembers.remove(recursionAnalyzer.traversedUnionMembers.size - 1)
          fixedUnionMember match {
            case nestedUnion: UnionShape => nestedUnion.anyOf.foreach(member => flattenedAnyOf += member)
            case other: Shape            => flattenedAnyOf += other
          }
        }
        flattenedAnyOf
      }
    )
  }

  private def normalizeAllowingRecursionIn(shape: Shape, condition: Boolean = true) =
    recursionAnalyzer.executeAllowingRecursionIn(shape, normalize, condition)

  private def isOptionalProperty(prop: PropertyShape) = prop.minCount.option().forall(_ == 0)

  private def canBeEmpty(array: ArrayShape) = array.minItems.option().forall(_ == 0)

  private def invalidNormalizedProperty(other: Shape): Unit = {
    context.errorHandler.violation(
      TransformationValidation,
      other.id,
      None,
      s"Resolution error: Expecting property shape, found $other",
      other.position(),
      other.location()
    )
  }

  private def reportInvalidRecursionError(lastVersion: Shape): Unit = {
    context.errorHandler.violation(
      RecursiveShapeSpecification,
      lastVersion.id,
      None,
      "Error recursive shape",
      lastVersion.position(),
      lastVersion.location()
    )
  }
}
