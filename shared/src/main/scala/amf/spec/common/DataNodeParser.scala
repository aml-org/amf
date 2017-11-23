package amf.spec.common

import amf.framework.model.document.Fragment.ExternalFragment
import amf.domain.extensions.{DataNode, ScalarNode, ArrayNode => DataArrayNode, ObjectNode => DataObjectNode}
import amf.framework.parser.Annotations
import amf.vocabulary.Namespace
import org.yaml.model._
import amf.parser.YScalarYRead
import amf.spec.ParserContext
import org.yaml.parser.YamlParser

/**
  * Parse an object as a fully dynamic value.
  */
case class DataNodeParser(node: YNode,
                          parameters: AbstractVariables = AbstractVariables(),
                          parent: Option[String] = None)(
  implicit ctx: ParserContext) {
  def parse(): DataNode = {
    node.tag.tagType match {
      case YType.Str =>
        if (node.as[YScalar].text.matches("^\\d{2}:\\d{2}(:\\d{2})?$")) {
          parseScalar(node.as[YScalar], "time")
        } else if (node.as[YScalar].text.matches("^\\d{4}-\\d{1,2}-\\d{1,2}?$")) {
          parseScalar(node.as[YScalar], "date")
        } else {
          parseScalar(node.as[YScalar], "string")
        }
      case YType.Int   => parseScalar(node.as[YScalar], "integer")
      case YType.Float => parseScalar(node.as[YScalar], "float")
      case YType.Bool  => parseScalar(node.as[YScalar], "boolean")
      case YType.Null  => parseScalar(node.as[YScalar], "nil")
      case YType.Seq   => parseArray(node.as[Seq[YNode]], node)
      case YType.Map   => parseObject(node.as[YMap])
      case YType.Timestamp =>
        if (node.as[YScalar].text.indexOf(":") > -1) {
          parseScalar(node.as[YScalar], "dateTime")
        } else {
          parseScalar(node.as[YScalar], "date")
        }

      // Included external fragment
      case _ if node.tag.text == "!include" => parseInclusion(node)

      case other => {
        throw new Exception(s"Cannot parse data node from AST structure $other")
      }
    }
  }

  protected def parseInclusion(node: YNode): DataNode = {
    node.value match {
      case reference: YScalar =>
        ctx.refs.find(ref => ref.parsedUrl == reference.text) match {
          case Some(ref) if ref.baseUnit.isInstanceOf[ExternalFragment] =>
            val includedText = ref.baseUnit.asInstanceOf[ExternalFragment].encodes.raw
            parseIncludedAST(includedText)
          case _ => ScalarNode(
            node.value.toString,
            Some((Namespace.Xsd + "string").iri())
          ).withId(parent.getOrElse("") + "/included")
        }
      case _ => ScalarNode(
        node.value.toString,
        Some((Namespace.Xsd + "string").iri())
      ).withId(parent.getOrElse("") + "/included")
    }
  }


  def parseIncludedAST(raw: String): DataNode = {
    YamlParser(raw).parse().find(_.isInstanceOf[YNode]) match {
        case Some(node: YNode) => DataNodeParser(node, parameters, parent).parse()
        case _                 => ScalarNode(raw, Some((Namespace.Xsd + "string").iri())).withId(parent.getOrElse("") + "/included")
      }
  }

  protected def parseScalar(ast: YScalar, dataType: String): DataNode = {
    val node = ScalarNode(ast.text, Some((Namespace.Xsd + dataType).iri()), Annotations(ast))
    parent.foreach(node.adopted)
    parameters.parseVariables(ast)
    node
  }

  protected def parseArray(seq: Seq[YNode], ast: YPart): DataNode = {
    val node = DataArrayNode(Annotations(ast))
    parent.foreach(node.adopted)
    seq.foreach { v =>
      val element = DataNodeParser(v, parameters, Some(node.id)).parse()
      node.addMember(element)
    }
    node
  }

  protected def parseObject(value: YMap): DataNode = {
    val node = DataObjectNode(Annotations(value))
    parent.foreach(node.adopted)
    value.entries.map { ast =>
      val key = ast.key.as[YScalar].text
      parameters.parseVariables(key)
      val value               = ast.value
      val propertyAnnotations = Annotations(ast)

      val propertyNode = DataNodeParser(value, parameters, Some(node.id)).parse()
      node.addProperty(key, propertyNode, propertyAnnotations)
    }
    node
  }
}
