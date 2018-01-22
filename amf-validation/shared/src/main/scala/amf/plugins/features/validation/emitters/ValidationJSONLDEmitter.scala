package amf.plugins.features.validation.emitters

import amf.ProfileNames
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.PartEmitter
import amf.core.parser.Position
import amf.core.validation.core.{FunctionConstraint, PropertyConstraint, ValidationSpecification}
import amf.core.vocabulary.Namespace
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YDocument, YType}
import org.yaml.render.JsonRender

import scala.collection.mutable.ListBuffer

/**
  * Generates a JSON-LD graph with the shapes for a set of validations
  * @param targetProfile which kind of messages should be generated
  */
class ValidationJSONLDEmitter(targetProfile: String) {

  private val jsValidatorEmitters: ListBuffer[PartEmitter]  = ListBuffer()
  private val jsConstraintEmitters: ListBuffer[PartEmitter] = ListBuffer()

  /**
    * Emit the JSON-LD for these validations
    * @param validations validations
    * @return JSON-LD graph with the validations
    */
  def emitJSON(validations: Seq[ValidationSpecification]): String =
    JsonRender.render(emitJSONLDAST(validations))

  private def emitJSONLDAST(validations: Seq[ValidationSpecification]): YDocument = {
    YDocument {
      _.list { b =>
        validations.foreach(emitValidation(b, _))
        jsValidatorEmitters.foreach(_.emit(b))
        jsConstraintEmitters.foreach(_.emit(b))
      }
    }
  }

  private def emitValidation(b: PartBuilder, validation: ValidationSpecification): Unit = {
    val validationId = validation.id()

    b.obj { p =>
      p.entry("@id", validationId)
      p.entry("@type", (Namespace.Shacl + "NodeShape").iri())

      val message = targetProfile match {
        case ProfileNames.RAML | ProfileNames.RAML08 => validation.ramlMessage.getOrElse(validation.message)
        case ProfileNames.OAS                        => validation.oasMessage.getOrElse(validation.message)
        case _                                       => validation.message
      }
      if (message != "") {
        p.entry((Namespace.Shacl + "message").iri(), genValue(_, message))
      }

      for {
        targetInstance <- validation.targetInstance
      } yield {
        p.entry((Namespace.Shacl + "targetNode").iri(), link(_, expandRamlId(targetInstance)))
      }

      for {
        targetClass <- validation.targetClass
      } yield {
        p.entry((Namespace.Shacl + "targetClass").iri(), link(_, expandRamlId(targetClass)))
      }

      for {
        closedShape <- validation.closed
      } yield {
        if (closedShape) {
          p.entry((Namespace.Shacl + "closed").iri(), genValue(_, closedShape.toString))
        }
      }

      for {
        targetObject <- validation.targetObject
      } yield {
        p.entry((Namespace.Shacl + "targetObjectsOf").iri(), link(_, Namespace.expand(targetObject).iri()))
      }

      if (validation.unionConstraints.nonEmpty) {
        p.entry((Namespace.Shacl + "or").iri(), _.obj {
          _.entry("@list",
                  _.list(l =>
                    validation.unionConstraints.foreach { v =>
                      link(l, v)
                  }))
        })
      }

      validation.functionConstraint match {
        case Some(f) => emitFunctionConstraint(p, validationId, f)
        case _       => // ignore
      }

      for {
        (constraint, values) <- validation.nodeConstraints.groupBy(_.constraint)
      } yield {
        p.entry(Namespace.expand(constraint).iri(),
                _.list(b => values.foreach(v => link(b, Namespace.expand(v.value).iri()))))
      }

      if (validation.propertyConstraints.nonEmpty) {
        p.entry(
          (Namespace.Shacl + "property").iri(),
          _.list { b =>
            for {
              constraint <- validation.propertyConstraints
            } yield {
              // processed properties will always include with /prop, this is a CONVENTION
              // can be tricking to follow when debugging
              if (isPropertyConstraintUri(constraint.name)) {
                // These are the standard constraints for AMF/RAML/OAS they have already being sanitised
                emitConstraint(b, constraint.name, constraint)
              } else {
                // this happens when the constraint comes from a profile document
                // an alias for a model element is all the name we provide
                val constraintSegment = if (constraint.name.indexOf("#") > -1) {
                  constraint.name.split("#").last.replace(".", "-")
                } else {
                  constraint.name.replace(".", "-")
                }
                emitConstraint(b, s"$validationId/prop/$constraintSegment", constraint)
              }
            }
          }
        )
      }
    }
  }

  private def isPropertyConstraintUri(name: String): Boolean = {
    (name.startsWith("http://") || name.startsWith("https://") || name.startsWith("file:")) &&
    name.indexOf("/prop") > -1
  }

  private def emitConstraint(b: PartBuilder, constraintId: String, constraint: PropertyConstraint): Unit = {
    b.obj { b =>
      b.entry("@id", constraintId)
      b.entry((Namespace.Shacl + "path").iri(), link(_, expandRamlId(constraint.ramlPropertyId)))

      constraint.maxCount.foreach(genPropertyConstraintValue(b, "maxCount", _, Some(constraint)))
      constraint.minCount.foreach(genPropertyConstraintValue(b, "minCount", _, Some(constraint)))
      constraint.maxLength.foreach(genPropertyConstraintValue(b, "maxLength", _, Some(constraint)))
      constraint.minLength.foreach(genPropertyConstraintValue(b, "minLength", _, Some(constraint)))
      constraint.maxExclusive.foreach(genPropertyConstraintValue(b, "maxExclusive", _, Some(constraint)))
      constraint.minExclusive.foreach(genPropertyConstraintValue(b, "minExclusive", _, Some(constraint)))
      constraint.maxInclusive.foreach(genPropertyConstraintValue(b, "maxInclusive", _, Some(constraint)))
      constraint.minInclusive.foreach(genPropertyConstraintValue(b, "minInclusive", _, Some(constraint)))
      constraint.pattern.foreach(v => genPropertyConstraintValue(b, "pattern", v))
      constraint.node.foreach(genPropertyConstraintValue(b, "node", _))
      constraint.datatype.foreach { v =>
        if (!v.endsWith("#float") && !v.endsWith("#number")) {
          // raml/oas 'number' are actually the union of integers and floats
          // i handle the data type integer and float inside of every constraint. Here only need to generate the simples data types for path entry
          b.entry((Namespace.Shacl + "datatype").iri(), link(_, v))
        }
      }
      if (constraint.`class`.nonEmpty) {
        if (constraint.`class`.length == 1) {
          b.entry((Namespace.Shacl + "class").iri(), link(_, constraint.`class`.head))
        } else {
          b.entry(
            (Namespace.Shacl + "or").iri(),
            _.obj {
              _.entry("@list",
                      _.list(l =>
                        constraint.`class`.foreach { v =>
                          l.obj {
                            _.entry((Namespace.Shacl + "class").iri(), link(_, v))
                          }
                      }))
            }
          )
        }
      }

      // custom builder
      constraint.custom.foreach { builder =>
        builder(b, constraintId)
      }

      if (constraint.in.nonEmpty) {
        b.entry(
          (Namespace.Shacl + "in").iri(),
          _.obj { _.entry("@list", _.list(b => constraint.in.foreach(genValue(b, _)))) }
        )
      }
    }
  }

  private def emitFunctionConstraint(b: EntryBuilder, validationId: String, f: FunctionConstraint): Unit = {
    genJSValidator(validationId, f)
    genJSConstraint(validationId, f)
    b.entry(f.validatorPath(validationId), genValue(_, "true"))
  }

  private def genJSConstraint(validationId: String, f: FunctionConstraint) = {
    val constraintId  = f.constraintId(validationId)
    val validatorId   = f.validatorId(validationId)
    val validatorPath = f.validatorPath(validationId)

    jsConstraintEmitters += new PartEmitter {
      override def emit(b: PartBuilder): Unit = {
        b.obj { b =>
          b.entry("@id", constraintId)
          b.entry("@type", (Namespace.Shacl + "ConstraintComponent").iri())
          b.entry(
            (Namespace.Shacl + "parameter").iri(),
            _.obj { b =>
              b.entry(
                (Namespace.Shacl + "path").iri(),
                _.obj(_.entry("@id", validatorPath))
              )
              b.entry(
                (Namespace.Shacl + "datatype").iri(),
                _.obj(_.entry("@id", (Namespace.Xsd + "boolean").iri()))
              )
            }
          )
          b.entry((Namespace.Shacl + "validator").iri(), _.obj(_.entry("@id", validatorId)))
        }
      }
      override def position(): Position = Position.ZERO
    }
  }

  private def genJSValidator(validationId: String, f: FunctionConstraint) = {
    val validatorId = f.validatorId(validationId)
    jsValidatorEmitters += new PartEmitter {
      override def emit(b: PartBuilder): Unit = {
        f.functionName match {
          case Some(fnName) =>
            b.obj { b =>
              b.entry("@id", validatorId)
              b.entry("@type", (Namespace.Shacl + "JSValidator").iri())
              f.message.foreach(msg => b.entry((Namespace.Shacl + "message").iri(), genValue(_, msg)))
              b.entry(
                (Namespace.Shacl + "jsLibrary").iri(),
                _.list { b =>
                  for { library <- f.libraries } {
                    b.obj {
                      _.entry(
                        (Namespace.Shacl + "jsLibraryURL").iri(),
                        _.obj { o =>
                          o.entry("@value", library)
                          o.entry("@type", "http://www.w3.org/2001/XMLSchema#anyUri")
                        }
                      )
                    }
                  }
                }
              )
              b.entry((Namespace.Shacl + "jsFunctionName").iri(), genValue(_, fnName))
            }

          case None =>
            f.code match {
              case Some(_) =>
                b.obj { b =>
                  b.entry("@id", validatorId)
                  b.entry("@type", (Namespace.Shacl + "JSValidator").iri())
                  f.message.foreach(msg => b.entry((Namespace.Shacl + "message").iri(), genValue(_, msg)))
                  b.entry(
                    (Namespace.Shacl + "jsLibrary").iri(),
                    _.list { b =>
                      for { library <- Seq(ValidationJSONLDEmitter.validationLibraryUrl) ++ f.libraries } {
                        b.obj {
                          _.entry(
                            (Namespace.Shacl + "jsLibraryURL").iri(),
                            _.obj { o =>
                              o.entry("@value", library)
                              o.entry("@type", "http://www.w3.org/2001/XMLSchema#anyUri")
                            }
                          )
                        }
                      }
                    }
                  )
                  b.entry((Namespace.Shacl + "jsFunctionName").iri(), genValue(_, f.computeFunctionName(validationId)))
                }
              case _ => throw new Exception("Cannot emit validator without JS code or JS function name")
            }
        }
      }
      override def position(): Position = Position.ZERO
    }
  }

  private def genPropertyConstraintValue(b: EntryBuilder,
                                         constraintName: String,
                                         value: String,
                                         constraint: Option[PropertyConstraint] = None): Unit = {
    constraint.flatMap(_.datatype) match {
      case Some(scalarType) if scalarType == (Namespace.Shapes + "number").iri() =>
        b.entry(
          (Namespace.Shacl + "or").iri(),
          _.obj {
            _.entry(
              "@list",
              _.list { l =>
                if (value.indexOf(".") == -1) {
                  l.obj { o =>
                    o.entry((Namespace.Shacl + constraintName).iri(),
                            genValue(_, value, Some((Namespace.Xsd + "integer").iri())))
                  }
                }

                l.obj { o =>
                  o.entry((Namespace.Shacl + constraintName).iri(),
                          genValue(_, value, Some((Namespace.Xsd + "float").iri())))
                }
              }
            )
          }
        )
      case Some(_) =>
        b.entry((Namespace.Shacl + constraintName).iri(), genValue(_, value, constraint.flatMap(_.datatype)))
      case None =>
        b.entry((Namespace.Shacl + constraintName).iri(), genValue(_, value, None))
    }

  }

  private def expandRamlId(s: String): String =
    if (s.startsWith("http://") || s.startsWith("https://") || s.startsWith("file:")) {
      s.trim
    } else {
      Namespace.expand(s.replace(".", ":")).iri().trim
    }

  private def genNonEmptyList(b: PartBuilder): Unit = {
    b.obj { b =>
      b.entry("@type", raw(_, (Namespace.Shacl + "NodeShape").iri()))
      b.entry((Namespace.Shacl + "message").iri(), raw(_, "List cannot be empty"))
      b.entry(
        (Namespace.Shacl + "property").iri(),
        _.list {
          _.obj { b =>
            b.entry((Namespace.Shacl + "path").iri(), link(_, (Namespace.Rdf + "first").iri()))
            b.entry(
              (Namespace.Shacl + "minCount").iri(),
              _.obj {
                _.entry("@value", raw(_, "1", YType.Int))
              }
            )
          }
        }
      )
    }
  }

  private def genValue(b: PartBuilder, s: String, dType: Option[String] = None): Unit = {
    dType match {
      case Some(dt) =>
        b.obj(p => {
          p.entry("@value", s)
          p.entry("@type", dt)
        })
      case None =>
        if (s.matches("[\\d]+")) {
          b.obj(_.entry("@value", raw(_, s, YType.Int)))
        } else if (s == "true" || s == "false") {
          b.obj(_.entry("@value", raw(_, s, YType.Bool)))
        } else if (Namespace.expand(s).iri() == Namespace.expand("amf-parser:NonEmptyList").iri()) {
          genNonEmptyList(b)
        } else if (s.startsWith("http://") || s.startsWith("https://") || s.startsWith("file:")) {
          link(b, s)
        } else {
          b.obj(b => b.entry("@value", s))
        }
    }
  }
}

object ValidationJSONLDEmitter {
  def validationLibraryUrl: String = (Namespace.AmfParser + "validationLibrary.js").iri()
}
