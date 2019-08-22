package amf.validations

import java.util.regex.Pattern

import amf.core.model.domain.{AmfArray, AmfScalar, DomainElement, Shape}
import amf.core.parser.Annotations
import amf.core.services.RuntimeValidator.CustomShaclFunctions
import amf.core.utils.RegexConverter
import amf.plugins.document.webapi.annotations.ParsedJSONSchema
import amf.plugins.domain.shapes.models.ScalarShape
import amf.plugins.domain.webapi.metamodel.WebApiModel
object CustomShaclFunctions {

  val functions: CustomShaclFunctions = Map(
    "minimumMaximumValidation" ->
      ((element, violation) => {
        val maybeMinInclusive = element.fields.fields().find { f =>
          f.field.value.iri().endsWith("minInclusive")
        } match {
          case Some(f) if f.value.value.isInstanceOf[AmfScalar] => Some(f.value.value.asInstanceOf[AmfScalar])
          case _                                                => None
        }

        val maybeMaxInclusive = element.fields.fields().find { f =>
          f.field.value.iri().endsWith("maxInclusive")
        } match {
          case Some(f) if f.value.value.isInstanceOf[AmfScalar] => Some(f.value.value.asInstanceOf[AmfScalar])
          case _                                                => None
        }

        if (maybeMaxInclusive.nonEmpty && maybeMinInclusive.nonEmpty) {
          val minInclusive = maybeMinInclusive.get.value.toString.toDouble
          val maxInclusive = maybeMaxInclusive.get.value.toString.toDouble
          if (minInclusive > maxInclusive) {
            violation(None)
          }
        }
      }),
    "pathParameterRequiredProperty" -> ((element, violation) => {
      val optBindingValue = element.fields
        .fields()
        .find { f =>
          f.field.value.iri().endsWith("binding")
        }
        .map(field => field.value.value)
        .collect { case AmfScalar(value, _) => value }

      val optRequiredValue = element.fields
        .fields()
        .find { f =>
          f.field.value.iri().endsWith("required")
        }
        .map(field => field.value.value)
        .collect { case AmfScalar(value, _) => value }

      (optBindingValue, optRequiredValue) match {
        case (Some("path"), Some(false)) | (Some("path"), None) =>
          violation(None)
        case _ =>
      }
    }),
    "fileParameterMustBeInFormData" -> ((element, violation) => {
      val optBindingValue = element.fields
        .fields()
        .find { f =>
          f.field.value.iri().endsWith("binding")
        }
        .map(field => field.value.value)
        .collect { case AmfScalar(value, _) => value }

      val optSchemaValueIsFile = element.fields
        .fields()
        .find { f =>
          f.field.value.iri().endsWith("schema")
        }
        .flatMap(field =>
          field.value.value match {
            case shape: Shape => Some(shape.ramlSyntaxKey == "fileShape")
            case _            => None
        })

      optSchemaValueIsFile.foreach(
        isFile =>
          if (isFile && (optBindingValue.isEmpty || optBindingValue.get != "formData"))
            violation(None)
      )
    }),
    "minMaxItemsValidation" -> ((element, violation) => {
      val maybeMinInclusive = element.fields.fields().find { f =>
        f.field.value.iri().endsWith("minCount")
      } match {
        case Some(f) if f.value.value.isInstanceOf[AmfScalar] => Some(f.value.value.asInstanceOf[AmfScalar])
        case _                                                => None
      }

      val maybeMaxInclusive = element.fields.fields().find { f =>
        f.field.value.iri().endsWith("maxCount")
      } match {
        case Some(f) if f.value.value.isInstanceOf[AmfScalar] => Some(f.value.value.asInstanceOf[AmfScalar])
        case _                                                => None
      }

      if (maybeMaxInclusive.nonEmpty && maybeMinInclusive.nonEmpty) {
        val minInclusive = maybeMinInclusive.get.value.toString.toDouble
        val maxInclusive = maybeMaxInclusive.get.value.toString.toDouble
        if (minInclusive > maxInclusive) {
          violation(None)
        }
      }
    }),
    "minMaxPropertiesValidation" -> ((element, violation) => {
      val maybeMinInclusive = element.fields.fields().find { f =>
        f.field.value.iri().endsWith("minProperties")
      } match {
        case Some(f) if f.value.value.isInstanceOf[AmfScalar] => Some(f.value.value.asInstanceOf[AmfScalar])
        case _                                                => None
      }

      val maybeMaxInclusive = element.fields.fields().find { f =>
        f.field.value.iri().endsWith("maxProperties")
      } match {
        case Some(f) if f.value.value.isInstanceOf[AmfScalar] => Some(f.value.value.asInstanceOf[AmfScalar])
        case _                                                => None
      }

      if (maybeMaxInclusive.nonEmpty && maybeMinInclusive.nonEmpty) {
        val minInclusive = maybeMinInclusive.get.toString.toDouble
        val maxInclusive = maybeMaxInclusive.get.toString.toDouble
        if (minInclusive > maxInclusive) {
          violation(None)
        }
      }
    }),
    "minMaxLengthValidation" -> ((element, violation) => {
      val maybeMinInclusive = element.fields.fields().find { f =>
        f.field.value.iri().endsWith("minLength")
      } match {
        case Some(f) if f.value.value.isInstanceOf[AmfScalar] => Some(f.value.value.asInstanceOf[AmfScalar])
        case _                                                => None
      }

      val maybeMaxInclusive = element.fields.fields().find { f =>
        f.field.value.iri().endsWith("maxLength")
      } match {
        case Some(f) if f.value.value.isInstanceOf[AmfScalar] => Some(f.value.value.asInstanceOf[AmfScalar])
        case _                                                => None
      }

      if (maybeMaxInclusive.nonEmpty && maybeMinInclusive.nonEmpty) {
        val minInclusive = maybeMinInclusive.get.value.toString.toDouble
        val maxInclusive = maybeMaxInclusive.get.value.toString.toDouble
        if (minInclusive > maxInclusive) {
          violation(None)
        }
      }
    }),
    "xmlWrappedScalar" -> ((element, violation) => {
      val isScalar = element.meta.`type`.exists(_.name == "ScalarShape")
      if (isScalar) {
        element.fields.fields().find { f =>
          f.field.value.iri().endsWith("xmlSerialization")
        } match {
          case Some(f) =>
            val xmlSerialization = f.value.value.asInstanceOf[DomainElement]
            xmlSerialization.fields
              .fields()
              .find(f => f.field.value.iri().endsWith("xmlWrapped"))
              .foreach { isWrappedEntry =>
                val isWrapped = isWrappedEntry.scalar.toBool
                if (isWrapped) {
                  violation(None)
                }
              }
          case None => // Nothing
        }
      }
    }),
    "xmlNonScalarAttribute" -> ((element, violation) => {
      element.fields.fields().find { f =>
        f.field.value.iri().endsWith("xmlSerialization")
      } match {
        case Some(f) =>
          val xmlSerialization = f.value.value.asInstanceOf[DomainElement]
          xmlSerialization.fields
            .fields()
            .find(f => f.field.value.iri().endsWith("xmlAttribute"))
            .foreach { isAttributeEntry =>
              val isAttribute = isAttributeEntry.scalar.toBool
              val isNonScalar = !element.meta.`type`.exists(_.name == "ScalarShape")
              if (isAttribute && isNonScalar) {
                violation(None)
              }
            }
        case None => // Nothing
      }
    }),
    "patternValidation" -> ((element, violation) => {
      element.fields
        .fields()
        .find(_.field.value.iri().endsWith("pattern"))
        .map(_.value.value.asInstanceOf[AmfScalar].toString)
        .foreach { pattern =>
          try Pattern.compile(pattern.convertRegex)
          catch {
            case _: Throwable =>
              violation(None)
          }
        }
    }),
    "nonEmptyListOfProtocols" -> ((element, violation) => {
      val maybeValue = element.fields
        .fields()
        .find(_.field.value.iri().endsWith("scheme"))
        .map(field => field.value)
      maybeValue
        .map(_.value)
        .foreach {
          case AmfArray(elements, _) if elements.isEmpty =>
            violation(Some(maybeValue.map(_.annotations).getOrElse(Annotations()), WebApiModel.Schemes))
          case _ =>
        }
    }),
    "datetimeFormatValue" -> ((element, violation) => {
      element match {
        case scalar: ScalarShape =>
          for {
            dataType <- Option(scalar.dataType).flatMap(_.option())
            format   <- Option(scalar.format).flatMap(_.option())
          } yield {
            if (dataType.endsWith("dateTime") && !(format == "rfc3339" || format == "rfc2616")) {
              if (!element.annotations.contains(classOf[ParsedJSONSchema])) violation(None)
            }
          }
        case _ =>
      }
    })
  )

}
