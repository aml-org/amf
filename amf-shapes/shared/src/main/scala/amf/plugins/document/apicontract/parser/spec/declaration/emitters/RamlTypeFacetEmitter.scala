package amf.plugins.document.apicontract.parser.spec.declaration.emitters

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.MapEntryEmitter
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.domain.apicontract.annotations.TypePropertyLexicalInfo
import amf.plugins.domain.shapes.metamodel.NodeShapeModel
import amf.plugins.domain.shapes.models.UnionShape
import org.yaml.model.YType

object RamlTypeFacetEmitter {
  def apply(typeName: String, shape: Shape): Option[EntryEmitter] = {
    shape.fields.?(NodeShapeModel.Inherits) match {
      case None =>
        // If the type is union and anyOf is empty it isn't resolved and type will be emitted in the UnionEmitter
        if (typeName == "union" && shape.asInstanceOf[UnionShape].anyOf.nonEmpty) None
        else {
          shape.annotations.find(classOf[TypePropertyLexicalInfo]) match {
            case Some(lexicalInfo) =>
              Some(MapEntryEmitter("type", typeName, YType.Str, lexicalInfo.range.start))
            case _ => None
          }
        }
      case _ => None
    }
  }
}
