package amf.apicontract.internal.spec.common.parser

import amf.core.client.common.validation.SeverityLevels

trait CustomSyntax {
  val nodes: Map[String, SpecNode]

  def apply(shape: String): SpecNode   = nodes(shape)
  def contains(shape: String): Boolean = nodes.contains(shape)
}

case class SpecNode(
    requiredFields: Set[SpecField] = Set(),
    possibleFields: Set[String] = Set()
)

case class SpecField(name: String, severity: String = SeverityLevels.VIOLATION)
