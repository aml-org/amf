package amf.spec.common

import amf.domain.extensions.{CustomDomainProperty, DomainExtension}
import amf.domain.{Annotations, DomainElement}
import amf.parser.YValueOps
import org.yaml.model._

import scala.collection.mutable.ListBuffer

case class AnnotationParser(element: DomainElement, map: YMap) {
  def parse(): Unit = {
    val domainExtensions: ListBuffer[DomainExtension] = ListBuffer()
    map.entries.foreach { entry =>
      val key = entry.key.value.toScalar.text
      if (WellKnownAnnotation.normalAnnotation(key)) {
        domainExtensions += ExtensionParser(key, element.id, entry).parse()
      }
    }
    if (domainExtensions.nonEmpty)
      element.withCustomDomainProperties(domainExtensions)
  }
}

case class ExtensionParser(annotationRamlName: String, parent: String, entry: YMapEntry) {
  def parse(): DomainExtension = {
    val domainExtension = DomainExtension()
    val annotationName  = WellKnownAnnotation.parseName(annotationRamlName)
    val dataNode        = DataNodeParser(entry.value, parent = Some(parent + s"/$annotationName")).parse()
    // TODO
    // this is temporary, we should look for the annotation in the annotationTypes declared in the schema
    val customDomainProperty = CustomDomainProperty(Annotations(entry)).withName(annotationName)
    domainExtension.adopted(parent)
    domainExtension
      .withExtension(dataNode)
      .withDefinedBy(customDomainProperty)
  }
}
