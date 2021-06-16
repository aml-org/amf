package amf.apicontract.internal.spec.raml.parser.document

import amf.apicontract.internal.spec.common.parser.{ReferencesParser, WebApiShapeParserContextAdapter}
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.core.client.scala.model.document.Module
import amf.core.client.scala.model.domain.AmfArray
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.annotations.SourceVendor
import amf.core.internal.metamodel.document.ModuleModel
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.YMap

/**
  *
  */
case class RamlModuleParser(root: Root)(implicit override val ctx: RamlWebApiContext) extends Raml10BaseSpecParser {

  def parseModule(): Module = {
    val module = Module(Annotations(root.parsed.asInstanceOf[SyamlParsedDocument].document))
      .withLocation(root.location)
      .adopted(root.location)
      .add(SourceVendor(ctx.vendor))

    root.parsed.asInstanceOf[SyamlParsedDocument].document.toOption[YMap].foreach { rootMap =>
      ctx.closedShape(module.id, rootMap, "module")

      val references = ReferencesParser(module, root.location, "uses", rootMap, root.references).parse()

      parseDeclarations(root, rootMap)
      UsageParser(rootMap, module).parse()

      addDeclarationsToModel(module)
      if (references.nonEmpty)
        module
          .setWithoutId(ModuleModel.References, AmfArray(references.baseUnitReferences()), Annotations.synthesized())

      AnnotationParser(module, rootMap, targetsFor(LIBRARY))(WebApiShapeParserContextAdapter(ctx)).parse()
    }

    ctx.futureDeclarations.resolve()

    module

  }
}
