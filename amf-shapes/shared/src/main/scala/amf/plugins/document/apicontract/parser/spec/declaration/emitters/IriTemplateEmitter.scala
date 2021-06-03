package amf.plugins.document.apicontract.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.AmfObject
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.domain.apicontract.metamodel.IriTemplateMappingModel
import org.yaml.model.YDocument.EntryBuilder

// TODO: is this for OAS only?
case class IriTemplateEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.obj { b =>
        val emitters = f
          .arrayValues[AmfObject]
          .flatMap(iriMapping => {
            for {
              variable <- iriMapping.fields.entry(IriTemplateMappingModel.TemplateVariable)
              link     <- iriMapping.fields.entry(IriTemplateMappingModel.LinkExpression)
            } yield {
              ValueEmitter(variable.scalar.toString, link)
            }
          })
        traverse(ordering.sorted(emitters), b)
      }
    )
  }

  override def position(): Position = pos(f.value.annotations)
}
