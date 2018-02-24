package amf.plugins.document.vocabularies2.emitters.dialects

import amf.core.annotations.{Aliases, LexicalInformation}
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.SpecOrdering.Lexical
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.vocabularies2.emitters.common.IdCounter
import amf.plugins.document.vocabularies2.metamodel.document.DialectModel
import amf.plugins.document.vocabularies2.model.document.Dialect
import amf.plugins.document.vocabularies2.model.domain.{NodeMapping, PropertyMapping}
import org.yaml.model.YDocument
import org.yaml.model.YDocument.EntryBuilder

trait AliasesConsumer {
  val aliases: Map[String,(String,String)]
  def aliasFor(id: String): Option[String] = {
    aliases.keySet.find(id.contains(_)) map { key =>
      val alias = aliases(key)._1
      val postfix = id.split(key).last
      alias + "." + postfix
    }
  }
}

case class PropertyMappingEmitter(propertyMapping: PropertyMapping, ordering: SpecOrdering, aliases: Map[String,(String, String)]) extends EntryEmitter with AliasesConsumer {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(propertyMapping.name(), _.obj { b =>
      aliasFor(propertyMapping.nodePropertyMapping()) match {
        case Some(propertyTermAlias) => MapEntryEmitter("propertyTerm", propertyTermAlias).emit(b)
        case None                 => propertyMapping.nodePropertyMapping()
      }
    })
  }

  override def position(): Position = propertyMapping.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
}


case class NodeMappingEmitter(nodeMapping: NodeMapping, ordering: SpecOrdering, aliases: Map[String,(String, String)]) extends EntryEmitter with AliasesConsumer {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(nodeMapping.name, _.obj { b =>
      aliasFor(nodeMapping.nodetypeMapping) match {
        case Some(classTermAlias) => MapEntryEmitter("classTerm", classTermAlias).emit(b)
        case None                 => nodeMapping.nodetypeMapping
      }
    })
    b.entry("mapping", _.obj { b =>
      val propertiesEmitters: Seq[PropertyMappingEmitter] = nodeMapping.propertiesMapping().map { pm: PropertyMapping =>
        PropertyMappingEmitter(pm, ordering, aliases)
      }
      traverse(propertiesEmitters, b)
    })
  }

  override def position(): Position = nodeMapping.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
}

case class ReferencesEmitter(references: Seq[BaseUnit], ordering: SpecOrdering, aliases: Map[String,(String, String)]) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val modules = references.collect({ case m: DeclaresModel => m })
    if (modules.nonEmpty) {

      b.entry("uses", _.obj { b =>
        traverse(ordering.sorted(modules.map(r => ReferenceEmitter(r, ordering, aliases))), b)
      })
    }
  }

  override def position(): Position = ZERO
}

case class ReferenceEmitter(reference: BaseUnit, ordering: SpecOrdering, aliases: Map[String,(String, String)])
  extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    aliases.get(reference.id) match {
      case Some((alias, location)) =>
        MapEntryEmitter(alias, location).emit(b)
      case _ => // TODO: emit violation
    }
  }

  override def position(): Position = ZERO
}


case class RamlDialectEmitter(dialect: Dialect) {

  val ordering: SpecOrdering = Lexical
  val aliases = collectAliases()

  def emitDialect(): YDocument = {
    val content: Seq[EntryEmitter] = rootLevelEmitters(ordering) ++ dialectEmitters(ordering)

    YDocument(b => {
      b.comment("%RAML 1.0 Dialect")
      b.obj { b =>
        traverse(ordering.sorted(content), b)
      }
    })
  }

  def collectAliases(): Map[String,(String, String)] = {
    val vocabFile = dialect.location.split("/").last
    val vocabFilePrefix = dialect.location.replace(vocabFile, "")

    val maps = dialect.annotations.find(classOf[Aliases]).map { aliases =>
      aliases.aliases.foldLeft(Map[String,String]()) { case (acc, (alias, id)) =>
        acc + (id -> alias)
      }
    }.getOrElse(Map())
    val idCounter = new IdCounter()
    dialect.references.foldLeft(Map[String,(String,String)]()) {
      case (acc: Map[String,(String,String)], m: DeclaresModel) =>
        val importLocation: String = if (m.location.contains(vocabFilePrefix)) {
          m.location.replace(vocabFilePrefix, "")
        } else {
          m.location.replace("file://", "")
        }

        if (maps.get(m.id).isDefined) {
          val alias = maps(m.id)
          acc + (m.id -> (alias, importLocation))
        } else {
          val nextAlias = idCounter.genId("uses_")
          acc + (m.id -> (nextAlias, importLocation))
        }
      case (acc: Map[String,(String,String)], _) => acc
    }
  }

  def rootLevelEmitters(ordering: SpecOrdering) =
    Seq(ReferencesEmitter(dialect.references, ordering, aliases)) ++
    nodeMappingDeclarationEmitters(dialect, ordering, aliases)

  def dialectEmitters(ordering: SpecOrdering) =
    dialectPropertiesEmitter(ordering)


  def dialectPropertiesEmitter(ordering: SpecOrdering) = {
    var emitters: Seq[EntryEmitter] = Nil

    emitters ++= Seq(new EntryEmitter {
      override def emit(b: YDocument.EntryBuilder): Unit = {
        MapEntryEmitter("dialect", dialect.name()).emit(b)
      }

      override def position(): Position =
        dialect.fields.entry(DialectModel.Name).get.value.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)

    })

    emitters ++= Seq(new EntryEmitter {
      override def emit(b: YDocument.EntryBuilder): Unit = MapEntryEmitter("version", dialect.version()).emit(b)

      override def position(): Position =
        dialect.fields.entry(DialectModel.Version).get.value.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
    })

    if (Option(dialect.usage).isDefined) {
      emitters ++= Seq(new EntryEmitter {
        override def emit(b: YDocument.EntryBuilder): Unit = MapEntryEmitter("usage", dialect.usage).emit(b)

        override def position(): Position =
          dialect.fields.entry(DialectModel.Usage).get.value.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
      })
    }

    emitters
  }

  def nodeMappingDeclarationEmitters(dialect: Dialect, ordering: SpecOrdering, aliases: Map[String, (String, String)]): Seq[EntryEmitter] = {
    val nodeMappingDeclarations = dialect.declares.collect { case nm : NodeMapping => nm }
    if (nodeMappingDeclarations.nonEmpty) {
      Seq(new EntryEmitter {
        override def emit(b: EntryBuilder): Unit = {
          b.entry("nodeMappings", _.obj { b =>
            val nodeMappingEmitters: Seq[EntryEmitter]= nodeMappingDeclarations.map { n => NodeMappingEmitter(n, ordering, aliases) }
            traverse(ordering.sorted(nodeMappingEmitters), b)
          })
        }

        override def position(): Position = {
          nodeMappingDeclarations.map(_.annotations.find(classOf[LexicalInformation])
            .map(_.range.start).getOrElse(ZERO))
            .filter(_ != ZERO)
            .sorted
            .headOption.getOrElse(ZERO)
        }
      })
    } else {
      Nil
    }
  }
}
