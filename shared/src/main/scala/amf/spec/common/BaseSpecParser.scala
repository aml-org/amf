package amf.spec.common

import amf.compiler.ParsedReference
import amf.document.Fragment.Fragment
import amf.document.{BaseUnit, Module}
import amf.domain.Annotation.ExplicitField
import amf.domain.`abstract`._
import amf.domain.{Annotations, CreativeWork, License, Organization}
import amf.metadata.domain.`abstract`.ParametrizedDeclarationModel
import amf.metadata.domain.{CreativeWorkModel, LicenseModel, OrganizationModel}
import amf.metadata.shape.{PropertyDependenciesModel, XMLSerializerModel}
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.shape.{PropertyDependencies, PropertyShape, XMLSerializer}
import amf.spec.Declarations
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar, YSequence, YValue}

import scala.collection.mutable

/**
  * Base spec parser.
  */
private[spec] trait BaseSpecParser {

  implicit val spec: SpecParserContext

  case class CreativeWorkParser(map: YMap) {
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

      AnnotationParser(() => creativeWork, map).parse()

      creativeWork
    }
  }

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

    def +=(alias: String, module: Module): Unit = {
      references += (alias -> module)
      val library = declarations.getOrCreateLibrary(alias)
      module.declares.foreach(library += _)
    }

    def +=(url: String, fragment: Fragment): Unit = {
      references += (url   -> fragment)
      declarations += (url -> fragment)
    }

  }

  case class ReferencesParser(key: String, map: YMap, references: Seq[ParsedReference]) {
    def parse(): ReferenceDeclarations = {
      val result: ReferenceDeclarations = parseLibraries()

      references.foreach {
        case ParsedReference(f: Fragment, s: String) => result += (s, f)
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
//              case module: Module => result.references += (key -> module)
              // todo: we shoul only return modules and not any type of base unit
              bu: BaseUnit =>
                result.references += (alias -> bu)
            }
          })
      )

      result
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
          val parameters = AbstractVariables()
          val dataNode   = DataNodeParser(value, parameters, Some(parent + s"/$key")).parse()

          declaration.withName(key).adopted(parent).withDataNode(dataNode)

          parameters.ifNonEmpty(p => declaration.withVariables(p))

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
          val copied: AbstractDeclaration = a.link(Some(parsedUrl), Some(annotations))
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

}

trait SpecParserContext {
  def link(node: YNode): Either[String, YNode]
}
