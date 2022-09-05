package amf.shapes.internal.spec.common.parser

import amf.core.client.scala.model.document.{BaseUnit, Document, Fragment, Module}
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.parse.document._
import amf.shapes.client.scala.model.document.JsonSchemaDocument

abstract class CommonReferencesParser(references: Seq[ParsedReference], register: ReferencesRegister)(implicit
    ctx: ShapeParserContext
) {

  def parse(): ReferenceCollector[BaseUnit] = {
    val result = CallbackReferenceCollector(register)
    parseLibraries(result)
    references.foreach {
      case ParsedReference(f: Fragment, origin: Reference, _) => result += (origin.url, f)
      case ParsedReference(d: Document, origin: Reference, _) => result += (origin.url, d)
      case ParsedReference(m: Module, origin: Reference, _)   => result += (origin.url, m)
      case _                                                  => // Nothing
    }
    result
  }

  protected def parseLibraries(declarations: ReferenceCollector[BaseUnit]): Unit
}

abstract class ReferencesRegister()(implicit ctx: ShapeParserContext) extends CollectionSideEffect[BaseUnit] {

  override def onCollect(alias: String, unit: BaseUnit): Unit

  protected def buildDeclarationMap(document: JsonSchemaDocument): Map[String, Shape] = {
    document.declares.map(shape => shape.asInstanceOf[Shape].name.value() -> shape.asInstanceOf[Shape]).toMap
  }
}
