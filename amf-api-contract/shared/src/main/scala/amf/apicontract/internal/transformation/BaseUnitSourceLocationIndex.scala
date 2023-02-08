package amf.apicontract.internal.transformation

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.annotations.SourceLocation

/** Indexes Base Units by their SourceLocation
  */
sealed case class BaseUnitSourceLocationIndex(index: Map[String, BaseUnit]) {
  def get(sourceLocation: String): Option[BaseUnit] = index.get(sourceLocation)
}

object BaseUnitSourceLocationIndex {
  def build(root: BaseUnit): BaseUnitSourceLocationIndex = {
    val index = flattenReferencesTree(root)
      .flatMap(bu =>
        bu.annotations
          .find(classOf[SourceLocation])
          .map(annotation => annotation.location -> bu)
      )
      .toMap
    BaseUnitSourceLocationIndex(index)
  }

  private def flattenReferencesTree(root: BaseUnit): Seq[BaseUnit] =
    root +: (root.references ++ root.references.flatMap(_.references))

}
