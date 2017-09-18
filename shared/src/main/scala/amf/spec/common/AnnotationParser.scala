package amf.spec.common

import amf.common.core._
import amf.domain.extensions.{
  CustomDomainProperty,
  DataNode,
  DomainExtension,
  ArrayNode => DataArrayNode,
  ObjectNode => DataObjectNode,
  ScalarNode => DataScalarNode
}
import amf.domain.{Annotations, DomainElement}
import amf.parser.YValueOps
import amf.vocabulary.Namespace
import org.yaml.model._

import scala.collection.mutable.ListBuffer

case class AnnotationParser(element: DomainElement, map: YMap) {
  def parse(): Unit = {
    val domainExtensions: ListBuffer[DomainExtension] = ListBuffer()
    map.entries.foreach { entry =>
      val key = entry.key.value.toScalar.text.unquote
      if (WellKnownAnnotation.normalAnnotation(key)) {
        domainExtensions += ExtensionParser(key, element.id, entry).parse()
      }
    }
    if (domainExtensions.nonEmpty)
      element.withCustomDomainProperties(domainExtensions)
  }
}

case class ExtensionParser(annotationRamlName: String, parent: String, entry: YMapEntry) {
  def parse(): DomainExtension = {
    val domainExtension = DomainExtension()
    val annotationName  = WellKnownAnnotation.parseName(annotationRamlName)
    val dataNode        = DataNodeParser(entry.value, Some(parent + s"/$annotationName")).parse()
    // TODO
    // this is temporary, we should look for the annotation in the annotationTypes declared in the schema
    val customDomainProperty = CustomDomainProperty(Annotations(entry)).withName(annotationName)
    domainExtension.adopted(parent)
    domainExtension
      .withExtension(dataNode)
      .withDefinedBy(customDomainProperty)
  }
}

case class DataNodeParser(value: YNode, parent: Option[String] = None) {
  def parse(): DataNode = {
    value.tag match {
      case YTag.Str   => parseScalar(value.value.toScalar, "string")
      case YTag.Int   => parseScalar(value.value.toScalar, "integer")
      case YTag.Float => parseScalar(value.value.toScalar, "float")
      case YTag.Bool  => parseScalar(value.value.toScalar, "boolean")
      case YTag.Null  => parseScalar(value.value.toScalar, "nil")
      case YTag.Seq   => parseArray(value.value.toSequence)
      case YTag.Map   => parseObject(value.value.toMap)
      case other      => throw new Exception(s"Cannot parse data node from AST structure $other")
    }
  }

  protected def parseScalar(ast: YScalar, datatype: String): DataNode = {
    val node = DataScalarNode(ast.text.unquote, Some((Namespace.Xsd + datatype).iri()), Annotations(ast))
    parent.foreach(node.adopted)
    node
  }

  protected def parseArray(value: YSequence): DataNode = {
    val node = DataArrayNode(Annotations(value))
    parent.foreach(node.adopted)
    value.nodes.foreach { ast =>
      val element = DataNodeParser(ast).parse()
      node.addMember(element)
    }
    node
  }

  protected def parseObject(value: YMap): DataNode = {
    val node = DataObjectNode(Annotations(value))
    parent.foreach(node.adopted)
    value.entries.map { ast =>
      val property            = ast.key.value.toScalar.text.unquote
      val value               = ast.value
      val propertyAnnotations = Annotations(ast)

      val propertyNode = DataNodeParser(value, Some(node.id)).parse()
      node.addProperty(property, propertyNode, propertyAnnotations)
    }
    node
  }
}
