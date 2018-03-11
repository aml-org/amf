package amf.plugins.document.vocabularies2.validation

import amf.core.validation.core.{PropertyConstraint, ValidationProfile, ValidationSpecification}
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies2.emitters.instances.DialectEmitterHelper
import amf.plugins.document.vocabularies2.model.document.Dialect
import amf.plugins.document.vocabularies2.model.domain.{NodeMapping, PropertyMapping}
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable.ListBuffer

class AMFDialectValidations(val dialect: Dialect) extends DialectEmitterHelper {

  def profile() = {
    val parsedValidations = validations()
    ValidationProfile(
      name = dialect.nameAndVersion(),
      baseProfileName = None,
      violationLevel = parsedValidations.map(_.name),
      validations = parsedValidations ++ ParserSideValidations.validations
    )
  }

  protected def validations(): List[ValidationSpecification] = {
    Option(dialect.documents()).flatMap(docs => Option(docs.root())).flatMap(root => Option(root.encoded())).map { mappingId =>
      Option(findNodeMappingById(mappingId)) match {
        case Some(nodeMapping) =>  emitEntityValidations(nodeMapping)
        case _                 => Nil
      }

    } getOrElse Nil
  }

  protected def emitEntityValidations(node: NodeMapping): List[ValidationSpecification] = {
    node.propertiesMapping().flatMap { propertyMapping =>
      emitPropertyValidations(node, propertyMapping)
    }.toList
  }

  protected def emitPropertyValidations(node: NodeMapping, prop: PropertyMapping): List[ValidationSpecification] = {
    val validations: ListBuffer[ValidationSpecification] = ListBuffer.empty

    if (prop.minimum().isDefined) {
      val minValue = prop.minimum().get
      val message = s"Property '${prop.name()}' minimum inclusive value is $minValue"
      validations += new ValidationSpecification(
        name = validationId(node, prop.name(), "minimum"),
        message = message,
        ramlMessage = Some(message),
        oasMessage = Some(message),
        targetClass = Seq(node.id),
        propertyConstraints = Seq(PropertyConstraint(
          ramlPropertyId = prop.nodePropertyMapping(),
          name = validationId(node, prop.name(), "minimum") + "/prop",
          message = Some(message),
          minInclusive = Some(minValue.toString)
        )))
    }

    if (prop.maximum().isDefined) {
      val maxValue = prop.maximum().get
      val message = s"Property '${prop.name()}' maximum inclusive value is $maxValue"
      validations += new ValidationSpecification(
        name = validationId(node, prop.name(), "maximum"),
        message = message,
        ramlMessage = Some(message),
        oasMessage = Some(message),
        targetClass = Seq(node.id),
        propertyConstraints = Seq(PropertyConstraint(
          ramlPropertyId = prop.nodePropertyMapping(),
          name = validationId(node, prop.name(), "maximum") + "/prop",
          message = Some(message),
          maxInclusive = Some(maxValue.toString)
        )))
    }

    if (prop.minCount().getOrElse(0) > 0) {
      val message = s"Property '${prop.name()}' is mandatory"
      validations += new ValidationSpecification(
        name = validationId(node, prop.name(), "required"),
        message = message,
        ramlMessage = Some(message),
        oasMessage = Some(message),
        targetClass = Seq(node.id),
        propertyConstraints = Seq(PropertyConstraint(
          ramlPropertyId = prop.nodePropertyMapping(),
          name = validationId(node, prop.name(), "required") + "/prop",
          message = Some(message),
          minCount = Some("1")
        )))
    }

    if (!Option(prop.allowMultiple()).getOrElse(false) && Option(prop.mapKeyProperty()).isEmpty) {
      val message = s"Property '${prop.name()}' cannot have more than 1 value"
      validations += new ValidationSpecification(
        name = validationId(node, prop.name(), "notCollection"),
        message = message,
        ramlMessage = Some(message),
        oasMessage = Some(message),
        targetClass = Seq(node.id),
        propertyConstraints = Seq(PropertyConstraint(
          ramlPropertyId = prop.nodePropertyMapping(),
          name = validationId(node, prop.name(), "notCollection") + "/prop",
          message = Some(message),
          maxCount = Some("1")
        )))
    }

    Option(prop.pattern()) match {
      case Some(pattern) =>
        val message = s"Property '${prop.name()}' must match pattern $pattern"
        validations += new ValidationSpecification(
          name = validationId(node, prop.name(), "pattern"),
          message = message,
          ramlMessage = Some(message),
          oasMessage = Some(message),
          targetClass = Seq(node.id),
          propertyConstraints = Seq(PropertyConstraint(
            ramlPropertyId = prop.nodePropertyMapping(),
            name = validationId(node, prop.name(), "pattern") + "/prop",
            message = Some(message),
            pattern = Some(pattern)
          )))
      case _  => // ignore
    }


    Option(prop.enum()) match {
      case Some(values) =>
        val message = s"Property '${prop.name()}' must match some value in ${values.mkString(",")}"
        validations += new ValidationSpecification(
          name = validationId(node, prop.name(), "enum"),
          message = message,
          ramlMessage = Some(message),
          oasMessage = Some(message),
          targetClass = Seq(node.id),
          propertyConstraints = Seq(PropertyConstraint(
            ramlPropertyId = prop.nodePropertyMapping(),
            name = validationId(node, prop.name(), "enum") + "/prop",
            message = Some(message),
            in = values.map(_.toString)
          )))
      case _ => // ignore
    }

    // ranges here
    if (Option(prop.literalRange()).isDefined) {
      val dataRange = prop.literalRange()
      dataRange match {

        case literal if literal.endsWith("number") || literal.endsWith("float") || literal.endsWith("double") =>
          val message = s"Property '${prop.name()}'  value must be of type ${(Namespace.Xsd + "integer").iri()} or ${(Namespace.Xsd + "float").iri()}"
          validations += new ValidationSpecification(
            name = validationId(node, prop.name(), "dialectRange"),
            message = message,
            ramlMessage = Some(message),
            oasMessage = Some(message),
            targetClass = Seq(node.id),
            propertyConstraints = Seq(PropertyConstraint(
              ramlPropertyId = prop.nodePropertyMapping(),
              name = validationId(node, prop.name(), "dialectRange") + "/prop",
              message = Some(message),
              custom = Some((b: EntryBuilder, parentId: String) => {
                b.entry(
                  (Namespace.Shacl + "or").iri(),
                  _.obj(_.entry("@list", _.list { l =>
                    l.obj { v =>
                      v.entry((Namespace.Shacl + "datatype").iri(), _.obj(_.entry("@id", (Namespace.Xsd + "integer").iri().trim)))
                    }
                    l.obj { v =>
                      v.entry((Namespace.Shacl + "datatype").iri(), _.obj(_.entry("@id", (Namespace.Xsd + "double").iri().trim)))
                    }
                  }))
                )
              })
            )))

        case literal                                =>
          val message = s"Property '${prop.name()}'  value must be of type $dataRange"
          validations += new ValidationSpecification(
            name = validationId(node, prop.name(), "dataRange"),
            message = message,
            ramlMessage = Some(message),
            oasMessage = Some(message),
            targetClass = Seq(node.id),
            propertyConstraints = Seq(PropertyConstraint(
              ramlPropertyId = prop.nodePropertyMapping(),
              name = validationId(node, prop.name(), "dataRange") + "/prop",
              message = Some(message),
              datatype = Some(literal)
            )))

      }
    }

    if (Option(prop.objectRange()).isDefined) {
      val message = s"Property '${prop.name()}'  value must be of type ${prop.objectRange()}"
          validations += new ValidationSpecification(
            name = validationId(node, prop.name(), "objectRange"),
            message = message,
            ramlMessage = Some(message),
            oasMessage = Some(message),
            targetClass = Seq(node.id),
            propertyConstraints = Seq(PropertyConstraint(
              ramlPropertyId = prop.nodePropertyMapping(),
              name = validationId(node, prop.name(), "objectRange") + "/prop",
              message = Some(message),
              `class` = prop.objectRange())
            ))
    }

    validations.toList
  }

  private def validationId(dialectNode: NodeMapping, propName: String, constraint: String): String = Option(dialectNode.id) match {
    case Some(id) => s"${id}_${propName}_${constraint}_validation"
    case None     => throw new Exception("Cannot generate validation for dialect node without ID")
  }

}
