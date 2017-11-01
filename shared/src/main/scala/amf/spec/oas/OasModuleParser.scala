package amf.spec.oas

import amf.compiler.Root
import amf.document.Module
import amf.domain.Annotation.SourceVendor
import amf.domain.Annotations
import amf.metadata.document.BaseUnitModel
import amf.parser.YValueOps
import amf.spec.declaration.ReferencesParser
import amf.validation.Validation

/**
  *
  */
case class OasModuleParser(root: Root, currentValidation: Validation) extends OasSpecParser(currentValidation) {

  def parseModule(): Module = {
    val module = Module(Annotations(root.document))
      .adopted(root.location)
      .add(SourceVendor(root.vendor))
    module.set(BaseUnitModel.Location, root.location)

    root.document.value.foreach(value => {
      val rootMap = value.toMap

      val references = ReferencesParser("x-uses", rootMap, root.references).parse()

      parseDeclarations(root, rootMap, references.declarations)

      // TODO invoke when it's done
      //    resourceTypes?
      //      traits?
      //      securitySchemes?
      UsageParser(rootMap, module).parse()

      val declarable = references.declarations.declarables()
      if (declarable.nonEmpty) module.withDeclares(declarable)
      if (references.references.nonEmpty) module.withReferences(references.solvedReferences())
    })

    module
  }
}
