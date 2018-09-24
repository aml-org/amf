package amf.plugins.document.vocabularies.parser

import amf.core.model.domain.{DataNode, ScalarNode, ArrayNode => DataArrayNode, ObjectNode => DataObjectNode}
import amf.core.parser.{Annotations, ParserContext}
import amf.core.utils.IdCounter
import amf.core.utils._
import amf.core.vocabulary.Namespace
import amf.plugins.features.validation.ParserSideValidations
import org.mulesoft.common.time.SimpleDateTime
import org.yaml.model._

import scala.collection.mutable.ListBuffer

/**
  * Parse an object as a fully dynamic value.
  */
case class DynamicExtensionParser(node: YNode, parent: Option[String] = None, idCounter: IdCounter = new IdCounter)(
    implicit ctx: ParserContext) {

  def parseTimestamp(node: YNode): (Seq[String], Seq[String]) = {
    val text = node.as[YScalar].text.toLowerCase()
    val date = text.split("t").headOption.getOrElse("")
    val rest = text.split("t").last
    val time = if (rest.contains("+")) {
      rest.split("\\+").head
    } else if (rest.contains("-")) {
      rest.split("-").head
    } else if (rest.contains("z")) {
      rest.split("z").head
    } else if (rest.contains(".")) {
      rest.split(".").head
    } else {
      rest
    }
    val dateParts = date.split("-")
    val timeParts = time.split(":")
    (dateParts, timeParts)
  }

  def parse(): DataNode = {
    node.tag.tagType match {
      case YType.Str       => parseScalar(node.as[YScalar], "string") // Date/time types are evaluated with patterns
      case YType.Int       => parseScalar(node.as[YScalar], "integer")
      case YType.Float     => parseScalar(node.as[YScalar], "double")
      case YType.Bool      => parseScalar(node.as[YScalar], "boolean")
      case YType.Null      => parseScalar(node.as[YScalar], "nil")
      case YType.Seq       => parseArray(node.as[Seq[YNode]], node)
      case YType.Map       => parseObject(node.as[YMap])
      case YType.Timestamp =>
        // TODO add time-only type in syaml and amf
        SimpleDateTime.parse(node.toString()).toOption match {
          case Some(sdt) =>
            try {
              sdt.toDate // This is to validate the parsed timestamp
              if (sdt.timeOfDay.isEmpty)
                parseScalar(node.as[YScalar], "date")
              else if (sdt.zoneOffset.isEmpty)
                parseScalar(node.as[YScalar], "dateTimeOnly")
              else
                parseScalar(node.as[YScalar], "dateTime")
            } catch {
              case _: Exception => parseScalar(node.as[YScalar], "string")
            }
          case None => parseScalar(node.as[YScalar], "string")
        }

      case other =>
        val parsed = parseScalar(YScalar(other.toString()), "string")
        ctx.violation(ParserSideValidations.ParsingErrorSpecification.id,
                      parsed.id,
                      None,
                      s"Cannot parse data node from AST structure '$other'",
                      node)
        parsed
    }
  }

  protected def baseUrl(url: String): String = {
    if (url.contains("://")) {
      val protocol  = url.split("://").head
      val path      = url.split("://").last
      val remaining = path.split("/").dropRight(1)
      s"$protocol://${remaining.mkString("/")}"
    } else {
      url.split("/").dropRight(1).mkString("/")
    }
  }

  protected def normalizeUrl(url: String): String = {
    if (url.contains("://")) {
      val protocol                  = url.split("://").head
      val path                      = url.split("://").last
      val remaining                 = path.split("/")
      var stack: ListBuffer[String] = new ListBuffer[String]()
      remaining.foreach {
        case "."   => // ignore
        case ".."  => stack = stack.dropRight(1)
        case other => stack += other
      }
      s"$protocol://${stack.mkString("/")}"
    } else {
      url
    }
  }

  protected def parseScalar(ast: YScalar, dataType: String): DataNode = {
    val finalDataType = if (dataType == "dateTimeOnly") {
      Some((Namespace.Shapes + "dateTimeOnly").iri())
    } else {
      Some((Namespace.Xsd + dataType).iri())
    }
    val node = ScalarNode(ast.text, finalDataType, Annotations(ast))
      .withName(idCounter.genId("scalar"))
    parent.foreach(node.adopted)
    node
  }

  protected def parseArray(seq: Seq[YNode], ast: YPart): DataNode = {
    val node = DataArrayNode(Annotations(ast)).withName(idCounter.genId("array"))
    parent.foreach(node.adopted)
    seq.foreach { v =>
      val element = DynamicExtensionParser(v, Some(node.id), idCounter).parse().forceAdopted(node.id)
      node.addMember(element)
    }
    node
  }

  protected def parseObject(value: YMap): DataNode = {
    val node = DataObjectNode(Annotations(value)).withName(idCounter.genId("object"))
    parent.foreach(node.adopted)
    value.entries.map { ast =>
      val key                 = ast.key.as[YScalar].text
      val value               = ast.value
      val propertyAnnotations = Annotations(ast)

      val propertyNode = DynamicExtensionParser(value, Some(node.id), idCounter).parse().forceAdopted(node.id)
      node.addProperty(key.urlComponentEncoded, propertyNode, propertyAnnotations)
      node.lexicalPropertiesAnnotation.map(a => node.annotations += a)
    }
    node
  }
}
