package amf.dialects
import amf.dialects._;
import amf.model.AmfScalar;
object RAML_1_0_VocabularyTopLevel {
case class VocabularyObject(val entity: DomainEntity=DomainEntity(Vocabulary),override val parent:Option[TopLevelObject]=None) extends TopLevelObject(entity,parent){
  def base():Option[String]= entity.string(Vocabulary.base)
  def withBase(value:String):VocabularyObject= {entity.set(Vocabulary.base.field , AmfScalar(value)); this}
  def dialect():Option[String]= entity.string(Vocabulary.dialectProperty)
  def withDialect(value:String):VocabularyObject= {entity.set(Vocabulary.dialectProperty.field , AmfScalar(value)); this}
  def version():Option[String]= entity.string(Vocabulary.version)
  def withVersion(value:String):VocabularyObject= {entity.set(Vocabulary.version.field , AmfScalar(value)); this}
  def usage():Option[String]= entity.string(Vocabulary.usage)
  def withUsage(value:String):VocabularyObject= {entity.set(Vocabulary.usage.field , AmfScalar(value)); this}
  def external():Seq[ExternalObject]= entity.entities(Vocabulary.externals).map(ExternalObject(_,Some(this)))
  def withExternal(value:ExternalObject):VocabularyObject= {entity.add(Vocabulary.externals.field , value.entity); this}
  def classTerms():Seq[ClassObject]= entity.entities(Vocabulary.classTerms).map(ClassObject(_,Some(this)))
  def withClassTerms(value:ClassObject):VocabularyObject= {entity.add(Vocabulary.classTerms.field , value.entity); this}
  def propertyTerms():Seq[PropertyObject]= entity.entities(Vocabulary.propertyTerms).map(PropertyObject(_,Some(this)))
  def withPropertyTerms(value:PropertyObject):VocabularyObject= {entity.add(Vocabulary.propertyTerms.field , value.entity); this}
}

case class ExternalObject(val entity: DomainEntity=DomainEntity(External),override val parent:Option[TopLevelObject]=None) extends TopLevelObject(entity,parent){
  def name():Option[String]= entity.string(External.name)
  def withName(value:String):ExternalObject= {entity.set(External.name.field , AmfScalar(value)); this}
  def uri():Option[String]= entity.string(External.uri)
  def withUri(value:String):ExternalObject= {entity.set(External.uri.field , AmfScalar(value)); this}
}

case class ClassObject(val entity: DomainEntity=DomainEntity(ClassTerm),override val parent:Option[TopLevelObject]=None) extends TopLevelObject(entity,parent){
  def id():Option[String]= entity.string(ClassTerm.idProperty)
  def withId(value:String):ClassObject= {entity.set(ClassTerm.idProperty.field , AmfScalar(value)); this}
  def displayName():Option[String]= entity.string(ClassTerm.displayName)
  def withDisplayName(value:String):ClassObject= {entity.set(ClassTerm.displayName.field , AmfScalar(value)); this}
  def description():Option[String]= entity.string(ClassTerm.description)
  def withDescription(value:String):ClassObject= {entity.set(ClassTerm.description.field , AmfScalar(value)); this}
  def `extends`():Seq[String]= entity.strings(ClassTerm.`extends`)
  def withExtends(value:String):ClassObject= {entity.add(ClassTerm.`extends`.field , AmfScalar(value)); this}
  def resolvedExtends():Seq[ClassObject]= resolveReferences(ClassTerm.`extends`,
(r,s)=>{r.asInstanceOf[VocabularyObject].classTerms.find(_.entity.id==s)}
 ,e=>ClassObject(e,Some(this)))
  def properties():Seq[String]= entity.strings(ClassTerm.properties)
  def withProperties(value:String):ClassObject= {entity.add(ClassTerm.properties.field , AmfScalar(value)); this}
  def resolvedProperties():Seq[PropertyObject]= resolveReferences(ClassTerm.properties,
(r,s)=>{r.asInstanceOf[VocabularyObject].propertyTerms.find(_.entity.id==s)}
 ,e=>PropertyObject(e,Some(this)))
}

case class PropertyObject(val entity: DomainEntity=DomainEntity(PropertyTerm),override val parent:Option[TopLevelObject]=None) extends TopLevelObject(entity,parent){
  def id():Option[String]= entity.string(PropertyTerm.idProperty)
  def withId(value:String):PropertyObject= {entity.set(PropertyTerm.idProperty.field , AmfScalar(value)); this}
  def description():Option[String]= entity.string(PropertyTerm.description)
  def withDescription(value:String):PropertyObject= {entity.set(PropertyTerm.description.field , AmfScalar(value)); this}
  def domain():Seq[String]= entity.strings(PropertyTerm.domain)
  def withDomain(value:String):PropertyObject= {entity.add(PropertyTerm.domain.field , AmfScalar(value)); this}
  def resolvedDomain():Seq[ClassObject]= resolveReferences(PropertyTerm.domain,
(r,s)=>{r.asInstanceOf[VocabularyObject].classTerms.find(_.entity.id==s)}
 ,e=>ClassObject(e,Some(this)))
  def range():Seq[String]= entity.strings(PropertyTerm.range)
  def withRange(value:String):PropertyObject= {entity.add(PropertyTerm.range.field , AmfScalar(value)); this}
  def resolvedRange():Seq[ClassObject]= resolveReferences(PropertyTerm.range,
(r,s)=>{r.asInstanceOf[VocabularyObject].classTerms.find(_.entity.id==s)}
 ,e=>ClassObject(e,Some(this)))
  def `extends`():Seq[String]= entity.strings(PropertyTerm.`extends`)
  def withExtends(value:String):PropertyObject= {entity.add(PropertyTerm.`extends`.field , AmfScalar(value)); this}
  def resolvedExtends():Seq[PropertyObject]= resolveReferences(PropertyTerm.`extends`,
(r,s)=>{r.asInstanceOf[VocabularyObject].propertyTerms.find(_.entity.id==s)}
 ,e=>PropertyObject(e,Some(this)))
}

}