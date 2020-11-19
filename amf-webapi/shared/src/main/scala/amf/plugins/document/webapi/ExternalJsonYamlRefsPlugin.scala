package amf.plugins.document.webapi

import amf.client.plugins.{AMFDocumentPluginSettings, AMFPlugin}
import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.Obj
import amf.core.model.document.{BaseUnit, ExternalFragment}
import amf.core.model.domain.{AnnotationGraphLoader, ExternalDomainElement}
import amf.core.parser.{
  Annotations,
  CompilerReferenceCollector,
  InferredLinkReference,
  LinkReference,
  ParsedDocument,
  ParserContext,
  ReferenceCollector,
  ReferenceHandler,
  SyamlParsedDocument
}
import amf.core.remote.Platform
import amf.core.utils._
import amf.plugins.features.validation.CoreValidations.UnresolvedReference
import org.yaml.model._

import scala.concurrent.{ExecutionContext, Future}

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
    inputParsed match {
      case parsed: SyamlParsedDocument =>
        links(parsed.document, ctx)
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

  def links(part: YPart, ctx: ParserContext): Unit = {
    val childrens = part match {
      case map: YMap if map.map.contains("$ref") =>
        collectRef(map, ctx)
        part.children.filter(c => c != map.entries.find(_.key.as[YScalar].text == "$ref").get)
      case _ => part.children
    }
    childrens.foreach(c => links(c, ctx))
  }

  private def collectRef(map: YMap, ctx: ParserContext): Unit = {
    val ref = map.map("$ref")
    ref.tagType match {
      case YType.Str =>
        val refValue = ref.as[String]
        if (!refValue.startsWith("#")) refUrls += RefNode(ref, refValue.split("#").head)
      case _ => ctx.eh.violation(UnresolvedReference, "", s"Unexpected $$ref with $ref", ref.value)
    }
  }
}

class ExternalJsonYamlRefsPlugin extends JsonSchemaPlugin {

  override val priority: Int = AMFDocumentPluginSettings.PluginPriorities.low

  override val ID: String = "JSON + Refs"

  override val vendors: Seq[String] = Seq(ID)

  override def modelEntities: Seq[Obj] = Nil

  override def serializableAnnotations(): Map[String, AnnotationGraphLoader] = Map.empty

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit, errorHandler: ErrorHandler, pipelineId: String): BaseUnit = unit

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes: Seq[String] = Seq("application/json", "application/yaml")

  /**
    * Parses an accepted document returning an optional BaseUnit
    */
  override def parse(document: Root,
                     ctx: ParserContext,
                     platform: Platform,
                     options: ParsingOptions): Option[BaseUnit] = document.parsed match {

    case parsed: SyamlParsedDocument =>
      val result =
        ExternalDomainElement(Annotations(parsed.document))
          .withId(document.location + "#/")
          .withRaw(document.raw)
          .withMediaType(docMediaType(document))
      result.parsed = Some(parsed.document.node)
      val references = document.references.map(_.unit)
      val fragment = ExternalFragment()
        .withLocation(document.location)
        .withId(document.location)
        .withEncodes(result)
        .withLocation(document.location)
      if (references.nonEmpty) fragment.withReferences(references)
      Some(fragment)

    case _ =>
      None
  }

  private def docMediaType(doc: Root) = if (doc.raw.isJson) "application/json" else "application/yaml"

  override def canParse(document: Root): Boolean = !document.raw.isXml // for JSON or YAML

  override def referenceHandler(eh: ErrorHandler): ReferenceHandler = new JsonRefsReferenceHandler()

  override def dependencies(): Seq[AMFPlugin] = Nil

  override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = Future.successful(this)

  /**
    * Does references in this type of documents be recursive?
    */
  override val allowRecursiveReferences: Boolean = true
}

object ExternalJsonYamlRefsPlugin extends ExternalJsonYamlRefsPlugin
