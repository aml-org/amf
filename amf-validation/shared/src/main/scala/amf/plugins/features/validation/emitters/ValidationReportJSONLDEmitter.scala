package amf.plugins.features.validation.emitters

import amf.core.annotations.LexicalInformation
import amf.core.validation.{AMFValidationReport, AMFValidationResult, SeverityLevels}
import amf.core.vocabulary.Namespace
import amf.core.emitter.BaseEmitters._
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.{YDocument, YType}
import org.yaml.render.JsonRender

/**
  * Generates a JSON-LD graph with for an AMF validation report
  */
object ValidationReportJSONLDEmitter {

  def shacl(postfix: String): String = (Namespace.Shacl + postfix).iri()

  def amfParser(postfix: String): String = (Namespace.AmfParser + postfix).iri()

  def emitJSON(report: AMFValidationReport): String = JsonRender.render(emitJSONLDAST(report))

  def emitJSONLDAST(report: AMFValidationReport): YDocument = {
    YDocument {
      _.obj { b =>
        b.entry("@type", shacl("ValidationReport"))
        b.entry(shacl("conforms"), raw(_, report.conforms.toString, YType.Bool))
        if (report.results.nonEmpty) {
          val sorted = report.results.sorted
          b.entry(
            shacl("result"),
            _.list(b => sorted.foreach(emitResult(b, _)))
          )
        }
      }
    }
  }

  private def emitResult(b: PartBuilder, result: AMFValidationResult): Unit = {
    b.obj { b =>
      b.entry("@type", shacl("ValidationResult"))
      b.entry(shacl("resultSeverity"), emitViolation(_, result.level))
      b.entry(
        shacl("focusNode"),
        _.obj(_.entry("@id", result.targetNode))
      )
      result.targetProperty foreach {
        case path if path != "" =>
          b.entry(
            shacl("resultPath"),
            _.obj(_.entry("@id", path))
          )
        case _ => // ignore
      }
      b.entry(shacl("resultMessage"), result.message)
      b.entry(
        shacl("sourceShape"),
        _.obj(_.entry("@id", result.validationId))
      )
      result.position.foreach(pos => b.entry(amfParser("lexicalPosition"), emitPosition(_, pos)))
    }
  }

  private def emitViolation(b: PartBuilder, severity: String): Unit = {
    b.obj(
      _.entry(
        "@id",
        severity match {
          case SeverityLevels.INFO      => shacl("Info")
          case SeverityLevels.WARNING   => shacl("Warning")
          case SeverityLevels.VIOLATION => shacl("Violation")
          case _                        => throw new Exception(s"Unknown severity level $severity")
        }
      ))
  }

  def emitPosition(b: PartBuilder, pos: LexicalInformation): Unit = {
    b.obj { b =>
      b.entry("@type", amfParser("Position"))
      b.entry(
        amfParser("start"),
        _.obj { b =>
          b.entry("@type", amfParser("Location"))
          b.entry(amfParser("line"), raw(_, pos.range.start.line.toString, YType.Int))
          b.entry(amfParser("column"), raw(_, pos.range.start.column.toString, YType.Int))
        }
      )
      b.entry(
        amfParser("end"),
        _.obj { b =>
          b.entry("@type", amfParser("Location"))
          b.entry(amfParser("line"), raw(_, pos.range.end.line.toString, YType.Int))
          b.entry(amfParser("column"), raw(_, pos.range.end.column.toString, YType.Int))
        }
      )
    }
  }
}
