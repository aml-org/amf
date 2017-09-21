package amf.validation.model

import amf.client.GenerationOptions
import amf.generator.JsonGenerator
import amf.graph.GraphEmitter.Emitter
import amf.parser.ASTEmitter
import amf.spec.common.BaseSpecEmitter
import amf.vocabulary.Namespace
import org.yaml.model.{YDocument, YType}

/**
  * Generates a JSON-LD graph with the shapes for a set of validations
  * @param targetProfile which kind of messages should be generated
  */
class JSONLDEmitter(targetProfile: String) extends BaseSpecEmitter {

  override val emitter = ASTEmitter()

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
      }
    }
  }

  private def emitValidation(validation: ValidationSpecification): Unit = {
    val validationId = if (validation.name.startsWith("http://") || validation.name.startsWith("https://")) {
      validation.name
    } else {
      Namespace.expand(validation.name).iri() match {
        case s if s.startsWith("http://") || s.startsWith("https://") => s
        case s  => (Namespace.Data + s).iri()
      }
    }
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

  private def genValue(s: String) = {
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
