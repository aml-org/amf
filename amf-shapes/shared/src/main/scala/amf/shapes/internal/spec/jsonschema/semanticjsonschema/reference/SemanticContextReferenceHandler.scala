package amf.shapes.internal.spec.jsonschema.semanticjsonschema.reference

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.document._
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.reference.SemanticContextReferenceHandler.SEMANTIC_CONTEXT_KEY
import org.yaml.model.{YMap, YPart}

object SemanticContextReferenceHandler {
  protected val SEMANTIC_CONTEXT_KEY = "@context"
}

case class SemanticContextReferenceHandler(errorHandler: AMFErrorHandler) extends ReferenceHandler {

  /** Collect references on given document. */
  private implicit val eh: SyamlAMFErrorHandler = new SyamlAMFErrorHandler(errorHandler)

  override def collect(document: ParsedDocument, ctx: ParserContext): CompilerReferenceCollector = {
    document match {
      case syamlDoc: SyamlParsedDocument =>
        val collector = new CompilerReferenceCollector()
        searchContextEntries(syamlDoc.document, collector)
        collector
      case _ => EmptyReferenceCollector
    }
  }

  private def searchContextEntries(node: YPart, collector: CompilerReferenceCollector): Unit = {
    val remainingToSearch = node match {
      case map: YMap =>
        collectContextLink(map, collector)
        map.map.filterKeys(_.asScalar.exists(_.value == SEMANTIC_CONTEXT_KEY)).values
      case other => other.children
    }
    remainingToSearch.foreach { part =>
      searchContextEntries(part, collector)
    }
  }

  private def collectContextLink(map: YMap, collector: CompilerReferenceCollector): Unit = {
    map.map
      .get(SEMANTIC_CONTEXT_KEY)
      .foreach { entry =>
        entry.asScalar.foreach { scalar =>
          collector += (scalar.text, LinkReference, entry.location)
        }
      }
  }
}
