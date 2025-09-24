package amf.shapes.internal.plugins.parser

import amf.core.client.common.{HighPriority, PluginPriority}
import amf.core.internal.remote.Mimes._

object JsonSchemaBasedSpecGraphParsePlugin extends BaseJsonLDSchemaGraphParsePlugin {

  /** Given that most of these specs has no entry to identify if the document parse plugin should apply or not and it
   *  always returns true, I need to increase the priority of graph parser. The applies of graph parser is correctly
   *  implemented. If not, some JSON-LDs could be processed as they are JSON instances of the specs
   */

  override def priority: PluginPriority = HighPriority

  override def mediaTypes: Seq[String] = Seq(`application/ld+json`)
}
