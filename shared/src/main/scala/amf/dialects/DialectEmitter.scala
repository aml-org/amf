package amf.dialects

import amf.common.AMFAST
import amf.common.AMFToken.{Comment, Root}
import amf.document.BaseUnit
import amf.domain.FieldEntry
import amf.metadata.document.FragmentModel
import amf.model.{AmfArray, AmfScalar}
import amf.parser.Position
import amf.spec.dialect.DomainEntity
import amf.spec.{ASTEmitterHelper, Emitter}

/**
  * Created by kor on 13/09/17.
  */
class DialectEmitter (val unit: BaseUnit) extends ASTEmitterHelper{

  var _root:DomainEntity=null;
  var _np:LocalNameProvider=null;

  case class RefValueEmitter(key: String, f: FieldEntry) extends Emitter {
    override def emit(): Unit = {
      sourceOr(f.value, entry { () =>
        raw(key)
        emitRef(f.scalar.toString)
      })
    }

    override def position(): Position = pos(f.value.annotations)
  }

  /** Emit a single value from an array as an entry. */
  case class RefArrayValueEmitter(key: String, f: FieldEntry) extends Emitter {
    override def emit(): Unit = {
      try {
        sourceOr(f.value, entry { () =>
          raw(key)
          //raw("A")
          if (f.array.values.size == 1) {
            val scalar = f.array.values.head.asInstanceOf[AmfScalar]
            emitRef(scalar.toString)

          }
          else array(() => {
            f.array.values.foreach(v => {
              emitRef(v.asInstanceOf[AmfScalar].toString);
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

  private def emitRef(string: String) = {
    try {
      val name = _np.localName(string);
      if (name == null) {
        raw(string)
      }
      else raw(name);
    }catch {
      case e:Exception =>e.printStackTrace()
    }
  }

  def  emit():AMFAST={
    emitter.root(Root) { () =>
      raw("%RAML Vocabulary 1.0", Comment)
      try {
        unit.fields.fields().foreach(k=>{
          if (k.field.value.iri()==FragmentModel.Encodes.value.iri()){
            val entity = k.value.value.asInstanceOf[DomainEntity]
            _root=entity;
            if (_root.definition.nameProvider!=null){
              _np=_root.definition.nameProvider(entity);
            }
            ObjectEmitter(entity).emit();
          }
        })
      }catch {
        case e:Exception =>e.printStackTrace()
      }
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
              res = new RefArrayValueEmitter(p.name, new FieldEntry(field, value));
            }
            else
            res = new ArrayValueEmitter(p.name, new FieldEntry(field, value));
          }
        }
      }
      else {
        if (p.isRef()){
            res=RefValueEmitter(p.name, new FieldEntry(field, obj.fields.getValue(field)))
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
        res = new ObjectEmitter(vl.asInstanceOf[DomainEntity]);
      }
    }
    return res;
  }

  case class ObjectMapEmmiter(p:DialectPropertyMapping,values:AmfArray) extends Emitter{

    override def emit(): Unit = {
      entry { () =>
        raw(p.name)
        //raw(p.name)
        map { () => {
          values.values.foreach(v=>{
            val entity = v.asInstanceOf[DomainEntity]
            entry({()=>{
              raw(localId(entity))
              ObjectEmitter(entity).emit();

            }})
          })
        }}
      }
    }

    override def position(): Position = Position.ZERO;
  }

  def localId(obj:DomainEntity): String ={
      if (_np!=null){
        val name=_np.localName(obj.id);
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
        raw(em.getOrElse("null"));
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
