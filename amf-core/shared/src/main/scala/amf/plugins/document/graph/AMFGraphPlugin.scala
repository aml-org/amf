package amf.plugins.document.graph

import amf.client.render.RenderOptions
import amf.core.Root
import amf.core.model.document.BaseUnit
import amf.core.parser._
import amf.core.plugins.{AMFDocumentPlugin, AMFPlugin}
import amf.core.remote.Platform
import amf.core.resolution.pipelines.BasicResolutionPipeline
import amf.core.vocabulary.Namespace
import amf.plugins.document.graph.parser.{GraphDependenciesReferenceHandler, GraphEmitter, GraphParser}
import org.yaml.model.YMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AMFGraphPlugin extends AMFDocumentPlugin {

  override def init(): Future[AMFPlugin] = Future { this }

  override val ID             = "AMF Graph"
  override def dependencies() = Seq()

  val vendors = Seq("AMF JSON-LD", "AMF Graph")

  override def modelEntities = Nil

  override def serializableAnnotations() = Map.empty

  override def documentSyntaxes = Seq(
    "application/ld+json",
    "application/json",
    "application/amf+json"
  )

  override def canParse(root: Root) = {
    val maybeMaps = root.parsed.document.node.toOption[Seq[YMap]]
    val maybeMap  = maybeMaps.flatMap(s => s.headOption)
    maybeMap match {
      case Some(m: YMap) =>
        m.key("@id").isDefined || m.key("@type").isDefined || m
          .key((Namespace.Document + "encodes").iri())
          .isDefined || m.key((Namespace.Document + "declares").iri()).isDefined
      case _ => false
    }

  }
  override def parse(root: Root, ctx: ParserContext, platform: Platform) =
    Some(GraphParser(platform).parse(root.parsed.document, root.location))

  override def canUnparse(unit: BaseUnit) = true

  override def unparse(unit: BaseUnit, options: RenderOptions) =
    Some(GraphEmitter.emit(unit, options))

  override def referenceHandler() = GraphDependenciesReferenceHandler

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit) = new BasicResolutionPipeline().resolve(unit)
}
