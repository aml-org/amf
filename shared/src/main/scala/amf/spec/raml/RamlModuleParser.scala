package amf.spec.raml

import amf.compiler.Root
import amf.document.Module
import amf.domain.Annotation.SourceVendor
import amf.domain.Annotations
import amf.parser.YValueOps
import amf.spec.ParserContext
import amf.spec.declaration.ReferencesParser

/**
  *
  */
case class RamlModuleParser(root: Root)(implicit val ctx: ParserContext) extends RamlSpecParser {

  def parseModule(): Module = {
    val module = Module(Annotations(root.document))
      .adopted(root.location)
      .add(SourceVendor(root.vendor))

    module.withLocation(root.location)

    root.document.value.foreach(document => {

      val rootMap    = document.toMap
      val references = ReferencesParser("uses", rootMap, root.references).parse()

      parseDeclarations(root, rootMap, references.declarations)

      // TODO invoke when it's done
      //    resourceTypes?
      //      traits?
      //      securitySchemes?
      UsageParser(rootMap, module).parse()

      val declarables = references.declarations.declarables()
      if (declarables.nonEmpty) module.withDeclares(declarables)
      if (references.references.nonEmpty) module.withReferences(references.solvedReferences())
    })
    module

  }
}
