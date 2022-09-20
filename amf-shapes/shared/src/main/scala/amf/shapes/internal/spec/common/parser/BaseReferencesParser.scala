package amf.shapes.internal.spec.common.parser

import amf.core.client.scala.model.document.{BaseUnit, Fragment}
import amf.core.client.scala.parse.document._
import amf.core.internal.parser.Root
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

object BaseReferencesParser {

  def apply(baseUnit: BaseUnit, root: Root, key: String)(implicit ctx: ShapeParserContext): BaseReferencesParser = {
    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    new BaseReferencesParser(baseUnit, root.location, key, map, root.references, BaseReferencesRegister())
  }

  def apply(baseUnit: BaseUnit, rootLoc: String, key: String, map: YMap, references: Seq[ParsedReference])(implicit
      ctx: ShapeParserContext
  ): BaseReferencesParser = {
    new BaseReferencesParser(baseUnit, rootLoc, key, map, references, BaseReferencesRegister())
  }
}

case class BaseReferencesRegister()(implicit ctx: ShapeParserContext) extends ReferencesRegister {
  override def onCollect(alias: String, unit: BaseUnit): Unit = {
    unit match {
      case fragment: Fragment => ctx.declarations += (alias, fragment)
      case jsonDoc: JsonSchemaDocument =>
        ctx.declarations.documentFragments += (alias -> (jsonDoc.encodes -> buildDeclarationMap(jsonDoc)))
      case _ => // ignore
    }
  }
}
