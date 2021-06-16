package amf.shapes.client.scala.model.domain

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.{Linkable, RecursiveShape, Shape}
import amf.core.client.scala.traversal.ModelTraversalRegistry
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.validation.CoreValidations.RecursiveShapeSpecification
import amf.shapes.internal.annotations.ParsedFromTypeExpression

trait ShapeHelpers { this: Shape =>

  def fromExternalSource: Boolean = this match {
    case any: AnyShape => any.referenceId.option().isDefined
    case _             => false
  }

  def typeExpression: Option[String] = this.annotations.find(classOf[ParsedFromTypeExpression]).map(_.value)

  def externalSourceID: Option[String] = this match {
    case any: AnyShape => any.referenceId.option()
    case _             => None
  }

  def cloneAllExamples(cloned: Shape, s: Shape): Unit = (cloned, s) match {
    case (cloned: AnyShape, s: AnyShape) =>
      cloned.withExamples(s.examples.map { e =>
        e.copyElement().asInstanceOf[Example]
      })
    case _ =>
  }

  def cloneShape(recursionErrorHandler: Option[AMFErrorHandler],
                 withRecursionBase: Option[String] = None,
                 traversal: ModelTraversalRegistry = ModelTraversalRegistry(),
                 cloneExamples: Boolean = false): this.type = {
    if (traversal.isInCurrentPath(this.id)) {
      buildFixPoint(withRecursionBase, this.name.value(), this, recursionErrorHandler).asInstanceOf[this.type]
    } else {
      val cloned: Shape = this match {
        case _: Linkable if this.isLink =>
          buildFixPoint(withRecursionBase, this.name.value(), this, recursionErrorHandler)
        case _: UnionShape                                       => UnionShape(annotations)
        case _: ScalarShape                                      => ScalarShape(annotations)
        case _: ArrayShape                                       => ArrayShape(annotations)
        case _: MatrixShape                                      => MatrixShape(annotations)
        case _: TupleShape                                       => TupleShape(annotations)
        case _: FileShape                                        => FileShape(annotations)
        case _: NilShape                                         => NilShape(annotations)
        case _: NodeShape                                        => NodeShape(annotations)
        case _: SchemaShape                                      => SchemaShape(annotations)
        case UnresolvedShape(_, annots, reference, parser, _, _) => UnresolvedShape(reference, annots, parser)
        case _: AnyShape                                         => AnyShape(annotations)
      }
      cloned.id = this.id
      copyFields(recursionErrorHandler, cloned, withRecursionBase, traversal + this.id)
      if (cloned.isInstanceOf[NodeShape]) {
        cloned.add(ExplicitField())
      }
      if (cloneExamples) cloneAllExamples(cloned, this)
      cloned.asInstanceOf[this.type]
    }
  }

  protected def buildFixPoint(id: Option[String],
                              name: String,
                              link: Linkable,
                              recursionErrorHandler: Option[AMFErrorHandler]): RecursiveShape = {
    if (recursionErrorHandler.isDefined && link.supportsRecursion.option().isEmpty) {
      recursionErrorHandler.get.violation(
        RecursiveShapeSpecification,
        link.id,
        None,
        "Error recursive shape",
        link.position(),
        link.location()
      )
    }
    val fixPointId = id.getOrElse(link.id)
    RecursiveShape(link).withFixPoint(fixPointId)
  }

}
