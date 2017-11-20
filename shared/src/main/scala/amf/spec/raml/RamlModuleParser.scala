package amf.spec.raml

import amf.compiler.Root
import amf.document.Module
import amf.domain.Annotation.SourceVendor
import amf.domain.Annotations
import amf.parser.YNodeLikeOps
import amf.plugins.domain.webapi.contexts.WebApiContext
import amf.spec.declaration.ReferencesParser
import org.yaml.model._

/**
  *
  */
case class RamlModuleParser(root: Root)(implicit val ctx: WebApiContext) extends RamlSpecParser {

  def parseModule(): Module = {
    val module = Module(Annotations(root.document))
      .adopted(root.location)
      .add(SourceVendor(root.vendor))

    module.withLocation(root.location)

    root.document.toOption[YMap].foreach { rootMap =>
      val references = ReferencesParser("uses", rootMap, root.references).parse(root.location)

      parseDeclarations(root, rootMap)

      UsageParser(rootMap, module).parse()

      val declarables = ctx.declarations.declarables()
      if (declarables.nonEmpty) module.withDeclares(declarables)
      if (references.references.nonEmpty) module.withReferences(references.solvedReferences())
    }
    ctx.declarations.resolve()

    module

  }
}
