package amf.spec.raml

import amf.compiler.Root
import amf.document.Module
import amf.domain.Annotation.SourceVendor
import amf.domain.Annotations
import amf.metadata.document.{BaseUnitModel, ModuleModel}
import amf.model.AmfArray
import amf.parser.{YMapOps, YValueOps}

/**
  *
  */
case class RamlModuleParser(override val root: Root) extends RamlSpecParser(root) {

  def parseModule(): Module = {
    val module = Module(Annotations(root.document))
      .adopted(root.location)
      .add(SourceVendor(root.vendor))
    module.set(BaseUnitModel.Location, root.location)

    root.document.value.foreach(document => {

      val rootMap       = document.toMap
      val enviromentRef = ReferencesParser(rootMap, root.references).parse()

      val declares = parseDeclares(rootMap)

      // TODO invoke when it's done
      //    resourceTypes?
      //      traits?
      //      securitySchemes?
      UsageParser(rootMap, module).parse()

      if (declares.nonEmpty) module.set(ModuleModel.Declares, AmfArray(declares))
      if (enviromentRef.nonEmpty) module.set(BaseUnitModel.References, AmfArray(enviromentRef.values.toSeq))
    })
    module

  }
}
