package amf.spec.common

import amf.common.AMFAST
import amf.common.AMFToken._
import amf.common.core._
import amf.domain.extensions.{CustomDomainProperty, DataNode, DomainExtension, ArrayNode => DataArrayNode, ObjectNode => DataObjectNode, ScalarNode => DataScalarNode}
import amf.domain.{Annotations, DomainElement}
import amf.vocabulary.Namespace

import scala.collection.mutable.ListBuffer

case class AnnotationParser(element: DomainElement, entries: Entries) {
  def parse(): Unit = {
    val domainExtensions:ListBuffer[DomainExtension] = ListBuffer()
    entries.entries.foreach {
      case (key, entry) => {
        if (WellKnownAnnotation.normalAnnotation(key)) {
          domainExtensions += ExtensionParser(key, element.id, entry).parse()
        }
      }
    }
    if (domainExtensions.nonEmpty)
      element.withCustomDomainProperties(domainExtensions)
  }
}

case class ExtensionParser(annotationRamlName: String, parent: String, entry: EntryNode) {
  def parse(): DomainExtension = {
    val domainExtension = DomainExtension()
    val annotationName = WellKnownAnnotation.parseName(annotationRamlName)
    val dataNode = DataNodeParser(entry.value, Some(parent + s"/$annotationName")).parse()
    // TODO
    // this is temporary, we should look for the annotation in the annotationTypes declared in the schema
    val customDomainProperty = CustomDomainProperty(entry.annotations()).withName(annotationName)
    domainExtension.adopted(parent)
    domainExtension
      .withExtension(dataNode)
      .withDefinedBy(customDomainProperty)
  }
}

case class DataNodeParser(value: AMFAST, parent: Option[String] = None) {
  def parse(): DataNode = {
    value.`type` match {
      case StringToken   => parseScalar(value, "string")
      case IntToken      => parseScalar(value, "integer")
      case FloatToken    => parseScalar(value, "float")
      case BooleanToken  => parseScalar(value, "boolean")
      case Null          => parseScalar(value, "nil")
      case SequenceToken => parseArray(value)
      case MapToken      => parseObject(value)
      case other         => throw new Exception(s"Cannot parse data node from AST structure $other")
    }
  }

  protected def parseScalar(ast: AMFAST, datatype: String): DataNode = {
    val node = DataScalarNode(ast.content.unquote, Some((Namespace.Xsd + datatype).iri()), Annotations(ast))
    if (parent.isDefined) node.adopted(parent.get)
    node
  }

  protected def parseArray(value: AMFAST): DataNode = {
    val node = DataArrayNode(Annotations(value))
    if (parent.isDefined) node.adopted(parent.get)
    value.children.foreach { ast =>
      val element = DataNodeParser(ast).parse()
      node.addMember(element)
    }
    node
  }

  protected def parseObject(value: AMFAST): DataNode = {
    val node = DataObjectNode(Annotations(value))
    if (parent.isDefined) node.adopted(parent.get)
    value.children.map { ast =>
      val property = ast.head.content.unquote
      val value = Option(ast).filter(_.children.size > 1).map(_.last).orNull
      val propertyAnnotations = Annotations(ast)

      val propertyNode = DataNodeParser(value, Some(node.id)).parse()
      node.addProperty(property, propertyNode, propertyAnnotations)
    }
    node
  }
}
