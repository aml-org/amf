package amf.agenticnetwork.internal.plugins.parse

import amf.core.client.scala.parse.document._
import amf.core.internal.parser.YMapOps
import org.yaml.model.{YDocument, YMap, YScalar, YType}

class AgenticNetworkReferenceHandler extends ReferenceHandler {
  val collector: CompilerReferenceCollector = CompilerReferenceCollector()

  override def collect(document: ParsedDocument, ctx: ParserContext): CompilerReferenceCollector = document match {
    case syaml: SyamlParsedDocument => collectImplementations(syaml)
    case _                          => collector
  }

  private def collectImplementations(parsed: SyamlParsedDocument): CompilerReferenceCollector = {
    val doc = parsed.document
    collectBrokers(doc)
    collector
  }

  private def collectBrokers(doc: YDocument): Unit = {
    // The references to collect are the implementations of the brokers in the path brokers.agentName.implementation
    doc.to[YMap] match {
      case Right(map) =>
        map.key("brokers").foreach { brokersEntry =>
          brokersEntry.value.to[YMap] match {
            case Right(brokersMap) =>
              brokersMap.entries.foreach { agentEntry =>
                agentEntry.value.to[YMap] match {
                  case Right(agentMap) =>
                    agentMap.key("implementation").foreach { implEntry =>
                      implEntry.value.tagType match {
                        case YType.Str =>
                          collector += (implEntry.value.as[YScalar].text, LinkReference, implEntry.value.location)
                        case _ => // ignore non-string implementations
                      }
                    }
                  case _ => // ignore non-map agent entries
                }
              }
            case _ => // ignore non-map brokers
          }
        }
      case _ => // ignore
    }
  }
}
