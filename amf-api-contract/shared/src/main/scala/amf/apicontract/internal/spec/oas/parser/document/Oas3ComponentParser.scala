package amf.apicontract.internal.spec.oas.parser.document

import amf.apicontract.client.scala.model.document.{APIContractProcessingData, ComponentModule}
import amf.apicontract.internal.metamodel.document.ComponentModuleModel
import amf.apicontract.internal.spec.common.parser.ReferencesParser
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.document.BaseUnitModel
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.{Root, YMapOps, YNodeLikeOps}
import amf.core.internal.remote.Spec
import amf.core.internal.utils._
import org.yaml.model.{YMap, YMapEntry}

case class Oas3ComponentParser(root: Root)(implicit val ctx: OasWebApiContext) extends OasSpecParser() {

  def parseComponent(): ComponentModule = {

    val module = ComponentModule(Annotations(root.parsed.asInstanceOf[SyamlParsedDocument].document))
      .withLocation(root.location)
      .withProcessingData(APIContractProcessingData().withSourceSpec(Spec.OAS30))
    module.set(BaseUnitModel.Location, root.location)

    root.parsed.asInstanceOf[SyamlParsedDocument].document.toOption[YMap].foreach { rootMap =>
      val references = ReferencesParser(module, root.location, "uses".asOasExtension, rootMap, root.references).parse()

      Oas3DocumentParser(root).parseDeclarations(root, rootMap, module)
      addDeclarationsToModel(module)

      if (references.nonEmpty) module.withReferences(references.baseUnitReferences())

      parseRootFields(rootMap, module)
    }

    module

  }

  private def parseRootFields(map: YMap, module: ComponentModule): Unit = {

    map.key("info", entry => parseInfoFields(entry, module))

  }

  private def parseInfoFields(entry: YMapEntry, module: ComponentModule): Unit = {

    val info = entry.value.as[YMap]

    info.key("title") match {
      case Some(title) => setScalarField(ComponentModuleModel.Name, title, title.value.toString, module)
      case None        => // TODO show error
    }
    info.key("version") match {
      case Some(version) => setScalarField(ComponentModuleModel.Version, version, version.value.toString, module)
      case None          => // TODO show error
    }
  }

  private def setScalarField(field: Field, entry: YMapEntry, value: Any, module: ComponentModule): Unit =
    module.set(field, AmfScalar(value, Annotations(entry.value)), Annotations(entry))

}
