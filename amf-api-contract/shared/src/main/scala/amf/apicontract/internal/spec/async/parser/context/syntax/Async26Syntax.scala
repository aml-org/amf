package amf.apicontract.internal.spec.async.parser.context.syntax

import amf.shapes.internal.spec.common.parser.SyntaxHelper._
import amf.apicontract.internal.spec.async.parser.bindings.Bindings.Pulsar
import amf.shapes.internal.spec.common.parser.SpecSyntax

object Async26Syntax extends SpecSyntax {
  override val nodes: Map[String, Set[String]] =
    add(
      Async25Syntax.nodes,
      "bindings" -> Set(Pulsar),
      "PulsarServerBinding" -> Set(
        "tenant",
        "bindingVersion"
      ),
      "PulsarChannelBinding" -> Set(
        "namespace",
        "persistence",
        "compaction",
        "geo-replication",
        "retention",
        "ttl",
        "deduplication",
        "bindingVersion"
      ),
      "PulsarChannelRetention" -> Set(
        "time",
        "size"
      )
    )
}
