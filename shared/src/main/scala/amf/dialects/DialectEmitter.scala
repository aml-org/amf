package amf.dialects

import amf.common.AMFAST
import amf.common.AMFToken.{Comment, Root}
import amf.document.{BaseUnit, Document}
import amf.domain.FieldEntry
import amf.model.{AmfArray, AmfScalar}
import amf.parser.Position
import amf.spec.dialect.DomainEntity
import amf.spec.{ASTEmitterHelper, Emitter}

/**
  * Created by kor on 13/09/17.
  */
class DialectEmitter (val unit: BaseUnit) extends ASTEmitterHelper{

  val _root:DomainEntity=retrieveDomainEntity(unit);
  var _np:LocalNameProvider=_;

  if (_root.definition.nameProvider!=null) {
     _np=_root.definition.nameProvider(_root);
  }

  private def retrieveDomainEntity(unit:BaseUnit) = unit match {
    case document: Document => document.encodes.asInstanceOf[DomainEntity]
  }

  case class RefValueEmitter(val parent:DialectPropertyMapping,key: String, f: FieldEntry) extends Emitter {
    override def emit(): Unit = {
      sourceOr(f.value, entry { () =>
        raw(key)
        emitRef(parent,f.scalar.toString)
      })
    }

    override def position(): Position = pos(f.value.annotations)
  }

  /** Emit a single value from an array as an entry. */
  case class RefArrayValueEmitter(parent:DialectPropertyMapping,key: String, f: FieldEntry) extends Emitter {
    override def emit(): Unit = {
      try {
        sourceOr(f.value, entry { () =>
          raw(key)
          //raw("A")
          if (f.array.values.size == 1) {
            val scalar = f.array.values.head.asInstanceOf[AmfScalar]
            emitRef(parent,scalar.toString)

          }
          else array(() => {
            f.array.values.foreach(v => {
              emitRef(parent,v.asInstanceOf[AmfScalar].toString);
              ;
            })
          })
        })
      }catch {
        case e:Exception=>e.printStackTrace()
      }
    }

    override def position(): Position = pos(f.value.annotations)
  }

  private def emitRef(dialectPropertyMapping: DialectPropertyMapping,string: String) = {
      if (_np==null){
        raw(string)

      }
      else {
        val name = _np.localName(string, dialectPropertyMapping);
        if (name == null) {
          raw(string)
        }
        else raw(name);
      }
  }

  def  emit():AMFAST={
    emitter.root(Root) { () =>
      raw(_root.definition._dialect.header.substring(1), Comment)
      ObjectEmitter(_root).emit();
    }
  }

  def createEmitter(obj:DomainEntity,p:DialectPropertyMapping): Emitter ={
    var res:Emitter=null;
    val field = p.field()
    if (p._noRAML){
      return null;
    }
    var vl=obj.fields.get(field);
    if (vl==null){
      return null;
    }
    if (p.isScalar()){
      if (p.isCollection()){
        if (vl.isInstanceOf[AmfArray]) {
          val value = obj.fields.getValue(field)
          if (value!=null&&value.value!=null){
            if (p.isRef()){
              res = new RefArrayValueEmitter(p,p.name, new FieldEntry(field, value));
            }
            else
            res = new ArrayValueEmitter(p.name, new FieldEntry(field, value));
          }
        }
      }
      else {
        if (p.isRef()){
            res=RefValueEmitter(p,p.name, new FieldEntry(field, obj.fields.getValue(field)))
        }
        else res = new ValueEmitter(p.name, new FieldEntry(field, obj.fields.getValue(field)));
      }
    }
    else {
      if (p.isCollection()){
          throw new RuntimeException("Not implemented yet");
      }
      else if (p.isMap()) {
          if (vl.isInstanceOf[AmfArray]){
            res=new ObjectMapEmmiter(p,(vl.asInstanceOf[AmfArray]));
          }
      }
      else{

        res = new ObjectKVEmmiter(p,vl.asInstanceOf[DomainEntity]);
      }
    }
    return res;
  }
  case class ObjectKVEmmiter(p:DialectPropertyMapping,entity: DomainEntity) extends Emitter{

    override def emit(): Unit = {
      entry { () =>
        raw(p.name)
        ObjectEmitter(entity).emit();
        }
      }


    override def position(): Position = Position.ZERO;
  }
  case class ObjectMapEmmiter(p:DialectPropertyMapping,values:AmfArray) extends Emitter{

    override def emit(): Unit = {
      if (values.values.isEmpty){
        return;
      }
      entry { () =>
        raw(p.name)
        //raw(p.name)
        map { () => {
          values.values.foreach(v=>{
            val entity = v.asInstanceOf[DomainEntity]
            entry({()=>{
              if (p._noLastSegmentTrimInMaps) {
                raw(localId(p, entity))
              }
              else{
                raw(lastSegment(entity))
              }
              ObjectEmitter(entity).emit();

            }})
          })
        }}
      }
    }

    override def position(): Position = Position.ZERO;
  }
  def lastSegment(obj:DomainEntity): String ={
    val ind:Int=Math.max(obj.id.lastIndexOf('/'),obj.id.lastIndexOf('#'));
    if (ind>0){
      return obj.id.substring(ind+1);
    }
    return obj.id;
  }

  def localId(dialectPropertyMapping: DialectPropertyMapping,obj:DomainEntity): String ={
      if (_np!=null){
        val name=_np.localName(obj.id,dialectPropertyMapping );
        if (name!=null){
          return name;
        }
      }
      return obj.id
  }

  case class ObjectEmitter(obj:DomainEntity)extends Emitter{

    override def emit(): Unit = {
      val scalarProp:DialectPropertyMapping=obj.definition._props().find(x=>x._fromVal).getOrElse(null)
      if (scalarProp!=null){
        val em = obj.string(scalarProp);
        if (scalarProp.isRef()){
          val name=_np.localName(em.getOrElse(null),scalarProp );
          if (name!=null){
            raw(name);
            return;
          }
        }
        val str = em.getOrElse("null")
        raw(str);
      }
      else map { ()=>
        obj.definition._props().foreach(p => {
          val em = createEmitter(obj, p);
          if (em != null) {
            try {
              em.emit();
            } catch {
              case e:Exception=>{e.printStackTrace()}
            }
          }
        })
      }
    }

    override def position(): Position = Position.ZERO;
  }
}

object DialectEmitter{
  def apply(unit: BaseUnit) = new DialectEmitter(unit)
}