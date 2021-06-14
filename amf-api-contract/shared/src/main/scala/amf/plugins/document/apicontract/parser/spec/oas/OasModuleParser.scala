package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.client.scala.model.document.Module
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.annotations.SourceVendor
import amf.core.internal.metamodel.document.BaseUnitModel
import amf.core.internal.parser.{Root, YNodeLikeOps}
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.AmfStrings
import amf.plugins.document.apicontract.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.declaration.ReferencesParser
import org.yaml.model._

/**
  *
  */
case class OasModuleParser(root: Root)(implicit val ctx: OasWebApiContext)
    extends OasSpecParser()(WebApiShapeParserContextAdapter(ctx)) {

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
      UsageParser(rootMap, module).parse()

      addDeclarationsToModel(module)
      if (references.nonEmpty) module.withReferences(references.baseUnitReferences())
    }

    module
  }
}
