package amf.plugins.document.webapi.parser.spec.oas

import amf.core.Root
import amf.core.annotations.SourceVendor
import amf.core.metamodel.document.BaseUnitModel
import amf.core.model.document.Module
import amf.core.parser.{Annotations, _}
import amf.core.utils.AmfStrings
import amf.plugins.document.webapi.annotations.DeclarationKeys
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.ReferencesParser
import org.yaml.model._

/**
  *
  */
case class OasModuleParser(root: Root)(implicit val ctx: OasWebApiContext) extends OasSpecParser {

  def parseModule(): Module = {
    val sourceVendor = SourceVendor(ctx.vendor)
    val module = Module(Annotations(root.parsed.asInstanceOf[SyamlParsedDocument].document))
      .withLocation(root.location)
      .adopted(root.location)
      .add(sourceVendor)
    module.set(BaseUnitModel.Location, root.location)

    root.parsed.asInstanceOf[SyamlParsedDocument].document.toOption[YMap].foreach { rootMap =>
      val references = ReferencesParser(module, root.location, "uses".asOasExtension, rootMap, root.references).parse()

      Oas2DocumentParser(root).parseDeclarations(root, rootMap)
      val declarationKeys = ctx.getDeclarationKeys
      if (declarationKeys.nonEmpty) module.add(DeclarationKeys(declarationKeys))

      UsageParser(rootMap, module).parse()

      val declarable = ctx.declarations.declarables()
      if (declarable.nonEmpty) module.withDeclares(declarable)
      if (references.references.nonEmpty) module.withReferences(references.solvedReferences())
    }

    module
  }
}
