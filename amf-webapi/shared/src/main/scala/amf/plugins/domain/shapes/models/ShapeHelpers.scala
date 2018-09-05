package amf.plugins.domain.shapes.models

import amf.core.annotations.ExplicitField
import amf.core.model.domain.extensions.PropertyShape
import amf.core.model.domain.{IdsTraversionCheck, Linkable, RecursiveShape, Shape}
import amf.core.parser.ErrorHandler
import amf.plugins.domain.shapes.annotations.ParsedFromTypeExpression
import amf.plugins.features.validation.ParserSideValidations

trait ShapeHelpers { this: Shape =>

  def fromTypeExpression: Boolean = this.annotations.contains(classOf[ParsedFromTypeExpression])

  def fromExternalSource: Boolean = this match {
    case any: AnyShape => any.referenceId.option().isDefined
    case _             => false
  }

  def typeExpression: String = this.annotations.find(classOf[ParsedFromTypeExpression]) match {
    case Some(expr: ParsedFromTypeExpression) => expr.value
    case _                                    => throw new Exception("Trying to extract non existent type expression")
  }

  def externalSourceID: Option[String] = this match {
    case any: AnyShape => any.referenceId.option()
    case _             => None
  }

  def cloneShape(recursionErrorHandler: Option[ErrorHandler],
                 withRecursionBase: Option[String] = None,
                 traversed: IdsTraversionCheck = IdsTraversionCheck()): this.type = {
    if (traversed.hasId(this.id)) {
      buildFixPoint(withRecursionBase, this.name.value(), this, recursionErrorHandler).asInstanceOf[this.type]
    } else {
      val cloned: Shape = this match {
        case _: Linkable if this.isLink =>
          buildFixPoint(withRecursionBase, this.name.value(), this, recursionErrorHandler)
        case _: RecursiveShape                             => RecursiveShape(annotations)
        case _: UnionShape                                 => UnionShape(annotations)
        case _: ScalarShape                                => ScalarShape(annotations)
        case _: ArrayShape                                 => ArrayShape(annotations)
        case _: MatrixShape                                => MatrixShape(annotations)
        case _: TupleShape                                 => TupleShape(annotations)
        case _: PropertyShape                              => PropertyShape(annotations)
        case _: FileShape                                  => FileShape(annotations)
        case _: NilShape                                   => NilShape(annotations)
        case _: NodeShape                                  => NodeShape(annotations)
        case _: SchemaShape                                => SchemaShape(annotations)
        case UnresolvedShape(_, annots, reference, parser) => UnresolvedShape(reference, annots, parser)
        case _: AnyShape                                   => AnyShape(annotations)
      }
      cloned.id = this.id
      copyFields(recursionErrorHandler, cloned, withRecursionBase, traversed + this.id)
      if (cloned.isInstanceOf[NodeShape]) {
        cloned.add(ExplicitField())
      }
      cloned.asInstanceOf[this.type]
    }
  }

  protected def buildFixPoint(id: Option[String],
                              name: String,
                              link: Linkable,
                              recursionErrorHandler: Option[ErrorHandler]): RecursiveShape = {
    if (recursionErrorHandler.isDefined && !link.supportsRecursion.option().getOrElse(false)) {
      recursionErrorHandler.get.violation(
        ParserSideValidations.RecursiveShapeSpecification.id,
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
