package amf.spec.common

import amf.compiler.ParsedReference
import amf.document.Fragment.Fragment
import amf.document.{BaseUnit, DeclaresModel, Document}
import amf.domain.Annotation.{Aliases, ExplicitField}
import amf.domain.`abstract`._
import amf.domain.dialects.DomainEntity
import amf.domain._
import amf.metadata.domain.`abstract`.ParametrizedDeclarationModel
import amf.metadata.domain._
import amf.metadata.shape.{PropertyDependenciesModel, XMLSerializerModel}
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.shape.{PropertyDependencies, PropertyShape, XMLSerializer}
import amf.spec.Declarations
import amf.spec.raml.RamlTypeParser
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar, YSequence, YType, YValue}

import scala.collection.mutable

/**
  * Base spec parser.
  */
private[spec] trait BaseSpecParser {

  implicit val spec: SpecParserContext

  case class OrganizationParser(map: YMap) {
    def parse(): Organization = {

      val organization = Organization(map)

      map.key("url", entry => {
        val value = ValueNode(entry.value)
        organization.set(OrganizationModel.Url, value.string(), Annotations(entry))
      })

      map.key("name", entry => {
        val value = ValueNode(entry.value)
        organization.set(OrganizationModel.Name, value.string(), Annotations(entry))
      })

      map.key("email", entry => {
        val value = ValueNode(entry.value)
        organization.set(OrganizationModel.Email, value.string(), Annotations(entry))
      })

      AnnotationParser(() => organization, map).parse()

      organization
    }
  }

  case class LicenseParser(map: YMap) {
    def parse(): License = {
      val license = License(map)

      map.key("url", entry => {
        val value = ValueNode(entry.value)
        license.set(LicenseModel.Url, value.string(), Annotations(entry))
      })

      map.key("name", entry => {
        val value = ValueNode(entry.value)
        license.set(LicenseModel.Name, value.string(), Annotations(entry))
      })

      AnnotationParser(() => license, map).parse()

      license
    }
  }

  case class ShapeDependenciesParser(map: YMap, properties: mutable.ListMap[String, PropertyShape]) {
    def parse(): Seq[PropertyDependencies] = {
      map.entries.flatMap(entry => NodeDependencyParser(entry, properties).parse())
    }
  }

  case class NodeDependencyParser(entry: YMapEntry, properties: mutable.ListMap[String, PropertyShape]) {
    def parse(): Option[PropertyDependencies] = {

      properties
        .get(entry.key.value.toScalar.text)
        .map(p => {
          PropertyDependencies(entry)
            .set(PropertyDependenciesModel.PropertySource, AmfScalar(p.id), Annotations(entry.key))
            .set(PropertyDependenciesModel.PropertyTarget, AmfArray(targets()), Annotations(entry.value))
        })
    }

    private def targets(): Seq[AmfScalar] = {
      ArrayNode(entry.value.value.toSequence)
        .strings()
        .scalars
        .flatMap(v => properties.get(v.value.toString).map(p => AmfScalar(p.id, v.annotations)))
    }
  }

  case class XMLSerializerParser(defaultName: String, map: YMap) {
    def parse(): XMLSerializer = {
      val serializer = XMLSerializer(map)
        .set(XMLSerializerModel.Attribute, value = false)
        .set(XMLSerializerModel.Wrapped, value = false)
        .set(XMLSerializerModel.Name, defaultName)

      map.key(
        "attribute",
        entry => {
          val value = ValueNode(entry.value)
          serializer.set(XMLSerializerModel.Attribute, value.boolean(), Annotations(entry) += ExplicitField())
        }
      )

      map.key("wrapped", entry => {
        val value = ValueNode(entry.value)
        serializer.set(XMLSerializerModel.Wrapped, value.boolean(), Annotations(entry) += ExplicitField())
      })

      map.key("name", entry => {
        val value = ValueNode(entry.value)
        serializer.set(XMLSerializerModel.Name, value.string(), Annotations(entry) += ExplicitField())
      })

      map.key("namespace", entry => {
        val value = ValueNode(entry.value)
        serializer.set(XMLSerializerModel.Namespace, value.string(), Annotations(entry))
      })

      map.key("prefix", entry => {
        val value = ValueNode(entry.value)
        serializer.set(XMLSerializerModel.Prefix, value.string(), Annotations(entry))
      })

      serializer
    }
  }

  case class ReferenceDeclarations(references: mutable.Map[String, BaseUnit] = mutable.Map(),
                                   declarations: Declarations = Declarations()) {

    def +=(alias: String, unit: BaseUnit): Unit = {
      references += (alias -> unit)
      val library = declarations.getOrCreateLibrary(alias)
      // todo : ignore domain entities of vocabularies?
      unit match {
        case d: DeclaresModel =>
          d.declares
            .filter({
              case _: DomainEntity => false
              case _               => true
            })
            .foreach(library += _)
      }
    }

    def +=(url: String, fragment: Fragment): Unit = {
      references += (url   -> fragment)
      declarations += (url -> fragment)
    }

    def +=(url: String, fragment: Document): Unit = references += (url -> fragment)

    def solvedReferences(): Seq[BaseUnit] = references.values.toSet.toSeq
  }

  case class ReferencesParser(key: String, map: YMap, references: Seq[ParsedReference]) {
    def parse(): ReferenceDeclarations = {
      val result: ReferenceDeclarations = parseLibraries()

      references.foreach {
        case ParsedReference(f: Fragment, s: String) => result += (s, f)
        case ParsedReference(d: Document, s: String) => result += (s, d)
        case _                                       =>
      }

      result
    }

    private def target(url: String): Option[BaseUnit] =
      references.find(r => r.parsedUrl.equals(url)).map(_.baseUnit)

    private def parseLibraries(): ReferenceDeclarations = {
      val result = ReferenceDeclarations()

      map.key(
        key,
        entry =>
          entry.value.value.toMap.entries.foreach(e => {
            val alias: String = e.key
            val url: String   = e.value
            target(url).foreach {
              case module: DeclaresModel => result += (alias, addAlias(module, alias)) // this is
              case other =>
                throw new Exception(s"Expected module but found: $other") // todo Uses should only reference modules...
            }
          })
      )

      result
    }

    private def addAlias(module: BaseUnit, alias: String): BaseUnit = {
      val aliasesOption = module.annotations.find(classOf[Aliases])
      if (aliasesOption.isDefined)
        aliasesOption.foreach(a => {
          module.annotations.reject(_.isInstanceOf[Aliases])
          module.add(a.copy(aliases = a.aliases ++ Seq(alias)))
        })
      else
        module.add(Aliases(Seq(alias)))

      module
    }
  }

  def parseResourceTypeDeclarations(key: String,
                                    map: YMap,
                                    customProperties: String,
                                    declarations: Declarations): Unit = {

    map.key(
      key,
      e => {
        e.value.value.toMap.entries.map(
          resourceEntry =>
            declarations += AbstractDeclarationParser(ResourceType(resourceEntry),
                                                      customProperties,
                                                      resourceEntry,
                                                      declarations).parse())
      }
    )
  }

  def parseTraitDeclarations(key: String, map: YMap, customProperties: String, declarations: Declarations): Unit = {
    map.key(
      key,
      e => {
        e.value.value.toMap.entries.map(traitEntry =>
          declarations += AbstractDeclarationParser(Trait(traitEntry), customProperties, traitEntry, declarations)
            .parse())
      }
    )
  }

  object AbstractDeclarationParser {

    def apply(declaration: AbstractDeclaration,
              parent: String,
              entry: YMapEntry,
              declarations: Declarations): AbstractDeclarationParser =
      new AbstractDeclarationParser(declaration, parent, entry.key.value.toScalar.text, entry.value, declarations)
  }

  case class AbstractDeclarationParser(declaration: AbstractDeclaration,
                                       parent: String,
                                       key: String,
                                       entryValue: YNode,
                                       declarations: Declarations) {
    def parse(): AbstractDeclaration = {

      spec.link(entryValue) match {
        case Left(link) => parseReferenced(declaration, link, Annotations(entryValue))
        case Right(value) =>
          val variables = AbstractVariables()
          val dataNode  = DataNodeParser(value, variables, Some(parent + s"/$key")).parse()

          declaration.withName(key).adopted(parent).withDataNode(dataNode)

          variables.ifNonEmpty(p => declaration.withVariables(p))

          declaration
      }
    }

    def parseReferenced(declared: AbstractDeclaration,
                        parsedUrl: String,
                        annotations: Annotations): AbstractDeclaration = {
      val d = declared match {
        case _: Trait        => declarations.findTrait(parsedUrl)
        case _: ResourceType => declarations.findResourceType(parsedUrl)
      }
      d.map { a =>
          val copied: AbstractDeclaration = a.link(parsedUrl, annotations)
          copied.withName(key)
        }
        .getOrElse(throw new IllegalStateException("Could not find abstract declaration in references map for link"))
    }
  }

  case class ParametrizedDeclarationParser(value: YValue,
                                           producer: String => ParametrizedDeclaration,
                                           declarations: Map[String, AbstractDeclaration]) {
    def parse(): ParametrizedDeclaration = {
      value match {
        case map: YMap =>
          // TODO is it always the first child?
          val entry = map.entries.head

          val name = entry.key.value.toScalar.text
          val declaration =
            producer(name).add(Annotations(value)).set(ParametrizedDeclarationModel.Target, declarations(name).id)
          val variables = entry.value.value.toMap.entries.map(
            variableEntry =>
              VariableValue(variableEntry)
                .withName(variableEntry.key.value.toScalar.text)
                .withValue(variableEntry.value.value.toScalar.text))

          declaration.withVariables(variables)
        case scalar: YScalar =>
          producer(scalar.text)
            .add(Annotations(value))
            .set(ParametrizedDeclarationModel.Target, declarations(scalar.text).id)
        case _ => throw new Exception("Invalid model extension.")
      }
    }
  }

  case class RamlParametersParser(map: YMap, producer: String => Parameter, declarations: Declarations) {
    def parse(): Seq[Parameter] =
      map.entries
        .map(entry => RamlParameterParser(entry, producer, declarations).parse())
  }

  case class RamlParameterParser(entry: YMapEntry, producer: String => Parameter, declarations: Declarations) {
    def parse(): Parameter = {

      val name      = entry.key.value.toScalar.text
      val parameter = producer(name).add(Annotations(entry)) // TODO parameter id is using a name that is not final.
      val map       = entry.value.value.toMap

      map.key("required", entry => {
        val value = ValueNode(entry.value)
        parameter.set(ParameterModel.Required, value.boolean(), Annotations(entry) += ExplicitField())
      })

      if (parameter.fields.entry(ParameterModel.Required).isEmpty) {
        val required = !name.endsWith("?")

        parameter.set(ParameterModel.Required, required)
        parameter.set(ParameterModel.Name, if (required) name else name.stripSuffix("?"))
      }

      map.key("description", entry => {
        val value = ValueNode(entry.value)
        parameter.set(ParameterModel.Description, value.string(), Annotations(entry))
      })

      RamlTypeParser(entry, shape => shape.withName("schema").adopted(parameter.id), declarations)
        .parse()
        .foreach(parameter.set(ParameterModel.Schema, _, Annotations(entry)))

      AnnotationParser(() => parameter, map).parse()

      parameter
    }
  }

  case class RamlResponseParser(entry: YMapEntry, producer: (String) => Response, declarations: Declarations) {
    def parse(): Response = {

      val node = ValueNode(entry.key)

      val response = producer(node.string().value.toString).add(Annotations(entry))
      val map      = entry.value.value.toMap

      response.set(ResponseModel.StatusCode, node.string())

      map.key("description", entry => {
        val value = ValueNode(entry.value)
        response.set(ResponseModel.Description, value.string(), Annotations(entry))
      })

      map.key(
        "headers",
        entry => {
          val parameters: Seq[Parameter] =
            RamlParametersParser(entry.value.value.toMap, response.withHeader, declarations)
              .parse()
              .map(_.withBinding("header"))
          response.set(RequestModel.Headers, AmfArray(parameters, Annotations(entry.value)), Annotations(entry))
        }
      )

      map.key(
        "body",
        entry => {
          val payloads = mutable.ListBuffer[Payload]()

          val payload = Payload()
          payload.adopted(response.id) // TODO review

          RamlTypeParser(entry, shape => shape.withName("default").adopted(payload.id), declarations)
            .parse()
            .foreach(payloads += payload.withSchema(_))

          entry.value.value match {
            case map: YMap =>
              map.regex(
                ".*/.*",
                entries => {
                  entries.foreach(entry => {
                    payloads += RamlPayloadParser(entry, response.withPayload, declarations).parse()
                  })
                }
              )
            case _ =>
          }
          if (payloads.nonEmpty)
            response.set(RequestModel.Payloads, AmfArray(payloads, Annotations(entry.value)), Annotations(entry))
        }
      )

      AnnotationParser(() => response, map).parse()

      response
    }
  }

  case class RamlPayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload, declarations: Declarations) {
    def parse(): Payload = {

      val payload = producer(Some(ValueNode(entry.key).string().value.toString)).add(Annotations(entry))

      entry.value.value match {
        case map: YMap =>
          // TODO
          // Should we clean the annotations here so they are not parsed again in the shape?
          AnnotationParser(() => payload, map).parse()
        case _ =>
      }

      entry.value.tag.tagType match {
        case YType.Null =>
        case _ =>
          RamlTypeParser(entry, shape => shape.withName("schema").adopted(payload.id), declarations)
            .parse()
            .foreach(payload.withSchema)

      }
      payload
    }
  }

  case class ArrayNode(ast: YSequence) {

    def strings(): AmfArray = {
      val elements = ast.nodes.map(child => ValueNode(child).string())
      AmfArray(elements, annotations())
    }

    private def annotations() = Annotations(ast)
  }

  case class ValueNode(ast: YNode) {

    def string(): AmfScalar = {
      val content = scalar.text
      AmfScalar(content, annotations())
    }

    def integer(): AmfScalar = {
      val content = scalar.text
      AmfScalar(content.toInt, annotations())
    }

    def boolean(): AmfScalar = {
      val content = scalar.text
      AmfScalar(content.toBoolean, annotations())
    }

    def negated(): AmfScalar = {
      val content = scalar.text
      AmfScalar(!content.toBoolean, annotations())
    }

    private def scalar = ast.value.toScalar

    private def annotations() = Annotations(ast)
  }

  case class OasCreativeWorkParser(map: YMap) {
    def parse(): CreativeWork = {

      val creativeWork = CreativeWork(map)

      map.key("url", entry => {
        val value = ValueNode(entry.value)
        creativeWork.set(CreativeWorkModel.Url, value.string(), Annotations(entry))
      })

      map.key("description", entry => {
        val value = ValueNode(entry.value)
        creativeWork.set(CreativeWorkModel.Description, value.string(), Annotations(entry))
      })

      map.key("x-title", entry => {
        val value = ValueNode(entry.value)
        creativeWork.set(CreativeWorkModel.Title, value.string(), Annotations(entry))
      })

      AnnotationParser(() => creativeWork, map).parse()

      creativeWork
    }
  }

  case class RamlCreativeWorkParser(map: YMap, withExtention: Boolean) {
    def parse(): CreativeWork = {

      val documentation = CreativeWork(Annotations(map))

      map.key("title", entry => {
        val value = ValueNode(entry.value)
        documentation.set(CreativeWorkModel.Title, value.string(), Annotations(entry))
      })

      map.key("content", entry => {
        val value = ValueNode(entry.value)
        documentation.set(CreativeWorkModel.Description, value.string(), Annotations(entry))
      })

      if (withExtention)
        map.key("(url)", entry => {
          val value = ValueNode(entry.value)
          documentation.set(CreativeWorkModel.Url, value.string(), Annotations(entry))
        })
      else
        map.key("url", entry => {
          val value = ValueNode(entry.value)
          documentation.set(CreativeWorkModel.Url, value.string(), Annotations(entry))
        })
      documentation
    }
  }

}

trait SpecParserContext {
  def link(node: YNode): Either[String, YNode]
}
