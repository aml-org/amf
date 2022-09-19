package amf.apicontract.internal.spec.oas.parser.document

import amf.apicontract.client.scala.model.document.{APIContractProcessingData, ComponentModule}
import amf.apicontract.internal.metamodel.document.ComponentModuleModel
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.spec.common.parser.{WebApiContext, WebApiLikeReferencesParser}
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{
  MandatoryEmptyPaths,
  MandatoryPathsProperty,
  MandatoryProperty
}
import amf.core.client.scala.model.domain.{AmfObject, AmfScalar}
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
      val references =
        WebApiLikeReferencesParser(module, root.location, "uses".asOasExtension, rootMap, root.references).parse()

      Oas3DocumentParser(root).parseDeclarations(root, rootMap, module)
      addDeclarationsToModel(module)

      if (references.nonEmpty) module.withReferences(references.baseUnitReferences())
      parseRootFields(rootMap, module)
      assertPaths(rootMap, module)
      ctx.closedShape(module, rootMap, "root")
    }
    module

  }

  private def parseRootFields(map: YMap, module: ComponentModule): Unit = {
    map.key("info") match {
      case Some(entry) => parseInfoFields(entry, module)
      case None        => mandatoryInfo(module)
    }
  }

  private def parseInfoFields(entry: YMapEntry, module: ComponentModule): Unit = {

    val info = entry.value.as[YMap]

    info.key("title") match {
      case Some(entry) => (ComponentModuleModel.Name in module)(entry)
      case None        => mandatoryTitle(module)
    }
    info.key("version") match {
      case Some(entry) => (ComponentModuleModel.Version in module)(entry)
      case None        => mandatoryVersion(module)
    }

    ctx.closedShape(module, info, "info")
  }

  private def assertPaths(map: YMap, module: ComponentModule): Unit = {
    map.key("paths") match {
      case Some(entry: YMapEntry) =>
        val hasEntries = entry.value.as[YMap].entries.nonEmpty
        if (hasEntries) mandatoryEmptyPathsError(module)
      case _ => mandatoryPathsError(module)
    }
  }

  private def mandatoryEmptyPathsError(element: AmfObject)(implicit ctx: WebApiContext): Unit = {
    ctx.eh.violation(MandatoryEmptyPaths, element, "'paths' must be an empty object")
  }

  private def mandatoryInfo(element: AmfObject)(implicit ctx: WebApiContext): Unit = {
    ctx.eh.violation(MandatoryProperty, element, "'info' node is mandatory")
  }

  private def mandatoryTitle(element: AmfObject)(implicit ctx: WebApiContext): Unit = {
    ctx.eh.violation(MandatoryProperty, element, "'title' node is mandatory")
  }

  private def mandatoryVersion(element: AmfObject)(implicit ctx: WebApiContext): Unit = {
    ctx.eh.violation(MandatoryProperty, element, "'version' node is mandatory")
  }
}
