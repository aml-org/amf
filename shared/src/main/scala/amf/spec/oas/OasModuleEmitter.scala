package amf.spec.oas

import amf.document.Module
import amf.metadata.document.BaseUnitModel
import amf.remote.Oas
import amf.spec.SpecOrdering
import org.yaml.model.YDocument

/**
  *
  */
case class OasModuleEmitter(module: Module) extends OasSpecEmitter {

  def emitModule(): YDocument = {

    val ordering: SpecOrdering = SpecOrdering.ordering(Oas, module.annotations)

    // TODO ordering??
    val declares         = DeclarationsEmitter(module.declares, ordering).emitters
    val referenceEmitter = Seq(ReferencesEmitter(module.references, ordering))

    val usageEmitter: Option[ValueEmitter] =
      module.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("x-usage", f))

    // TODO invoke traits end resource types

    emitter.document({ () =>
      map { () =>
        entry { () =>
          raw("swagger")
          raw("2.0")
        }
        traverse(ordering.sorted(declares ++ usageEmitter ++ referenceEmitter))
      }
    })
  }

}
