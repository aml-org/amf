package amf.validation.emitters

import amf.client.GenerationOptions
import amf.generator.JsonGenerator
import amf.graph.GraphEmitter.Emitter
import amf.parser.ASTEmitter
import amf.spec.common.BaseSpecEmitter
import amf.validation.model.{FunctionConstraint, PropertyConstraint, ValidationSpecification}
import amf.vocabulary.Namespace
import org.yaml.model.{YDocument, YType}

import scala.collection.mutable.ListBuffer

/**
  * Generates a JSON-LD graph with the shapes for a set of validations
  * @param targetProfile which kind of messages should be generated
  */
class ValidationJSONLDEmitter(targetProfile: String) extends BaseSpecEmitter {

  override val emitter = ASTEmitter()

  val jsValidatorEmitters: ListBuffer[() => Unit] = ListBuffer()
  val jsConstraintEmitters: ListBuffer[() => Unit] = ListBuffer()

  /**
    * Emit the JSON-LD for these validations
    * @param validations
    * @return JSON-LD graph with the validations
    */
  def emitJSON(validations: Seq[ValidationSpecification]): String =
    new JsonGenerator().generate(emitJSONLDAST(validations)).toString

  private def emitJSONLDAST(validations: Seq[ValidationSpecification]): YDocument = {
    Emitter(emitter, GenerationOptions()).emitter.document { () =>
      array { () =>
        validations.foreach(emitValidation)
        jsValidatorEmitters.foreach(e => e())
        jsConstraintEmitters.foreach(e => e())
      }
    }
  }

  private def emitValidation(validation: ValidationSpecification): Unit = {
    val validationId = validation.id()

    map { () =>
      entry { () =>
        raw("@id")
        raw(validationId)
      }

      entry { () =>
        raw("@type"); raw((Namespace.Shacl + "NodeShape").iri())
      }


      val message = targetProfile match {
        case "RAML" => validation.ramlMessage.getOrElse(validation.message)
        case "OAS"  => validation.ramlMessage.getOrElse(validation.message)
        case _      => validation.message
      }
      if (message != "") {
        entry { () =>
          raw((Namespace.Shacl + "message").iri())
          genValue(message)
        }
      }

      for {
        targetClass <- validation.targetClass
      } yield {
        entry { () =>
          raw((Namespace.Shacl + "targetClass").iri())
          link(expandRamlId(targetClass))
        }
      }

      for {
        targetClass <- validation.targetObject
      } yield {
        entry { () =>
          raw((Namespace.Shacl + "targetObjectsOf").iri())
          link(Namespace.expand(targetClass).iri())
        }
      }

      validation.functionConstraint match {
        case Some(f) => emitFunctionConstraint(validationId, f)
        case _       => // ignore
      }

      for {
        (constraint, values) <- validation.nodeConstraints.groupBy(_.constraint)
      } yield {
        entry { () =>
          raw(Namespace.expand(constraint).iri())
          array { () =>
            values.foreach( v => link(Namespace.expand(v.value).iri()))
          }
        }
      }

      if (validation.propertyConstraints.nonEmpty) {
        entry { () =>
          raw((Namespace.Shacl + "property").iri())
          array { () =>
            for {
              constraint <- validation.propertyConstraints
            } yield {
              if (constraint.name.startsWith("http://") || constraint.name.startsWith("https://")) {
                // These are the standard constraints for AMF/RAML/OAS they have already being sanitised
                emitConstraint(constraint.name, constraint)
              } else {
                // this happens when the constraint comes from a profile document
                // an alias for a model element is all the name we provide
                emitConstraint(s"$validationId/prop/${constraint.name.replace(".", "-")}", constraint)
              }
            }
          }
        }
      }
    }
  }

  def escapeRegex(v: String): _root_.scala.Predef.String = {
    v flatMap { c => if (c == '\\') { Seq('\\','\\') } else { Seq(c) } }
  }

  private  def emitConstraint(constraintId: String, constraint: PropertyConstraint): Unit = {
    map { () =>
      entry { () =>
        raw("@id")
        raw(constraintId)
      }
      entry { () =>
        raw((Namespace.Shacl + "path").iri())
        link(expandRamlId(constraint.ramlPropertyId))
      }


      constraint.maxCount.foreach { v =>
        genPropertyConstraintValue("maxCount", v)
      }
      constraint.minCount.foreach { v =>
        genPropertyConstraintValue("minCount", v)
      }
      constraint.maxExclusive.foreach { v =>
        genPropertyConstraintValue("maxExclusive", v)
      }
      constraint.minExclusive.foreach { v =>
        genPropertyConstraintValue("maxExclusive", v)
      }
      constraint.maxInclusive.foreach { v =>
        genPropertyConstraintValue("maxInclusive", v)
      }
      constraint.minInclusive.foreach { v =>
        genPropertyConstraintValue("minInclusive", v)
      }
      constraint.pattern.foreach { v =>
        genPropertyConstraintValue("pattern", escapeRegex(v))
      }
      constraint.node.foreach { v =>
        genPropertyConstraintValue("node", v)
      }
      constraint.datatype.foreach { v =>
        entry { () =>
          raw((Namespace.Shacl + "datatype").iri())
          link(v)
        }
      }
      constraint.`class`.foreach { v =>
        entry { () =>
          raw((Namespace.Shacl + "class").iri())
          link(v)
        }
      }
      if (constraint.in.nonEmpty) {
        entry { () =>
          raw((Namespace.Shacl + "in").iri())
          map { () =>
            entry { () =>
              raw("@list")
              array { () =>
                constraint.in.foreach(genValue)
              }
            }
          }
        }
      }
    }
  }

  protected def emitFunctionConstraint(validationId: String, f: FunctionConstraint): Unit = {
    genJSValidator(validationId, f)
    genJSConstraint(validationId, f)
    entry { ()  =>
      raw(f.validatorPath(validationId))
      genValue("true")
    }
  }

  protected def genJSConstraint(validationId: String, f: FunctionConstraint): jsConstraintEmitters.type = {
    val constraintId = f.constraintId(validationId)
    val validatorId = f.validatorId(validationId)
    val validatorPath = f.validatorPath(validationId)

    jsConstraintEmitters += (() => {
      map { () =>
        entry { () =>
          raw("@id")
          raw(constraintId)
        }
        entry { () =>
          raw("@type")
          raw((Namespace.Shacl + "ConstraintComponent").iri())
        }
        entry { () =>
          raw((Namespace.Shacl + "parameter").iri())
          map { () =>
            entry { () =>
              raw((Namespace.Shacl + "path").iri())
              map { () =>
                entry { () =>
                  raw("@id")
                  raw(validatorPath)
                }
              }
            }
            entry { () =>
              raw((Namespace.Shacl + "datatype").iri())
              map { () =>
                entry { () =>
                  raw("@id")
                  raw((Namespace.Xsd + "boolean").iri())
                }
              }
            }
          }
        }
        entry { () =>
          raw((Namespace.Shacl + "validator").iri())
          map { () =>
            entry { () =>
              raw("@id")
              raw(validatorId)
            }
          }
        }
      }
    })
  }
  protected def genJSValidator(validationId: String, f: FunctionConstraint): jsValidatorEmitters.type = {
    val validatorId = f.validatorId(validationId)
    jsValidatorEmitters += (() => {
      f.functionName match {
        case Some(fnName) =>
          map { () =>
            entry { () =>
              raw("@id")
              raw(validatorId)
            }
            entry { () =>
              raw("@type")
              raw((Namespace.Shacl + "JSValidator").iri())
            }
            f.message match {
              case Some(msg) => entry { () =>
                raw((Namespace.Shacl + "message").iri())
                genValue(msg)
              }
              case _ => // no message
            }
            entry { () =>
              raw((Namespace.Shacl + "jsLibrary").iri())
              array { () =>
                for {library <- f.libraries} {
                  map { () =>
                    entry { () =>
                      raw((Namespace.Shacl + "jsLibraryURL").iri())
                      map {() =>
                        entry {() =>
                          raw("@value")
                          raw(ValidationJSONLDEmitter.validationLibraryUrl)
                        }
                      }
                    }
                  }
                }
              }
            }
            entry { () =>
              raw((Namespace.Shacl + "jsFunctionName").iri())
              genValue(fnName)
            }
          }

        case None =>
          f.code match {
            case Some(_) =>
              map { () =>
                entry { () =>
                  raw("@id")
                  raw(validatorId)
                }
                entry { () =>
                  raw("@type")
                  raw((Namespace.Shacl + "JSValidator").iri())
                }
                f.message match {
                  case Some(msg) => entry { () =>
                    raw((Namespace.Shacl + "message").iri())
                    genValue(msg)
                  }
                  case _ => // no message
                }
                entry { () =>
                  raw((Namespace.Shacl + "jsLibrary").iri())
                  map { () =>
                    entry { () =>
                      raw((Namespace.Shacl + "jsLibraryURL").iri())
                      map {() =>
                        entry {() =>
                          raw("@value")
                          raw(ValidationJSONLDEmitter.validationLibraryUrl)
                        }
                      }
                    }
                  }
                }
                entry { () =>
                  raw((Namespace.Shacl + "jsFunctionName").iri())
                  genValue(f.computeFunctionName(validationId))
                }
              }
            case _          => throw new Exception("Cannot emit validator without JS code or JS function name")
          }
      }
    })
  }

  private def genPropertyConstraintValue(constraintName: String, value: String): Unit = {
    entry { ()=>
      raw((Namespace.Shacl + constraintName).iri())
      genValue(value)
    }
  }

  private def expandRamlId(s: String): String =
    if (s.startsWith("http://") || s.startsWith("https://")) {
      s
    } else {
      Namespace.expand(s.replace(".",":")).iri()
    }

  private def genNonEmptyList(): Unit = {
    map { () =>
      entry { () =>
        raw("@type"); raw((Namespace.Shacl + "NodeShape").iri())
      }
      entry { () =>
        raw((Namespace.Shacl + "message").iri()); raw("List cannot be empty")
      }
      entry { () =>
        raw((Namespace.Shacl + "property").iri())
        array {() =>
          map { () =>
            entry { () =>
              raw((Namespace.Shacl + "path").iri()); link((Namespace.Rdf + "first").iri())
            }
            entry { () =>
              raw((Namespace.Shacl + "minCount").iri())
              map { () =>
                entry { () =>
                  raw("@value")
                  raw("1", YType.Int)
                }
              }
            }
          }
        }
      }
    }
  }

  private def genValue(s: String): Unit = {
    if (s.matches("[\\d]+")) {
      map { () =>
        entry { () =>
          raw("@value")
          raw(s, YType.Int)
        }
      }
    } else if (s == "true" || s == "false") {
      map { () =>
        entry { () =>
          raw("@value")
          raw(s, YType.Bool)
        }
      }
    } else if (Namespace.expand(s).iri() == Namespace.expand("amf-parser:NonEmptyList").iri()) {
      genNonEmptyList()
    } else if (s.startsWith("http://") || s.startsWith("https://")) {
      link(s)
    } else {
      map { () =>
        entry { () =>
          raw("@value")
          raw(s)
        }
      }
    }
  }

}

object ValidationJSONLDEmitter {
  def validationLibraryUrl = (Namespace.AmfParser + "validationLibrary.js").iri()
}