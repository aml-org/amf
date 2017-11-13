package amf.spec.common

import amf.domain.extensions.{CustomDomainProperty, DomainExtension}
import amf.domain.{Annotations, DomainElement}
import org.yaml.model._
import amf.parser.YScalarYRead

import scala.collection.mutable.ListBuffer

case class AnnotationParser(lazyElement: () => DomainElement, map: YMap) {
  def parse(): Unit = {
    val domainExtensions: ListBuffer[DomainExtension] = ListBuffer()
    map.entries.foreach { entry =>
      val key = entry.key.as[YScalar].text
      if (WellKnownAnnotation.normalAnnotation(key)) {
        domainExtensions += ExtensionParser(key, lazyElement().id, entry).parse()
      }
    }
    if (domainExtensions.nonEmpty)
      lazyElement().withCustomDomainProperties(domainExtensions)
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
      .withName(annotationName)
  }
}
