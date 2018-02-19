package amf.plugins.document.webapi

import amf.core.Root
import amf.core.client.GenerationOptions
import amf.core.model.document.BaseUnit
import amf.core.parser.ParserContext
import amf.core.plugins.{AMFDocumentPlugin, AMFPlugin}
import amf.core.remote.Platform
import amf.plugins.document.webapi.parser.PayloadParser
import amf.plugins.document.webapi.references.PayloadReferenceCollector
import amf.plugins.document.webapi.resolution.pipelines.CanonicalShapePipeline
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import org.yaml.model.{YMap, YScalar}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PayloadPlugin extends AMFDocumentPlugin {

  override val ID = "AMF Payload"

  val vendors = Seq("AMF Payload")

  override def modelEntities = Nil

  override def serializableAnnotations() = Map.empty

  override def dependencies() = Seq(WebAPIDomainPlugin, DataShapesDomainPlugin)

  // we are looking for documents with a very specific payload
  // otherwise, this plugin can become the fallback option.
  // Fallback option should be an external fragment.
  override def documentSyntaxes = Seq(
    "application/amf+json",
    "application/amf+yaml" /*,
    "application/json",
    "application/yaml"*/
  )

  override def parse(root: Root, parentContext: ParserContext, platform: Platform) = {
    implicit val ctx = parentContext
    Some(PayloadParser(root.parsed.document, root.location).parseUnit())
  }

  override def canParse(root: Root) = notRAML(root) && notOAS(root) // any document can be parsed as a Payload
  override def referenceCollector() = new PayloadReferenceCollector

  private def notRAML(root: Root) = root.parsed.comment.isEmpty || !root.parsed.comment.get.metaText.startsWith("%")

  private def notOAS(root: Root) = {
    root.parsed.document.node.value match {
      case map: YMap => {
        !map.entries.exists(_.key.value.asInstanceOf[YScalar].text.startsWith("swagger"))
      }
      case _ => true
    }
  }

  // Unparsing payloads not supported
  override def unparse(unit: BaseUnit, options: GenerationOptions) = None

  override def canUnparse(unit: BaseUnit) = false

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit) = new CanonicalShapePipeline().resolve(unit)

  override def init(): Future[AMFPlugin] = Future { this }
}
