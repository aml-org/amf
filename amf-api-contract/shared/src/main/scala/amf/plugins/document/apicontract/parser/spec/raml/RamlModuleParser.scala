package amf.plugins.document.apicontract.parser.spec.raml

import amf.core.client.scala.model.document.Module
import amf.core.client.scala.model.domain.AmfArray
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.annotations.SourceVendor
import amf.core.internal.metamodel.document.ModuleModel
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.{Root, YNodeLikeOps}
import amf.shapes.internal.spec.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.apicontract.parser.RamlWebApiContextType.LIBRARY
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.common.AnnotationParser
import amf.plugins.document.apicontract.parser.spec.declaration.ReferencesParser
import amf.plugins.document.apicontract.parser.spec.raml.RamlAnnotationTargets.targetsFor
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
