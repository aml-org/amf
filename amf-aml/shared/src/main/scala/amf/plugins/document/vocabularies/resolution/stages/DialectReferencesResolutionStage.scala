package amf.plugins.document.vocabularies.resolution.stages

import amf.core.annotations.Aliases
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.parser.{Annotations, ErrorHandler, Fields}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectFragment, DialectLibrary, Vocabulary}
import amf.plugins.document.vocabularies.model.domain.{External, NodeMapping}

class DialectReferencesResolutionStage()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage() {

  def findDeclarations(model: BaseUnit, acc: Map[String, NodeMapping] = Map()): Map[String, NodeMapping] = {
    val updateDeclarations = model match {
      case lib: DeclaresModel => {
        lib.declares.collect { case nodeMapping: NodeMapping => nodeMapping }.foldLeft(acc) {
          case (acc, mapping) =>
            acc.updated(mapping.id, mapping)
        }
      }
      case _ => acc
    }

    model.references.collect { case lib: DeclaresModel => lib }.foldLeft(updateDeclarations) {
      case (acc, lib) =>
        findDeclarations(lib, acc)
    }
  }

  def findExternals(model: BaseUnit, acc: Set[String] = Set()): Set[String] = {
    val updateDeclarations = model match {
      case lib: DialectLibrary       => acc ++ lib.externals.map(_.base.value())
      case dialect: Dialect          => acc ++ dialect.externals.map(_.base.value())
      case fragment: DialectFragment => acc ++ fragment.externals.map(_.base.value())
      case _                         => acc
    }

    model.references.foldLeft(updateDeclarations) {
      case (acc, lib) =>
        findExternals(lib, acc)
    }
  }

  def findVocabularies(model: BaseUnit, acc: Set[Vocabulary] = Set()): Set[Vocabulary] = {
    val updateDeclarations = model.references.foldLeft(acc) {
      case (acc, ref) =>
        acc ++ ref.references.collect { case vocab: Vocabulary => vocab }
    }

    model.references.collect { case lib: DialectLibrary => lib }.foldLeft(updateDeclarations) {
      case (acc, lib) =>
        findVocabularies(lib, acc)
    }
  }

  override def resolve[T <: BaseUnit](model: T): T = {
    var allDeclarations = findDeclarations(model)
    var allExternals    = findExternals(model)
    var allVocabularies = findVocabularies(model)

    val finalDeclarations = allDeclarations.values.zipWithIndex.map {
      case (mapping: NodeMapping, i) =>
        if (mapping.isLink) {
          val target = mapping.linkTarget.get.asInstanceOf[NodeMapping]
          val fields = Fields()
          target.fields.fields().foreach { entry =>
            fields.setWithoutId(entry.field, entry.value.value, entry.value.annotations)
          }
          NodeMapping(fields, Annotations()).withId(mapping.id).withName(s"node$i")
        } else {
          mapping.withName(s"node$i")
        }
    }.toSeq

    val vocabulariesAliases =
      allVocabularies.zipWithIndex.foldLeft(Map[Aliases.Alias, (Aliases.FullUrl, Aliases.RelativeUrl)]()) {
        case (acc, (vocab, i)) =>
          acc.updated(s"vocab$i", (vocab.id, vocab.id))
      }

    val finalExternals: Seq[External] = allExternals.toSeq.zipWithIndex.map {
      case (external: String, i) =>
        External().withBase(external).withAlias(s"external$i").withId(model.location + s"#/external/external$i")
    }

    val resolved = model match {
      case dialect: Dialect =>
        Dialect()
          .withId(dialect.id)
          .withLocation(dialect.location)
          .withDocuments(dialect.documents())
          .withDeclares(finalDeclarations)
          .withExternals(finalExternals)
          .withName(dialect.name().value())
          .withVersion(dialect.version().value())
      case library: DialectLibrary =>
        DialectLibrary()
          .withId(library.id)
          .withLocation(library.location)
          .withDeclares(finalDeclarations)
          .withExternals(finalExternals)
      case fragment: DialectFragment =>
        DialectFragment()
          .withId(fragment.id)
          .withLocation(fragment.location)
          .withEncodes(fragment.encodes)
          .withExternals(finalExternals)
    }

    resolved.annotations += Aliases(vocabulariesAliases.toSet)

    resolved.asInstanceOf[T]
  }

}
