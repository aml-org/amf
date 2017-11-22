package amf.plugins.document.graph

import amf.client.GenerationOptions
import amf.core.Root
import amf.document.BaseUnit
import amf.framework.plugins.AMFDomainPlugin
import amf.parser._
import amf.plugins.document.graph.parser.{GraphEmitter, GraphParser}
import amf.plugins.document.graph.references.AMFGraphReferenceCollector
import amf.remote.Platform
import amf.spec.ParserContext
import amf.vocabulary.Namespace
import org.yaml.model.YMap

import scala.concurrent.Future

object AMFGraphPlugin extends AMFDomainPlugin {

  override val ID = "AMF Graph"

  val vendors = Seq("AMF JSON-LD")

  override def domainSyntaxes = Seq(
    "application/ld+json",
    "application/json",
    "application/amf+json"
  )

  override def canParse(root: Root) = {
    val maybeMaps = root.parsed.document.node.toOption[Seq[YMap]]
    val maybeMap         = maybeMaps.flatMap(s => s.headOption)
    maybeMap match {
      case Some(m: YMap) => m.key((Namespace.Document + "encodes").iri()).isDefined
      case _             => false
    }

  }
  override def parse(root: Root, ctx: ParserContext,  platform: Platform) =
    Some(GraphParser(platform).parse(root.parsed.document, root.location))

  override def canUnparse(unit: BaseUnit) = true

  override def unparse(unit: BaseUnit, options: GenerationOptions) =
    Some(GraphEmitter.emit(unit, options))

  override def referenceCollector() = new AMFGraphReferenceCollector()

}


