package amf.spec.raml

import amf.compiler.RamlHeader
import amf.document.Module
import amf.metadata.document.BaseUnitModel
import amf.remote.Raml
import amf.spec.SpecOrdering
import org.yaml.model.YDocument

/**
  *
  */
case class RamlModuleEmitter(module: Module) extends RamlSpecEmitter {

  def emitModule(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Raml, module.annotations)

    // TODO ordering??
    val declares         = DeclarationsEmitter(module.declares, ordering).emitters
    val referenceEmitter = Seq(ReferencesEmitter(module.references, ordering))

    val usageEmitter: Option[ValueEmitter] =
      module.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("usage", f))

    // TODO invoke traits end resource types

    emitter.document({ () =>
      comment(RamlHeader.Raml10Library.text)
      map { () =>
        traverse(ordering.sorted(declares ++ usageEmitter ++ referenceEmitter))
      }
    })
  }

}
