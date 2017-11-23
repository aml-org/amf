package amf.plugins.document.webapi.parser

import amf.framework.parser._
import amf.plugins.document.webapi.parser.FragmentTypes._
import org.yaml.model.YMap

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class FragmentType(headerText: String)

object FragmentTypes {
  object DataTypeFragment          extends FragmentType("DataType")
  object ResourceTypeFragment      extends FragmentType("ResourceType")
  object TraitFragment             extends FragmentType("Trait")
  object AnnotationTypeFragment    extends FragmentType("AnnotationTypeDeclaration")
  object DocumentationItemFragment extends FragmentType("DocumentationItem")
  object SecuritySchemeFragment    extends FragmentType("SecurityScheme")
  object NamedExampleFragment      extends FragmentType("NamedExample")
  object UnknownFragment           extends FragmentType("?")
  def apply(map: YMap): FragmentType = FragmentTypeDetection(map).detect()
}

case class FragmentTypeDetection(map: YMap) {

  def detect(): FragmentType = {
    val matchingTypes = ListBuffer[FragmentType]()

    map
      .regex(
        "default|schema|example|examples|facets|xml|enum|properties|" +
          "minProperties|maxProperties|additionalProperties|discriminator|discriminatorValue|" +
          "uniqueItems|items|minItems|maxItems|minLength|maxLength|pattern|minimum|maximum|" +
          "format|multipleOf|fileTypes")
      .headOption
      .foreach(_ => matchingTypes += DataTypeFragment)

    // This can conflict with a web api linked through a overlay/extension and having
    // also title.
    // Both properties must appear and be the only properties in a
    // documentation item
    if (map.key("title").isDefined && map.key("content").isDefined) {
      matchingTypes += DocumentationItemFragment
    }

    map.key("allowedTargets").foreach(_ => matchingTypes += AnnotationTypeFragment)

    map.regex("headers|queryParameters").headOption.foreach(_ => matchingTypes += TraitFragment)

    map.regex("get|patch|put|post|delete|options|head").headOption.foreach(_ => matchingTypes += ResourceTypeFragment)

    map.entries
      .flatMap(e => e.value.toOption[YMap])
      .flatMap(m => m.regex("value|strict"))
      .headOption
      .foreach(_ => matchingTypes += NamedExampleFragment)

    val entries = map.regex("describedBy|settings|x-describedBy|x-settings")
    if (entries.isEmpty)
      map
        .key("type")
        .filter(
          f =>
            List("OAuth 1.0",
                 "OAuth 2.0",
                 "Basic Authentication",
                 "Digest Authentication",
                 "Pass Through",
                 "basic",
                 "apiKey",
                 "oauth2").contains(f.value.toString))
        .foreach(_ => matchingTypes += SecuritySchemeFragment)
    else
      matchingTypes += SecuritySchemeFragment

    /** If none, or more than one know fragment type applies to the given fields, will return unknow for safety.
      * Only return the found type if one and only one matches
      */
    matchingTypes.size match {
      case s if s == 1 => matchingTypes.head
      case _           => UnknownFragment
    }
  }
}
