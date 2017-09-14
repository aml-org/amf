package amf.spec.dialect

import amf.common.AMFToken.SequenceToken
import amf.compiler.Root
import amf.dialects.{Dialect, DialectNode, DialectPropertyMapping, DialectRegistry}
import amf.document.Document
import amf.domain.{Annotations, DomainElement, Fields}
import amf.metadata.Type
import amf.model.{AmfArray, AmfScalar}
import amf.spec.raml.{ArrayNode, Entries, ValueNode}

import scala.collection.mutable.ListBuffer

/**
  * Created by kor on 12/09/17.
  */

case class DomainEntity(linkValue: Option[String], definition: DialectNode, fields: Fields, annotations: Annotations) extends DomainElement {


  override def adopted(parent: String) = {
    if (Option(this.id).isEmpty) {
      linkValue match {
        case Some(link) =>
          parent.charAt(parent.length - 1) match {
            case '/' => withId(s"$parent$link")
            case '#' => withId(s"$parent$link")
            case _ => withId(s"$parent/$link")
          }
        case _          =>
          withId(parent)
      }
    }
    this
  }


  def string(m: DialectPropertyMapping): Option[String] = {
    val fieldValue = this.fields.get(m.field())
    fieldValue match {
      case scalar: AmfScalar => Some(scalar.toString)
      case _                 => None
    }
  }

  def addValue(m:DialectPropertyMapping,v:String): Unit ={
      this.add(m.field(),AmfScalar(v));
  }

  def strings(m:DialectPropertyMapping):Seq[String]={
    val q=this.fields.get(m.field());
    if (q.isInstanceOf[AmfScalar]){
      return List(q.asInstanceOf[AmfScalar].toString);
    }
    if (q.isInstanceOf[AmfArray]){
      val ar=q.asInstanceOf[AmfArray];
      var values:List[String]=List();
      ar.values.foreach(x=>{
        if (x.isInstanceOf[AmfScalar]){
          values=values.::(x.toString);
        }
      })
      return values;
    }
    return List();
  }
  def entities(m:DialectPropertyMapping):Seq[DomainEntity]={
    val q=this.fields.get(m.field());
    if (q.isInstanceOf[DomainEntity]){
      return List(q.asInstanceOf[DomainEntity]);
    }
    if (q.isInstanceOf[AmfArray]){
      val ar=q.asInstanceOf[AmfArray];
      return ar.values.filter(_.isInstanceOf[DomainEntity]).asInstanceOf[List[DomainEntity]];
    }
    return List();
  }

  def mapElementWithId(m:DialectPropertyMapping,id:String): Option[DomainEntity] ={
    val q=this.fields.get(m.field());
    if (q.isInstanceOf[AmfArray]) {
      val ar = q.asInstanceOf[AmfArray];
      return ar.values.filter(v=>v.isInstanceOf[DomainEntity]).asInstanceOf[List[DomainEntity]].find(x=>x.id==id);
    }
    return Option.empty;
  }

  override def dynamicTypes(): Seq[String] = definition.calcTypes(this).map(_.iri());
}

class DialectParser(val dialect: Dialect, val root: Root) {

  private val resolver = dialect.resolver.resolver(root)

  def parseDocument(): Document = {
    val dialectDocument = parse()

    Document()
      .adopted(root.location)
      .withEncodes(dialectDocument)
  }

  def parse(): DomainEntity =  parse(Entries(root.ast.last))

  def parse(entries: Entries): DomainEntity = {
    val entity = DomainEntity(null, dialect.root, Fields(), Annotations(entries.ast))
    entity.withId(root.location+"#");

    //root.ast.last
    parseNode(entries, entity)
    if (dialect.refiner!=null){
      dialect.refiner.refine(entity);
    }
    return entity;
  }

  def parseNode(entries: Entries, node: DomainEntity): Unit = {
    if (node.definition._id!=null){
      entries.key(node.definition._id,entry=>{
        if (entry.value != null) {
          val value = ValueNode(entry.value)
          value.string().value match {
            case base: String => node.withId(base);
          }
        }
      })
    }
    if (entries.entries.isEmpty) {
      var vl = ValueNode(entries.ast);
      node.definition._props().find(v => v._fromVal).foreach(f => {
        setScalar(node, f, vl);
      });
      return;
    }
    node.definition._props().foreach(f => {
      entries.key(f.name, entryNode => {

        if (f.isMap()) {
          val entries = new Entries(entryNode.value).entries
          val classTerms = ListBuffer[DomainEntity]()
          entries.foreach {
            case (classTermName, entry) =>
              val ent = new DomainEntity(classTermName, f.range.asInstanceOf[DialectNode], Fields(), Annotations(entry.ast));
              classTerms += ent;
              val field1 = f.field()
              node.add(field1, ent);
              ent.set(f._hash.field(), classTermName);
              parseNode(new Entries(entry.value), ent);
          }
        }
        else if (f.isCollection()) {
          entryNode.value.`type` match {
            case SequenceToken => {
              if (f.isScalar()) {
                if (f.range == Type.Str) {
                  val value = ArrayNode(entryNode.value)
                  node.set(f.field(), value.strings(), entryNode.annotations());
                }
                if (f.range == Type.Iri) {
                  val value = ArrayNode(entryNode.value)
                  val array = value.strings()
                  var scalars = array.values.map(AmfScalar(_))
                  node.set(f.field(), AmfArray(scalars.map(resolveValue(f, _))), entryNode.annotations());
                }
                else {
                  throw new IllegalStateException("Does not know how to parse sequences of other scalars yet");
                }
              }
              else {
                throw new IllegalStateException("Does not know how to parse sequences of instances yet");
              }
            }
            case _ => {
              if (f.isScalar()) {
                val resolvedVal = resolveValue(f,ValueNode(entryNode.value).string())
                node.setArray(f.field(),Seq(resolvedVal))
              }
              else {
                throw new IllegalStateException("Does not know how to parse sequences of instances yet");
              }
            }
          }
        }
        else {
          val value = ValueNode(entryNode.value);
          setScalar(node, f, value)
        }
      })
    })
  }

  private def resolveValue(f: DialectPropertyMapping, value: AmfScalar): AmfScalar = {
    if (f.isRef()) {
      return AmfScalar(resolver.resolve(root, value.toString(), f._referenceTarget), Annotations());
    }
    return value;
  }

  private def setScalar(node: DomainEntity, f: DialectPropertyMapping, value: ValueNode) = node.set(f.field(), resolveValue(f, value.string()));

}

object DialectParser {

  def apply(root: Root, dialects: DialectRegistry) = {
    val dialectDeclaration = root.ast.head.content
    dialects.get(dialectDeclaration) match {
      case Some(dialect) => new DialectParser(dialect,root)
      case _             => throw new Exception(s"Unknown dialect $dialectDeclaration")
    }
  }

}