package amf.validation.emitters

import amf.client.GenerationOptions
import amf.domain.Annotation
import amf.generator.JsonGenerator
import amf.graph.GraphEmitter.Emitter
import amf.parser.ASTEmitter
import amf.spec.common.BaseSpecEmitter
import amf.validation.{AMFValidationReport, AMFValidationResult, SeverityLevels}
import amf.vocabulary.Namespace
import org.yaml.model.{YDocument, YType}

/**
  * Generates a JSON-LD graph with for an AMF validation report
  */
object ValidationReportJSONLDEmitter extends BaseSpecEmitter {

  override val emitter = ASTEmitter()

  def shacl(postfix: String): String = (Namespace.Shacl + postfix).iri()

  def amfParser(postfix: String): String = (Namespace.AmfParser + postfix).iri()

  def emitJSON(report: AMFValidationReport): String =
    new JsonGenerator().generate(emitJSONLDAST(report)).toString

  def emitJSONLDAST(report: AMFValidationReport): YDocument = {
    Emitter(emitter, GenerationOptions()).emitter.document { () =>
      map { () =>
        entry { () =>
          raw("@type")
          raw(shacl("ValidationReport"))
        }
        entry { () =>
          raw(shacl("conforms"))
          raw(report.conforms.toString, YType.Bool)
        }
        if (report.results.nonEmpty) {
          entry { () =>
            raw(shacl("result"))
            array { () =>
              report.results.foreach(result => emitResult(result))
            }
          }
        }
      }
    }
  }

  def emitResult(result: AMFValidationResult): Unit = {
    map { () =>
      entry { () =>
        raw("@type")
        raw(shacl("ValidationResult"))
      }

      entry { () =>
        raw(shacl("resultSeverity"))
        emitViolation(result.level)
      }
      entry { () =>
        raw(shacl("focusNode"))
        map { () =>
          entry { () =>
            raw("@id")
            raw(result.targetNode)
          }
        }
      }
      result.targetProperty match {
        case Some(path) if path != "" => entry { () =>
          raw(shacl("resultPath"))
          map { () =>
            entry { () =>
              raw("@id")
              raw(path)
            }
          }
        }
        case _ => // ignore
      }
      entry { () =>
        raw(shacl("resultMessage"))
        raw(result.message)
      }
      entry { () =>
        raw(shacl("sourceShape"))
        map { () =>
          entry { () =>
            raw("@id")
            raw(result.validationId)
          }
        }
      }
      result.position match {
        case Some(pos) => entry { () =>
          raw(amfParser("lexicalPosition"))
          emitPosition(pos)
        }
        case _ => // ignore
      }
    }
  }

  def emitViolation(severity: String): Unit = {
    val level = severity match {
      case SeverityLevels.INFO => shacl("Info")
      case SeverityLevels.WARNING => shacl("Warning")
      case SeverityLevels.VIOLATION => shacl("Violation")
      case _ => throw new Exception(s"Unknown severity level $severity")
    }
    map { () =>
      entry { () =>
        raw("@id")
        raw(level)
      }
    }
  }

  def emitPosition(pos: Annotation.LexicalInformation): Unit = {
    map { () =>
      entry { () =>
        raw("@type")
        raw(amfParser("Position"))
      }
      entry { () =>
        raw(amfParser("start"))
        map { () =>
          entry { () =>
            raw("@type")
            raw(amfParser("Location"))
          }
          entry { () =>
            raw(amfParser("line"))
            raw(pos.range.start.line.toString, YType.Int)
          }
          entry { () =>
            raw(amfParser("column"))
            raw(pos.range.start.column.toString, YType.Int)
          }
        }
      }
      entry { () =>
        raw(amfParser("end"))
        map { () =>
          entry { () =>
            raw("@type")
            raw(amfParser("Location"))
          }
          entry { () =>
            raw(amfParser("line"))
            raw(pos.range.end.line.toString, YType.Int)
          }
          entry { () =>
            raw(amfParser("column"))
            raw(pos.range.end.column.toString, YType.Int)
          }
        }
      }
    }
  }

}
