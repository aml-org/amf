package amf.plugins.document.graph

import amf.core.emitter.RenderOptions
import amf.core.Root
import amf.core.metamodel.Obj
import amf.core.model.document.BaseUnit
import amf.core.model.domain.AnnotationGraphLoader
import amf.core.parser._
import amf.client.plugins.{AMFDocumentPlugin, AMFPlugin}
import amf.core.client.ParsingOptions
import amf.core.rdf.{RdfModelDocument, RdfModelEmitter, RdfModelParser}
import amf.core.remote.Platform
import amf.core.resolution.pipelines.{BasicResolutionPipeline, ResolutionPipeline}
import amf.core.unsafe.PlatformSecrets
import amf.core.vocabulary.Namespace
import amf.plugins.document.graph.parser.{GraphDependenciesReferenceHandler, GraphEmitter, GraphParser}
import org.yaml.model.YMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AMFGraphPlugin extends AMFDocumentPlugin with PlatformSecrets {

  override def init(): Future[AMFPlugin] = Future { this }

  override val ID             = "AMF Graph"
  override def dependencies() = Seq()

  val vendors = Seq("AMF JSON-LD", "AMF Graph")

  override def modelEntities: Seq[Obj] = Nil

  override def serializableAnnotations(): Map[String, AnnotationGraphLoader] = Map.empty

  override def documentSyntaxes = Seq(
    "application/ld+json",
    "application/json",
    "application/amf+json"
  )

  override def canParse(root: Root): Boolean = {
    root.parsed match {
      case parsed: SyamlParsedDocument =>
        val maybeMaps = parsed.document.node.toOption[Seq[YMap]]
        val maybeMap  = maybeMaps.flatMap(s => s.headOption)
        maybeMap match {
          case Some(m: YMap) =>
            m.key("@id").isDefined || m.key("@type").isDefined || m
              .key((Namespace.Document + "encodes").iri())
              .isDefined || m.key((Namespace.Document + "declares").iri()).isDefined
          case _ => false
        }
      case _: RdfModelDocument         => true

      case _                           => false
    }
  }

  override def parse(root: Root, ctx: ParserContext, platform: Platform, options: ParsingOptions): Option[BaseUnit] =
    root.parsed match {
      case parsed: SyamlParsedDocument =>
        Some(GraphParser(platform).parse(parsed.document, effectiveUnitUrl(root.location, options)))
      case parsed: RdfModelDocument    =>
        Some(new RdfModelParser(platform)(ctx).parse(parsed.model, effectiveUnitUrl(root.location, options)))
      case _                           =>
        None
    }

  override def canUnparse(unit: BaseUnit) = true

  override def unparse(unit: BaseUnit, options: RenderOptions): Some[ParsedDocument] =
     if (options.isAmfJsonLdSerilization || platform.rdfFramework.isEmpty) {
       Some(SyamlParsedDocument(comment = None, document = GraphEmitter.emit(unit, options)))
     } else {
       val rdfModel = platform.rdfFramework.get.unitToRdfModel(unit , options)
       Some(RdfModelDocument(model = rdfModel))
     }


  override def referenceHandler(): ReferenceHandler = GraphDependenciesReferenceHandler

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit, pipelineId: String = ResolutionPipeline.DEFAULT_PIPELINE): BaseUnit =
    new BasicResolutionPipeline(unit).resolve()

  /**
    * Does references in this type of documents be recursive?
    */
  override val allowRecursiveReferences: Boolean = true

  protected def effectiveUnitUrl(location: String, options: ParsingOptions): String = {
    options.definedBaseUrl match {
      case Some(url) => url
      case None      => location
    }
  }

}
