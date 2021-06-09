package amf.plugins.document.apicontract.parser.spec.common

import amf.core.annotations.{ReferenceId, ScalarType}
import amf.core.metamodel.domain.ScalarNodeModel
import amf.core.model.DataType
import amf.core.model.document.{EncodesModel, ExternalFragment}
import amf.core.model.domain.ScalarNode.forDataType
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{DataNode, LinkNode, ScalarNode, ArrayNode => DataArrayNode, ObjectNode => DataObjectNode}
import amf.core.parser.{Annotations, _}
import amf.core.utils._
import amf.plugins.features.validation.CoreValidations.SyamlError
import amf.validations.ShapeParserSideValidations.ExceededMaxYamlReferences
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

trait DataNodeParserContext {
  def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty]
  def refs: Seq[ParsedReference]
  def getMaxYamlReferences: Option[Int]
  def fragments: Map[String, FragmentRef]
}

class DataNodeParser private (
    node: YNode,
    refsCounter: AliasCounter,
    parameters: AbstractVariables = AbstractVariables(),
    parent: Option[String] = None,
    idCounter: IdCounter = new IdCounter)(implicit ctx: ErrorHandlingContext with DataNodeParserContext) {

  def parse(): DataNode = {
    if (refsCounter.exceedsThreshold(node)) {
      ctx.violation(
        ExceededMaxYamlReferences,
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
    val node = DataObjectNode(Annotations(value)).withSynthesizeName(idCounter.genId("object"))
    parent.foreach(p => node.adopted(p))
    value.entries.map { entry =>
      parameters.parseVariables(entry.key)
      val value               = entry.value
      val propertyAnnotations = Annotations(entry)

      val propertyNode =
        new DataNodeParser(value, refsCounter, parameters, Some(node.id), idCounter).parse().forceAdopted(node.id)
      node.addProperty(keyFor(entry), propertyNode, propertyAnnotations)
    }
    node
  }

  private def keyFor(ast: YMapEntry) =
    ast.key.as[YScalar].text
}

case class ScalarNodeParser(
    parameters: AbstractVariables = AbstractVariables(),
    parent: Option[String] = None,
    idCounter: IdCounter = new IdCounter)(implicit ctx: ErrorHandlingContext with DataNodeParserContext) {

  private def newScalarNode(value: amf.core.parser.ScalarNode,
                            dataType: String,
                            annotations: Annotations): ScalarNode = {
    val scalar = new ScalarNode(Fields(), annotations)
    annotations += ScalarType(dataType)
    scalar.set(ScalarNodeModel.DataType, forDataType(dataType), Annotations.synthesized())
    scalar.set(ScalarNodeModel.Value, value.text(), Annotations.inferred())
  }

  protected def parseScalar(node: YNode, dataType: String): DataNode = {
    val finalDataType = DataType(dataType)
    val scalarNode    = amf.core.parser.ScalarNode(node)
    val dataNode = newScalarNode(scalarNode, finalDataType, Annotations(node))
      .withSynthesizeName(idCounter.genId("scalar"))
    parent.foreach(p => dataNode.adopted(p))
    parameters.parseVariables(scalarNode.text().toString)
    dataNode
  }

  def parse(node: YNode): DataNode = {
    node.tag.tagType match {
      case YType.Str       => parseScalar(node, "string") // Date/time types are evaluated with patterns
      case YType.Int       => parseScalar(node, "integer")
      case YType.Float     => parseScalar(node, "double")
      case YType.Bool      => parseScalar(node, "boolean")
      case YType.Null      => parseScalar(node, "nil")
      case YType.Timestamp =>
        // TODO add time-only type in syaml and amf
        SimpleDateTime.parse(node.toString()).toOption match {
          case Some(sdt) =>
            try {
              sdt.toDate // This is to validate the parsed timestamp
              if (sdt.timeOfDay.isEmpty)
                parseScalar(node, "date")
              else if (sdt.zoneOffset.isEmpty)
                parseScalar(node, "dateTimeOnly")
              else
                parseScalar(node, "dateTime")
            } catch {
              case _: Exception => parseScalar(node, "string")
            }
          case None => parseScalar(node, "string")
        }

      // Included external fragment
      case _ if node.tagType == YType.Include => parseInclusion(node)

      case other =>
        val parsed = parseScalar(YNode(other.toString()), "string")
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
            ctx.fragments.get(reference.text).map(_.encoded) match {
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
  def parse(parent: Option[String], idCounter: IdCounter)(node: YNode)(
      implicit ctx: ErrorHandlingContext with DataNodeParserContext): DataNode =
    new DataNodeParser(node,
                       refsCounter = AliasCounter(ctx.getMaxYamlReferences),
                       parent = parent,
                       idCounter = idCounter).parse()

  def apply(node: YNode, parameters: AbstractVariables = AbstractVariables(), parent: Option[String] = None)(
      implicit ctx: ErrorHandlingContext with DataNodeParserContext): DataNodeParser = {
    new DataNodeParser(node = node,
                       refsCounter = AliasCounter(ctx.getMaxYamlReferences),
                       parameters = parameters,
                       parent = parent)
  }

  def apply(node: YNode, parameters: AbstractVariables, parent: Option[String], idCounter: IdCounter)(
      implicit ctx: ErrorHandlingContext with DataNodeParserContext): DataNodeParser = {
    new DataNodeParser(node, AliasCounter(ctx.getMaxYamlReferences), parameters, parent, idCounter)
  }
}
