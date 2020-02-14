package amf.plugins.document.webapi.parser.spec.common

import amf.core.annotations.LexicalInformation
import amf.core.model.document.{EncodesModel, ExternalFragment}
import amf.core.model.domain.{DataNode, LinkNode, ScalarNode, ArrayNode => DataArrayNode, ObjectNode => DataObjectNode}
import amf.core.parser.{Annotations, _}
import amf.core.utils._
import amf.core.vocabulary.Namespace
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.features.validation.ParserSideValidations.SyamlError
import org.mulesoft.common.time.SimpleDateTime
import org.yaml.model.YNode.Alias
import org.mulesoft.lexer.InputRange
import org.yaml.model._
import org.yaml.parser.YamlParser

import scala.collection.mutable.ListBuffer

case class RefCounter(var count: Int = 0, maxRefThreshold: Long = RefCounter.defaultThreshold) {

  def exceedsThreshold(node: YNode): Boolean = {
    register(node)
    count > maxRefThreshold
  }

  def register(node: YNode): Unit = if (node.isInstanceOf[Alias]) count = count + 1
}

object RefCounter {
  val defaultThreshold = 10000
  def apply(ctx: WebApiContext): RefCounter =
    new RefCounter(maxRefThreshold = defaultThreshold)
}

/**
  * Parse an object as a fully dynamic value.
  */
class DataNodeParser(node: YNode,
                     refsCounter: RefCounter,
                     parameters: AbstractVariables = AbstractVariables(),
                     parent: Option[String] = None,
                     idCounter: IdCounter = new IdCounter)(implicit ctx: WebApiContext) {

  def parse(): DataNode = {
    if (refsCounter.exceedsThreshold(node)) {
      ctx.violation(
        SyamlError,
        parent.getOrElse(""),
        "Exceeded maximum yaml references threshold",
        node
      )
      DataObjectNode()
    } else {
      node.tag.tagType match {
        case YType.Seq => parseArray(node.as[Seq[YNode]], node)
        case YType.Map => parseObject(node.as[YMap])
        case _         => ScalarNodeParser(parameters, parent, idCounter).parse(node)
      }
    }
  }

  protected def parseArray(seq: Seq[YNode], ast: YPart): DataNode = {
    val node = DataArrayNode(Annotations(ast)).withName(idCounter.genId("array"))
    parent.foreach(node.adopted)
    seq.foreach { v =>
      val element =
        new DataNodeParser(v, refsCounter, parameters, Some(node.id), idCounter).parse().forceAdopted(node.id)
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

      val propertyNode =
        new DataNodeParser(value, refsCounter, parameters, Some(node.id), idCounter).parse().forceAdopted(node.id)
      node.addProperty(key.urlComponentEncoded, propertyNode, propertyAnnotations)
      node.lexicalPropertiesAnnotation.map(a => node.annotations += a)
    }
    node
  }
}

case class ScalarNodeParser(parameters: AbstractVariables = AbstractVariables(),
                            parent: Option[String] = None,
                            idCounter: IdCounter = new IdCounter)(implicit ctx: WebApiContext) {

  protected def parseScalar(ast: YScalar, dataType: String): DataNode = {
    val finalDataType =
      if (dataType == "dateTimeOnly") {
        Some((Namespace.Shapes + "dateTimeOnly").iri())
      } else {
        Some((Namespace.Xsd + dataType).iri())
      }
    val node = ScalarNode(ast.text, finalDataType, Annotations(ast))
      .withName(idCounter.genId("scalar"))
    parent.foreach(node.adopted)
    parameters.parseVariables(ast)
    node
  }

  def parse(node: YNode): DataNode = {
    node.tag.tagType match {
      case YType.Str       => parseScalar(node.as[YScalar], "string") // Date/time types are evaluated with patterns
      case YType.Int       => parseScalar(node.as[YScalar], "integer")
      case YType.Float     => parseScalar(node.as[YScalar], "double")
      case YType.Bool      => parseScalar(node.as[YScalar], "boolean")
      case YType.Null      => parseScalar(node.toOption[YScalar].getOrElse(YScalar("null")), "nil")
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

      // Included external fragment
      case _ if node.tagType == YType.Include => parseInclusion(node)

      case other =>
        val parsed = parseScalar(YScalar(other.toString()), "string")
        ctx.violation(SyamlError, parsed.id, None, s"Cannot parse scalar node from AST structure '$other'", node)
        parsed
    }
  }

  protected def parseInclusion(node: YNode): DataNode = {
    node.value match {
      case reference: YScalar =>
        ctx.refs.find(ref => ref.origin.url == reference.text) match {
          case Some(ref) if ref.unit.isInstanceOf[ExternalFragment] =>
            val includedText = ref.unit.asInstanceOf[ExternalFragment].encodes.raw.value()
            parseIncludedAST(includedText, node)
          case Some(ref) if ref.unit.isInstanceOf[EncodesModel] =>
            parseLink(reference.text).withLinkedDomainElement(ref.unit.asInstanceOf[EncodesModel].encodes)
          case _ =>
            ctx.declarations.fragments.get(reference.text).map(_.encoded) match {
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

  private def inputRangeString(range: InputRange): String =
    s"[(${range.lineFrom},${range.columnFrom})-(${range.lineTo},${range.columnTo})]"

  def parseIncludedAST(raw: String, node: YNode): DataNode = {
    YamlParser(raw, node.sourceName).withIncludeTag("!include").parse().find(_.isInstanceOf[YNode]) match {
      case Some(node: YNode) => new DataNodeParser(node, RefCounter(ctx), parameters, parent, idCounter).parse()
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

}

object DataNodeParser {
  def parse(parent: Option[String])(node: YNode)(implicit ctx: WebApiContext): DataNode =
    new DataNodeParser(node, refsCounter = RefCounter(ctx), parent = parent).parse()

  def apply(node: YNode, parameters: AbstractVariables = AbstractVariables(), parent: Option[String] = None)(
      implicit ctx: WebApiContext): DataNodeParser = {
    new DataNodeParser(node = node, refsCounter = RefCounter(ctx), parameters = parameters, parent = parent)
  }
}
