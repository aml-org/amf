package amf.dialects

import amf.model.{AmfArray, AmfElement, AmfObject, AmfScalar}



/**
  * Created by kor on 21/09/17.
  */
class TopLevelObject(val domainEntity: DomainEntity, val parent:Option[TopLevelObject]) {


  def root:TopLevelObject=if (parent.isDefined) parent.get.root else this;

  def canEqual(other: Any): Boolean = other.isInstanceOf[TopLevelObject]

  override def equals(other: Any): Boolean = other match {
    case that: TopLevelObject =>
      (that canEqual this) &&
        domainEntity == that.domainEntity
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(domainEntity)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  protected def resolveReference[T]( refP:DialectPropertyMapping, resolver:(TopLevelObject,String) => Option[T], v:(DomainEntity) => T):Option[T]={
    domainEntity.fields.get(refP.field) match {
      case s:AmfScalar => resolver(root,s.toString);
      case e:DomainEntity=>Some(v(e));
    }
  }

  protected def resolveReferences[T]( refP:DialectPropertyMapping, resolver:(TopLevelObject,String) => Option[T], v:(DomainEntity) => T):List[T]={
    domainEntity.fields.get(refP.field) match {
      case s:AmfScalar =>resolver(root,s.toString).toList;
      case a:AmfArray =>
        a.values.map(_ match{
          case s:AmfScalar =>resolver(root,s.toString).get;
          case e:DomainEntity=>v(e);
        }).toList
      case e:DomainEntity=>List(v(e));
    }
  }

  protected def resolveReferences2Options[T]( refP:DialectPropertyMapping, resolver:(TopLevelObject,String) => Option[T], v:(DomainEntity) => T):List[Option[T]]={
    domainEntity.fields.get(refP.field) match {
      case s:AmfScalar =>List(resolver(root,s.toString));
      case a:AmfArray =>
        a.values.map(_ match{
          case s:AmfScalar =>resolver(root,s.toString);
          case e:DomainEntity=>Some(v(e));
        }).toList
      case e:DomainEntity=>List(Some(v(e)));
    }
  }
}
