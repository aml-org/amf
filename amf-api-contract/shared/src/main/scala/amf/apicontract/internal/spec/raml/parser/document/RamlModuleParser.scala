package amf.apicontract.internal.spec.raml.parser.document

import amf.apicontract.client.scala.model.document.APIContractProcessingData
import amf.apicontract.internal.spec.common.parser.ReferencesParser
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.raml.parser.document.RamlAnnotationTargets.targetsFor
import amf.core.client.scala.model.document.Module
import amf.core.client.scala.model.domain.AmfArray
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.metamodel.document.ModuleModel
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.{Root, YNodeLikeOps}
import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.raml.parser.RamlWebApiContextType.LIBRARY
import amf.shapes.internal.spec.common.parser.AnnotationParser
import org.yaml.model.YMap

/** */
case class RamlModuleParser(root: Root, spec: Spec)(implicit override val ctx: RamlWebApiContext)
    extends Raml10BaseSpecParser {

  def parseModule(): Module = {
    val module = Module(Annotations(root.parsed.asInstanceOf[SyamlParsedDocument].document))
      .withLocation(root.location)
      .withProcessingData(APIContractProcessingData().withSourceSpec(spec))

    root.parsed.asInstanceOf[SyamlParsedDocument].document.toOption[YMap].foreach { rootMap =>
      ctx.closedShape(module, rootMap, "module")

      val references = ReferencesParser(module, root.location, "uses", rootMap, root.references).parse()

      parseDeclarations(root, rootMap)
      UsageParser(rootMap, module).parse()

      addDeclarationsToModel(module)
      if (references.nonEmpty)
        module
          .setWithoutId(ModuleModel.References, AmfArray(references.baseUnitReferences()), Annotations.synthesized())

      AnnotationParser(module, rootMap, targetsFor(LIBRARY)).parse()
    }

    ctx.futureDeclarations.resolve()

    module

  }
}
