package amf.plugins.document.vocabularies.emitters.vocabularies

import amf.core.annotations.LexicalInformation
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.core.remote.Raml
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies.emitters.common.ExternalEmitter
import amf.plugins.document.vocabularies.metamodel.document.VocabularyModel
import amf.plugins.document.vocabularies.metamodel.domain.{ClassTermModel, ObjectPropertyTermModel}
import amf.plugins.document.vocabularies.model.document.Vocabulary
import amf.plugins.document.vocabularies.model.domain.{ClassTerm, PropertyTerm, VocabularyReference}
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YDocument, YType}

trait AliasMapper {
  def aliasFor(id: String, aliasMapping: Map[String, String]): String = {
    if (id.contains(Namespace.Xsd.base)) {
      id.split(Namespace.Xsd.base).last match {
        case "anyUri"  => "uri"
        case "anyType" => "any"
        case v         => v
      }
    } else {
      aliasMapping.keys.find(k => id.contains(k)) match {
        case Some(k) =>
          val alias = aliasMapping(k)
          id.replace(k, s"$alias.")
        case None =>
          id.split("#").last.split("/").last
      }
    }
  }

  def buildAliasMapping(vocabulary: Vocabulary): Map[String, String] = {
    var aliasMapping: Map[String, String] = Map()
    vocabulary.externals.foreach { external =>
      aliasMapping += (external.base.value() -> external.alias.value())
    }
    vocabulary.imports.foreach { imported =>
      aliasMapping += (imported.base.value() -> imported.alias.value())
    }
    aliasMapping
  }
}

private case class ClassTermEmitter(classTerm: ClassTerm, ordering: SpecOrdering, aliasMapping: Map[String, String])
    extends EntryEmitter
    with AliasMapper {
  override def emit(b: EntryBuilder): Unit = {
    val classAlias                    = aliasFor(classTerm.id, aliasMapping)
    var ctEmitters: Seq[EntryEmitter] = Seq()

    classTerm.displayName
      .option()
      .foreach(displayName => ctEmitters ++= Seq(MapEntryEmitter("displayName", displayName)))

    classTerm.description
      .option()
      .foreach(description => ctEmitters ++= Seq(MapEntryEmitter("description", description)))

    if (classTerm.subClassOf.nonEmpty) {
      ctEmitters ++= Seq(new EntryEmitter {
        override def emit(b: EntryBuilder): Unit = {
          if (classTerm.subClassOf.length == 1) {
            b.entry("extends", aliasFor(classTerm.subClassOf.head.value(), aliasMapping))
          } else {
            b.entry("extends", _.list({ l =>
              classTerm.subClassOf.foreach { extended =>
                l += aliasFor(extended.value(), aliasMapping)
              }
            }))
          }
        }
        override def position(): Position =
          classTerm.fields
            .get(ClassTermModel.SubClassOf)
            .annotations
            .find(classOf[LexicalInformation])
            .map(_.range.start)
            .getOrElse(ZERO)
      })
    }

    if (classTerm.properties.nonEmpty) {
      ctEmitters ++= Seq(new EntryEmitter {
        override def emit(b: EntryBuilder): Unit = {
          b.entry("properties", _.list({ l =>
            classTerm.properties.foreach { prop =>
              l += aliasFor(prop.value(), aliasMapping)
            }
          }))
        }
        override def position(): Position =
          classTerm.fields
            .get(ClassTermModel.SubClassOf)
            .annotations
            .find(classOf[LexicalInformation])
            .map(_.range.start)
            .getOrElse(ZERO)
      })
    }
    if (ctEmitters.isEmpty) {
      MapEntryEmitter(classAlias, "", YType.Null).emit(b)
    } else {
      b.entry(classAlias, _.obj({ ct =>
        traverse(ordering.sorted(ctEmitters), ct)
      }))
    }
  }

  override def position(): Position =
    classTerm.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
}

private case class PropertyTermEmitter(propertyTerm: PropertyTerm,
                                       ordering: SpecOrdering,
                                       aliasMapping: Map[String, String])
    extends EntryEmitter
    with AliasMapper {
  override def emit(b: EntryBuilder): Unit = {
    val propertyAlias                 = aliasFor(propertyTerm.id, aliasMapping)
    var ptEmitters: Seq[EntryEmitter] = Seq()

    propertyTerm.displayName
      .option()
      .foreach(displayName => ptEmitters ++= Seq(MapEntryEmitter("displayName", displayName)))

    propertyTerm.description
      .option()
      .foreach(description => ptEmitters ++= Seq(MapEntryEmitter("description", description)))

    if (propertyTerm.subPropertyOf.nonEmpty) {
      ptEmitters ++= Seq(new EntryEmitter {
        override def emit(b: EntryBuilder): Unit = {
          if (propertyTerm.subPropertyOf.size == 1) {
            b.entry("extends", aliasFor(propertyTerm.subPropertyOf.head.value(), aliasMapping))
          } else {
            b.entry("extends", _.list({ l =>
              propertyTerm.subPropertyOf.foreach { extended =>
                l += aliasFor(extended.value(), aliasMapping)
              }
            }))
          }
        }
        override def position(): Position =
          propertyTerm.fields
            .get(ObjectPropertyTermModel.SubPropertyOf)
            .annotations
            .find(classOf[LexicalInformation])
            .map(_.range.start)
            .getOrElse(ZERO)
      })
    }

    if (propertyTerm.range.nonNull) {
      ptEmitters ++= Seq(new EntryEmitter {
        override def emit(b: EntryBuilder): Unit = {
          b.entry("range", aliasFor(propertyTerm.range.value(), aliasMapping))
        }
        override def position(): Position =
          propertyTerm.fields
            .get(ObjectPropertyTermModel.Range)
            .annotations
            .find(classOf[LexicalInformation])
            .map(_.range.start)
            .getOrElse(ZERO)
      })
    }
    if (ptEmitters.isEmpty) {
      MapEntryEmitter(propertyAlias, "", YType.Null).emit(b)
    } else {
      b.entry(propertyAlias, _.obj({ ct =>
        traverse(ordering.sorted(ptEmitters), ct)
      }))
    }
  }

  override def position(): Position =
    propertyTerm.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
}

private case class ImportEmitter(vocabularyReference: VocabularyReference,
                                 vocabulary: Vocabulary,
                                 ordering: SpecOrdering)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val vocabFile       = vocabulary.location().getOrElse(vocabulary.id).split("/").last
    val vocabFilePrefix = vocabulary.location().getOrElse(vocabulary.id).replace(vocabFile, "")

    val vocabularyReferenceFile = vocabulary.references.find(_.id == vocabularyReference.reference.value()) match {
      case Some(reference: BaseUnit) => reference.location().getOrElse(reference.id)
      case None                      => throw new Exception("Cannot regenerate vocabulary link without reference")
    }

    val importLocation = if (vocabularyReferenceFile.contains(vocabFilePrefix)) {
      vocabularyReferenceFile.replace(vocabFilePrefix, "")
    } else {
      vocabularyReferenceFile.replace("file://", "")
    }

    MapEntryEmitter(vocabularyReference.alias.value(), importLocation).emit(b)
  }

  override def position(): Position =
    vocabularyReference.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
}

case class VocabularyEmitter(vocabulary: Vocabulary) extends AliasMapper {

  val aliasMapping: Map[String, String] = buildAliasMapping(vocabulary)

  def emitVocabulary(): YDocument = {
    val ordering: SpecOrdering = SpecOrdering.ordering(Raml, vocabulary.annotations)

    val content: Seq[EntryEmitter] = rootLevelEmitters(ordering) ++ vocabularyEmitters(ordering)

    YDocument(b => {
      b.comment("%Vocabulary 1.0")
      b.obj { b =>
        traverse(ordering.sorted(content), b)
      }
    })
  }

  def rootLevelEmitters(ordering: SpecOrdering): Seq[EntryEmitter] =
    externalEmitters(ordering) ++ importEmitters(ordering)

  def externalEmitters(ordering: SpecOrdering): Seq[EntryEmitter] = {
    if (vocabulary.externals.nonEmpty) {
      Seq(new EntryEmitter {
        override def emit(b: EntryBuilder): Unit = {
          b.entry("external", _.obj({ b =>
            traverse(ordering.sorted(vocabulary.externals.map(external => ExternalEmitter(external, ordering))), b)
          }))
        }

        override def position(): Position = {
          vocabulary.externals
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

  def importEmitters(ordering: SpecOrdering): Seq[EntryEmitter] = {
    if (vocabulary.imports.nonEmpty) {
      Seq(new EntryEmitter {
        override def emit(b: EntryBuilder): Unit = {
          b.entry("uses", _.obj({ b =>
            traverse(ordering.sorted(
                       vocabulary.imports.map(vocabularyRef => ImportEmitter(vocabularyRef, vocabulary, ordering))),
                     b)
          }))
        }

        override def position(): Position = {
          vocabulary.imports
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

  private def vocabularyPropertiesEmitter(ordering: SpecOrdering) = {
    var emitters: Seq[EntryEmitter] = Nil

    emitters ++= Seq(new EntryEmitter {
      override def emit(b: EntryBuilder): Unit = {
        MapEntryEmitter("base", vocabulary.base.value()).emit(b)
      }

      override def position(): Position = ZERO

    })

    emitters ++= Seq(new EntryEmitter {
      override def emit(b: EntryBuilder): Unit = MapEntryEmitter("vocabulary", vocabulary.name.value()).emit(b)

      override def position(): Position =
        vocabulary.fields
          .get(VocabularyModel.Name)
          .annotations
          .find(classOf[LexicalInformation])
          .map(_.range.start)
          .getOrElse(ZERO)
    })

    if (vocabulary.usage.nonEmpty) {
      emitters ++= Seq(new EntryEmitter {
        override def emit(b: EntryBuilder): Unit = MapEntryEmitter("usage", vocabulary.usage.value()).emit(b)

        override def position(): Position =
          vocabulary.fields
            .get(VocabularyModel.Usage)
            .annotations
            .find(classOf[LexicalInformation])
            .map(_.range.start)
            .getOrElse(ZERO)
      })
    }

    emitters
  }

  def classTermsEmitter(ordering: SpecOrdering): Seq[EntryEmitter] = {
    val classTerms = vocabulary.declares.filter(_.isInstanceOf[ClassTerm]).asInstanceOf[Seq[ClassTerm]]
    if (classTerms.nonEmpty) {
      Seq(
        new EntryEmitter {
          override def emit(b: EntryBuilder): Unit =
            b.entry("classTerms", _.obj({ b =>
              traverse(ordering.sorted(classTerms.map(ct => ClassTermEmitter(ct, ordering, aliasMapping))), b)
            }))

          override def position(): Position =
            classTerms
              .map(_.annotations.find(classOf[LexicalInformation]).map(_.range.start))
              .find(_.isDefined)
              .flatten
              .getOrElse(ZERO)
        }
      )
    } else {
      Nil
    }
  }

  def propertyTermsEmitter(ordering: SpecOrdering): Seq[EntryEmitter] = {
    val propertyTerms = vocabulary.declares.filter(_.isInstanceOf[PropertyTerm]).asInstanceOf[Seq[PropertyTerm]]
    if (propertyTerms.nonEmpty) {
      Seq(
        new EntryEmitter {
          override def emit(b: EntryBuilder): Unit =
            b.entry("propertyTerms", _.obj({ b =>
              traverse(ordering.sorted(propertyTerms.map(pt => PropertyTermEmitter(pt, ordering, aliasMapping))), b)
            }))

          override def position(): Position =
            propertyTerms
              .map(_.annotations.find(classOf[LexicalInformation]).map(_.range.start))
              .find(_.isDefined)
              .flatten
              .getOrElse(ZERO)
        }
      )
    } else {
      Nil
    }
  }

  def vocabularyEmitters(ordering: SpecOrdering): Seq[EntryEmitter] = {
    vocabularyPropertiesEmitter(ordering) ++ classTermsEmitter(ordering) ++ propertyTermsEmitter(ordering)
  }
}
