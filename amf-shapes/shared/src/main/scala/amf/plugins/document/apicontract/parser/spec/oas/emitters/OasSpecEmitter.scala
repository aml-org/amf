package amf.plugins.document.apicontract.parser.spec.oas.emitters

import amf.core.client.common.position.Position
import amf.core.client.common.position.Position.ZERO
import amf.core.client.scala.model.document.{BaseUnit, Module}
import amf.core.internal.annotations.Aliases
import amf.core.internal.render.BaseEmitters.{MapEntryEmitter, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.{AmfStrings, IdCounter}
import org.yaml.model.YDocument.EntryBuilder

class OasSpecEmitter {

  case class ReferencesEmitter(baseUnit: BaseUnit, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val aliases    = baseUnit.annotations.find(classOf[Aliases]).getOrElse(Aliases(Set()))
      val references = baseUnit.references
      val modules    = references.collect({ case m: Module => m })
      if (modules.nonEmpty) {
        var modulesEmitted = Map[String, Module]()
        val idCounter      = new IdCounter()
        val aliasesEmitters: Seq[Option[EntryEmitter]] = aliases.aliases.map {
          case (alias, (fullUrl, localUrl)) =>
            modules.find(_.id == fullUrl) match {
              case Some(module) =>
                modulesEmitted += (module.id -> module)
                Some(
                  ReferenceEmitter(module,
                                   Some(Aliases(Set(alias -> (fullUrl, localUrl)))),
                                   ordering,
                                   () => idCounter.genId("uses")))
              case _ => None
            }
        }.toSeq
        val missingModuleEmitters = modules.filter(m => modulesEmitted.get(m.id).isEmpty).map { module =>
          Some(ReferenceEmitter(module, Some(Aliases(Set())), ordering, () => idCounter.genId("uses")))
        }
        val finalEmitters = (aliasesEmitters ++ missingModuleEmitters).collect { case Some(e) => e }
        b.entry("uses".asOasExtension, _.obj { b =>
          traverse(ordering.sorted(finalEmitters), b)
        })
      }
    }

    override def position(): Position = ZERO
  }

  case class ReferenceEmitter(reference: BaseUnit,
                              aliases: Option[Aliases],
                              ordering: SpecOrdering,
                              aliasGenerator: () => String)
      extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      val aliasesMap = aliases.getOrElse(Aliases(Set())).aliases
      val effectiveAlias = aliasesMap.find { case (_, (f, _)) => f == reference.id } map { case (a, (_, r)) => (a, r) } getOrElse {
        (aliasGenerator(), name)
      }
      MapEntryEmitter(effectiveAlias._1, effectiveAlias._2).emit(b)
    }

    private def name: String = reference.location().getOrElse(reference.id)

    override def position(): Position = ZERO
  }
}
