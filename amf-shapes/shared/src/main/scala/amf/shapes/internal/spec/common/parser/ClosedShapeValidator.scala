package amf.shapes.internal.spec.common.parser

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.AmfObject
import amf.core.internal.remote.Spec
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{ClosedShapeSpecification, ClosedShapeSpecificationWarning}
import org.yaml.model.{YMap, YMapEntry, YPart, YScalar}

trait IgnoreCriteria {
  def shouldIgnore(shape: String, property: String): Boolean
}

object IgnoreAllCriteria extends IgnoreCriteria {
  override def shouldIgnore(shape: String, property: String): Boolean = true
}

object DontIgnoreCriteria extends IgnoreCriteria {
  override def shouldIgnore(shape: String, property: String): Boolean = false
}

trait ClosedShapeValidator {
  def evaluate(node: AmfObject, ast: YMap, shape: String)(implicit eh: AMFErrorHandler): Unit

  protected def throwClosedShapeWarning(node: AmfObject, message: String, entry: YPart)(implicit
      eh: AMFErrorHandler
  ): Unit =
    eh.warning(ClosedShapeSpecificationWarning, node, message, entry.location)

  protected def throwClosedShapeError(node: AmfObject, message: String, entry: YPart)(implicit
      eh: AMFErrorHandler
  ): Unit =
    eh.violation(ClosedShapeSpecification, node, message, entry.location)

  protected def getEntryKey(entry: YMapEntry): String = {
    entry.key.asOption[YScalar].map(_.text).getOrElse(entry.key.toString)
  }
}

case class UnknownShapeValidator(spec: Spec) extends ClosedShapeValidator {
  override def evaluate(node: AmfObject, ast: YMap, shape: String)(implicit eh: AMFErrorHandler): Unit = {
    throwClosedShapeError(node, s"Cannot validate unknown node type $shape for $spec", ast)
  }
}

object DefaultClosedShapeValidator {
  def apply(ignore: IgnoreCriteria, spec: Spec, syntax: SpecSyntax): ClosedShapeValidator =
    DefaultClosedShapeValidator(ignore, spec, syntax, UnknownShapeValidator(spec))
}

case class DefaultClosedShapeValidator(
    ignore: IgnoreCriteria,
    spec: Spec,
    syntax: SpecSyntax,
    next: ClosedShapeValidator
) extends ClosedShapeValidator {

  def evaluate(node: AmfObject, ast: YMap, shape: String)(implicit eh: AMFErrorHandler): Unit = {
    syntax.nodes.get(shape) match {
      case Some(properties) =>
        ast.entries.foreach { entry =>
          val key: String = getEntryKey(entry)
          if (!ignore.shouldIgnore(shape, key) && !properties(key)) {
            throwClosedShapeError(node, s"Property '$key' not supported in a $spec $shape node", entry)
          }
        }
      case None => next.evaluate(node, ast, shape)
    }
  }
}
