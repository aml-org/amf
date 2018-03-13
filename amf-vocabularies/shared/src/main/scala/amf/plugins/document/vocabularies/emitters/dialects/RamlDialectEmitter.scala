package amf.plugins.document.vocabularies.emitters.dialects

import amf.core.annotations.{Aliases, LexicalInformation}
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.SpecOrdering.Lexical
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.{AmfScalar, DomainElement}
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies.annotations.AliasesLocation
import amf.plugins.document.vocabularies.emitters.common.{ExternalEmitter, IdCounter}
import amf.plugins.document.vocabularies.emitters.instances.DialectEmitterHelper
import amf.plugins.document.vocabularies.metamodel.document.DialectModel
import amf.plugins.document.vocabularies.metamodel.domain.{DocumentMappingModel, PropertyMappingModel}
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectFragment, DialectLibrary}
import amf.plugins.document.vocabularies.model.domain._
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YDocument, YNode, YType}

trait AliasesConsumer extends DialectEmitterHelper {
  val dialect: Dialect
  val aliases: Map[String,(String,String)]
  def aliasFor(id: String): Option[String] = {
    maybeFindNodeMappingById(id) match {
      case Some(nodeMapping: NodeMapping) => {
        if (id.startsWith(dialect.id)) {
          Some(nodeMapping.name)
        } else {
          aliases.keySet.find(id.contains(_)).map { key =>
            val alias = aliases(key)._1
            alias + "." + nodeMapping.name
          } orElse { Some(nodeMapping.name) }
        }
      }

      case _ =>
        if (id.startsWith(dialect.id)) {
          Some(id.split(dialect.id).last.replace("/declarations/", ""))
        } else {
          aliases.keySet.find(id.contains(_)) map { key =>
            val alias = aliases(key)._1
            val postfix = id.split(key).last match {
              case id if id.contains("/declarations/") => id.replace("/declarations/", "")
              case nonLibraryDeclaration => nonLibraryDeclaration
            }
            alias + "." + postfix
          }
        }
    }
  }
}

trait PosExtractor {
  def fieldPos(element: DomainElement, field: Field) = {
    element.fields.entry(field).map {
      _.value.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
    }.getOrElse(ZERO)
  }
}

case class LibraryDocumentModelEmitter(dialect: Dialect, ordering: SpecOrdering, aliases: Map[String, (String, String)]) extends EntryEmitter with AliasesConsumer {
  val mapping: DocumentMapping =  dialect.documents().library()
  var emitters: Seq[EntryEmitter] = Seq()

  override def emit(b: EntryBuilder): Unit = {
    Option(mapping.declaredNodes()) match {
      case Some(declarations) =>
        val declaredNodes = declarations
          .map { declaration =>
            aliasFor(declaration.mappedNode()) match {
              case Some(declaredId) => MapEntryEmitter(declaration.name(), declaredId)
              case _                => MapEntryEmitter(declaration.name(), declaration.mappedNode())
            }
          }
        val sortedNodes = ordering.sorted(declaredNodes)
        emitters ++= Seq(new EntryEmitter {

          override def emit(b: EntryBuilder): Unit = {
            b.entry("declares", _.obj { b =>
              traverse(sortedNodes, b)
            })
          }

          override def position(): Position = sortedNodes.head.position
        })
      case _ => // ignore
    }
    b.entry("library", _.obj { b =>
      traverse(ordering.sorted(emitters), b)
    })
  }

  override def position(): Position = {
    val allPos = dialect.documents().library().declaredNodes().map { lib =>
      lib.annotations.find(classOf[LexicalInformation]).map(_.range.start)
    }.collect { case Some(pos) => pos }
    allPos.sorted.headOption.getOrElse(ZERO)
  }
}

case class RootDocumentModelEmitter(dialect: Dialect, ordering: SpecOrdering, aliases: Map[String, (String, String)]) extends EntryEmitter with AliasesConsumer {
  val mapping: DocumentMapping =  dialect.documents().root()
  var emitters: Seq[EntryEmitter] = Seq()

  override def emit(b: EntryBuilder): Unit = {
    Option(mapping.encoded()) match {
      case Some(encodedId) =>
        aliasFor(encodedId) match {
          case Some(alias) => emitters ++= Seq(MapEntryEmitter("encodes", alias))
          case None        => emitters ++= Seq(MapEntryEmitter("encodes", encodedId))
        }
      case _ => // ignore
    }
    Option(mapping.declaredNodes()) match {
      case Some(declarations) if declarations.nonEmpty =>
        val declaredNodes = declarations
          .map { declaration =>
            aliasFor(declaration.mappedNode()) match {
              case Some(declaredId) => MapEntryEmitter(declaration.name(), declaredId)
              case _                => MapEntryEmitter(declaration.name(), declaration.mappedNode())
            }
          }
        val sortedNodes = ordering.sorted(declaredNodes)
        emitters ++= Seq(new EntryEmitter {

          override def emit(b: EntryBuilder): Unit = {
            b.entry("declares", _.obj { b =>
              traverse(sortedNodes, b)
            })
          }

          override def position(): Position = {
            sortedNodes.head.position
          }
        })
      case _ => // ignore
    }
    b.entry("root", _.obj { b =>
      traverse(ordering.sorted(emitters), b)
    })
  }

  override def position(): Position = dialect.documents().root().annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
}

case class FragmentMappingEmitter(dialect: Dialect, fragment: DocumentMapping, ordering: SpecOrdering, aliases: Map[String, (String, String)]) extends EntryEmitter with AliasesConsumer {

  override def emit(b: EntryBuilder): Unit = {
    aliasFor(fragment.encoded()) match {
      case Some(alias) => MapEntryEmitter(fragment.documentName(), alias).emit(b)
      case _           => MapEntryEmitter(fragment.documentName(), fragment.encoded()).emit(b)
    }
  }

  override def position(): Position = fragment.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
}

case class FragmentsDocumentModelEmitter(dialect: Dialect, ordering: SpecOrdering, aliases: Map[String, (String, String)]) extends EntryEmitter with AliasesConsumer {
  var emitters: Seq[EntryEmitter] = dialect.documents().fragments().map { fragmentMapping => FragmentMappingEmitter(dialect, fragmentMapping, ordering, aliases) }

  override def emit(b: EntryBuilder): Unit = {

    b.entry("fragments", _.obj { b =>
      b.entry("encodes", _.obj { b =>
        ordering.sorted(emitters).foreach(_.emit(b))
      })
    })
  }

  override def position(): Position = ordering.sorted(emitters).headOption.map{ e: EntryEmitter => e.position() }.getOrElse(ZERO)
}

case class DocumentsModelEmitter(dialect: Dialect, ordering: SpecOrdering, aliases: Map[String, (String, String)]) extends EntryEmitter with AliasesConsumer {
  val documents = dialect.documents()
  var emitters: Seq[EntryEmitter] = Seq()

  override def emit(b: EntryBuilder): Unit = {
    b.entry("documents", _.obj { b =>
      // Root Emitter
      if (Option(documents.root()).isDefined) {
        emitters ++= Seq(RootDocumentModelEmitter(dialect, ordering, aliases))
      }

      // Fragments emitter
      if (Option(documents.fragments()).isDefined && documents.fragments().nonEmpty) {
        emitters ++= Seq(FragmentsDocumentModelEmitter(dialect, ordering, aliases))
      }

      // Module emitter
      if (Option(documents.library()).isDefined) {
        emitters ++= Seq(LibraryDocumentModelEmitter(dialect, ordering, aliases))
      }
      traverse(ordering.sorted(emitters), b)
    })
  }

  override def position(): Position = {
    val rootEncodedPosition: Seq[Position] = Option(documents.root()).flatMap { root =>
      documents.root().fields.entry(DocumentMappingModel.EncodedNode).flatMap { enc =>
        enc.value.annotations.find(classOf[LexicalInformation]).map(_.range.start)
      }
    }.map(pos => Seq(pos)).getOrElse(Nil)

    val rootDeclared: Seq[PublicNodeMapping] = Option(documents.root()).map { root => Option(root.declaredNodes()).getOrElse(Nil) }.getOrElse(Nil)
    val rootDeclaredPositions: Seq[Position] = rootDeclared.flatMap(_.annotations.find(classOf[LexicalInformation])).map(_.range.start)

    val libraryDeclarations: Seq[PublicNodeMapping] = Option(documents.library()).map(_.declaredNodes()).getOrElse(Nil)
    val libraryDeclarationPositions = libraryDeclarations.map(_.annotations.find(classOf[LexicalInformation]).map(_.range.start)).collect { case Some(pos) => pos}

    val fragmentPositions = documents.fragments().flatMap { fragment => fragment.annotations.find(classOf[LexicalInformation]).map(_.range.start) }

    (rootEncodedPosition ++ rootDeclaredPositions ++ fragmentPositions ++ libraryDeclarationPositions).sorted.headOption.getOrElse(ZERO)
  }
}

case class PropertyMappingEmitter(dialect: Dialect, propertyMapping: PropertyMapping, ordering: SpecOrdering, aliases: Map[String,(String, String)]) extends EntryEmitter with AliasesConsumer with PosExtractor {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(propertyMapping.name(), _.obj { b =>
      var emitters: Seq[EntryEmitter] = Seq()

      aliasFor(propertyMapping.nodePropertyMapping()) match {
        case Some(propertyTermAlias) =>
          val pos = fieldPos(propertyMapping, PropertyMappingModel.NodePropertyMapping)
          emitters ++= Seq(MapEntryEmitter("propertyTerm", propertyTermAlias, YType.Str, pos))
        case None                    =>
      }

      Option(propertyMapping.literalRange()).foreach {
        case literal if literal.endsWith("anyUri")  =>
          val pos = fieldPos(propertyMapping, PropertyMappingModel.LiteralRange)
          emitters ++= Seq(MapEntryEmitter("range", "uri", YType.Str, pos))

        case literal if literal.endsWith("anyType") =>
          val pos = fieldPos(propertyMapping, PropertyMappingModel.LiteralRange)
          emitters ++= Seq(MapEntryEmitter("range", "any", YType.Str, pos))

        case literal if literal.endsWith("number") =>
          val pos = fieldPos(propertyMapping, PropertyMappingModel.LiteralRange)
          emitters ++= Seq(MapEntryEmitter("range", "number", YType.Str, pos))

        case literal                                =>
          val pos = fieldPos(propertyMapping, PropertyMappingModel.LiteralRange)
          emitters ++= Seq(MapEntryEmitter("range", literal.split(Namespace.Xsd.base).last, YType.Str, pos))
      }

      Option(propertyMapping.objectRange()).foreach { nodeIds =>
        val pos = fieldPos(propertyMapping, PropertyMappingModel.ObjectRange)
        val targets = nodeIds.map { nodeId =>
          aliasFor(nodeId) match {
            case Some(nodeMappingAlias) => Some(nodeMappingAlias)
            case _                      => None
          }
        }.collect { case Some(alias) => alias}

        if (targets.size == 1)
          emitters ++= Seq(MapEntryEmitter("range", targets.head, YType.Str, pos))
        else if (targets.size > 1)
          emitters ++= Seq(new EntryEmitter {
            override def emit(b: EntryBuilder): Unit = b.entry("range", _.list { b =>
              targets.foreach( target => ScalarEmitter(AmfScalar(target)).emit(b))
            })
            override def position(): Position = pos
          })
      }

      Option(propertyMapping.mapKeyProperty()).foreach { value =>
        val pos = fieldPos(propertyMapping, PropertyMappingModel.MapKeyProperty)
        aliasFor(value) match {
          case Some(propertyId) => emitters ++= Seq(MapEntryEmitter("mapKey", propertyId, YType.Str, pos))
          case _                =>
        }
      }

      Option(propertyMapping.mapValueProperty()).foreach { value =>
        val pos = fieldPos(propertyMapping, PropertyMappingModel.MapValueProperty)
        aliasFor(value) match {
          case Some(propertyId) => emitters ++= Seq(MapEntryEmitter("mapValue", propertyId, YType.Str, pos))
          case _                =>
        }
      }

      propertyMapping.fields.entry(PropertyMappingModel.MinCount) foreach { entry =>
        val value = entry.value.value.asInstanceOf[AmfScalar].value
        val pos = fieldPos(propertyMapping, entry.field)
        value match {
          case 0 => emitters ++= Seq(MapEntryEmitter("mandatory", "false", YType.Bool, pos))
          case 1 => emitters ++= Seq(MapEntryEmitter("mandatory", "true", YType.Bool, pos))
        }
      }

      propertyMapping.fields.entry(PropertyMappingModel.Pattern) foreach { entry =>
        val value = entry.value.value.asInstanceOf[AmfScalar].value.toString
        val pos = fieldPos(propertyMapping, entry.field)
        emitters ++= Seq(MapEntryEmitter("pattern", value, YType.Str, pos))
      }

      propertyMapping.fields.entry(PropertyMappingModel.Minimum) foreach { entry =>
        val value = entry.value.value.asInstanceOf[AmfScalar].value
        val pos = fieldPos(propertyMapping, entry.field)
        emitters ++= Seq(MapEntryEmitter("minimum", value.toString, YType.Int, pos))
      }

      propertyMapping.fields.entry(PropertyMappingModel.Maximum) foreach { entry =>
        val value = entry.value.value.asInstanceOf[AmfScalar].value
        val pos = fieldPos(propertyMapping, entry.field)
        emitters ++= Seq(MapEntryEmitter("maximum", value.toString, YType.Int, pos))
      }

      propertyMapping.fields.entry(PropertyMappingModel.AllowMultiple) foreach { entry =>
        val value = entry.value.value.asInstanceOf[AmfScalar].value
        val pos = fieldPos(propertyMapping, entry.field)
        emitters ++= Seq(MapEntryEmitter("allowMultiple", value.toString, YType.Bool, pos))
      }

      propertyMapping.fields.entry(PropertyMappingModel.Sorted) foreach { entry =>
        val value = entry.value.value.asInstanceOf[AmfScalar].value
        val pos = fieldPos(propertyMapping, entry.field)
        emitters ++= Seq(MapEntryEmitter("sorted", value.toString, YType.Bool, pos))
      }

      propertyMapping.fields.entry(PropertyMappingModel.Enum) foreach { entry =>
        emitters ++= Seq(ArrayEmitter("enum", entry, ordering))
      }

      propertyMapping.fields.entry(PropertyMappingModel.TypeDiscriminator) foreach { entry =>
        val pos = fieldPos(propertyMapping, entry.field)
        val typesMapping = propertyMapping.typeDiscrminator()
        emitters ++= Seq(new EntryEmitter {
          override def emit(b: EntryBuilder): Unit = b.entry("typeDiscriminator", _.obj { b =>
            typesMapping.foreach { case (alias, nodeMappingId) =>
              aliasFor(nodeMappingId) match {
                case Some(nodeMapping) => b.entry(alias, nodeMapping)
                case _                 => b.entry(alias, nodeMappingId)
              }
            }
          })

          override def position(): Position = pos
        })
      }

      propertyMapping.fields.entry(PropertyMappingModel.TypeDiscriminatorName) foreach { entry =>
        val value = entry.value.value.asInstanceOf[AmfScalar].value.toString
        val pos = fieldPos(propertyMapping, entry.field)
        emitters ++= Seq(MapEntryEmitter("typeDiscriminatorName", value, YType.Str, pos))
      }

      ordering.sorted(emitters).foreach(_.emit(b))
    })
  }

  override def position(): Position = propertyMapping.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
}


case class NodeMappingEmitter(dialect: Dialect, nodeMapping: NodeMapping, ordering: SpecOrdering, aliases: Map[String,(String, String)]) extends EntryEmitter with AliasesConsumer {
  override def emit(b: EntryBuilder): Unit = {
    if (nodeMapping.isLink) {
      if (isFragment(nodeMapping.linkTarget.get, dialect)) {
        b.entry(nodeMapping.name, YNode.include(nodeMapping.linkLabel.get))
      } else {
        b.entry(nodeMapping.name, nodeMapping.linkLabel.get)
      }
    } else {
      b.entry(nodeMapping.name, _.obj { b =>
        aliasFor(nodeMapping.nodetypeMapping) match {
          case Some(classTermAlias) => MapEntryEmitter("classTerm", classTermAlias).emit(b)
          case None => nodeMapping.nodetypeMapping
        }
        b.entry("mapping", _.obj { b =>
          val propertiesEmitters: Seq[PropertyMappingEmitter] = nodeMapping.propertiesMapping().map { pm: PropertyMapping =>
            PropertyMappingEmitter(dialect, pm, ordering, aliases)
          }
          traverse(propertiesEmitters, b)
        })
      })
    }
  }

  def isFragment(elem: DomainElement, dialect: Dialect): Boolean = {
    dialect.references.exists {
      case ref: DialectFragment => ref.encodes.id == elem.id
      case _                    => false
    }
  }

  override def position(): Position = nodeMapping.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
}

case class ReferencesEmitter(baseUnit: BaseUnit, ordering: SpecOrdering, aliases: Map[String,(String, String)]) extends EntryEmitter {
  val modules = baseUnit.references.collect({ case m: DeclaresModel => m })
  override def emit(b: EntryBuilder): Unit = {
    if (modules.nonEmpty) {
      b.entry("uses", _.obj { b =>
        traverse(ordering.sorted(modules.map(r => ReferenceEmitter(r, ordering, aliases))), b)
      })
    }
  }

  override def position(): Position =
    baseUnit.annotations.find(classOf[AliasesLocation]).map(annot => Position((annot.position, 0))).getOrElse(ZERO)
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

trait RamlDialectDocumentsEmitters {

  val dialect: Dialect
  val aliases:  Map[String,(String, String)]

  def collectAliases(): Map[String,(String, String)] = {
    val vocabFile = dialect.location.split("/").last
    val vocabFilePrefix = dialect.location.replace(vocabFile, "")

    val maps = dialect.annotations.find(classOf[Aliases]).map { aliases =>
      aliases.aliases.foldLeft(Map[String,String]()) { case (acc, (alias, id)) =>
        acc + (id -> alias)
      }
    }.getOrElse(Map())
    val idCounter = new IdCounter()
    val dialectReferences = dialect.references.foldLeft(Map[String,(String,String)]()) {
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
    dialect.externals.foldLeft(dialectReferences) {
      case (acc: Map[String,(String,String)], e: External) =>
        acc + (e.base -> (e.alias, ""))
    }
  }

  def rootLevelEmitters(ordering: SpecOrdering) =
    Seq(ReferencesEmitter(dialect, ordering, aliases)) ++
      nodeMappingDeclarationEmitters(dialect, ordering, aliases) ++
      externalEmitters(ordering)

  def externalEmitters(ordering: SpecOrdering): Seq[EntryEmitter] = {
    if (dialect.externals.nonEmpty) {
      Seq(new EntryEmitter {
        override def emit(b: YDocument.EntryBuilder): Unit = {
          b.entry("external", _.obj({ b =>
            traverse(ordering.sorted(dialect.externals.map(external => ExternalEmitter(external, ordering))), b)
          }))
        }

        override def position(): Position = {
          dialect.externals
            .map(e => e.annotations.find(classOf[LexicalInformation]).map(_.range.start))
            .filter(_.nonEmpty)
            .map(_.get)
            .sortBy(_.line)
            .headOption
            .getOrElse(ZERO)
        }
      })
    } else {
      Nil
    }
  }

  def nodeMappingDeclarationEmitters(dialect: Dialect, ordering: SpecOrdering, aliases: Map[String, (String, String)]): Seq[EntryEmitter] = {
    val nodeMappingDeclarations = dialect.declares.collect { case nm : NodeMapping => nm }
    if (nodeMappingDeclarations.nonEmpty) {
      Seq(new EntryEmitter {
        override def emit(b: EntryBuilder): Unit = {
          b.entry("nodeMappings", _.obj { b =>
            val nodeMappingEmitters: Seq[EntryEmitter]= nodeMappingDeclarations.map { n => NodeMappingEmitter(dialect, n, ordering, aliases) }
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

case class RamlDialectEmitter(dialect: Dialect) extends RamlDialectDocumentsEmitters {

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

    if (Option(dialect.documents()).isDefined) {
      emitters ++= Seq(DocumentsModelEmitter(dialect, ordering, aliases))
    }

    emitters
  }


}

case class RamlDialectLibraryEmitter(library: DialectLibrary) extends RamlDialectDocumentsEmitters {

  val ordering: SpecOrdering = Lexical
  override val dialect: Dialect = toDialect(library)
  val aliases = collectAliases()


  def emitDialectLibrary(): YDocument = {
    val content: Seq[EntryEmitter] = rootLevelEmitters(ordering) ++ dialectEmitters(ordering)

    YDocument(b => {
      b.comment("%RAML Library / RAML 1.0 Dialect")
      b.obj { b =>
        traverse(ordering.sorted(content), b)
      }
    })
  }

  protected def toDialect(library: DialectLibrary): Dialect = Dialect(library.fields, library.annotations).withId(library.id)


  def dialectEmitters(ordering: SpecOrdering) =
    dialectPropertiesEmitter(ordering)


  def dialectPropertiesEmitter(ordering: SpecOrdering) = {
    var emitters: Seq[EntryEmitter] = Nil


    if (Option(dialect.usage).isDefined) {
      emitters ++= Seq(new EntryEmitter {
        override def emit(b: YDocument.EntryBuilder): Unit = MapEntryEmitter("usage", dialect.usage).emit(b)

        override def position(): Position =
          dialect.fields.entry(DialectModel.Usage).get.value.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
      })
    }

    emitters
  }

}

