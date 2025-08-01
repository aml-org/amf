package amf.shapes.internal.plugins.parser.entry

import org.yaml.model.{YMap, YNode, YScalar}

trait IdEntryVersion {

  def parseIdVersion(ast: YNode): Option[IdVersion] =
    parseVersionEntry(ast)
      .map(_.value)
      .collect { case scalar: YScalar => scalar.text }
      .flatMap(getIdVersionFromString)

  private def parseVersionEntry(ast: YNode): Option[YNode] = {
    ast.value match {
      case map: YMap => map.map.get(idKey)
      case _         => None
    }
  }

  protected def getIdVersionFromString(text: String): Option[IdVersion]

  protected val idKey: String
}

abstract class IdVersion(val version: String)
