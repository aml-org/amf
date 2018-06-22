package amf.plugins.features.validation

import org.json4s.JsonAST.{JArray, JString}
import org.json4s.{JObject, JValue}

/**
  * Created by antoniogarrote on 18/07/2017.
  */

trait JSONLDParser {

  def extractSubject(jsonld: JObject): Option[String] = {
    val JObject(properties) = jsonld
    properties.find {
      case ("@id", _) => true
      case _ => false
    } match {
      case Some(("@id", JString(id))) => Some(id)
      case _ => None
    }
  }

  /**
    * Extracts all the IDs for a JSON-LD node property
    * @param json JSON value
    * @param toFind property to find
    * @return
    */
  def extractIds(json: JValue, toFind: String): List[String] =
    extractObjects(json, toFind).map {
      case node@JObject(_) =>
        extractObject(node, "@id") match {
          case Some(JString(id)) => Some(id)
          case _ => None
        }
      case _ => None
    }.filter(_.isDefined)
      .map(_.get)


  /**
    * Extracts the first ID for a JSON-LD node property
    * @param json JSON Value
    * @param toFind property to find
    * @return
    */
  def extractId(json: JValue, toFind: String): Option[String] = extractIds(json, toFind).headOption

  /**
    * Extracts a value from an expanded JSON-LD node
    * @param json JSON-LD resource
    * @param toFind property to find in the resource
    * @return Optional value found
    */
  def extractValues(json: JValue, toFind: String): List[JValue] =
    extractObjects(json, toFind)
    .map {
      case JObject(values) =>
        values.map {
          case ("@value", jvalue) => Some(jvalue)
          case _ => None
        }.find(_.isDefined)
      case _ => None
    }.filter(_.isDefined)
     .map(_.get.get)

  /**
    * Extracts a single value by property from a JSON-LD resource node
    * @param json JSON-LD resource node
    * @param toFind property to find
    * @return
    */
  def extractValue(json: JValue, toFind: String): Option[JValue] = extractValues(json, toFind).headOption

  private def extractObjects(json: JValue, toFind: String): List[JValue] =
    json match {
      case JObject(obj) =>
        val found = obj.filter { case (property, _) => property == toFind }
        if (found.isEmpty) {
          List.empty
        } else {
          found.head match {
            case (_,JArray(values)) => values
            case (_,v) => List(v)
            case _ => List.empty
          }
        }
      case _ => List.empty
    }

  private def extractObject(json: JValue, toFind: String): Option[JValue] = extractObjects(json, toFind).headOption

}
