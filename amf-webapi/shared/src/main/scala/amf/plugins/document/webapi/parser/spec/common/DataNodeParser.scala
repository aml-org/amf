package amf.plugins.document.webapi.parser.spec.common

import amf.core.annotations.ReferenceId
import amf.core.model.DataType
import amf.core.model.document.{EncodesModel, ExternalFragment}
import amf.core.model.domain.{DataNode, LinkNode, ScalarNode, ArrayNode => DataArrayNode, ObjectNode => DataObjectNode}
import amf.core.parser.{Annotations, _}
import amf.core.utils._
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.features.validation.CoreValidations.SyamlError
import amf.validations.ParserSideValidations.ExeededMaxYamlReferences
import org.mulesoft.common.time.SimpleDateTime
import org.mulesoft.lexer.InputRange
import org.yaml.model.YNode.MutRef
import org.yaml.model._
import org.yaml.parser.YamlParser

import scala.collection.mutable.ListBuffer

/**
  * Parse an object as a fully dynamic value.
  */
// TODO: should have private constructor
class DataNodeParser private (node: YNode,
                              refsCounter: AliasCounter,
                              parameters: AbstractVariables = AbstractVariables(),
                              parent: Option[String] = None,
                              idCounter: IdCounter = new IdCounter)(implicit ctx: WebApiContext) {

  def parse(): DataNode = {
    if (refsCounter.exceedsThreshold(node)) {
      ctx.violation(
        ExeededMaxYamlReferences,
        parent.getOrElse(""),
        "Exceeded maximum yaml references threshold"
      )
      DataObjectNode()
    } else {
      val parsedNode = node.tag.tagType match {
        case YType.Seq => parseArray(node.as[Seq[YNode]], node)
        case YType.Map => parseObject(node.as[YMap])
        case _         => ScalarNodeParser(parameters, parent, idCounter).parse(node)
      }
      node match {
        case m: MutRef =>
          (m.origValue, m.target) match {
            case (referenceText: YScalar, Some(target)) =>
              val url: String = target.location.sourceName
              referenceText.text.split("#") match {
                case Array(_, fragmentPath) => parsedNode.annotations += ReferenceId(s"$url#$fragmentPath")
                case _                      => parsedNode.annotations += ReferenceId(url)
              }
            case (_, Some(target)) =>
              parsedNode.annotations += ReferenceId(s"${target.location.sourceName}")
            case _ => // Ignore
          }
        case _ => // Ignore
      }
      parsedNode
    }
  }

  protected def parseArray(seq: Seq[YNode], ast: YPart): DataNode = {
    val node = DataArrayNode(Annotations(ast)).withName(idCounter.genId("array"))
    parent.foreach(p => node.adopted(p))
    val members: ListBuffer[DataNode] = ListBuffer()
    for { v <- seq } yield {
      members += new DataNodeParser(v, refsCounter, parameters, Some(node.id), idCounter).parse().forceAdopted(node.id)
    }
    node.withMembers(members)
    node
  }

  protected def parseObject(value: YMap): DataNode = {
    val node = DataObjectNode(Annotations(value)).withName(idCounter.genId("object"))
    parent.foreach(p => node.adopted(p))
    value.entries.map { ast =>
      val key = ast.key.as[YScalar].text
      parameters.parseVariables(key)
      val value               = ast.value
      val propertyAnnotations = Annotations(ast)

      val propertyNode =
        new DataNodeParser(value, refsCounter, parameters, Some(node.id), idCounter).parse().forceAdopted(node.id)
      node.addProperty(key, propertyNode, propertyAnnotations)
    }
    node
  }
}

case class ScalarNodeParser(parameters: AbstractVariables = AbstractVariables(),
                            parent: Option[String] = None,
                            idCounter: IdCounter = new IdCounter)(implicit ctx: WebApiContext) {

  protected def parseScalar(ast: YScalar, dataType: String): DataNode = {
    val finalDataType = Some(DataType(dataType))
    val node = ScalarNode(ast.text, finalDataType, Annotations(ast))
      .withName(idCounter.genId("scalar"))
    parent.foreach(p => node.adopted(p))
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
        ctx.eh.violation(SyamlError, parsed.id, None, s"Cannot parse scalar node from AST structure '$other'", node)
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
    val n = YamlParser(raw, node.sourceName).withIncludeTag("!include").document().node
    if (n.isNull) ScalarNode(raw, Some(DataType.String)).withId(parent.getOrElse("") + "/included")
    else DataNodeParser(n, parameters, parent, idCounter).parse()
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
  def parse(parent: Option[String], idCounter: IdCounter)(node: YNode)(implicit ctx: WebApiContext): DataNode =
    new DataNodeParser(node,
                       refsCounter = AliasCounter(ctx.options.getMaxYamlReferences),
                       parent = parent,
                       idCounter = idCounter).parse()

  def apply(node: YNode, parameters: AbstractVariables = AbstractVariables(), parent: Option[String] = None)(
      implicit ctx: WebApiContext): DataNodeParser = {
    new DataNodeParser(node = node,
                       refsCounter = AliasCounter(ctx.options.getMaxYamlReferences),
                       parameters = parameters,
                       parent = parent)
  }

  def apply(node: YNode, parameters: AbstractVariables, parent: Option[String], idCounter: IdCounter)(
      implicit ctx: WebApiContext): DataNodeParser = {
    new DataNodeParser(node, AliasCounter(ctx.options.getMaxYamlReferences), parameters, parent, idCounter)
  }
}
