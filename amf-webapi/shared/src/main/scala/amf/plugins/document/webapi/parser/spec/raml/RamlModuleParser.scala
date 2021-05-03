package amf.plugins.document.webapi.parser.spec.raml

import amf.core.Root
import amf.core.annotations.SourceVendor
import amf.core.metamodel.document.ModuleModel
import amf.core.model.document.Module
import amf.core.model.domain.AmfArray
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.RamlWebApiContextType.LIBRARY
import amf.plugins.document.webapi.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.document.webapi.parser.spec.declaration.ReferencesParser
import amf.plugins.document.webapi.parser.spec.raml.RamlAnnotationTargets.targetsFor
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
