package amf.spec.raml

import amf.compiler.Root
import amf.document.Module
import amf.domain.Annotation.SourceVendor
import amf.domain.Annotations
import amf.parser.YNodeLikeOps
import amf.spec.ParserContext
import amf.spec.declaration.ReferencesParser
import org.yaml.model._

/**
  *
  */
case class RamlModuleParser(root: Root)(implicit val ctx: ParserContext) extends RamlSpecParser {

  def parseModule(): Module = {
    val module = Module(Annotations(root.document))
      .adopted(root.location)
      .add(SourceVendor(root.vendor))

    module.withLocation(root.location)

    root.document.toOption[YMap].foreach { rootMap =>
      val references = ReferencesParser("uses", rootMap, root.references).parse(root.location)

      parseDeclarations(root, rootMap)

      // TODO invoke when it's done
      //    resourceTypes?
      //      traits?
      //      securitySchemes?
      UsageParser(rootMap, module).parse()

      val declarables = ctx.declarations.declarables()
      if (declarables.nonEmpty) module.withDeclares(declarables)
      if (references.references.nonEmpty) module.withReferences(references.solvedReferences())
    }
    module

  }
}
