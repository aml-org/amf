package amf.plugins.document.webapi.contexts

import amf.core.parser.ParserContext
import amf.core.remote.Vendor
import amf.plugins.document.webapi.parser.spec.SpecSyntax
import amf.plugins.domain.shapes.models.Shape
import amf.core.validation.ParserSideValidations.ClosedShapeSpecification
import org.yaml.model.{YMap, YMapEntry, YNode}

class WebApiContext(override val vendor: Vendor, private val wrapped: ParserContext, override val spec: SpecAwareContext, override val syntax: SpecSyntax) extends ParserContext(wrapped.rootContextDocument, wrapped.refs, Some(wrapped.declarations)) with SpecAwareContext {

  override def ignore(shape: String, property: String) = spec.ignore(shape, property)

  def link(value: YNode): Either[String, YNode] = spec.link(value)

  /** Validate closed shape. */
  def closedShape(node: String, ast: YMap, shape: String, annotation: Boolean = false): Unit = {
    syntax.nodes.get(shape) match {
      case Some(props) =>
        val properties = if (annotation) {
          props ++ syntax.nodes("annotation")
        } else {
          props
        }

        ast.entries.foreach { entry =>
          val key: String = entry.key
          if (spec.ignore(shape, key)) {
            // annotation or path in endpoint/webapi => ignore
          } else if (!properties(key)) {
            violation(ClosedShapeSpecification.id(),
              node,
              s"Property $key not supported in a $vendor $shape node",
              entry)
          }
        }
      case None => throw new Exception(s"Cannot validate unknown node type $shape for $vendor")
    }
  }

  /**
    * raml types nodes are different from other shapes because they can have 'custom facets' essentially, client
    * defined constraints expressed as additional properties syntactically in the type definition.
    * The problem is that they cannot be recognised just looking into the AST as we do with annotations, so we
    * need to first, compute them, and then, add them as additional valid properties to the set of properties that
    * can be defined in the AST node
    */
  def closedRamlTypeShape(shape: Shape, ast: YMap, shapeType: String, annotation: Boolean = false): Unit = {
    val node = shape.id
    val facets = shape.collectCustomShapePropertyDefinitions(onlyInherited =  true)

    syntax.nodes.get(shapeType) match {
      case Some(props) =>
        val initialProperties = if (annotation) {
          props ++ syntax.nodes("annotation")
        } else {
          props
        }
        val allResults: Seq[Seq[YMapEntry]] = facets.map { propertiesMap =>
          val totalProperties = initialProperties ++ propertiesMap.keys.toSet
          val acc: Seq[YMapEntry] = Seq.empty
          ast.entries.foldLeft(acc) { (results: Seq[YMapEntry], entry) =>
            val key: String = entry.key
            if (spec.ignore(shapeType, key)) {
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
            violation(ClosedShapeSpecification.id(),
              node,
              s"Properties ${errors.map(_.key).mkString(",")} not supported in a $vendor $shapeType node",
              errors.head) // pointing only to the first failed error
        }


      case None => throw new Exception(s"Cannot validate unknown node type $shapeType for $vendor")
    }
  }

}
