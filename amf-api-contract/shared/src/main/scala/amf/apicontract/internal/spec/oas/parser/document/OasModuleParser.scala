package amf.apicontract.internal.spec.oas.parser.document

import amf.apicontract.client.scala.model.document.APIContractProcessingData
import amf.apicontract.internal.spec.common.parser.WebApiLikeReferencesParser
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.client.scala.model.document.Module
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.metamodel.document.BaseUnitModel
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.{Root, YNodeLikeOps}
import amf.core.internal.remote.Spec
import amf.core.internal.utils._
import org.yaml.model.YMap

/** */
case class OasModuleParser(root: Root, spec: Spec)(implicit val ctx: OasWebApiContext) extends OasSpecParser() {

  def parseModule(): Module = {
    val module = Module(Annotations(root.parsed.asInstanceOf[SyamlParsedDocument].document))
      .withLocation(root.location)
      .withProcessingData(APIContractProcessingData().withSourceSpec(spec))
    module.set(BaseUnitModel.Location, root.location)

    root.parsed.asInstanceOf[SyamlParsedDocument].document.toOption[YMap].foreach { rootMap =>
      val references =
        WebApiLikeReferencesParser(module, root.location, "uses".asOasExtension, rootMap, root.references).parse()

      Oas2DocumentParser(root).parseDeclarations(root, rootMap, module)
      UsageParser(rootMap, module).parse()

      addDeclarationsToModel(module)
      if (references.nonEmpty) module.withReferences(references.baseUnitReferences())
    }

    module
  }
}
