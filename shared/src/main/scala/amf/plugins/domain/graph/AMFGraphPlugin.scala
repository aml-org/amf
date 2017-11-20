package amf.plugins.domain.graph

import amf.parser._
import amf.compiler.Root
import amf.framework.plugins.AMFDomainPlugin
import amf.plugins.domain.graph.parser.GraphParser
import amf.remote.Platform
import amf.spec.ParserContext
import amf.vocabulary.Namespace
import org.yaml.model.YMap

class AMFGraphPlugin(platform: Platform) extends AMFDomainPlugin {

  override def domainSyntaxes = Seq(
    "application/ld+json",
    "application/json"
  )

  override def parse(root: Root, ctx: ParserContext) =
    Some(GraphParser(platform).parse(root.document, root.location))

  override def accept(root: Root) = {
    val maybeMaps = root.document.node.toOption[Seq[YMap]]
    val maybeMap         = maybeMaps.flatMap(s => s.headOption)
    maybeMap match {
      case Some(m: YMap) => m.key((Namespace.Document + "encodes").iri()).isDefined
      case _             => false
    }

  }
}
