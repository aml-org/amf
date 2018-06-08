package amf.plugins.document.webapi.parser.spec.common

import amf.core.model.document.{EncodesModel, ExternalFragment}
import amf.core.model.domain.{DataNode, LinkNode, ScalarNode, ArrayNode => DataArrayNode, ObjectNode => DataObjectNode}
import amf.core.parser.{Annotations, _}
import amf.core.utils._
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model._
import org.yaml.parser.YamlParser

import scala.collection.mutable.ListBuffer

/**
  * We need to generate unique IDs for all data nodes if the name is not set
  */
class IdCounter {
  private var c = 0

  def genId(id: String): String = {
    c += 1
    s"${id}_$c"
  }

  def reset(): Unit = c = 0
}

/**
  * Parse an object as a fully dynamic value.
  */
case class DataNodeParser(node: YNode,
                          parameters: AbstractVariables = AbstractVariables(),
                          parent: Option[String] = None,
                          idCounter: IdCounter = new IdCounter)(implicit ctx: WebApiContext) {

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
      case YType.Str =>
        if (node
              .as[YScalar]
              .text
              .matches(
                "(\\d{4})-(\\d{2})-(\\d{2})(T|t)(\\d{2})\\:(\\d{2})\\:(\\d{2})(([+-](\\d{2})\\:(\\d{2}))|(\\.\\d+)?Z|(\\.\\d+)?z)")) {
          val (dateParts, timeParts) = parseTimestamp(node)
          if (dateParts(1).toInt < 13 && dateParts(2).toInt < 32 && timeParts(0).toInt < 24 && timeParts(1).toInt < 60 && timeParts(
                1).toInt < 60)
            parseScalar(node.as[YScalar], "dateTime")
          else
            parseScalar(node.as[YScalar], "string")

        } else if (node.as[YScalar].text.matches("^\\d{2}:\\d{2}:\\d{2}$")) {
          val nodeScalar = node.as[YScalar]
          val parts      = nodeScalar.text.split(":")
          if (parts(0).toInt < 24 && parts(1).toInt < 60 && parts(1).toInt < 60)
            parseScalar(nodeScalar, "time")
          else
            parseScalar(node.as[YScalar], "string")
        } else if (node.as[YScalar].text.matches("^\\d{2}:\\d{2}$")) {
          val text  = node.as[YScalar].text
          val parts = text.split(":")
          if (parts(0).toInt < 24 && parts(1).toInt < 60)
            parseScalar(YScalar(text + ":00"), "time")
          else
            parseScalar(node.as[YScalar], "string")
        } else if (node.as[YScalar].text.matches("^\\d{4}-\\d{1,2}-\\d{1,2}?$")) {
          val nodeScalar = node.as[YScalar]
          val parts      = nodeScalar.text.split("-")
          if (parts(1).toInt < 13 && parts(2).toInt < 32)
            parseScalar(nodeScalar, "date")
          else
            parseScalar(node.as[YScalar], "string")
        } else {
          parseScalar(node.as[YScalar], "string")
        }
      case YType.Int   => parseScalar(node.as[YScalar], "integer")
      case YType.Float => parseScalar(node.as[YScalar], "double")
      case YType.Bool  => parseScalar(node.as[YScalar], "boolean")
      case YType.Null  => parseScalar(node.as[YScalar], "nil")
      case YType.Seq   => parseArray(node.as[Seq[YNode]], node)
      case YType.Map   => parseObject(node.as[YMap])
      case YType.Timestamp =>
        try {
          val (dateParts, timeParts) = parseTimestamp(node)

          if (node
                .as[YScalar]
                .text
                .matches(
                  "(\\d{4})-(\\d{2})-(\\d{2})(T|t)(\\d{2})\\:(\\d{2})\\:(\\d{2})(([+-](\\d{2})\\:(\\d{2}))|(\\.\\d+)?Z|(\\.\\d+)?z)")) {

            if (dateParts(1).toInt < 13 && dateParts(2).toInt < 32 && timeParts(0).toInt < 24 && timeParts(1).toInt < 60 && timeParts(
                  1).toInt < 60)
              parseScalar(node.as[YScalar], "dateTime")
            else
              parseScalar(node.as[YScalar], "string")
          } else if (node
                       .as[YScalar]
                       .text
                       .indexOf(":") > -1 && node.as[YScalar].text.toLowerCase().indexOf("t") > -1) {
            if (dateParts(1).toInt < 13 && dateParts(2).toInt < 32 && timeParts(0).toInt < 24 && timeParts(1).toInt < 60 && timeParts(
                  1).toInt < 60)
              parseScalar(node.as[YScalar], "dateTimeOnly")
            else
              parseScalar(node.as[YScalar], "string")
          } else if (node.as[YScalar].text.indexOf(":") > -1) {
            if (dateParts(1).toInt < 13 && dateParts(2).toInt < 32 && timeParts(0).toInt < 24 && timeParts(1).toInt < 60 && timeParts(
                  1).toInt < 60)
              parseScalar(node.as[YScalar], "dateTime")
            else
              parseScalar(node.as[YScalar], "string")
          } else {
            if (dateParts(1).toInt < 13 && dateParts(2).toInt < 32)
              parseScalar(node.as[YScalar], "date")
            else
              parseScalar(node.as[YScalar], "string")
          }
        } catch {
          case e: Exception =>
            parseScalar(node.as[YScalar], "string")
        }

      // Included external fragment
      case _ if node.tagType == YType.Include => parseInclusion(node)

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

  protected def parseInclusion(node: YNode): DataNode = {
    node.value match {
      case reference: YScalar =>
        ctx.refs.find(ref => ref.origin.url == reference.text) match {
          case Some(ref) if ref.unit.isInstanceOf[ExternalFragment] =>
            val includedText = ref.unit.asInstanceOf[ExternalFragment].encodes.raw.value()
            parseIncludedAST(includedText)
          case Some(ref) if ref.unit.isInstanceOf[EncodesModel] =>
            parseLink(reference.text).withLinkedDomainElement(ref.unit.asInstanceOf[EncodesModel].encodes)
          case _ =>
            ctx.declarations.fragments.get(reference.text) match {
              case Some(domainElement) =>
                parseLink(reference.text).withLinkedDomainElement(domainElement)
              case _ =>
                parseLink(reference.text)
            }
        }
      case _ =>
        parseLink(node.value.toString)
    }
  }

  def parseIncludedAST(raw: String): DataNode = {
    YamlParser(raw).withIncludeTag("!include").parse().find(_.isInstanceOf[YNode]) match {
      case Some(node: YNode) => DataNodeParser(node, parameters, parent, idCounter).parse()
      case _                 => ScalarNode(raw, Some((Namespace.Xsd + "string").iri())).withId(parent.getOrElse("") + "/included")
    }
  }

  /**
    * Generates a new LinkNode base on the text of a label and the fragments in the context
    * @param linkText local text pointing to fragment
    * @return the parsed LinkNode
    */
  protected def parseLink(linkText: String): LinkNode = {
    if (linkText.contains(":")) {
      LinkNode(linkText, linkText).withId(linkText.normalizeUrl)
    } else {
      val localUrl  = parent.getOrElse("#").split("#").head
      val leftLink  = if (localUrl.endsWith("/")) localUrl else s"${baseUrl(localUrl)}/"
      val rightLink = if (linkText.startsWith("/")) linkText.drop(1) else linkText
      val finalLink = normalizeUrl(leftLink + rightLink)
      LinkNode(linkText, finalLink).withId(finalLink)
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
    } else if (dataType == "rfc2616") {
      Some((Namespace.Shapes + "rfc2616").iri())
    } else {
      Some((Namespace.Xsd + dataType).iri())
    }
    val node = ScalarNode(ast.text, finalDataType, Annotations(ast))
      .withName(idCounter.genId("scalar"))
    parent.foreach(node.adopted)
    parameters.parseVariables(ast)
    node
  }

  protected def parseArray(seq: Seq[YNode], ast: YPart): DataNode = {
    val node = DataArrayNode(Annotations(ast)).withName(idCounter.genId("array"))
    parent.foreach(node.adopted)
    seq.foreach { v =>
      val element = DataNodeParser(v, parameters, Some(node.id), idCounter).parse().forceAdopted(node.id)
      node.addMember(element)
    }
    node
  }

  protected def parseObject(value: YMap): DataNode = {
    val node = DataObjectNode(Annotations(value)).withName(idCounter.genId("object"))
    parent.foreach(node.adopted)
    value.entries.map { ast =>
      val key = ast.key.as[YScalar].text
      parameters.parseVariables(key)
      val value               = ast.value
      val propertyAnnotations = Annotations(ast)

      val propertyNode = DataNodeParser(value, parameters, Some(node.id), idCounter).parse().forceAdopted(node.id)
      node.addProperty(key.urlComponentEncoded, propertyNode, propertyAnnotations)
      node.lexicalPropertiesAnnotation.map(a => node.annotations += a)
    }
    node
  }
}
