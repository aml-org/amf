package amf.dialects

import amf.compiler.Root
import amf.domain.Annotation.SynthesizedField
import amf.domain.{Annotations, Fields}
import amf.domain.dialects.DomainEntity
import amf.model.AmfScalar
import amf.spec.dialects._
import amf.vocabulary.{Namespace, ValueType}

import scala.collection.mutable

/**
  * Created by Pavel Petrochenko on 12/09/17.
  */
class VocabPartDialect(override val shortName: String, namespace: Namespace = Namespace.Meta)
    extends DialectNode(shortName, namespace) {
  id = Some((namespace + shortName).iri())
}

case class Declaration(override val shortName: String, override val namespace: Namespace = Namespace.Meta)
    extends VocabPartDialect(shortName, namespace = namespace) {
  val idProperty: DialectPropertyMapping = str(
    "id",
    _.copy(namespace = Some(Namespace.Schema), jsonld = false, noRAML = true, scalaNameOverride = Some("idProperty")))
}

object ClassTerm extends Declaration("Class", Namespace.Owl) {
  val displayName: DialectPropertyMapping =
    str("displayName", _.copy(namespace = Some(Namespace.Schema), rdfName = Some("name")))
  val description: DialectPropertyMapping = str("description", _.copy(namespace = Some(Namespace.Schema)))

  val example: DialectPropertyMapping = str("example", _.copy(namespace = Some(Namespace.Schema), collection = true))

  val `extends`: DialectPropertyMapping = ref(
    "extends",
    ClassTerm,
    _.copy(collection = true, rdfName = Some("subClassOf"), namespace = Some(Namespace.Rdfs)))

  val `properties`: DialectPropertyMapping = ref("properties", PropertyTerm, _.copy(collection = true, jsonld = false))

}

object PropertyTerm extends Declaration("Property") {
  val displayName: DialectPropertyMapping =
    str("displayName", _.copy(namespace = Some(Namespace.Schema), rdfName = Some("name")))

  val description: DialectPropertyMapping = str("description", _.copy(namespace = Some(Namespace.Schema)))

  val example: DialectPropertyMapping = str("example", _.copy(namespace = Some(Namespace.Schema), collection = true))

  val domain: DialectPropertyMapping =
    ref("domain", ClassTerm, _.copy(collection = true, namespace = Some(Namespace.Rdfs), noRAML = true))
  val range: DialectPropertyMapping =
    ref("range", ClassTerm, _.copy(collection = true, namespace = Some(Namespace.Rdfs)))
  val `extends`: DialectPropertyMapping = ref(
    "extends",
    PropertyTerm,
    _.copy(collection = true, rdfName = Some("subPropertyOf"), namespace = Some(Namespace.Rdfs)))

  val DATATYPE_PROPERTY = ValueType("http://www.w3.org/2002/07/owl#DatatypeProperty")
  val OBJECT_PROPERTY   = ValueType("http://www.w3.org/2002/07/owl#ObjectProperty")

  fieldValueDiscriminator(range)
    .add(TypeBuiltins.ANY, DATATYPE_PROPERTY)
    .add(TypeBuiltins.STRING, DATATYPE_PROPERTY)
    .add(TypeBuiltins.INTEGER, DATATYPE_PROPERTY)
    .add(TypeBuiltins.NUMBER, DATATYPE_PROPERTY)
    .add(TypeBuiltins.FLOAT, DATATYPE_PROPERTY)
    .add(TypeBuiltins.BOOLEAN, DATATYPE_PROPERTY)
    .add(TypeBuiltins.URI, DATATYPE_PROPERTY)
    .defaultValue = Some(OBJECT_PROPERTY)
}

object External extends VocabPartDialect("External") {
  val name: DialectPropertyMapping = str("name")
  val uri: DialectPropertyMapping  = str("uri", _.copy(fromVal = true))
}
object NameSpaceImport extends VocabPartDialect("NameSpaceImport") {
  val name: DialectPropertyMapping = str("name")
  val uri: DialectPropertyMapping  = str("uri", _.copy(fromVal = true))
}


object Vocabulary extends VocabPartDialect("Vocabulary") {

  val base: DialectPropertyMapping = str("base")
  val vocabularyProperty: DialectPropertyMapping =
    str("vocabulary", _.copy(scalaNameOverride = Some("vocabularyProperty")))
  val version: DialectPropertyMapping = str("version")
  var usage: DialectPropertyMapping =
    str("usage", _.copy(namespace = Some(Namespace.Schema), rdfName = Some("description")))
  var externals: DialectPropertyMapping =
    map("external", External.name, External, _.copy(scalaNameOverride = Some("externals")))
  var uses: DialectPropertyMapping =
    map("uses", NameSpaceImport.name, NameSpaceImport, _.copy(scalaNameOverride = Some("externals")))

  var classTerms: DialectPropertyMapping =
    map("classTerms",
        ClassTerm.idProperty,
        ClassTerm,
        _.copy(rdfName = Some("classes"), noLastSegmentTrimInMaps = true))
  var propertyTerms: DialectPropertyMapping =
    map("propertyTerms",
        PropertyTerm.idProperty,
        PropertyTerm,
        _.copy(rdfName = Some("properties"), noLastSegmentTrimInMaps = true))

  var externalTerms: DialectPropertyMapping =
    map("externalTerms",
        PropertyTerm.idProperty,
        PropertyTerm,
        _.copy(rdfName = Some("externalEntities"), noRAML = true))

  withGlobalIdField("base")
  withType("http://www.w3.org/2002/07/owl#Ontology")

  nameProvider = {
    val provider: LocalNameProviderFactory = new BasicNameProvider(_, List(externals))
    Some(provider)
  }
}
class VocabularyRAMLRefiner extends RamlRefiner {
  def refine(voc: DomainEntity): Unit = {
    val vtop=new RAML_1_0_VocabularyTopLevel.VocabularyObject(voc,None);
    vtop.classTerms().foreach(c=>{
      var props=List[String]();
      vtop.propertyTerms().foreach(p=>{
          p.domain().foreach(d=>{
            if (d.equals(c.domainEntity.id)){
              props=props.::(p.entity.id);
            }
          })
      if (!c.domainEntity.fields.raw(ClassTerm.`properties`.field()).isDefined) {
        c.domainEntity.set(ClassTerm.`properties`.field(), props);
      }
    })
    })
  }
}
class VocabularyRefiner() extends Refiner {
  def refine(voc: DomainEntity, resolver: ReferenceResolver): Unit = {
    // term IDs can be external ones, we avoid using the full external URI as a postfix
    for {
      vocabularyTerm <- voc.entities(Vocabulary.classTerms) ++ voc.entities(Vocabulary.propertyTerms)
    } yield {
      if (vocabularyTerm.id.indexOf("#") != -1) {
        val fragment = vocabularyTerm.id.split("#").last
        if (fragment.indexOf(".") > -1) {
          resolver.resolveRef(fragment) match {
            case Some(resolvedId) => {
              vocabularyTerm.id = resolvedId
            }
            case None => // ignore
          }
        }
      }
    }

    resolver match {
      case basicResolver: BasicResolver =>
        val externalEntities = basicResolver.resolvedExternals.map { resolvedId =>
          val entity =
            new DomainEntity(None, new DialectNode("ExternalEntity", Namespace.Meta), Fields(), Annotations())
          entity.id = resolvedId
          entity
        }
        voc.setArrayWithoutId(Vocabulary.externalTerms.field(),
          collection.immutable.Seq(externalEntities.toSeq: _*).sortBy(_.id))
      case _ => // ignore
    }

    // Set the domain of properties based on the 'properties' property fo class terms
    for {
      classTerm <- voc.entities(Vocabulary.classTerms)
      property <- classTerm.strings(ClassTerm.`properties`)
      propertyTerm <- getOrCreateProperty(voc, property)
    } yield {
      propertyTerm.addValue(PropertyTerm.domain, classTerm.id)
    }
  }

  private def getOrCreateProperty(voc: DomainEntity, property: String) = {
    voc.mapElementWithId(Vocabulary.propertyTerms, property).orElse({
      val prop=new DomainEntity(None,PropertyTerm,Fields(),Annotations());
      prop.annotations.+=(SynthesizedField())
      prop.withId(property)

      voc.fields.addWithoutId(Vocabulary.propertyTerms.field(),prop);
      Some(prop)
    })
  }
}


  object VocabularyLanguageDefinition
    extends Dialect("RAML 1.0 Vocabulary",
      "",
      Vocabulary,
      resolver =
        (root: Root, uses, ctx) => new BasicResolver(root, List(Vocabulary.externals), uses)(ctx)) {
    jsonLDrefiner = {
      val ref = new VocabularyRefiner()
      Some(ref)
    }
    ramlRefiner=Some(new VocabularyRAMLRefiner())
  }


