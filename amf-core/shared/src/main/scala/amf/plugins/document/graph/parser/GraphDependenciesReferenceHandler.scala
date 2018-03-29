package amf.plugins.document.graph.parser

import amf.core.parser.{ParsedDocument, ParserContext, ReferenceHandler, _}
import amf.core.vocabulary.Namespace
import org.yaml.model._

object GraphDependenciesReferenceHandler extends ReferenceHandler {

  val graphDependenciesPredicate: String = (Namespace.Document + "graphDependencies").iri()

  override def collect(parsed: ParsedDocument, ctx: ParserContext): ReferenceCollector = {
    val document  = parsed.document
    val maybeMaps = document.node.toOption[Seq[YMap]]
    maybeMaps.flatMap(s => s.headOption) match {
      case Some(map: YMap) =>
        map.entries.find(_.key.as[String] == graphDependenciesPredicate) match {
          case Some(entry) => processDependencyEntry(entry)
          case None        => EmptyReferenceCollector
        }
      case None => EmptyReferenceCollector
    }
  }

  protected def processDependencyEntry(entry: YMapEntry): ReferenceCollector = {
    entry.value.tagType match {
      case YType.Seq =>
        val links: IndexedSeq[Option[(String, YNode)]] = entry.value.as[YSequence].nodes.map { node =>
          node.tagType match {
            case YType.Map => extractLink(node)
            case _         => None
          }
        }
        val collector = ReferenceCollector()
        links.foreach {
          case Some((link, linkEntry)) => collector += (link, UnspecifiedReference, linkEntry)
          case _                       =>
        }
        collector
    }
  }

  protected def extractLink(node: YNode): Option[(String, YNode)] = {
    node.as[YMap].entries.find(_.key.as[String] == "@id") match {
      case Some(entry) => Some((entry.value.as[String], entry.value))
      case _           => None
    }
  }
}
