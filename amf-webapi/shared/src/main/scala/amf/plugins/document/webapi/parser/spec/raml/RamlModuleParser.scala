package amf.plugins.document.webapi.parser.spec.raml

import amf.core.Root
import amf.core.annotations.SourceVendor
import amf.core.model.document.Module
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.ReferencesParser
import org.yaml.model._

/**
  *
  */
case class RamlModuleParser(root: Root)(implicit override val ctx: RamlWebApiContext) extends Raml10BaseSpecParser {

  def parseModule(): Module = {
    val module = Module(Annotations(root.parsed.asInstanceOf[SyamlParsedDocument].document))
      .withLocation(root.location)
      .adopted(root.location)
      .add(SourceVendor(ctx.vendor))

    module.withLocation(root.location)

    root.parsed.asInstanceOf[SyamlParsedDocument].document.toOption[YMap].foreach { rootMap =>
      ctx.closedShape(module.id, rootMap, "module")

      val references = ReferencesParser(module, "uses", rootMap, root.references).parse(root.location)

      parseDeclarations(root, rootMap)

      UsageParser(rootMap, module).parse()

      val declarables = ctx.declarations.declarables()
      if (declarables.nonEmpty) module.withDeclares(declarables)
      if (references.references.nonEmpty) module.withReferences(references.solvedReferences())
    }

    ctx.futureDeclarations.resolve()

    module

  }
}
