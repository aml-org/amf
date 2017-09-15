package amf.spec

import amf.common.core.Strings
import amf.domain.Annotation.ExplicitField
import amf.domain.{Annotations, CreativeWork, License, Organization}
import amf.metadata.domain.{CreativeWorkModel, LicenseModel, OrganizationModel}
import amf.metadata.shape.{PropertyDependenciesModel, XMLSerializerModel}
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.shape.{PropertyDependencies, PropertyShape, XMLSerializer}
import org.yaml.model.{YMap, YMapEntry, YNode, YSequence}

import scala.collection.mutable

/**
  * Created by pedro.colunga on 9/15/17.
  */
private[spec] object BaseSpecParser {

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
        .get(entry.key.value.toScalar.text.unquote)
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

  case class ArrayNode(ast: YSequence) {

    def strings(): AmfArray = {
      val elements = ast.nodes.map(child => ValueNode(child).string())
      AmfArray(elements, annotations())
    }

    private def annotations() = Annotations(ast)
  }

  case class ValueNode(ast: YNode) {

    def string(): AmfScalar = {
      val content = scalar.text.unquote
      AmfScalar(content, annotations())
    }

    def integer(): AmfScalar = {
      val content = scalar.text.unquote
      AmfScalar(content.toInt, annotations())
    }

    def boolean(): AmfScalar = {
      val content = scalar.text.unquote
      AmfScalar(content.toBoolean, annotations())
    }

    def negated(): AmfScalar = {
      val content = scalar.text.unquote
      AmfScalar(!content.toBoolean, annotations())
    }

    private def scalar = ast.value.toScalar

    private def annotations() = Annotations(ast)
  }

}
