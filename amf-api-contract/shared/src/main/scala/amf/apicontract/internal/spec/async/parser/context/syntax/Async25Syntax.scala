package amf.apicontract.internal.spec.async.parser.context.syntax

import amf.shapes.internal.spec.common.parser.SpecSyntax
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.GooglePubSub

object Async25Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] = add(Async24Syntax.nodes,
    "server" -> Set(
      "tags"
    ),
    "bindings"-> Set(GooglePubSub),
    "GooglePubSubChannelBinding" -> Set(
      "labels",
      "messageRetentionDuration",
      "messageStoragePolicy",
      "schemaSettings",
      "topic",
      "bindingVersion"
    ),
    "GooglePubSubMessageStoragePolicy" -> Set(
      "allowedPersistenceRegions"
    ),
    "GooglePubSubSchemaSettings" -> Set(
      "encoding",
      "firstRevisionId",
      "lastRevisionId",
      "name"
    ),
    "GooglePubSubMessageBinding" -> Set(
      "attributes",
      "orderingKey",
      "schema",
      "bindingVersion"
    )
  )
}
