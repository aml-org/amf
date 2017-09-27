package amf.spec.common

import amf.domain.Annotations
import amf.domain.extensions.{
  DataNode,
  ArrayNode => DataArrayNode,
  ObjectNode => DataObjectNode,
  ScalarNode => DataScalarNode
}
import amf.parser.YValueOps
import amf.vocabulary.Namespace
import org.yaml.model._

/**
  * Parse an object as a fully dynamic value.
  */
case class DataNodeParser(value: YNode,
                          parameters: AbstractVariables = AbstractVariables(),
                          parent: Option[String] = None) {
  def parse(): DataNode = {
    value.tag.tagType match {
      case YType.Str   => parseScalar(value.value.toScalar, "string")
      case YType.Int   => parseScalar(value.value.toScalar, "integer")
      case YType.Float => parseScalar(value.value.toScalar, "float")
      case YType.Bool  => parseScalar(value.value.toScalar, "boolean")
      case YType.Null  => parseScalar(value.value.toScalar, "nil")
      case YType.Seq   => parseArray(value.value.toSequence)
      case YType.Map   => parseObject(value.value.toMap)
      case other       => throw new Exception(s"Cannot parse data node from AST structure $other")
    }
  }

  protected def parseScalar(ast: YScalar, dataType: String): DataNode = {
    val node = DataScalarNode(ast.text, Some((Namespace.Xsd + dataType).iri()), Annotations(ast))
    parent.foreach(node.adopted)
    parameters.parseVariables(ast)
    node
  }

  protected def parseArray(value: YSequence): DataNode = {
    val node = DataArrayNode(Annotations(value))
    parent.foreach(node.adopted)
    value.nodes.foreach { ast =>
      val element = DataNodeParser(ast, parameters).parse()
      node.addMember(element)
    }
    node
  }

  protected def parseObject(value: YMap): DataNode = {
    val node = DataObjectNode(Annotations(value))
    parent.foreach(node.adopted)
    value.entries.map { ast =>
      val key = ast.key.value.toScalar.text
      parameters.parseVariables(key)
      val value               = ast.value
      val propertyAnnotations = Annotations(ast)

      val propertyNode = DataNodeParser(value, parameters, Some(node.id)).parse()
      node.addProperty(key, propertyNode, propertyAnnotations)
    }
    node
  }
}
