package amf.shapes.internal.spec.raml.parser

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.plugins.syntax.SYamlAMFParserErrorHandler
import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.common.parser.{IgnoreCriteria, ShapeParserContext, SpecSyntax}
import amf.shapes.internal.spec.RamlShapeTypeBeautifier
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.ClosedShapeSpecification
import org.yaml.model.{IllegalTypeHandler, YMap, YMapEntry, YScalar}

/** raml types nodes are different from other shapes because they can have 'custom facets' essentially, client defined
  * constraints expressed as additional properties syntactically in the type definition. The problem is that they cannot
  * be recognised just looking into the AST as we do with annotations, so we need to first, compute them, and then, add
  * them as additional valid properties to the set of properties that can be defined in the AST node
  */
object ClosedRamlTypeShape {

  def eval(shape: Shape, ast: YMap, shapeType: String, typeInfo: TypeInfo)(implicit ctx: ShapeParserContext): Unit = {
    eval(shape, ast, shapeType, typeInfo, ctx.syntax, ctx.ignoreCriteria, ctx.spec)
  }

  def eval(
      shape: Shape,
      ast: YMap,
      shapeType: String,
      typeInfo: TypeInfo,
      syntax: SpecSyntax,
      ignoreCriteria: IgnoreCriteria,
      spec: Spec
  )(implicit ctx: ShapeParserContext): Unit = {
    val eh                                        = ctx.eh
    implicit val errorHandler: IllegalTypeHandler = new SYamlAMFParserErrorHandler(eh)

    val facets     = shape.collectCustomShapePropertyDefinitions(onlyInherited = true)
    val shapeLabel = RamlShapeTypeBeautifier.beautify(shapeType)

    syntax.nodes.get(shapeType) match {
      case Some(props) =>
        var initialProperties = props
        if (typeInfo.isAnnotation) initialProperties ++= syntax.nodes("annotation")
        if (typeInfo.isPropertyOrParameter) initialProperties ++= syntax.nodes("property")
        val allResults: Seq[Seq[YMapEntry]] = facets.map { propertiesMap =>
          val totalProperties     = initialProperties ++ propertiesMap.keys.toSet
          val acc: Seq[YMapEntry] = Seq.empty
          ast.entries.foldLeft(acc) { (results: Seq[YMapEntry], entry) =>
            val key: String = entry.key.as[YScalar].text
            if (ignoreCriteria.shouldIgnore(shapeType, key)) {
              results
            } else if (!totalProperties(key)) {
              results ++ Seq(entry)
            } else {
              results
            }
          }
        }
        allResults.find(_.nonEmpty) match {
          case None => // at least we found a solution, this is a valid shape
          case Some(errors: Seq[YMapEntry]) =>
            val subject = if (errors.size > 1) "Properties" else "Property"
            eh.violation(
              ClosedShapeSpecification,
              shape,
              s"$subject ${errors.map(_.key.as[YScalar].text).map(e => s"'$e'").mkString(",")} not supported in a $spec $shapeLabel node",
              errors.head.location
            ) // pointing only to the first failed error
        }

      case None =>
        eh.violation(
          ClosedShapeSpecification,
          shape.id,
          s"Cannot validate unknown node type $shapeType for $spec",
          shape.annotations
        )
    }
  }
}
