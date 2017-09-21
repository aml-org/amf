package amf.spec.oas

import amf.compiler.Root
import amf.document.Module
import amf.domain.Annotation.SourceVendor
import amf.domain.Annotations
import amf.metadata.document.{BaseUnitModel, ModuleModel}
import amf.model.AmfArray
import amf.parser.YValueOps

/**
  *
  */
case class OasModuleParser(root: Root) extends OasSpecParser(root) {

  def parseModule(): Module = {
    val module = Module(Annotations(root.document))
      .adopted(root.location)
      .add(SourceVendor(root.vendor))
    module.set(BaseUnitModel.Location, root.location)

    root.document.value.foreach(value => {
      val rootMap = value.toMap

      val enviromentRef = ReferencesParser(rootMap, root.references).parse()

      val declares = parseDeclares(rootMap)

      // TODO invoke when it's done
      //    resourceTypes?
      //      traits?
      //      securitySchemes?
      UsageParser(rootMap, module).parse()

      if (enviromentRef.nonEmpty) module.set(BaseUnitModel.References, AmfArray(enviromentRef.values.toSeq))
      if (declares.nonEmpty) module.set(ModuleModel.Declares, AmfArray(declares))
    })

    module
  }
}
