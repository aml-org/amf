package amf.model

trait DeclaresModel {

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  val declares: java.util.List[amf.domain.DomainElement]
}
