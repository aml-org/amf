package amf.plugins.document.apicontract.references

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.document._
import amf.core.internal.validation.CoreValidations.UnresolvedReference
import org.yaml.model._

class JsonRefsReferenceHandler extends ReferenceHandler {

  private val references            = CompilerReferenceCollector()
  private var refUrls: Set[RefNode] = Set()

  case class RefNode(node: YNode, nodeValue: String) {
    override def equals(obj: Any): Boolean = obj match {
      case RefNode(_, aValue) => nodeValue == aValue
      case _                  => false
    }

    override def hashCode(): Int = nodeValue.hashCode
  }

  override def collect(inputParsed: ParsedDocument, ctx: ParserContext): CompilerReferenceCollector = {
    collect(inputParsed)(ctx.eh)
  }

  private def collect(inputParsed: ParsedDocument)(implicit errorHandler: AMFErrorHandler) = {
    inputParsed match {
      case parsed: SyamlParsedDocument =>
        links(parsed.document)
        refUrls.foreach { ref =>
          if (ref.nodeValue.startsWith("http:") || ref.nodeValue.startsWith("https:"))
            references += (ref.nodeValue, LinkReference, ref.node) // this is not for all scalars, link must be a string
          else
            references += (ref.nodeValue, InferredLinkReference, ref.node) // Is inferred because we don't know how to dereference by default
        }
      case _ => // ignore
    }

    references
  }

  private def links(part: YPart)(implicit errorHandler: AMFErrorHandler): Unit = {
    val childrens = part match {
      case map: YMap if map.map.contains("$ref") =>
        collectRef(map)
        part.children.filter(c => c != map.entries.find(_.key.as[YScalar].text == "$ref").get)
      case _ => part.children
    }
    childrens.foreach(c => links(c))
  }

  private def collectRef(map: YMap)(implicit errorHandler: AMFErrorHandler): Unit = {
    val ref = map.map("$ref")
    ref.tagType match {
      case YType.Str =>
        val refValue = ref.as[String]
        if (!refValue.startsWith("#")) refUrls += RefNode(ref, refValue.split("#").head)
      case _ => errorHandler.violation(UnresolvedReference, "", s"Unexpected $$ref with $ref", ref.value)
    }
  }
}
