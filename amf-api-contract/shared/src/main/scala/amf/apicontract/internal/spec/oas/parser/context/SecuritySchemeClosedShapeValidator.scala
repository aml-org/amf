package amf.apicontract.internal.spec.oas.parser.context

import amf.apicontract.internal.spec.common.parser.{CustomSyntax, SpecNode}
import amf.core.client.common.validation.SeverityLevels
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.AmfObject
import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.common.parser.{ClosedShapeValidator, IgnoreCriteria}
import org.yaml.model.{YMap, YPart}

case class SecuritySchemeClosedShapeValidator(
    syntax: CustomSyntax,
    spec: Spec,
    ignore: IgnoreCriteria,
    next: ClosedShapeValidator
) extends ClosedShapeValidator {
  override def evaluate(node: AmfObject, ast: YMap, shape: String)(implicit eh: AMFErrorHandler): Unit = {
    if (syntax.contains(shape)) {
      val keys = ast.entries.map(getEntryKey)
      validateCustomSyntax(node, ast, shape, keys)
    } else next.evaluate(node, ast, shape)
  }

  private def validateCustomSyntax(node: AmfObject, ast: YMap, shape: String, keys: Seq[String])(implicit
      eh: AMFErrorHandler
  ): Unit = {
    if (syntax.contains(shape)) {
      val SpecNode(requiredFields, possible) = syntax(shape)

      // if entries don't contain required fields
      requiredFields.foreach { field =>
        if (!keys.contains(field.name)) {
          val isWarning = field.severity == SeverityLevels.WARNING
          if (isWarning)
            throwClosedShapeWarning(node, s"Property '${field.name}' is required in a $spec $shape node", ast)
          else throwClosedShapeError(node, s"Property '${field.name}' is required in a $spec $shape node", ast)
        }
      }

      // if invalid fields are present
      val required = requiredFields.map(_.name)
      keys.foreach(key => {
        if (!possible.contains(key) && !required.contains(key) && !ignore.shouldIgnore(shape, key)) {
          throwClosedShapeWarning(
            node,
            s"Property '$key' not supported in a $spec $shape node",
            getAstEntry(ast, key)
          )
        }
      })
    }
  }

  private def getAstEntry(ast: YMap, entry: String): YPart =
    ast.entries.find(yMapEntry => yMapEntry.key.asScalar.map(_.text).get == entry).get
}
