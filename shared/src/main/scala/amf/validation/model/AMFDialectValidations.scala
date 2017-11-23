package amf.validation.model

import amf.framework.metamodel.Type.{Bool, Int, Iri, Str}
import amf.spec.dialects.{Dialect, DialectNode, DialectPropertyMapping}
import amf.vocabulary.Namespace
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable.ListBuffer

class AMFDialectValidations(dialect: Dialect) {

  def profile() = {
    val parsedValidations = validations()
    ValidationProfile(
      name = dialect.name,
      baseProfileName = None,
      violationLevel = parsedValidations.map(_.name),
      validations = parsedValidations ++ ParserSideValidations.validations,
    )
  }

  protected def validations(): List[ValidationSpecification] = emitEntityValidations(dialect.root)

  protected def emitEntityValidations(node: DialectNode): List[ValidationSpecification] = {
    node.props.flatMap { case (propName, prop) => emitPropertyValidations(node, propName, prop) }.toList
  }

  protected def emitPropertyValidations(node: DialectNode, propName: String, prop: DialectPropertyMapping): List[ValidationSpecification] = {
    val validations: ListBuffer[ValidationSpecification] = ListBuffer.empty
    if (prop.required) {
      val message = s"Property ${prop.name} is mandatory"
      validations += new ValidationSpecification(
        name = validationId(node, propName, "required"),
        message = message,
        ramlMessage = Some(message),
        oasMessage = Some(message),
        targetClass = Seq(node.`type`.head.iri()),
        propertyConstraints = Seq(new PropertyConstraint(
          ramlPropertyId = prop.iri(),
          name = validationId(node, propName, "required") + "/prop",
          message = Some(message),
          minCount = Some("1")
        )))
    }

    if (!prop.collection && prop.hash.isEmpty) {
      val message = s"Property ${prop.name} cannot have more than 1 value"
      validations += new ValidationSpecification(
        name = validationId(node, propName, "notCollection"),
        message = message,
        ramlMessage = Some(message),
        oasMessage = Some(message),
        targetClass = Seq(node.`type`.head.iri()),
        propertyConstraints = Seq(new PropertyConstraint(
          ramlPropertyId = prop.iri(),
          name = validationId(node, propName, "notCollection") + "/prop",
          message = Some(message),
          maxCount = Some("1")
        )))
    }

    prop.pattern match {
      case Some(pattern) =>
        val message = s"Property ${prop.name} must match pattern $pattern"
        validations += new ValidationSpecification(
          name = validationId(node, propName, "pattern"),
          message = message,
          ramlMessage = Some(message),
          oasMessage = Some(message),
          targetClass = Seq(node.`type`.head.iri()),
          propertyConstraints = Seq(new PropertyConstraint(
            ramlPropertyId = prop.iri(),
            name = validationId(node, propName, "pattern") + "/prop",
            message = Some(message),
            pattern = Some(pattern)
          )))
      case _  => // ignore
    }


    prop.enum match {
      case Some(values) => {
        val message = s"Property ${prop.name} must match some value in ${values.mkString(",")}"
        validations += new ValidationSpecification(
          name = validationId(node, propName, "enum"),
          message = message,
          ramlMessage = Some(message),
          oasMessage = Some(message),
          targetClass = Seq(node.`type`.head.iri()),
          propertyConstraints = Seq(new PropertyConstraint(
            ramlPropertyId = prop.iri(),
            name = validationId(node, propName, "enum") + "/prop",
            message = Some(message),
            in = values
          )))
      }
      case _ => // ignore
    }

    // ranges here
    if (prop.iri() == (Namespace.Meta + "range").iri()) {
      // this is hardcoded just in the case of the dialects 'dialect'
      // In RAML Dialect 1.0 we define range of a term as being either a link to a nodeMapping
      // ane external entity or a literalURI

      val message = s"Property $propName range  value must be of type node mapping, a reference or a literal type"
      validations += new ValidationSpecification(
        name = validationId(node, propName, "dialectRange"),
        message = message,
        ramlMessage = Some(message),
        oasMessage = Some(message),
        targetClass = Seq(node.`type`.head.iri()),
        propertyConstraints = Seq(new PropertyConstraint(
          ramlPropertyId = prop.iri(),
          name = validationId(node, propName, "dialectRange") + "/prop",
          message = Some(message),
          custom = Some((b: EntryBuilder, parentId: String) => {
            b.entry(
              (Namespace.Shacl + "or").iri(),
              _.obj {
                _.entry("@list",
                  _.list { l =>
                    l.obj {
                      _.entry(
                        (Namespace.Shacl + "class").iri(),
                        _.obj(_.entry("@id", (Namespace.Meta + "NodeDefinition").iri().trim))
                      )
                    }
                    l.obj {
                      _.entry(
                        (Namespace.Shacl + "class").iri(),
                        _.obj(_.entry("@id", (Namespace.Meta + "ExternalEntity").iri().trim))
                      )
                    }
                    l.obj {
                      _.entry(
                        (Namespace.Shacl + "in").iri(),
                        _.obj(_.entry("@list", _.list { l =>
                          l.obj(_.entry("@id", (Namespace.Xsd + "string").iri().trim))
                          l.obj(_.entry("@id", (Namespace.Xsd + "integer").iri().trim))
                          l.obj(_.entry("@id", (Namespace.Xsd + "float").iri().trim))
                          l.obj(_.entry("@id", (Namespace.Xsd + "boolean").iri().trim))
                          l.obj(_.entry("@id", (Namespace.Xsd + "anyUri").iri().trim))
                        }))
                      )
                    }
                  }
                )
              }
            )
          })
          // `class` = types.map(_.`type`.head.iri()) ++ Seq((Namespace.Meta + "ExternalEntity").iri())
        )))

    } else if (prop.iri() != (Namespace.Rdfs + "range").iri()) {
      // this is hardcoded just in the case of the vocabularies 'dialect'
      // In RAML Vocabularies 1.0 we define range of a term as being either a link to a class term
      // or one of the special strings for the data types
      // We are not allowing this in general in vocabularies and is not consistent OWL but we need to
      // support it on the parser side

      // This is a special validation for this edge case
      // Normal validation, either a union of classes, a single class or a scalar range
      prop.unionTypes match {
        // Multiple object ranges
        case Some(types) =>
          val message = s"Property ${prop.name}  value must be of type ${types.map(_.`type`.head.name)}"
          validations += new ValidationSpecification(
            name = validationId(node, propName, "objectRange"),
            message = message,
            ramlMessage = Some(message),
            oasMessage = Some(message),
            targetClass = Seq(node.`type`.head.iri()),
            propertyConstraints = Seq(new PropertyConstraint(
              ramlPropertyId = prop.iri(),
              name = validationId(node, propName, "objectRange") + "/prop",
              message = Some(message),
              `class` = types.map(_.`type`.head.iri()) ++ Seq((Namespace.Meta + "ExternalEntity").iri())
            )))
          types.foreach { case childNode: DialectNode =>
            validations ++= emitEntityValidations(childNode)
          }

        // Single object range
        case _ => prop.rangeAsDialect match {
          case Some(childNode) =>
            val message = s"Property ${prop.name} value must be of type ${childNode.shortName}"
            validations += new ValidationSpecification(
              name = validationId(node, propName, "objectRange"),
              message = message,
              ramlMessage = Some(message),
              oasMessage = Some(message),
              targetClass = Seq(node.`type`.head.iri()),
              propertyConstraints = Seq(new PropertyConstraint(
                ramlPropertyId = prop.iri(),
                name = validationId(node, propName, "objectRange") + "/prop",
                message = Some(message),
                `class` = Seq(childNode.`type`.head.iri(), (Namespace.Meta + "ExternalEntity").iri())
              )))
            validations ++= emitEntityValidations(childNode)

          // datatype range
          case None =>

            prop.referenceTarget match {
              case Some(target) =>
                val message = s"Property ${prop.name} value must be of type ${target.shortName}"
                validations += new ValidationSpecification(
                  name = validationId(node, propName, "objectRange"),
                  message = message,
                  ramlMessage = Some(message),
                  oasMessage = Some(message),
                  targetClass = Seq(node.`type`.head.iri()),
                  propertyConstraints = Seq(new PropertyConstraint(
                    ramlPropertyId = prop.iri(),
                    name = validationId(node, propName, "objectRange") + "/prop",
                    message = Some(message),
                    `class` = Seq(target.`type`.head.iri(), (Namespace.Meta + "ExternalEntity").iri())
                  )))
              // we don't emit reference target because that will create a loop, IT'S a REFERENCE!

              case None =>
                val dataRange = prop.range match {
                  case Str => Namespace.uri("xsd:string").iri()
                  case Int => Namespace.uri("xsd:integer").iri()
                  case Bool => Namespace.uri("xsd:boolean").iri()
                  case Iri => Namespace.uri("xsd:anyUri").iri()
                  case other => throw new Exception(s"Unknown scalar range $other")
                }
                val message = s"Property ${prop.name}  value must be of type $dataRange"
                validations += new ValidationSpecification(
                  name = validationId(node, propName, "dataRange"),
                  message = message,
                  ramlMessage = Some(message),
                  oasMessage = Some(message),
                  targetClass = Seq(node.`type`.head.iri()),
                  propertyConstraints = Seq(new PropertyConstraint(
                    ramlPropertyId = prop.iri(),
                    name = validationId(node, propName, "dataRange") + "/prop",
                    message = Some(message),
                    datatype = Some(dataRange)
                  )))
            }
        }
      }
    }

    validations.toList
  }

  private def validationId(dialectNode: DialectNode, propName: String, constraint: String): String = dialectNode.id match {
    case Some(id) => s"${id}_${propName}_${constraint}_validation"
    case None     => {
      throw new Exception("Cannot generate validation for dialect node without ID")
    }
  }

}
