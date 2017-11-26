package amf.plugins.document.webapi.parser.spec.oas

import amf.core.Root
import amf.core.annotations.SourceVendor
import amf.core.metamodel.document.BaseUnitModel
import amf.core.model.document.Module
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.ReferencesParser
import org.yaml.model._

/**
  *
  */
case class OasModuleParser(root: Root)(implicit val ctx: WebApiContext) extends OasSpecParser {

  def parseModule(): Module = {
    val sourceVendor = SourceVendor(root.vendor)
    val module = Module(Annotations(root.parsed.document))
      .adopted(root.location)
      .add(sourceVendor)
    module.set(BaseUnitModel.Location, root.location)

    root.parsed.document.toOption[YMap].foreach { rootMap =>
      val references = ReferencesParser("x-uses", rootMap, root.references).parse(root.location)

      parseDeclarations(root, rootMap)

      UsageParser(rootMap, module).parse()

      val declarable = ctx.declarations.declarables()
      if (declarable.nonEmpty) module.withDeclares(declarable)
      if (references.references.nonEmpty) module.withReferences(references.solvedReferences())
    }

    module
  }
}
