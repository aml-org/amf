package amf.metadata

import amf.document._
import amf.domain._
import amf.domain.`abstract`._
import amf.domain.dialects.DomainEntity
import amf.domain.extensions.{CustomDomainProperty, DataNode, DomainExtension, ShapeExtension}
import amf.domain.security._
import amf.metadata.document._
import amf.metadata.domain._
import amf.metadata.domain.`abstract`._
import amf.metadata.domain.dialects.DialectEntityModel
import amf.metadata.domain.extensions.{
  CustomDomainPropertyModel,
  DataNodeModel,
  DomainExtensionModel,
  ShapeExtensionModel
}
import amf.metadata.domain.security._
import amf.metadata.shape._
import amf.shape._

trait MetaModelTypeMapping {

  /** Metadata Type references. */
  protected def metaModel(instance: Any): Obj = instance match {
    case _: Extension                          => ExtensionModel
    case _: Overlay                            => OverlayModel
    case _: Document                           => DocumentModel
    case _: WebApi                             => WebApiModel
    case _: Organization                       => OrganizationModel
    case _: License                            => LicenseModel
    case _: CreativeWork                       => CreativeWorkModel
    case _: EndPoint                           => EndPointModel
    case _: Operation                          => OperationModel
    case _: Parameter                          => ParameterModel
    case _: Request                            => RequestModel
    case _: Response                           => ResponseModel
    case _: Payload                            => PayloadModel
    case _: UnionShape                         => UnionShapeModel
    case _: NodeShape                          => NodeShapeModel
    case _: ArrayShape                         => ArrayShapeModel
    case _: MatrixShape                        => MatrixShapeModel
    case _: FileShape                          => FileShapeModel
    case _: ScalarShape                        => ScalarShapeModel
    case _: AnyShape                           => AnyShapeModel
    case _: NilShape                           => NilShapeModel
    case _: PropertyShape                      => PropertyShapeModel
    case _: SchemaShape                        => SchemaShapeModel
    case _: XMLSerializer                      => XMLSerializerModel
    case _: PropertyDependencies               => PropertyDependenciesModel
    case _: DomainExtension                    => DomainExtensionModel
    case _: ShapeExtension                     => ShapeExtensionModel
    case _: CustomDomainProperty               => CustomDomainPropertyModel
    case _: DataNode                           => DataNodeModel
    case _: Module                             => ModuleModel
    case _: ResourceType                       => ResourceTypeModel
    case _: Trait                              => TraitModel
    case _: ParametrizedResourceType           => ParametrizedResourceTypeModel
    case _: ParametrizedTrait                  => ParametrizedTraitModel
    case _: VariableValue                      => VariableValueModel
    case _: ExternalDomainElement              => ExternalDomainElementModel
    case _: SecurityScheme                     => SecuritySchemeModel
    case _: OAuth1Settings                     => OAuth1SettingsModel
    case _: OAuth2Settings                     => OAuth2SettingsModel
    case _: ApiKeySettings                     => ApiKeySettingsModel
    case _: Settings                           => SettingsModel
    case _: Scope                              => ScopeModel
    case _: ParametrizedSecurityScheme         => ParametrizedSecuritySchemeModel
    case _: Example                            => ExampleModel
    case entity: DomainEntity                  => new DialectEntityModel(entity)
    case _: Fragment.ExternalFragment          => FragmentsTypesModels.ExternalFragmentModel
    case _: Fragment.DocumentationItem         => FragmentsTypesModels.DocumentationItemModel
    case _: Fragment.DataType                  => FragmentsTypesModels.DataTypeModel
    case _: Fragment.ResourceTypeFragment      => FragmentsTypesModels.ResourceTypeModel
    case _: Fragment.TraitFragment             => FragmentsTypesModels.TraitModel
    case _: Fragment.NamedExample              => FragmentsTypesModels.NamedExampleModel
    case _: Fragment.AnnotationTypeDeclaration => FragmentsTypesModels.AnnotationTypeDeclarationModel
    case _: Fragment.SecurityScheme            => FragmentsTypesModels.SecuritySchemeModel
    case _: Fragment.DialectFragment           => FragmentsTypesModels.DialectNodeModel
    case _: Fragment.Fragment                  => FragmentModel
    case unresolved: UnresolvedShape           => ShapeModel
    case _                                     => throw new Exception(s"Missing metadata mapping for $instance")
  }

}
