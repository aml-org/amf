package amf.plugins.features.validation

import amf.core.validation.core.{ValidationReport, ValidationResult}
import org.json4s
import org.json4s._
import org.json4s.native.JsonMethods._

/**
  * Parses a JSON-LD report
  */
class JVMValidationReport(jsonld: String) extends ValidationReport with JSONLDParser {

  val SHACL_CONFORMS = "http://www.w3.org/ns/shacl#conforms"
  val SHACL_RESULT   = "http://www.w3.org/ns/shacl#result"

  val parsed: JArray = parse(jsonld) match {
    case list @ JArray(_) => list
    case other            => JArray(List(other))
  }
  val reportNode: JObject = findReport()

  override def conforms: Boolean = extractValue(reportNode, SHACL_CONFORMS) match {
    case Some(JBool(b)) => b
    case _              => throw new Exception(s"Cannot find property $SHACL_CONFORMS in report")
  }

  override def results: List[ValidationResult] =
    extractIds(reportNode, SHACL_RESULT)
      .map { resultNodeId =>
        JVMValidationResult(findNode(resultNodeId))
      }

  private def findNode(resultId: String): JObject = {
    val foundNode = parsed.arr.find {
      case JObject(properties) =>
        properties.exists {
          case ("@id", JString(s)) => s == resultId
          case _                   => false
        }
      case _ => false
    }

    foundNode match {
      case Some(node: JObject) => node
      case _                   => throw new Exception(s"Cannot find node with ID $resultId")
    }
  }

  /**
    * Finds the node in the JSON-LD document containing the report.
    * It must be a node containing the property "shacl:conforms".
    * We assume that the JSON-LD is valid expanded JSON-Ld
    * @return the JSON-LD node containing the report
    */
  private def findReport(): JObject = {
    parsed match {
      case JArray(objs) =>
        val foundReport = objs.filter(checkConforms)
        if (foundReport.length == 1) {
          foundReport.head.asInstanceOf[JObject]
        } else {
          throw new Exception("Cannot find report node")
        }
    }
  }

  /**
    * Checks if the JSON-LD node passed as an argument is the SHACL report by checking the shacl:conforms property
    *
    * @param value JSON Value
    * @return does the report conforms?
    */
  private def checkConforms(value: json4s.JValue): Boolean = {
    value match {
      case JObject(_) =>
        extractValue(value, SHACL_CONFORMS) match {
          case Some(_) => true
          case None    => false
        }
      case _ => false
    }
  }

  override def toString: String = ValidationReport.displayString(this)
}
