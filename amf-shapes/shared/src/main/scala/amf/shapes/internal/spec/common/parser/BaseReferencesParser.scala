package amf.shapes.internal.spec.common.parser

import amf.core.client.scala.model.document.{BaseUnit, Fragment}
import amf.core.client.scala.parse.document._
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import org.yaml.model.YMap

case class BaseReferencesParser(
    baseUnit: BaseUnit,
    rootLoc: String,
    key: String,
    map: YMap,
    references: Seq[ParsedReference],
    register: ReferencesRegister
)(implicit ctx: ShapeParserContext)
    extends CommonReferencesParser(references, register) {

  override protected def parseLibraries(declarations: ReferenceCollector[BaseUnit]): Unit = Unit

}

class BaseReferencesRegister()(implicit ctx: ShapeParserContext) extends ReferencesRegister {
  override def onCollect(alias: String, unit: BaseUnit): Unit = {
    unit match {
      case fragment: Fragment => ctx.declarations += (alias, fragment)
      case jsonDoc: JsonSchemaDocument =>
        ctx.declarations.documentFragments += (alias -> (jsonDoc.encodes -> buildDeclarationMap(jsonDoc)))
      case _ => // ignore
    }
  }
}
