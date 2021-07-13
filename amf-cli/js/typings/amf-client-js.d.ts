declare module 'amf-client-js' {
  namespace org {
    namespace mulesoft {
      namespace common {
        namespace io {
          export class LimitedStringBuffer          {
            length: number
            toString: string

            constructor(limit: number)

          }
          export class LimitReachedException          {
            constructor()

          }
        }
      }
    }
    namespace yaml {
      namespace builder {
        export class JsOutputBuilder        {
          isDefined: boolean
          result: undefined

          constructor()

          list(f: undefined): any

          obj(f: undefined): any

          doc(f: undefined): any


        }
      }
    }
  }
  namespace amf {
    namespace shapes {
      namespace client {
        namespace platform {
          namespace model {
            namespace domain {
              export class DataArrangeShape extends AnyShape              {
                minItems: core.client.platform.model.IntField
                maxItems: core.client.platform.model.IntField
                uniqueItems: core.client.platform.model.BoolField

                withMinItems(minItems: number): this

                withMaxItems(maxItems: number): this

                withUniqueItems(uniqueItems: boolean): this


              }
            }
          }
        }
      }
    }
    namespace aml {
      namespace client {
        namespace platform {
          export class BaseAMLConfiguration extends AMFGraphConfiguration          {
            withParsingOptions(parsingOptions: ParsingOptions): BaseAMLConfiguration

            withRenderOptions(renderOptions: RenderOptions): BaseAMLConfiguration

            withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseAMLConfiguration

            withResourceLoader(rl: ResourceLoader): BaseAMLConfiguration

            withResourceLoaders(rl: Array<ResourceLoader>): BaseAMLConfiguration

            withUnitCache(cache: core.client.platform.reference.ClientUnitCache): BaseAMLConfiguration

            withTransformationPipeline(pipeline: TransformationPipeline): BaseAMLConfiguration

            withEventListener(listener: core.client.platform.config.AMFEventListener): BaseAMLConfiguration

            withDialect(dialect: Dialect): BaseAMLConfiguration


          }
          export class BaseAMLClient extends AMFGraphClient          {
            parseDialect(url: string): Promise<AMLDialectResult>

            parseDialectInstance(url: string): Promise<AMLDialectInstanceResult>

            parseValidationProfile(url: string): Promise<ValidationProfile>

            parseValidationProfile(instance: DialectInstance): ValidationProfile

            parseVocabulary(url: string): Promise<AMLVocabularyResult>


          }
          namespace model {
            namespace domain {
              export class PropertyTerm implements core.client.platform.model.domain.DomainElement              {
                displayName: core.client.platform.model.StrField
                name: core.client.platform.model.StrField
                customDomainProperties: Array<DomainExtension>
                description: core.client.platform.model.StrField
                subPropertyOf: Array<core.client.platform.model.StrField>
                isExternalLink: core.client.platform.model.BoolField
                id: string
                range: core.client.platform.model.StrField
                position: Range
                extendsNode: Array<core.client.platform.model.domain.DomainElement>

                withName(name: string): PropertyTerm

                withDescription(description: string): PropertyTerm

                graph(): core.client.platform.model.domain.Graph

                withSubClasOf(superProperties: Array<string>): PropertyTerm

                withIsExternalLink(isExternalLink: boolean): core.client.platform.model.domain.DomainElement

                withExtendsNode(extension: Array<core.client.platform.model.domain.ParametrizedDeclaration>): this

                withCustomDomainProperties(extensions: Array<DomainExtension>): this

                withDisplayName(displayName: string): PropertyTerm

                withId(id: string): this

                withRange(range: string): PropertyTerm


              }
            }
          }
        }
      }
    }
    namespace core {
      namespace internal {
        namespace remote {
          export interface Vendor          {
            readonly name: string
            isRaml: boolean
            isOas: boolean
            isAsync: boolean
            readonly mediaType: string

          }
          namespace server {
            export class Path            {
              static sep: string
              static delimiter: string

              normalize(p: string): string

              join(paths: undefined): string

              resolve(pathSegments: undefined): string

              isAbsolute(path: string): boolean

              relative(from: string, to: string): string

              dirname(p: string): string

              basename(p: string, ext: string): string

              extname(p: string): string


            }
            export class JsServerPlatform            {
              instance(): JsServerPlatform

              platform(): string


            }
          }
          namespace browser {
            export class JsBrowserPlatform            {
              instance(): JsBrowserPlatform


            }
          }
        }
      }
      namespace client {
        namespace common {
          namespace validation {
            interface ValidationMode {}
            export class StrictValidationMode implements ValidationMode {}
            export class ScalarRelaxedValidationMode implements ValidationMode {}
            export interface MessageStyle            {
              profileName: ProfileName

            }
          }
        }
        namespace platform {
          namespace config {
            export interface AMFEventListener            {
              notifyEvent(event: AMFEvent): void


            }
            export interface AMFEvent            {
              readonly name: string

            }
            export class StartingParsingEvent            {
              url: string
              mediaType: undefined | string

            }
            export class StartingContentParsingEvent            {
              url: string
              content: Content

            }
            export class ParsedSyntaxEvent            {
              url: string
              content: Content

            }
            export class ParsedModelEvent            {
              url: string
              unit: model.document.BaseUnit

            }
            export class FinishedParsingEvent            {
              url: string
              unit: model.document.BaseUnit

            }
            export class StartingTransformationEvent            {
              pipeline: TransformationPipeline

            }
            export class FinishedTransformationStepEvent            {
              step: TransformationStep
              index: number

            }
            export class FinishedTransformationEvent            {
              unit: model.document.BaseUnit

            }
            export class StartingValidationEvent            {
              totalPlugins: number

            }
            export class FinishedValidationPluginEvent            {
              result: AMFValidationReport

            }
            export class FinishedValidationEvent            {
              result: AMFValidationReport

            }
            export class StartingRenderingEvent            {
              unit: model.document.BaseUnit
              mediaType: string

            }
            export class FinishedRenderingASTEvent            {
              unit: model.document.BaseUnit

            }
            export class FinishedRenderingSyntaxEvent            {
              unit: model.document.BaseUnit

            }
          }
          namespace reference {
            export interface UnitCache            {
              fetch(url: string): Promise<CachedReference>


            }
            export interface ClientUnitCache            {
              fetch(url: string): Promise<CachedReference>


            }
          }
          namespace resource {
            export class BaseHttpResourceLoader implements ResourceLoader            {
              fetch(resource: string): Promise<Content>

              accepts(resource: string): boolean


            }
            export interface BaseFileResourceLoader extends ResourceLoader            {
              fetch(resource: string): Promise<Content>

              fetchFile(resource: string): Promise<Content>

              accepts(resource: string): boolean


            }
          }
          namespace model {
            export interface ValueField<T>            {
              readonly option: undefined | T
              isNull: boolean
              nonNull: boolean
              toString: string

              value(): T

              is(other: T): boolean

              is(accepts: undefined): boolean

              remove(): void


            }
            export class StrField implements ValueField<string>            {
              isNull: boolean
              nonNull: boolean
              readonly option: undefined | string
              nonEmpty: boolean
              isNullOrEmpty: boolean
              toString: string

              annotations(): Annotations

              value(): string

              remove(): void

              is(other: string): boolean

              is(accepts: undefined): boolean


            }
            export class IntField            {
              readonly option: undefined | number

              annotations(): Annotations

              value(): number

              remove(): void


            }
            export class FloatField            {
              readonly option: undefined | number

              annotations(): Annotations

              value(): number

              remove(): void


            }
            export class DoubleField            {
              readonly option: undefined | number

              annotations(): Annotations

              value(): number

              remove(): void


            }
            export class BoolField            {
              readonly option: undefined | boolean

              annotations(): Annotations

              value(): boolean

              remove(): void


            }
            export class AnyField implements ValueField<any>            {
              nonNull: boolean
              isNull: boolean
              toString: string
              readonly option: undefined | any

              annotations(): Annotations

              value(): any

              remove(): void

              is(other: any): boolean

              is(accepts: undefined): boolean


            }
            export interface Annotable            {
              annotations(): Annotations


            }
            namespace document {
              export class Fragment implements BaseUnit, EncodesModel              {
                location: string
                usage: StrField
                sourceVendor: undefined | Vendor
                id: string
                raw: undefined | string
                modelVersion: StrField
                encodes: domain.DomainElement

                findByType(typeId: string): Array<domain.DomainElement>

                cloneUnit(): BaseUnit

                withReferences(references: Array<BaseUnit>): this

                withRaw(raw: string): this

                withUsage(usage: string): this

                findById(id: string): undefined | domain.DomainElement

                withLocation(location: string): this

                withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): BaseUnit

                withEncodes(encoded: domain.DomainElement): this

                references(): Array<BaseUnit>

                withId(id: string): this


              }
              export interface EncodesModel              {
                encodes: domain.DomainElement

                withEncodes(encoded: domain.DomainElement): this


              }
              export interface DeclaresModel              {
                declares: Array<domain.DomainElement>

                withDeclaredElement(declared: domain.DomainElement): this

                withDeclares(declares: Array<domain.DomainElement>): this


              }
              export interface BaseUnit              {
                id: string
                raw: undefined | string
                location: string
                usage: StrField
                modelVersion: StrField
                sourceVendor: undefined | Vendor

                references(): Array<BaseUnit>

                withReferences(references: Array<BaseUnit>): this

                withId(id: string): this

                withRaw(raw: string): this

                withLocation(location: string): this

                withUsage(usage: string): this

                findById(id: string): undefined | domain.DomainElement

                findByType(typeId: string): Array<domain.DomainElement>

                cloneUnit(): BaseUnit

                withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): BaseUnit


              }
            }
            namespace domain {
              export class VariableValue implements DomainElement              {
                name: StrField
                customDomainProperties: Array<DomainExtension>
                isExternalLink: BoolField
                id: string
                position: Range
                extendsNode: Array<DomainElement>
                value: DataNode

                withName(name: string): this

                graph(): Graph

                withValue(value: DataNode): this

                withIsExternalLink(isExternalLink: boolean): DomainElement

                withExtendsNode(extension: Array<ParametrizedDeclaration>): this

                withCustomDomainProperties(extensions: Array<DomainExtension>): this

                withId(id: string): this


              }
              export interface Shape extends DomainElement, Linkable              {
                name: StrField
                displayName: StrField
                description: StrField
                defaultValue: DataNode
                defaultValueStr: StrField
                values: Array<DataNode>
                inherits: Array<Shape>
                customShapeProperties: Array<ShapeExtension>
                customShapePropertyDefinitions: Array<PropertyShape>
                or: Array<Shape>
                and: Array<Shape>
                xone: Array<Shape>
                not: Shape
                readOnly: BoolField
                writeOnly: BoolField
                deprecated: BoolField
                ifShape: Shape
                elseShape: Shape
                thenShape: Shape

                withName(name: string): this

                withDisplayName(name: string): this

                withDescription(description: string): this

                withDefaultValue(defaultVal: DataNode): this

                withValues(values: Array<DataNode>): this

                withInherits(inherits: Array<Shape>): this

                withOr(subShapes: Array<Shape>): this

                withAnd(subShapes: Array<Shape>): this

                withXone(subShapes: Array<Shape>): this

                withNode(shape: Shape): this

                withDefaultStr(value: string): this

                withCustomShapeProperties(customShapeProperties: Array<ShapeExtension>): this

                withCustomShapePropertyDefinitions(propertyDefinitions: Array<PropertyShape>): this

                withCustomShapePropertyDefinition(name: string): PropertyShape

                withReadOnly(readOnly: boolean): this

                withWriteOnly(writeOnly: boolean): this

                withDeprecated(deprecated: boolean): this

                withIf(ifShape: Shape): this

                withElse(elseShape: Shape): this

                withThen(thenShape: Shape): this


              }
              export class RecursiveShape implements Shape              {
                defaultValueStr: StrField
                displayName: StrField
                name: StrField
                customDomainProperties: Array<DomainExtension>
                xone: Array<Shape>
                readOnly: BoolField
                description: StrField
                fixpoint: StrField
                deprecated: BoolField
                customShapePropertyDefinitions: Array<PropertyShape>
                or: Array<Shape>
                elseShape: Shape
                linkTarget: undefined | DomainElement
                isLink: boolean
                isExternalLink: BoolField
                customShapeProperties: Array<ShapeExtension>
                thenShape: Shape
                id: string
                ifShape: Shape
                writeOnly: BoolField
                not: Shape
                values: Array<DataNode>
                position: Range
                inherits: Array<Shape>
                linkLabel: StrField
                defaultValue: DataNode
                extendsNode: Array<DomainElement>
                and: Array<Shape>

                withValues(values: Array<DataNode>): this

                linkCopy(): Linkable

                withOr(subShapes: Array<Shape>): this

                withName(name: string): this

                withDescription(description: string): this

                withIf(ifShape: Shape): this

                withCustomShapePropertyDefinition(name: string): PropertyShape

                graph(): Graph

                withIsExternalLink(isExternalLink: boolean): DomainElement

                withFixPoint(shapeId: string): this

                withLinkLabel(label: string): this

                withCustomShapePropertyDefinitions(propertyDefinitions: Array<PropertyShape>): this

                withReadOnly(readOnly: boolean): this

                withInherits(inherits: Array<Shape>): this

                withAnd(subShapes: Array<Shape>): this

                withExtendsNode(extension: Array<ParametrizedDeclaration>): this

                withWriteOnly(writeOnly: boolean): this

                withCustomDomainProperties(extensions: Array<DomainExtension>): this

                withLinkTarget(target: undefined): this

                withDisplayName(name: string): this

                withDefaultValue(defaultVal: DataNode): this

                withThen(thenShape: Shape): this

                withDefaultStr(value: string): this

                withCustomShapeProperties(customShapeProperties: Array<ShapeExtension>): this

                withId(id: string): this

                withElse(elseShape: Shape): this

                withXone(subShapes: Array<Shape>): this

                withDeprecated(deprecated: boolean): this

                withNode(shape: Shape): this


              }
              export interface ParametrizedDeclaration extends DomainElement              {
                name: StrField
                target: AbstractDeclaration
                variables: Array<VariableValue>

                withName(name: string): this

                withTarget(target: AbstractDeclaration): this

                withVariables(variables: Array<VariableValue>): this


              }
              export interface Linkable              {
                linkTarget: undefined | DomainElement
                isLink: boolean
                linkLabel: StrField

                linkCopy(): Linkable

                withLinkTarget(target: undefined): this

                withLinkLabel(label: string): this


              }
              export class Graph              {
                types(): Array<string>

                properties(): Array<string>

                scalarByProperty(id: string): Array<any>

                getObjectByPropertyId(id: string): Array<DomainElement>

                remove(uri: string): this


              }
              export interface DomainElement extends CustomizableElement              {
                customDomainProperties: Array<DomainExtension>
                extendsNode: Array<DomainElement>
                id: string
                position: Range
                isExternalLink: BoolField

                withCustomDomainProperties(extensions: Array<DomainExtension>): this

                withExtendsNode(extension: Array<ParametrizedDeclaration>): this

                withId(id: string): this

                withIsExternalLink(isExternalLink: boolean): DomainElement

                graph(): Graph


              }
              export interface DataNode extends DomainElement              {
                name: StrField

                withName(name: string): this


              }
              export interface CustomizableElement              {
                customDomainProperties: Array<DomainExtension>

                withCustomDomainProperties(extensions: Array<DomainExtension>): this


              }
              export class AbstractDeclaration implements DomainElement, Linkable              {
                name: StrField
                customDomainProperties: Array<DomainExtension>
                description: StrField
                dataNode: DataNode
                variables: Array<StrField>
                linkTarget: undefined | DomainElement
                isLink: boolean
                isExternalLink: BoolField
                id: string
                position: Range
                linkLabel: StrField
                extendsNode: Array<DomainElement>

                linkCopy(): AbstractDeclaration

                withName(name: string): this

                withDescription(description: string): this

                withDataNode(dataNode: DataNode): this

                graph(): Graph

                withIsExternalLink(isExternalLink: boolean): DomainElement

                withLinkLabel(label: string): this

                withExtendsNode(extension: Array<ParametrizedDeclaration>): this

                withCustomDomainProperties(extensions: Array<DomainExtension>): this

                withLinkTarget(target: undefined): this

                withVariables(variables: Array<string>): this

                withId(id: string): this


              }
            }
          }
        }
      }
    }
    namespace apicontract {
      namespace client {
        namespace platform {
          namespace model {
            namespace domain {
              namespace bindings {
                export interface ChannelBinding extends core.client.platform.model.domain.DomainElement, core.client.platform.model.domain.Linkable                {
                }
                export interface OperationBinding extends core.client.platform.model.domain.DomainElement, core.client.platform.model.domain.Linkable                {
                }
                export interface MessageBinding extends core.client.platform.model.domain.DomainElement, core.client.platform.model.domain.Linkable                {
                }
                export interface ServerBinding extends core.client.platform.model.domain.DomainElement, core.client.platform.model.domain.Linkable                {
                }
              }
              namespace api {
                export class Api<A> implements core.client.platform.model.domain.DomainElement                {
                  name: core.client.platform.model.StrField
                  customDomainProperties: Array<DomainExtension>
                  endPoints: Array<EndPoint>
                  provider: Organization
                  security: Array<SecurityRequirement>
                  identifier: core.client.platform.model.StrField
                  description: core.client.platform.model.StrField
                  documentations: Array<CreativeWork>
                  servers: Array<Server>
                  schemes: Array<core.client.platform.model.StrField>
                  license: License
                  isExternalLink: core.client.platform.model.BoolField
                  sourceVendor: undefined | Vendor
                  termsOfService: core.client.platform.model.StrField
                  version: core.client.platform.model.StrField
                  id: string
                  contentType: Array<core.client.platform.model.StrField>
                  accepts: Array<core.client.platform.model.StrField>
                  position: Range
                  extendsNode: Array<core.client.platform.model.domain.DomainElement>

                  withDocumentationTitle(title: string): CreativeWork

                  withEndPoint(path: string): EndPoint

                  withDefaultServer(url: string): Server

                  withDocumentationUrl(url: string): CreativeWork

                  graph(): core.client.platform.model.domain.Graph

                  withIsExternalLink(isExternalLink: boolean): core.client.platform.model.domain.DomainElement

                  withExtendsNode(extension: Array<core.client.platform.model.domain.ParametrizedDeclaration>): this

                  withCustomDomainProperties(extensions: Array<DomainExtension>): this

                  withServer(url: string): Server

                  withId(id: string): this


                }
              }
            }
          }
        }
      }
    }
  }
  export class XMLSerializer implements amf.core.client.platform.model.domain.DomainElement  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    prefix: amf.core.client.platform.model.StrField
    wrapped: amf.core.client.platform.model.BoolField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    attribute: amf.core.client.platform.model.BoolField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    namespace: amf.core.client.platform.model.StrField

    constructor()

    withName(name: string): this

    withWrapped(wrapped: boolean): this

    withPrefix(prefix: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withAttribute(attribute: boolean): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withNamespace(namespace: string): this

    withId(id: string): this


  }
  export class UnionShape extends AnyShape  {
    anyOf: Array<amf.core.client.platform.model.domain.Shape>

    constructor()

    withAnyOf(anyOf: Array<amf.core.client.platform.model.domain.Shape>): UnionShape


  }
  export class TupleShape extends amf.shapes.client.platform.model.domain.DataArrangeShape  {
    items: Array<amf.core.client.platform.model.domain.Shape>
    closedItems: amf.core.client.platform.model.BoolField
    additionalItemsSchema: amf.core.client.platform.model.domain.Shape

    constructor()

    withItems(items: Array<amf.core.client.platform.model.domain.Shape>): this

    withClosedItems(closedItems: boolean): this

    linkCopy(): TupleShape


  }
  export class SchemaShape extends AnyShape  {
    mediaType: amf.core.client.platform.model.StrField
    raw: amf.core.client.platform.model.StrField
    location: undefined | string

    constructor()

    withMediatype(mediaType: string): this

    withRaw(text: string): this

    linkCopy(): SchemaShape


  }
  export class SchemaDependencies implements amf.core.client.platform.model.domain.DomainElement  {
    source: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    target: amf.core.client.platform.model.domain.Shape
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withSchemaTarget(schema: amf.core.client.platform.model.domain.Shape): this

    withPropertySource(propertySource: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class ScalarShape extends AnyShape  {
    dataType: amf.core.client.platform.model.StrField
    pattern: amf.core.client.platform.model.StrField
    minLength: amf.core.client.platform.model.IntField
    maxLength: amf.core.client.platform.model.IntField
    minimum: amf.core.client.platform.model.DoubleField
    maximum: amf.core.client.platform.model.DoubleField
    exclusiveMinimum: amf.core.client.platform.model.BoolField
    exclusiveMaximum: amf.core.client.platform.model.BoolField
    format: amf.core.client.platform.model.StrField
    multipleOf: amf.core.client.platform.model.DoubleField
    encoding: amf.core.client.platform.model.StrField
    mediaType: amf.core.client.platform.model.StrField
    schema: amf.core.client.platform.model.domain.Shape

    constructor()

    withDataType(dataType: string): this

    withPattern(pattern: string): this

    withMinLength(min: number): this

    withMaxLength(max: number): this

    withMinimum(min: number): this

    withMaximum(max: number): this

    withExclusiveMinimum(min: boolean): this

    withExclusiveMaximum(max: boolean): this

    withFormat(format: string): this

    withMultipleOf(multiple: number): this

    withEncoding(encoding: string): this

    withMediaType(mediaType: string): this

    withSchema(schema: amf.core.client.platform.model.domain.Shape): this

    linkCopy(): ScalarShape


  }
  export class PropertyDependencies implements amf.core.client.platform.model.domain.DomainElement  {
    source: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    target: Array<amf.core.client.platform.model.StrField>
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withPropertySource(propertySource: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withPropertyTarget(propertyTarget: Array<string>): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class NodeShape extends AnyShape  {
    minProperties: amf.core.client.platform.model.IntField
    maxProperties: amf.core.client.platform.model.IntField
    closed: amf.core.client.platform.model.BoolField
    discriminator: amf.core.client.platform.model.StrField
    discriminatorValue: amf.core.client.platform.model.StrField
    discriminatorMapping: Array<IriTemplateMapping>
    discriminatorValueMapping: Array<DiscriminatorValueMapping>
    properties: Array<PropertyShape>
    additionalPropertiesSchema: amf.core.client.platform.model.domain.Shape
    dependencies: Array<PropertyDependencies>
    schemaDependencies: Array<SchemaDependencies>
    propertyNames: amf.core.client.platform.model.domain.Shape
    unevaluatedProperties: boolean
    unevaluatedPropertiesSchema: amf.core.client.platform.model.domain.Shape

    constructor()

    withMinProperties(min: number): this

    withMaxProperties(max: number): this

    withClosed(closed: boolean): this

    withDiscriminator(discriminator: string): this

    withDiscriminatorValue(value: string): this

    withDiscriminatorMapping(mappings: Array<IriTemplateMapping>): this

    withProperties(properties: Array<PropertyShape>): this

    withAdditionalPropertiesSchema(additionalPropertiesSchema: amf.core.client.platform.model.domain.Shape): this

    withDependencies(dependencies: Array<PropertyDependencies>): this

    withSchemaDependencies(dependencies: Array<SchemaDependencies>): this

    withPropertyNames(propertyNames: amf.core.client.platform.model.domain.Shape): this

    withUnevaluatedProperties(value: boolean): this

    withUnevaluatedPropertiesSchema(schema: amf.core.client.platform.model.domain.Shape): this

    withProperty(name: string): PropertyShape

    withDependency(): PropertyDependencies

    withInheritsObject(name: string): NodeShape

    withInheritsScalar(name: string): ScalarShape

    linkCopy(): NodeShape


  }
  export class NilShape extends AnyShape  {
    constructor()

    linkCopy(): NilShape


  }
  export class MatrixShape extends ArrayShape  {
    constructor()

    withItems(items: amf.core.client.platform.model.domain.Shape): this


  }
  export class IriTemplateMapping implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    templateVariable: amf.core.client.platform.model.StrField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    linkExpression: amf.core.client.platform.model.StrField
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    graph(): amf.core.client.platform.model.domain.Graph

    withTemplateVariable(variable: string): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withLinkExpression(expression: string): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class FileShape extends AnyShape  {
    fileTypes: Array<amf.core.client.platform.model.StrField>
    pattern: amf.core.client.platform.model.StrField
    minLength: amf.core.client.platform.model.IntField
    maxLength: amf.core.client.platform.model.IntField
    minimum: amf.core.client.platform.model.DoubleField
    maximum: amf.core.client.platform.model.DoubleField
    exclusiveMinimum: amf.core.client.platform.model.BoolField
    exclusiveMaximum: amf.core.client.platform.model.BoolField
    format: amf.core.client.platform.model.StrField
    multipleOf: amf.core.client.platform.model.DoubleField

    constructor()

    withFileTypes(fileTypes: Array<string>): this

    withPattern(pattern: string): this

    withMinLength(min: number): this

    withMaxLength(max: number): this

    withMinimum(min: number): this

    withMaximum(max: number): this

    withExclusiveMinimum(min: boolean): this

    withExclusiveMaximum(max: boolean): this

    withFormat(format: string): this

    withMultipleOf(multiple: number): this

    linkCopy(): FileShape


  }
  export class Example implements amf.core.client.platform.model.domain.DomainElement, amf.core.client.platform.model.domain.Linkable  {
    displayName: amf.core.client.platform.model.StrField
    mediaType: amf.core.client.platform.model.StrField
    name: amf.core.client.platform.model.StrField
    strict: amf.core.client.platform.model.BoolField
    toYaml: string
    customDomainProperties: Array<DomainExtension>
    location: undefined | string
    description: amf.core.client.platform.model.StrField
    structuredValue: amf.core.client.platform.model.domain.DataNode
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    toJson: string
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    value: amf.core.client.platform.model.StrField

    constructor()

    linkCopy(): Example

    withName(name: string): this

    withStructuredValue(value: amf.core.client.platform.model.domain.DataNode): this

    withDescription(description: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withMediaType(mediaType: string): this

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withValue(value: string): this

    withLinkTarget(target: undefined): this

    withStrict(strict: boolean): this

    withDisplayName(displayName: string): this

    withId(id: string): this


  }
  export class DiscriminatorValueMapping implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    targetShape: amf.core.client.platform.model.domain.Shape
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    value: amf.core.client.platform.model.StrField

    constructor()

    withTargetShape(shape: amf.core.client.platform.model.domain.Shape): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withValue(value: string): this

    withId(id: string): this


  }
  export class CreativeWork implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    url: amf.core.client.platform.model.StrField
    description: amf.core.client.platform.model.StrField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    title: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withDescription(description: string): this

    withTitle(title: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withUrl(url: string): this

    withId(id: string): this


  }
  export class ArrayShape extends amf.shapes.client.platform.model.domain.DataArrangeShape  {
    items: amf.core.client.platform.model.domain.Shape
    contains: amf.core.client.platform.model.domain.Shape
    minContains: number
    maxContains: number
    unevaluatedItems: boolean
    unevaluatedItemsSchema: amf.core.client.platform.model.domain.Shape

    constructor()

    withItems(items: amf.core.client.platform.model.domain.Shape): this

    withContains(contains: amf.core.client.platform.model.domain.Shape): this

    withMinContains(amount: number): this

    withMaxContains(amount: number): this

    withUnevaluatedItemsSchema(schema: amf.core.client.platform.model.domain.Shape): this

    withUnevaluatedItems(value: boolean): this

    linkCopy(): ArrayShape


  }
  export class AnyShape implements amf.core.client.platform.model.domain.Shape  {
    defaultValueStr: amf.core.client.platform.model.StrField
    displayName: amf.core.client.platform.model.StrField
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    examples: Array<Example>
    xone: Array<amf.core.client.platform.model.domain.Shape>
    readOnly: amf.core.client.platform.model.BoolField
    description: amf.core.client.platform.model.StrField
    documentation: CreativeWork
    deprecated: amf.core.client.platform.model.BoolField
    xmlSerialization: XMLSerializer
    customShapePropertyDefinitions: Array<PropertyShape>
    or: Array<amf.core.client.platform.model.domain.Shape>
    elseShape: amf.core.client.platform.model.domain.Shape
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    customShapeProperties: Array<ShapeExtension>
    thenShape: amf.core.client.platform.model.domain.Shape
    id: string
    ifShape: amf.core.client.platform.model.domain.Shape
    writeOnly: amf.core.client.platform.model.BoolField
    comment: amf.core.client.platform.model.StrField
    not: amf.core.client.platform.model.domain.Shape
    values: Array<amf.core.client.platform.model.domain.DataNode>
    position: Range
    isDefaultEmpty: boolean
    inherits: Array<amf.core.client.platform.model.domain.Shape>
    linkLabel: amf.core.client.platform.model.StrField
    defaultValue: amf.core.client.platform.model.domain.DataNode
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    and: Array<amf.core.client.platform.model.domain.Shape>

    constructor()

    withValues(values: Array<amf.core.client.platform.model.domain.DataNode>): this

    linkCopy(): AnyShape

    withOr(subShapes: Array<amf.core.client.platform.model.domain.Shape>): this

    withName(name: string): this

    withDescription(description: string): this

    trackedExample(trackId: string): undefined | Example

    withIf(ifShape: amf.core.client.platform.model.domain.Shape): this

    withCustomShapePropertyDefinition(name: string): PropertyShape

    withExamples(examples: Array<Example>): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withCustomShapePropertyDefinitions(propertyDefinitions: Array<PropertyShape>): this

    withReadOnly(readOnly: boolean): this

    withInherits(inherits: Array<amf.core.client.platform.model.domain.Shape>): this

    withAnd(subShapes: Array<amf.core.client.platform.model.domain.Shape>): this

    withComment(comment: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withWriteOnly(writeOnly: boolean): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    inlined(): boolean

    withLinkTarget(target: undefined): this

    withDisplayName(name: string): this

    withDefaultValue(defaultVal: amf.core.client.platform.model.domain.DataNode): this

    withXMLSerialization(xmlSerialization: XMLSerializer): this

    withThen(thenShape: amf.core.client.platform.model.domain.Shape): this

    withDefaultStr(value: string): this

    withCustomShapeProperties(customShapeProperties: Array<ShapeExtension>): this

    withId(id: string): this

    withElse(elseShape: amf.core.client.platform.model.domain.Shape): this

    withExample(mediaType: string): Example

    withXone(subShapes: Array<amf.core.client.platform.model.domain.Shape>): this

    withDocumentation(documentation: CreativeWork): this

    withDeprecated(deprecated: boolean): this

    withNode(shape: amf.core.client.platform.model.domain.Shape): this


  }
  export class ApiDomainElementEmitter  {
    emitToBuilder<T>(element: amf.core.client.platform.model.domain.DomainElement, emissionStructure: Vendor, eh: ClientErrorHandler, builder: org.yaml.builder.JsOutputBuilder): void


  }
  export class Trait extends amf.core.client.platform.model.domain.AbstractDeclaration  {
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement

    constructor()

    linkCopy(): Trait

    asOperation<T>(unit: T, profile: ProfileName): Operation


  }
  export class ResourceType extends amf.core.client.platform.model.domain.AbstractDeclaration  {
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement

    constructor()

    linkCopy(): ResourceType

    asEndpoint<T>(unit: T, profile: ProfileName): EndPoint


  }
  export class ParametrizedTrait implements amf.core.client.platform.model.domain.ParametrizedDeclaration  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    variables: Array<amf.core.client.platform.model.domain.VariableValue>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    target: amf.core.client.platform.model.domain.AbstractDeclaration
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withName(name: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withVariables(variables: Array<amf.core.client.platform.model.domain.VariableValue>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withTarget(target: amf.core.client.platform.model.domain.AbstractDeclaration): this

    withId(id: string): this


  }
  export class ParametrizedResourceType implements amf.core.client.platform.model.domain.ParametrizedDeclaration  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    variables: Array<amf.core.client.platform.model.domain.VariableValue>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    target: amf.core.client.platform.model.domain.AbstractDeclaration
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withName(name: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withVariables(variables: Array<amf.core.client.platform.model.domain.VariableValue>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withTarget(target: amf.core.client.platform.model.domain.AbstractDeclaration): this

    withId(id: string): this


  }
  export class Settings implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    additionalProperties: amf.core.client.platform.model.domain.DataNode
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withAdditionalProperties(properties: amf.core.client.platform.model.domain.DataNode): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class OAuth1Settings extends Settings  {
    requestTokenUri: amf.core.client.platform.model.StrField
    authorizationUri: amf.core.client.platform.model.StrField
    tokenCredentialsUri: amf.core.client.platform.model.StrField
    signatures: Array<amf.core.client.platform.model.StrField>

    constructor()

    withRequestTokenUri(requestTokenUri: string): this

    withAuthorizationUri(authorizationUri: string): this

    withTokenCredentialsUri(tokenCredentialsUri: string): this

    withSignatures(signatures: Array<string>): this


  }
  export class OAuth2Settings extends Settings  {
    flows: Array<OAuth2Flow>
    authorizationGrants: Array<amf.core.client.platform.model.StrField>

    constructor()

    withFlows(flows: Array<OAuth2Flow>): this

    withAuthorizationGrants(grants: Array<string>): this


  }
  export class ApiKeySettings extends Settings  {
    name: amf.core.client.platform.model.StrField
    in: amf.core.client.platform.model.StrField

    constructor()

    withName(name: string): this

    withIn(inVal: string): this


  }
  export class HttpApiKeySettings extends Settings  {
    name: amf.core.client.platform.model.StrField
    in: amf.core.client.platform.model.StrField

    constructor()

    withName(name: string): this

    withIn(inVal: string): this


  }
  export class HttpSettings extends Settings  {
    scheme: amf.core.client.platform.model.StrField
    bearerFormat: amf.core.client.platform.model.StrField

    constructor()

    withScheme(scheme: string): this

    withBearerFormat(bearerFormat: string): this


  }
  export class OpenIdConnectSettings extends Settings  {
    url: amf.core.client.platform.model.StrField
    scopes: Array<Scope>

    constructor()

    withUrl(url: string): this

    withScopes(scopes: Array<Scope>): this


  }
  export class SecurityScheme implements amf.core.client.platform.model.domain.DomainElement, amf.core.client.platform.model.domain.Linkable  {
    displayName: amf.core.client.platform.model.StrField
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    description: amf.core.client.platform.model.StrField
    queryString: amf.core.client.platform.model.domain.Shape
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    queryParameters: Array<Parameter>
    headers: Array<Parameter>
    type: amf.core.client.platform.model.StrField
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    settings: Settings
    responses: Array<Response>

    constructor()

    withHeaders(headers: Array<Parameter>): this

    linkCopy(): SecurityScheme

    withName(name: string): this

    withQueryParameter(name: string): Parameter

    withDescription(description: string): this

    withHttpSettings(): HttpSettings

    withOAuth2Settings(): OAuth2Settings

    withQueryString(queryString: amf.core.client.platform.model.domain.Shape): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withResponses(responses: Array<Response>): this

    withResponse(name: string): Response

    withLinkLabel(label: string): this

    withOpenIdConnectSettings(): OpenIdConnectSettings

    withQueryParameters(queryParameters: Array<Parameter>): this

    withHeader(name: string): Parameter

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withOAuth1Settings(): OAuth1Settings

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withDefaultSettings(): Settings

    withLinkTarget(target: undefined): this

    withHttpApiKeySettings(): HttpApiKeySettings

    withDisplayName(displayName: string): this

    withSettings(settings: Settings): this

    withType(type: string): this

    withId(id: string): this

    withApiKeySettings(): ApiKeySettings


  }
  export class SecurityRequirement implements amf.core.client.platform.model.domain.DomainElement  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    schemes: Array<ParametrizedSecurityScheme>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withName(name: string): this

    withScheme(): ParametrizedSecurityScheme

    withSchemes(schemes: Array<ParametrizedSecurityScheme>): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class Scope implements amf.core.client.platform.model.domain.DomainElement  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    description: amf.core.client.platform.model.StrField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withName(name: string): this

    withDescription(description: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class ParametrizedSecurityScheme implements amf.core.client.platform.model.domain.DomainElement  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    description: amf.core.client.platform.model.StrField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    scheme: SecurityScheme
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    hasNullSecurityScheme: boolean
    settings: Settings

    constructor()

    withName(name: string): this

    withDescription(description: string): this

    withHttpSettings(): HttpSettings

    withOAuth2Settings(): OAuth2Settings

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withOpenIdConnectSettings(): OpenIdConnectSettings

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withOAuth1Settings(): OAuth1Settings

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withDefaultSettings(): Settings

    withScheme(scheme: SecurityScheme): this

    withSettings(settings: Settings): this

    withId(id: string): this

    withApiKeySettings(): ApiKeySettings


  }
  export class OAuth2Flow implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    accessTokenUri: amf.core.client.platform.model.StrField
    scopes: Array<Scope>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    flow: amf.core.client.platform.model.StrField
    authorizationUri: amf.core.client.platform.model.StrField
    position: Range
    refreshUri: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withFlow(flow: string): this

    withScopes(scopes: Array<Scope>): this

    withAuthorizationUri(authorizationUri: string): this

    withAccessTokenUri(accessTokenUri: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this

    withRefreshUri(refreshUri: string): this


  }
  export class WebSocketsChannelBinding implements amf.apicontract.client.platform.model.domain.bindings.ChannelBinding  {
    method: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    query: amf.core.client.platform.model.domain.Shape
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    headers: amf.core.client.platform.model.domain.Shape
    type: amf.core.client.platform.model.StrField
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    linkCopy(): WebSocketsChannelBinding

    withMethod(method: string): this

    withHeaders(headers: amf.core.client.platform.model.domain.Shape): this

    graph(): amf.core.client.platform.model.domain.Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withType(type: string): this

    withId(id: string): this

    withQuery(query: amf.core.client.platform.model.domain.Shape): this


  }
  export class MqttServerBinding implements amf.apicontract.client.platform.model.domain.bindings.ServerBinding  {
    customDomainProperties: Array<DomainExtension>
    clientId: amf.core.client.platform.model.StrField
    keepAlive: amf.core.client.platform.model.IntField
    cleanSession: amf.core.client.platform.model.BoolField
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    lastWill: MqttServerLastWill
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withKeepAlive(keepAlive: number): this

    linkCopy(): MqttServerBinding

    withClientId(clientId: string): this

    withCleanSession(cleanSession: boolean): this

    graph(): amf.core.client.platform.model.domain.Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withId(id: string): this

    withLastWill(lastWill: MqttServerLastWill): this


  }
  export class MqttServerLastWill implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    retain: amf.core.client.platform.model.BoolField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    message: amf.core.client.platform.model.StrField
    topic: amf.core.client.platform.model.StrField
    qos: amf.core.client.platform.model.IntField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withRetain(retain: boolean): this

    withMessage(message: string): this

    withTopic(topic: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withQos(qos: number): this

    withId(id: string): this


  }
  export class MqttOperationBinding implements amf.apicontract.client.platform.model.domain.bindings.OperationBinding  {
    customDomainProperties: Array<DomainExtension>
    retain: amf.core.client.platform.model.BoolField
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    qos: amf.core.client.platform.model.IntField
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    linkCopy(): MqttOperationBinding

    withRetain(retain: boolean): this

    graph(): amf.core.client.platform.model.domain.Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withQos(qos: number): this

    withId(id: string): this


  }
  export class MqttMessageBinding implements amf.apicontract.client.platform.model.domain.bindings.MessageBinding  {
    customDomainProperties: Array<DomainExtension>
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    linkCopy(): MqttMessageBinding

    graph(): amf.core.client.platform.model.domain.Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class KafkaOperationBinding implements amf.apicontract.client.platform.model.domain.bindings.OperationBinding  {
    customDomainProperties: Array<DomainExtension>
    clientId: amf.core.client.platform.model.domain.Shape
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    groupId: amf.core.client.platform.model.domain.Shape
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    linkCopy(): KafkaOperationBinding

    withGroupId(groupId: amf.core.client.platform.model.domain.Shape): this

    withClientId(clientId: amf.core.client.platform.model.domain.Shape): this

    graph(): amf.core.client.platform.model.domain.Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class KafkaMessageBinding implements amf.apicontract.client.platform.model.domain.bindings.MessageBinding  {
    customDomainProperties: Array<DomainExtension>
    messageKey: amf.core.client.platform.model.domain.Shape
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withKey(key: amf.core.client.platform.model.domain.Shape): this

    linkCopy(): KafkaMessageBinding

    graph(): amf.core.client.platform.model.domain.Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class HttpOperationBinding implements amf.apicontract.client.platform.model.domain.bindings.OperationBinding  {
    method: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    operationType: amf.core.client.platform.model.StrField
    query: amf.core.client.platform.model.domain.Shape
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    linkCopy(): HttpOperationBinding

    withMethod(method: string): this

    withOperationType(type: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withId(id: string): this

    withQuery(query: amf.core.client.platform.model.domain.Shape): this


  }
  export class HttpMessageBinding implements amf.apicontract.client.platform.model.domain.bindings.MessageBinding  {
    customDomainProperties: Array<DomainExtension>
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    headers: amf.core.client.platform.model.domain.Shape
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    linkCopy(): HttpMessageBinding

    withHeaders(headers: amf.core.client.platform.model.domain.Shape): this

    graph(): amf.core.client.platform.model.domain.Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class Amqp091OperationBinding implements amf.apicontract.client.platform.model.domain.bindings.OperationBinding  {
    priority: amf.core.client.platform.model.IntField
    customDomainProperties: Array<DomainExtension>
    timestamp: amf.core.client.platform.model.BoolField
    mandatory: amf.core.client.platform.model.BoolField
    replyTo: amf.core.client.platform.model.StrField
    deliveryMode: amf.core.client.platform.model.IntField
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    ack: amf.core.client.platform.model.BoolField
    bcc: Array<amf.core.client.platform.model.StrField>
    position: Range
    cc: Array<amf.core.client.platform.model.StrField>
    userId: amf.core.client.platform.model.StrField
    linkLabel: amf.core.client.platform.model.StrField
    expiration: amf.core.client.platform.model.IntField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withDeliveryMode(deliveryMode: number): this

    linkCopy(): Amqp091OperationBinding

    withCc(cC: Array<string>): this

    withTimestamp(timestamp: boolean): this

    withReplyTo(replyTo: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withMandatory(mandatory: boolean): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withBcc(bCC: Array<string>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withUserId(userId: string): this

    withAck(ack: boolean): this

    withPriority(priority: number): this

    withExpiration(expiration: number): this

    withId(id: string): this


  }
  export class Amqp091MessageBinding implements amf.apicontract.client.platform.model.domain.bindings.MessageBinding  {
    customDomainProperties: Array<DomainExtension>
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    messageType: amf.core.client.platform.model.StrField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    contentEncoding: amf.core.client.platform.model.StrField
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withMessageType(messageType: string): this

    linkCopy(): Amqp091MessageBinding

    withContentEncoding(contentEncoding: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class Amqp091ChannelBinding implements amf.apicontract.client.platform.model.domain.bindings.ChannelBinding  {
    is: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    queue: Amqp091Queue
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    exchange: Amqp091ChannelExchange
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    linkCopy(): Amqp091ChannelBinding

    withQueue(queue: Amqp091Queue): this

    graph(): amf.core.client.platform.model.domain.Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withIs(is: string): this

    withExchange(exchange: Amqp091ChannelExchange): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class Amqp091ChannelExchange implements amf.core.client.platform.model.domain.DomainElement  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    autoDelete: amf.core.client.platform.model.BoolField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    vHost: amf.core.client.platform.model.StrField
    position: Range
    type: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    durable: amf.core.client.platform.model.BoolField

    constructor()

    withName(name: string): this

    withDurable(durable: boolean): this

    withVHost(vHost: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withAutoDelete(autoDelete: boolean): this

    withType(type: string): this

    withId(id: string): this


  }
  export class Amqp091Queue implements amf.core.client.platform.model.domain.DomainElement  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    autoDelete: amf.core.client.platform.model.BoolField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    vHost: amf.core.client.platform.model.StrField
    exclusive: amf.core.client.platform.model.BoolField
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    durable: amf.core.client.platform.model.BoolField

    constructor()

    withName(name: string): this

    withDurable(durable: boolean): this

    withVHost(vHost: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExclusive(exclusive: boolean): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withAutoDelete(autoDelete: boolean): this

    withId(id: string): this


  }
  export class ServerBindings implements amf.core.client.platform.model.domain.DomainElement, amf.core.client.platform.model.domain.Linkable  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    bindings: Array<amf.apicontract.client.platform.model.domain.bindings.ServerBinding>
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withBindings(bindings: Array<amf.apicontract.client.platform.model.domain.bindings.ServerBinding>): this

    linkCopy(): ServerBindings

    withName(name: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class OperationBindings implements amf.core.client.platform.model.domain.DomainElement, amf.core.client.platform.model.domain.Linkable  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    bindings: Array<amf.apicontract.client.platform.model.domain.bindings.OperationBinding>
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    linkCopy(): OperationBindings

    withName(name: string): this

    withBindings(bindings: Array<amf.apicontract.client.platform.model.domain.bindings.OperationBinding>): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class MessageBindings implements amf.core.client.platform.model.domain.DomainElement, amf.core.client.platform.model.domain.Linkable  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    bindings: Array<amf.apicontract.client.platform.model.domain.bindings.MessageBinding>
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    linkCopy(): MessageBindings

    withName(name: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withBindings(bindings: Array<amf.apicontract.client.platform.model.domain.bindings.MessageBinding>): this

    withId(id: string): this


  }
  export class EmptyBinding implements amf.apicontract.client.platform.model.domain.bindings.ServerBinding, amf.apicontract.client.platform.model.domain.bindings.OperationBinding, amf.apicontract.client.platform.model.domain.bindings.ChannelBinding, amf.apicontract.client.platform.model.domain.bindings.MessageBinding  {
    customDomainProperties: Array<DomainExtension>
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    type: amf.core.client.platform.model.StrField
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    linkCopy(): EmptyBinding

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withType(type: string): this

    withId(id: string): this


  }
  export class ChannelBindings implements amf.core.client.platform.model.domain.DomainElement, amf.core.client.platform.model.domain.Linkable  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    bindings: Array<amf.apicontract.client.platform.model.domain.bindings.ChannelBinding>
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    linkCopy(): ChannelBindings

    withName(name: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withBindings(bindings: Array<amf.apicontract.client.platform.model.domain.bindings.ChannelBinding>): this

    withId(id: string): this


  }
  export class WebApi extends amf.apicontract.client.platform.model.domain.api.Api<WebApi>  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    endPoints: Array<EndPoint>
    provider: Organization
    security: Array<SecurityRequirement>
    identifier: amf.core.client.platform.model.StrField
    description: amf.core.client.platform.model.StrField
    documentations: Array<CreativeWork>
    servers: Array<Server>
    schemes: Array<amf.core.client.platform.model.StrField>
    license: License
    isExternalLink: amf.core.client.platform.model.BoolField
    sourceVendor: undefined | Vendor
    termsOfService: amf.core.client.platform.model.StrField
    version: amf.core.client.platform.model.StrField
    id: string
    contentType: Array<amf.core.client.platform.model.StrField>
    accepts: Array<amf.core.client.platform.model.StrField>
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withDocumentationTitle(title: string): CreativeWork

    withSecurity(security: Array<SecurityRequirement>): this

    withProvider(provider: Organization): this

    withName(name: string): this

    withEndPoint(path: string): EndPoint

    withDefaultServer(url: string): Server

    withDocumentation(documentations: Array<CreativeWork>): this

    withDescription(description: string): this

    withDocumentationUrl(url: string): CreativeWork

    withLicense(license: License): this

    graph(): amf.core.client.platform.model.domain.Graph

    withEndPoints(endPoints: Array<EndPoint>): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withTermsOfService(terms: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withVersion(version: string): this

    withContentType(contentType: Array<string>): this

    withServer(url: string): Server

    withServers(servers: Array<Server>): this

    withSchemes(schemes: Array<string>): this

    withIdentifier(identifier: string): this

    withAccepts(accepts: Array<string>): this

    withId(id: string): this


  }
  export class AsyncApi extends amf.apicontract.client.platform.model.domain.api.Api<AsyncApi>  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    endPoints: Array<EndPoint>
    provider: Organization
    security: Array<SecurityRequirement>
    identifier: amf.core.client.platform.model.StrField
    description: amf.core.client.platform.model.StrField
    documentations: Array<CreativeWork>
    servers: Array<Server>
    schemes: Array<amf.core.client.platform.model.StrField>
    license: License
    isExternalLink: amf.core.client.platform.model.BoolField
    sourceVendor: undefined | Vendor
    termsOfService: amf.core.client.platform.model.StrField
    version: amf.core.client.platform.model.StrField
    id: string
    contentType: Array<amf.core.client.platform.model.StrField>
    accepts: Array<amf.core.client.platform.model.StrField>
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withDocumentationTitle(title: string): CreativeWork

    withSecurity(security: Array<SecurityRequirement>): this

    withProvider(provider: Organization): this

    withName(name: string): this

    withEndPoint(path: string): EndPoint

    withDefaultServer(url: string): Server

    withDocumentation(documentations: Array<CreativeWork>): this

    withDescription(description: string): this

    withDocumentationUrl(url: string): CreativeWork

    withLicense(license: License): this

    graph(): amf.core.client.platform.model.domain.Graph

    withEndPoints(endPoints: Array<EndPoint>): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withTermsOfService(terms: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withVersion(version: string): this

    withContentType(contentType: Array<string>): this

    withServer(url: string): Server

    withServers(servers: Array<Server>): this

    withSchemes(schemes: Array<string>): this

    withIdentifier(identifier: string): this

    withAccepts(accepts: Array<string>): this

    withId(id: string): this


  }
  export class TemplatedLink implements amf.core.client.platform.model.domain.DomainElement  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    server: Server
    description: amf.core.client.platform.model.StrField
    mapping: Array<IriTemplateMapping>
    operationId: amf.core.client.platform.model.StrField
    isExternalLink: amf.core.client.platform.model.BoolField
    operationRef: amf.core.client.platform.model.StrField
    id: string
    requestBody: amf.core.client.platform.model.StrField
    template: amf.core.client.platform.model.StrField
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withRequestBody(requestBody: string): this

    withServer(server: Server): this

    withName(name: string): this

    withMapping(mapping: Array<IriTemplateMapping>): this

    withDescription(description: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withTemplate(template: string): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withOperationId(operationId: string): this

    withId(id: string): this

    withOperationRef(operationRef: string): this


  }
  export class Tag implements amf.core.client.platform.model.domain.DomainElement  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    description: amf.core.client.platform.model.StrField
    documentation: CreativeWork
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withName(name: string): this

    withDescription(description: string): this

    withVariables(documentation: CreativeWork): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class Server implements amf.core.client.platform.model.domain.DomainElement  {
    protocolVersion: amf.core.client.platform.model.StrField
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    security: Array<SecurityRequirement>
    url: amf.core.client.platform.model.StrField
    description: amf.core.client.platform.model.StrField
    bindings: ServerBindings
    variables: Array<Parameter>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    protocol: amf.core.client.platform.model.StrField

    constructor()

    withSecurity(security: Array<SecurityRequirement>): this

    withVariables(variables: Array<Parameter>): this

    withDescription(description: string): this

    withProtocol(protocol: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withVariable(name: string): Parameter

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withBindings(bindings: ServerBindings): this

    withProtocolVersion(protocolVersion: string): this

    withUrl(url: string): this

    withId(id: string): this


  }
  export class Response extends Message  {
    statusCode: amf.core.client.platform.model.StrField
    headers: Array<Parameter>
    links: Array<TemplatedLink>

    constructor()

    withStatusCode(statusCode: string): this

    withHeaders(headers: Array<Parameter>): this

    withLinks(links: Array<TemplatedLink>): this

    withHeader(name: string): Parameter

    linkCopy(): Response


  }
  export class Request extends Message  {
    required: amf.core.client.platform.model.BoolField
    queryParameters: Array<Parameter>
    headers: Array<Parameter>
    queryString: amf.core.client.platform.model.domain.Shape
    uriParameters: Array<Parameter>
    cookieParameters: Array<Parameter>

    constructor()

    withRequired(required: boolean): this

    withQueryParameters(parameters: Array<Parameter>): this

    withHeaders(headers: Array<Parameter>): this

    withQueryString(queryString: amf.core.client.platform.model.domain.Shape): this

    withUriParameters(uriParameters: Array<Parameter>): this

    withCookieParameters(cookieParameters: Array<Parameter>): this

    withQueryParameter(name: string): Parameter

    withHeader(name: string): Parameter

    withUriParameter(name: string): Parameter

    withCookieParameter(name: string): Parameter

    linkCopy(): Request


  }
  export class Payload implements amf.core.client.platform.model.domain.DomainElement  {
    mediaType: amf.core.client.platform.model.StrField
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    examples: Array<Example>
    encoding: Array<Encoding>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    schema: amf.core.client.platform.model.domain.Shape
    schemaMediaType: amf.core.client.platform.model.StrField
    position: Range
    encodings: Array<Encoding>
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withEncoding(name: string): Encoding

    withEncodings(encoding: Array<Encoding>): this

    withName(name: string): this

    withScalarSchema(name: string): ScalarShape

    withExample(name: string): Example

    withExamples(examples: Array<Example>): this

    withObjectSchema(name: string): NodeShape

    withSchema(schema: amf.core.client.platform.model.domain.Shape): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withMediaType(mediaType: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withSchemaMediaType(mediaType: string): this

    withEncoding(encoding: Array<Encoding>): this

    withId(id: string): this


  }
  export class Parameter implements amf.core.client.platform.model.domain.DomainElement  {
    name: amf.core.client.platform.model.StrField
    binding: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    examples: Array<Example>
    style: amf.core.client.platform.model.StrField
    description: amf.core.client.platform.model.StrField
    payloads: Array<Payload>
    deprecated: amf.core.client.platform.model.BoolField
    allowReserved: amf.core.client.platform.model.BoolField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    schema: amf.core.client.platform.model.domain.Shape
    explode: amf.core.client.platform.model.BoolField
    parameterName: amf.core.client.platform.model.StrField
    position: Range
    required: amf.core.client.platform.model.BoolField
    allowEmptyValue: amf.core.client.platform.model.BoolField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withPayloads(payloads: Array<Payload>): this

    withExplode(explode: boolean): this

    withName(name: string): this

    withScalarSchema(name: string): ScalarShape

    withExample(name: string): Example

    withDescription(description: string): this

    withStyle(style: string): this

    withAllowEmptyValue(allowEmptyValue: boolean): this

    withPayload(mediaType: string): Payload

    withExamples(examples: Array<Example>): this

    withObjectSchema(name: string): NodeShape

    withSchema(schema: amf.core.client.platform.model.domain.Shape): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withAllowReserved(allowReserved: boolean): this

    withRequired(required: boolean): this

    withId(id: string): this

    withParameterName(name: string): this

    withBinding(binding: string): this

    withDeprecated(deprecated: boolean): this


  }
  export class Organization implements amf.core.client.platform.model.domain.DomainElement  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    email: amf.core.client.platform.model.StrField
    url: amf.core.client.platform.model.StrField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withEmail(email: string): this

    withName(name: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withUrl(url: string): this

    withId(id: string): this


  }
  export class Operation implements amf.core.client.platform.model.domain.DomainElement, amf.core.client.platform.model.domain.Linkable  {
    method: amf.core.client.platform.model.StrField
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    request: Request
    security: Array<SecurityRequirement>
    description: amf.core.client.platform.model.StrField
    bindings: OperationBindings
    tags: Array<Tag>
    documentation: CreativeWork
    deprecated: amf.core.client.platform.model.BoolField
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    operationId: amf.core.client.platform.model.StrField
    servers: Array<Server>
    schemes: Array<amf.core.client.platform.model.StrField>
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    contentType: Array<amf.core.client.platform.model.StrField>
    accepts: Array<amf.core.client.platform.model.StrField>
    position: Range
    isAbstract: amf.core.client.platform.model.BoolField
    linkLabel: amf.core.client.platform.model.StrField
    callbacks: Array<Callback>
    requests: Array<Request>
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    summary: amf.core.client.platform.model.StrField
    responses: Array<Response>

    constructor()

    withSecurity(security: Array<SecurityRequirement>): this

    linkCopy(): Operation

    withName(name: string): this

    withMethod(method: string): this

    withDescription(description: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withBindings(bindings: OperationBindings): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withResponses(responses: Array<Response>): this

    withResponse(name: string): Response

    withLinkLabel(label: string): this

    withTags(tags: Array<Tag>): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withSummary(summary: string): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withContentType(contentType: Array<string>): this

    withOperationId(operationId: string): this

    withCallbacks(callbacks: Array<Callback>): this

    withServers(servers: Array<Server>): this

    withSchemes(schemes: Array<string>): this

    withServer(name: string): Server

    withCallback(name: string): Callback

    withAccepts(accepts: Array<string>): this

    withAbstract(abs: boolean): this

    withRequest(request: Request): this

    withId(id: string): this

    withRequest(): Request

    withDocumentation(documentation: CreativeWork): this

    withDeprecated(deprecated: boolean): this


  }
  export class Message implements amf.core.client.platform.model.domain.DomainElement, amf.core.client.platform.model.domain.Linkable  {
    displayName: amf.core.client.platform.model.StrField
    name: amf.core.client.platform.model.StrField
    headerSchema: NodeShape
    customDomainProperties: Array<DomainExtension>
    examples: Array<Example>
    description: amf.core.client.platform.model.StrField
    bindings: MessageBindings
    tags: Array<Tag>
    documentation: CreativeWork
    payloads: Array<Payload>
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    correlationId: CorrelationId
    isAbstract: amf.core.client.platform.model.BoolField
    title: amf.core.client.platform.model.StrField
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    headerExamples: Array<Example>
    summary: amf.core.client.platform.model.StrField

    constructor()

    withHeaderSchema(schema: NodeShape): this

    withPayloads(payloads: Array<Payload>): this

    withPayload(): Payload

    linkCopy(): Message

    withAbstract(isAbstract: boolean): this

    withName(name: string): this

    withDescription(description: string): this

    withTitle(title: string): this

    withBindings(bindings: MessageBindings): this

    withPayload(mediaType: string): Payload

    withExamples(examples: Array<Example>): this

    graph(): amf.core.client.platform.model.domain.Graph

    withHeaderExamples(examples: Array<Example>): this

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withTags(tags: Array<Tag>): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withSummary(summary: string): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withPayload(mediaType: undefined | string): Payload

    withDisplayName(displayName: string): this

    withCorrelationId(correlationId: CorrelationId): this

    withId(id: string): this

    withDocumentation(documentation: CreativeWork): this


  }
  export class License implements amf.core.client.platform.model.domain.DomainElement  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    url: amf.core.client.platform.model.StrField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withName(name: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withUrl(url: string): this

    withId(id: string): this


  }
  export class EndPoint implements amf.core.client.platform.model.domain.DomainElement  {
    parent: undefined | EndPoint
    operations: Array<Operation>
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    path: amf.core.client.platform.model.StrField
    security: Array<SecurityRequirement>
    description: amf.core.client.platform.model.StrField
    bindings: ChannelBindings
    relativePath: string
    payloads: Array<Payload>
    servers: Array<Server>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    parameters: Array<Parameter>
    summary: amf.core.client.platform.model.StrField

    constructor()

    withSecurity(security: Array<SecurityRequirement>): this

    withPayloads(payloads: Array<Payload>): this

    withPath(path: string): this

    withParameter(name: string): Parameter

    withName(name: string): this

    withDescription(description: string): this

    withOperation(method: string): Operation

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withPayload(name: string): Payload

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withSummary(summary: string): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withServer(url: string): Server

    withOperations(operations: Array<Operation>): this

    withServers(servers: Array<Server>): this

    withParameters(parameters: Array<Parameter>): this

    withId(id: string): this

    withBindings(bindings: ChannelBindings): this


  }
  export class Encoding implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    style: amf.core.client.platform.model.StrField
    allowReserved: amf.core.client.platform.model.BoolField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    contentType: amf.core.client.platform.model.StrField
    explode: amf.core.client.platform.model.BoolField
    position: Range
    propertyName: amf.core.client.platform.model.StrField
    headers: Array<Parameter>
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withContentType(contentType: string): this

    withHeaders(headers: Array<Parameter>): this

    withExplode(explode: boolean): this

    withStyle(style: string): this

    withPropertyName(propertyName: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withHeader(name: string): Parameter

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withAllowReserved(allowReserved: boolean): this

    withId(id: string): this


  }
  export class CorrelationId implements amf.core.client.platform.model.domain.DomainElement, amf.core.client.platform.model.domain.Linkable  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    description: amf.core.client.platform.model.StrField
    idLocation: amf.core.client.platform.model.StrField
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    linkCopy(): CorrelationId

    withName(name: string): this

    withDescription(description: string): this

    withIdLocation(idLocation: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class Callback implements amf.core.client.platform.model.domain.DomainElement  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    expression: amf.core.client.platform.model.StrField
    endpoint: EndPoint
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withExpression(expression: string): this

    withName(name: string): this

    withEndpoint(endpoint: EndPoint): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withEndpoint(path: string): EndPoint

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class Overlay extends Document  {
    constructor()

  }
  export class DocumentationItem extends amf.core.client.platform.model.document.Fragment  {
    constructor()

  }
  export class DataType extends amf.core.client.platform.model.document.Fragment  {
    constructor()

  }
  export class NamedExample extends amf.core.client.platform.model.document.Fragment  {
    constructor()

  }
  export class ResourceTypeFragment extends amf.core.client.platform.model.document.Fragment  {
    constructor()

  }
  export class TraitFragment extends amf.core.client.platform.model.document.Fragment  {
    constructor()

  }
  export class AnnotationTypeDeclaration extends amf.core.client.platform.model.document.Fragment  {
    constructor()

  }
  export class SecuritySchemeFragment extends amf.core.client.platform.model.document.Fragment  {
    constructor()

  }
  export class Extension extends Document  {
    constructor()

  }
  export class RAMLConfiguration  {
    static RAML10(): AMFConfiguration

    static RAML08(): AMFConfiguration

    static RAML(): AMFConfiguration


  }
  export class OASConfiguration  {
    static OAS20(): AMFConfiguration

    static OAS30(): AMFConfiguration

    static OAS(): AMFConfiguration


  }
  export class WebAPIConfiguration  {
    static WebAPI(): AMFConfiguration


  }
  export class AsyncAPIConfiguration  {
    static Async20(): AMFConfiguration


  }
  export class AMFClient extends amf.aml.client.platform.BaseAMLClient  {
    constructor(configuration: AMFConfiguration)

    getConfiguration(): AMFConfiguration

    parseDocument(url: string): Promise<AMFDocumentResult>

    parseLibrary(url: string): Promise<AMFLibraryResult>

    transformDefault(bu: amf.core.client.platform.model.document.BaseUnit, targetMediaType: string): AMFResult

    transformEditing(bu: amf.core.client.platform.model.document.BaseUnit, targetMediaType: string): AMFResult

    transformCompatibility(bu: amf.core.client.platform.model.document.BaseUnit, targetMediaType: string): AMFResult

    transformCache(bu: amf.core.client.platform.model.document.BaseUnit, targetMediaType: string): AMFResult


  }
  export class ProvidedMediaType  {
    static readonly Raml08: '+yaml'
    static readonly Raml10: '+yaml'
    static readonly Oas20: string
    static readonly Oas20Yaml: '+yaml'
    static readonly Oas20Json: '+json'
    static readonly Oas30: string
    static readonly Oas30Yaml: '+yaml'
    static readonly Oas30Json: '+json'
    static readonly Async20: string
    static readonly Async20Yaml: '+yaml'
    static readonly Async20Json: '+json'
    static readonly Payload: string
    static readonly PayloadYaml: '+yaml'
    static readonly PayloadJson: '+json'
    static readonly AMF: string
    static readonly JsonSchema: string

  }
  export class Vendor  {
    static readonly RAML08: Vendor
    static readonly RAML10: Vendor
    static readonly OAS20: Vendor
    static readonly OAS30: Vendor
    static readonly ASYNC: Vendor
    static readonly ASYNC20: Vendor
    static readonly AMF: Vendor
    static readonly PAYLOAD: Vendor
    static readonly AML: Vendor
    static readonly JSONSCHEMA: Vendor

    apply(name: string): Vendor

    mediaType: string
  }
  export class Variable  {
    name: string
    value: amf.core.client.platform.model.domain.DataNode

    constructor(name: string, value: amf.core.client.platform.model.domain.DataNode)

  }
  export class ValidationResult  {
    message: string
    level: string
    targetNode: string
    targetProperty: string
    validationId: string
    source: any
    lexical: Range
    location: undefined | string

    constructor(message: string, level: string, targetNode: string, targetProperty: string, validationId: string, position: Range, location: string)

  }
  export class PayloadParsingResult  {
    fragment: PayloadFragment
    results: Array<ValidationResult>

    constructor(fragment: PayloadFragment, results: Array<ValidationResult>)

  }
  export class ValidationShapeSet  {
    candidates: Array<ValidationCandidate>
    defaultSeverity: string

    constructor(candidates: Array<ValidationCandidate>, closure: Array<amf.core.client.platform.model.domain.Shape>, defaultSeverity: string)

  }
  export class ValidationCandidate  {
    shape: amf.core.client.platform.model.domain.Shape
    payload: PayloadFragment

    constructor(shape: amf.core.client.platform.model.domain.Shape, payload: PayloadFragment)

  }
  export class AMFValidator  {
    validate(bu: amf.core.client.platform.model.document.BaseUnit, conf: AMFGraphConfiguration): Promise<AMFValidationReport>

    validate(bu: amf.core.client.platform.model.document.BaseUnit, profileName: ProfileName, conf: AMFGraphConfiguration): Promise<AMFValidationReport>


  }
  export class AMFValidationReport  {
    conforms: boolean
    model: string
    profile: ProfileName
    results: Array<ValidationResult>
    toString: string

    constructor(model: string, profile: ProfileName, results: Array<ValidationResult>)

    toStringMaxed(max: number): string


  }
  export class TransformationPipelineBuilder  {
    static empty(pipelineName: string): TransformationPipelineBuilder

    static fromPipeline(pipeline: TransformationPipeline): TransformationPipelineBuilder

    static fromPipeline(pipelineName: string, conf: AMFGraphConfiguration): undefined | TransformationPipelineBuilder

    build(): TransformationPipeline

    withName(newName: string): TransformationPipelineBuilder

    append(newStage: TransformationStep): TransformationPipelineBuilder

    prepend(newStage: TransformationStep): TransformationPipelineBuilder
  }
  export class AMFTransformer  {
    transform(unit: amf.core.client.platform.model.document.BaseUnit, configuration: AMFGraphConfiguration): AMFResult

    transform(unit: amf.core.client.platform.model.document.BaseUnit, pipelineName: string, configuration: AMFGraphConfiguration): AMFResult


  }
  export class ResourceNotFound  {
    readonly msj: string

    constructor(msj: string)

  }
  export class ResourceLoaderFactory  {
    static create(loader: ClientResourceLoader): ResourceLoader
  }
  export class AMFRenderer  {
    render(bu: amf.core.client.platform.model.document.BaseUnit, env: AMFGraphConfiguration): string

    render(bu: amf.core.client.platform.model.document.BaseUnit, mediaType: string, env: AMFGraphConfiguration): string

    renderGraphToBuilder<T>(bu: amf.core.client.platform.model.document.BaseUnit, builder: org.yaml.builder.JsOutputBuilder, config: AMFGraphConfiguration): T


  }
  export class CachedReference  {
    url: string
    content: amf.core.client.platform.model.document.BaseUnit
    resolved: boolean

    constructor(url: string, content: amf.core.client.platform.model.document.BaseUnit, resolved: boolean)

  }
  export class AMFParser  {
    parse(url: string, configuration: AMFGraphConfiguration): Promise<AMFResult>

    parse(url: string, mediaType: string, configuration: AMFGraphConfiguration): Promise<AMFResult>

    parseContent(content: string, configuration: AMFGraphConfiguration): Promise<AMFResult>

    parseContent(content: string, mediaType: string, configuration: AMFGraphConfiguration): Promise<AMFResult>


  }
  export class ShapeExtension implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    definedBy: PropertyShape
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    extension: amf.core.client.platform.model.domain.DataNode

    constructor()

    withDefinedBy(definedBy: PropertyShape): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withExtension(extension: amf.core.client.platform.model.domain.DataNode): this

    withId(id: string): this


  }
  export class ScalarNode implements amf.core.client.platform.model.domain.DataNode  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    toString: undefined
    dataType: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    value: amf.core.client.platform.model.StrField

    constructor()
    constructor(value: string, dataType: string)

    withName(name: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withDataType(dataType: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withValue(value: string): this

    withId(id: string): this


  }
  export class PropertyShape implements amf.core.client.platform.model.domain.Shape  {
    defaultValueStr: amf.core.client.platform.model.StrField
    displayName: amf.core.client.platform.model.StrField
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    path: amf.core.client.platform.model.StrField
    xone: Array<amf.core.client.platform.model.domain.Shape>
    readOnly: amf.core.client.platform.model.BoolField
    description: amf.core.client.platform.model.StrField
    deprecated: amf.core.client.platform.model.BoolField
    customShapePropertyDefinitions: Array<PropertyShape>
    or: Array<amf.core.client.platform.model.domain.Shape>
    elseShape: amf.core.client.platform.model.domain.Shape
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    maxCount: amf.core.client.platform.model.IntField
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    customShapeProperties: Array<ShapeExtension>
    thenShape: amf.core.client.platform.model.domain.Shape
    id: string
    range: amf.core.client.platform.model.domain.Shape
    ifShape: amf.core.client.platform.model.domain.Shape
    writeOnly: amf.core.client.platform.model.BoolField
    patternName: amf.core.client.platform.model.StrField
    not: amf.core.client.platform.model.domain.Shape
    values: Array<amf.core.client.platform.model.domain.DataNode>
    position: Range
    inherits: Array<amf.core.client.platform.model.domain.Shape>
    linkLabel: amf.core.client.platform.model.StrField
    defaultValue: amf.core.client.platform.model.domain.DataNode
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    and: Array<amf.core.client.platform.model.domain.Shape>
    minCount: amf.core.client.platform.model.IntField

    constructor()

    withValues(values: Array<amf.core.client.platform.model.domain.DataNode>): this

    withPath(path: string): this

    linkCopy(): PropertyShape

    withOr(subShapes: Array<amf.core.client.platform.model.domain.Shape>): this

    withName(name: string): this

    withRange(range: amf.core.client.platform.model.domain.Shape): this

    withDescription(description: string): this

    withMaxCount(max: number): this

    withIf(ifShape: amf.core.client.platform.model.domain.Shape): this

    withCustomShapePropertyDefinition(name: string): PropertyShape

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withCustomShapePropertyDefinitions(propertyDefinitions: Array<PropertyShape>): this

    withReadOnly(readOnly: boolean): this

    withPatternName(pattern: string): this

    withInherits(inherits: Array<amf.core.client.platform.model.domain.Shape>): this

    withAnd(subShapes: Array<amf.core.client.platform.model.domain.Shape>): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withWriteOnly(writeOnly: boolean): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withDisplayName(name: string): this

    withDefaultValue(defaultVal: amf.core.client.platform.model.domain.DataNode): this

    withThen(thenShape: amf.core.client.platform.model.domain.Shape): this

    withMinCount(min: number): this

    withDefaultStr(value: string): this

    withCustomShapeProperties(customShapeProperties: Array<ShapeExtension>): this

    withId(id: string): this

    withElse(elseShape: amf.core.client.platform.model.domain.Shape): this

    withXone(subShapes: Array<amf.core.client.platform.model.domain.Shape>): this

    withDeprecated(deprecated: boolean): this

    withNode(shape: amf.core.client.platform.model.domain.Shape): this


  }
  export class ObjectNode implements amf.core.client.platform.model.domain.DataNode  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    properties: Map<string, amf.core.client.platform.model.domain.DataNode>
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withName(name: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    getProperty(property: string): undefined | amf.core.client.platform.model.domain.DataNode

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this

    addProperty(property: string, node: amf.core.client.platform.model.domain.DataNode): this


  }
  export class ExternalDomainElement implements amf.core.client.platform.model.domain.DomainElement  {
    mediaType: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    raw: amf.core.client.platform.model.StrField
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withRaw(raw: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withMediaType(mediaType: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class DomainExtension implements amf.core.client.platform.model.domain.DomainElement  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    definedBy: CustomDomainProperty
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>
    extension: amf.core.client.platform.model.domain.DataNode

    constructor()

    withName(name: string): this

    withDefinedBy(property: CustomDomainProperty): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this

    withExtension(node: amf.core.client.platform.model.domain.DataNode): this


  }
  export class CustomDomainProperty implements amf.core.client.platform.model.domain.DomainElement, amf.core.client.platform.model.domain.Linkable  {
    displayName: amf.core.client.platform.model.StrField
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    description: amf.core.client.platform.model.StrField
    domain: Array<amf.core.client.platform.model.StrField>
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    schema: amf.core.client.platform.model.domain.Shape
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    linkCopy(): CustomDomainProperty

    withName(name: string): this

    withDescription(description: string): this

    withDomain(domain: Array<string>): this

    withSchema(schema: amf.core.client.platform.model.domain.Shape): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    withDisplayName(displayName: string): this

    withId(id: string): this


  }
  export class ArrayNode implements amf.core.client.platform.model.domain.DataNode  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    members: Array<amf.core.client.platform.model.domain.DataNode>
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withName(name: string): this

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this

    addMember(member: amf.core.client.platform.model.domain.DataNode): this


  }
  export class Module implements amf.core.client.platform.model.document.BaseUnit, amf.core.client.platform.model.document.DeclaresModel, amf.core.client.platform.model.domain.CustomizableElement  {
    customDomainProperties: Array<DomainExtension>
    location: string
    usage: amf.core.client.platform.model.StrField
    sourceVendor: undefined | Vendor
    id: string
    raw: undefined | string
    modelVersion: amf.core.client.platform.model.StrField
    declares: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    findByType(typeId: string): Array<amf.core.client.platform.model.domain.DomainElement>

    cloneUnit(): amf.core.client.platform.model.document.BaseUnit

    withReferences(references: Array<amf.core.client.platform.model.document.BaseUnit>): this

    withDeclaredElement(declared: amf.core.client.platform.model.domain.DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | amf.core.client.platform.model.domain.DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): amf.core.client.platform.model.document.BaseUnit

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withDeclares(declares: Array<amf.core.client.platform.model.domain.DomainElement>): this

    references(): Array<amf.core.client.platform.model.document.BaseUnit>

    withId(id: string): this


  }
  export class PayloadFragment extends amf.core.client.platform.model.document.Fragment  {
    mediaType: amf.core.client.platform.model.StrField
    dataNode: amf.core.client.platform.model.domain.DataNode

    constructor(scalar: ScalarNode, mediaType: string)
    constructor(obj: ObjectNode, mediaType: string)
    constructor(arr: ArrayNode, mediaType: string)

  }
  export class ExternalFragment extends amf.core.client.platform.model.document.Fragment  {
    constructor()

  }
  export class Document implements amf.core.client.platform.model.document.BaseUnit, amf.core.client.platform.model.document.EncodesModel, amf.core.client.platform.model.document.DeclaresModel  {
    location: string
    usage: amf.core.client.platform.model.StrField
    sourceVendor: undefined | Vendor
    id: string
    raw: undefined | string
    modelVersion: amf.core.client.platform.model.StrField
    encodes: amf.core.client.platform.model.domain.DomainElement
    declares: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()
    constructor(encoding: amf.core.client.platform.model.domain.DomainElement)

    findByType(typeId: string): Array<amf.core.client.platform.model.domain.DomainElement>

    cloneUnit(): amf.core.client.platform.model.document.BaseUnit

    withReferences(references: Array<amf.core.client.platform.model.document.BaseUnit>): this

    withDeclaredElement(declared: amf.core.client.platform.model.domain.DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | amf.core.client.platform.model.domain.DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): amf.core.client.platform.model.document.BaseUnit

    withEncodes(encoded: amf.core.client.platform.model.domain.DomainElement): this

    withDeclares(declares: Array<amf.core.client.platform.model.domain.DomainElement>): this

    references(): Array<amf.core.client.platform.model.document.BaseUnit>

    withId(id: string): this


  }
  export class Annotations  {
    isLocal: boolean
    t: boolean
    resolvedLink: undefined | string
    resolvedLinkTarget: undefined | string
    inheritanceProvenance: undefined | string
    inlinedElement: boolean
    autoGeneratedName: boolean

    constructor()

    lexical(): Range

    custom(): Array<DomainExtension>

    fragmentName(): undefined | string

    location(): undefined | string

    isTrackedBy(trackId: string): boolean


  }
  export class ShapeRenderOptions  {
    isWithDocumentation: boolean
    isWithCompactedEmission: boolean
    withoutDocumentation: ShapeRenderOptions
    withoutCompactedEmission: ShapeRenderOptions

    constructor()
    constructor(_internal: undefined)

  }
  export class RenderOptions  {
    withPrettyPrint: RenderOptions
    withoutPrettyPrint: RenderOptions
    withSourceMaps: RenderOptions
    withoutSourceMaps: RenderOptions
    withCompactUris: RenderOptions
    withoutCompactUris: RenderOptions
    withFlattenedJsonLd: RenderOptions
    withoutFlattenedJsonLd: RenderOptions
    withoutAmfJsonLdSerialization: RenderOptions
    withAmfJsonLdSerialization: RenderOptions
    withNodeIds: RenderOptions
    isWithCompactUris: boolean
    isWithSourceMaps: boolean
    isAmfJsonLdSerilization: boolean
    isPrettyPrint: boolean
    isEmitNodeIds: boolean
    isFlattenedJsonLd: boolean
    shapeRenderOptions: ShapeRenderOptions

    constructor()

    withShapeRenderOptions(s: ShapeRenderOptions): RenderOptions


  }
  export class ParsingOptions  {
    isAmfJsonLdSerialization: boolean
    definedBaseUrl: undefined | string
    getMaxYamlReferences: undefined | number
    withoutAmfJsonLdSerialization: ParsingOptions
    withAmfJsonLdSerialization: ParsingOptions

    constructor()

    withBaseUnitUrl(baseUnit: string): ParsingOptions

    withoutBaseUnitUrl(): ParsingOptions

    setMaxYamlReferences(value: number): ParsingOptions


  }
  export class EventNames  {
    static readonly StartingParsing: 'StartingParsing'
    static readonly StartingContentParsing: 'StartingContentParsing'
    static readonly ParsedSyntax: 'ParsedSyntax'
    static readonly ParsedModel: 'ParsedModel'
    static readonly FinishedParsing: 'FinishedParsing'
    static readonly StartingTransformation: 'StartingTransformation'
    static readonly FinishedTransformationStep: 'FinishedTransformationStep'
    static readonly FinishedTransformation: 'FinishedTransformation'
    static readonly StartingValidation: 'StartingValidation'
    static readonly FinishedValidationPlugin: 'FinishedValidationPlugin'
    static readonly FinishedValidation: 'FinishedValidation'
    static readonly StartingRendering: 'StartingRendering'
    static readonly FinishedRenderingAST: 'FinishedRenderingAST'
    static readonly FinishedRenderingSyntax: 'FinishedRenderingSyntax'

  }
  export class AMFGraphConfiguration  {
    static empty(): AMFGraphConfiguration

    static predefined(): AMFGraphConfiguration

    createClient(): AMFGraphClient

    payloadValidatorFactory(): ShapePayloadValidatorFactory

    withParsingOptions(parsingOptions: ParsingOptions): AMFGraphConfiguration

    withRenderOptions(renderOptions: RenderOptions): AMFGraphConfiguration

    withErrorHandlerProvider(provider: ErrorHandlerProvider): AMFGraphConfiguration

    withResourceLoader(rl: ResourceLoader): AMFGraphConfiguration

    withResourceLoaders(rl: Array<ResourceLoader>): AMFGraphConfiguration

    withUnitCache(cache: amf.core.client.platform.reference.ClientUnitCache): AMFGraphConfiguration

    withTransformationPipeline(pipeline: TransformationPipeline): AMFGraphConfiguration

    withEventListener(listener: amf.core.client.platform.config.AMFEventListener): AMFGraphConfiguration

    merge(other: AMFGraphConfiguration): AMFGraphConfiguration
  }
  export class AMFGraphClient  {
    constructor(configuration: AMFGraphConfiguration)

    getConfiguration(): AMFGraphConfiguration

    parse(url: string): Promise<AMFResult>

    parse(url: string, mediaType: string): Promise<AMFResult>

    parseContent(content: string): Promise<AMFResult>

    parseContent(content: string, mediaType: string): Promise<AMFResult>

    transform(bu: amf.core.client.platform.model.document.BaseUnit): AMFResult

    transform(bu: amf.core.client.platform.model.document.BaseUnit, pipelineName: string): AMFResult

    render(bu: amf.core.client.platform.model.document.BaseUnit): string

    render(bu: amf.core.client.platform.model.document.BaseUnit, mediaType: string): string

    renderGraphToBuilder<T>(bu: amf.core.client.platform.model.document.BaseUnit, builder: org.yaml.builder.JsOutputBuilder): T

    validate(bu: amf.core.client.platform.model.document.BaseUnit): Promise<AMFValidationReport>

    validate(bu: amf.core.client.platform.model.document.BaseUnit, profileName: ProfileName): Promise<AMFValidationReport>


  }
  export class ValidationMode  {
    static readonly StrictValidationMode: amf.core.client.common.validation.ValidationMode
    static readonly ScalarRelaxedValidationMode: amf.core.client.common.validation.ValidationMode

  }
  export class SeverityLevels  {
    static readonly WARNING: 'Warning'
    static readonly INFO: 'Info'
    static readonly VIOLATION: 'Violation'

    unapply(arg: string): string


  }
  export class ProfileNames  {
    static readonly AMF: ProfileName
    static readonly OAS20: ProfileName
    static readonly OAS30: ProfileName
    static readonly RAML10: ProfileName
    static readonly RAML08: ProfileName
    static readonly ASYNC: ProfileName
    static readonly ASYNC20: ProfileName
    static readonly AML: ProfileName
    static readonly PAYLOAD: ProfileName

  }
  export class ProfileName  {
    profile: string
    messageStyle: amf.core.client.common.validation.MessageStyle
    toString: string

    constructor(profile: string)

    isOas(): boolean

    isRaml(): boolean


  }
  export class MessageStyles  {
    static readonly RAML: amf.core.client.common.validation.MessageStyle
    static readonly OAS: amf.core.client.common.validation.MessageStyle
    static readonly ASYNC: amf.core.client.common.validation.MessageStyle
    static readonly AMF: amf.core.client.common.validation.MessageStyle

  }
  export class PipelineName  {
    static from(targetMediaType: string, pipelineId: string): string
  }
  export class PipelineId  {
    static readonly Default: 'default'
    static readonly Editing: 'editing'
    static readonly Compatibility: 'compatibility'
    static readonly Cache: 'cache'

  }
  export class Content  {
    toString: string
    url: string

    constructor(stream: string, url: string)
    constructor(stream: string, url: string, mime: string)

  }
  export class Position  {
    isZero: boolean
    toString: string
    line: number
    column: number

    constructor(line: number, column: number)

    lt(o: Position): boolean

    min(other: Position): Position

    max(other: Position): Position

    compareTo(o: Position): number


  }
  export class Range  {
    toString: string
    start: Position
    end: Position

    constructor(start: Position, end: Position)

    extent(other: Range): Range

    contains(other: Range): boolean


  }
  export class JsServerHttpResourceLoader extends amf.core.client.platform.resource.BaseHttpResourceLoader  {
    constructor()

    fetch(resource: string): any


  }
  export class JsServerFileResourceLoader implements amf.core.client.platform.resource.BaseFileResourceLoader  {
    constructor()

    fetch(resource: string): Promise<Content>

    accepts(resource: string): boolean

    fetchFile(resource: string): any

    ensureFileAuthority(str: string): string


  }
  export class JsBrowserHttpResourceLoader extends amf.core.client.platform.resource.BaseHttpResourceLoader  {
    constructor()

    fetch(resource: string): any


  }
  export class TransformationStepFactory  {
    static from(step: JsTransformationStep): TransformationStep
  }

  export class AMFEventListenerFactory {
    static from(listener: JsAMFEventListener): amf.core.client.platform.config.AMFEventListener
  }

  export interface JsAMFEventListener {
    notifyEvent(event: amf.core.client.platform.config.AMFEvent): void
  }

  export class ExecutionEnvironment  {
    constructor()

  }
  export class DefaultExecutionEnvironment  {
    apply(): ExecutionEnvironment


  }
  export class SHACLValidator  {
    constructor()

    validate(data: string, dataMediaType: string, shapes: string, shapesMediaType: string): any

    report(data: string, dataMediaType: string, shapes: string, shapesMediaType: string): any


  }
  export class AmlDomainElementEmitter  {
    emitToBuilder<T>(element: amf.core.client.platform.model.domain.DomainElement, emissionStructure: Dialect, eh: ClientErrorHandler, builder: org.yaml.builder.JsOutputBuilder): void


  }
  export class VocabularyReference implements amf.core.client.platform.model.domain.DomainElement  {
    reference: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    alias: amf.core.client.platform.model.StrField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withAlias(alias: string): VocabularyReference

    graph(): amf.core.client.platform.model.domain.Graph

    withReference(reference: string): VocabularyReference

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class UnionNodeMapping implements amf.core.client.platform.model.domain.DomainElement, amf.core.client.platform.model.domain.Linkable  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    typeDiscriminator(): Map<string, string>

    linkCopy(): UnionNodeMapping

    withName(name: string): UnionNodeMapping

    withTypeDiscriminatorName(name: string): UnionNodeMapping

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withObjectRange(range: Array<string>): UnionNodeMapping

    withLinkLabel(label: string): this

    objectRange(): Array<amf.core.client.platform.model.StrField>

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinkTarget(target: undefined): this

    typeDiscriminatorName(): amf.core.client.platform.model.StrField

    withId(id: string): this

    withTypeDiscriminator(typesMapping: Map<string, string>): UnionNodeMapping


  }
  export class SemanticExtension implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    extensionName(): amf.core.client.platform.model.StrField

    extensionMappingDefinition(): amf.core.client.platform.model.StrField

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtensionMappingDefinition(annotationMapping: string): SemanticExtension

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withExtensionName(name: string): SemanticExtension

    withId(id: string): this


  }
  export class PublicNodeMapping implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withName(name: string): PublicNodeMapping

    mappedNode(): amf.core.client.platform.model.StrField

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withMappedNode(mappedNode: string): PublicNodeMapping

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    name(): amf.core.client.platform.model.StrField

    withId(id: string): this


  }
  export class ObjectPropertyTerm extends amf.aml.client.platform.model.domain.PropertyTerm  {
    constructor()

  }
  export class DatatypePropertyTerm extends amf.aml.client.platform.model.domain.PropertyTerm  {
    constructor()

  }
  export class PropertyMapping implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    maximum(): amf.core.client.platform.model.DoubleField

    typeDiscriminator(): Map<string, string>

    classification(): string

    withEnum(values: Array<any>): PropertyMapping

    enum(): Array<amf.core.client.platform.model.AnyField>

    minCount(): amf.core.client.platform.model.IntField

    withMapKeyProperty(key: string): PropertyMapping

    withName(name: string): PropertyMapping

    literalRange(): amf.core.client.platform.model.StrField

    externallyLinkable(): amf.core.client.platform.model.BoolField

    withTypeDiscriminatorName(name: string): PropertyMapping

    sorted(): amf.core.client.platform.model.BoolField

    minimum(): amf.core.client.platform.model.DoubleField

    pattern(): amf.core.client.platform.model.StrField

    graph(): amf.core.client.platform.model.domain.Graph

    withLiteralRange(range: string): PropertyMapping

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withObjectRange(range: Array<string>): PropertyMapping

    objectRange(): Array<amf.core.client.platform.model.StrField>

    withPattern(pattern: string): PropertyMapping

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withMapValueProperty(value: string): PropertyMapping

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withSorted(sorted: boolean): PropertyMapping

    withNodePropertyMapping(propertyId: string): PropertyMapping

    allowMultiple(): amf.core.client.platform.model.BoolField

    withMinimum(min: number): PropertyMapping

    withMaximum(max: number): PropertyMapping

    nodePropertyMapping(): amf.core.client.platform.model.StrField

    typeDiscriminatorName(): amf.core.client.platform.model.StrField

    name(): amf.core.client.platform.model.StrField

    withAllowMultiple(allow: boolean): PropertyMapping

    mapValueProperty(): amf.core.client.platform.model.StrField

    mapKeyProperty(): amf.core.client.platform.model.StrField

    withExternallyLinkable(linkable: boolean): PropertyMapping

    withId(id: string): this

    withTypeDiscriminator(typesMapping: Map<string, string>): PropertyMapping

    withMinCount(minCount: number): PropertyMapping


  }
  export class NodeMapping implements amf.core.client.platform.model.domain.DomainElement, amf.core.client.platform.model.domain.Linkable  {
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    idTemplate: amf.core.client.platform.model.StrField
    linkTarget: undefined | amf.core.client.platform.model.domain.DomainElement
    isLink: boolean
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    mergePolicy: amf.core.client.platform.model.StrField
    nodetypeMapping: amf.core.client.platform.model.StrField
    position: Range
    linkLabel: amf.core.client.platform.model.StrField
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withIdTemplate(idTemplate: string): NodeMapping

    linkCopy(): NodeMapping

    withName(name: string): NodeMapping

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withNodeTypeMapping(nodeType: string): NodeMapping

    withLinkTarget(target: undefined): this

    withPropertiesMapping(props: Array<PropertyMapping>): NodeMapping

    propertiesMapping(): Array<PropertyMapping>

    withMergePolicy(mergePolicy: string): NodeMapping

    withId(id: string): this


  }
  export class External implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    base: amf.core.client.platform.model.StrField
    alias: amf.core.client.platform.model.StrField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withAlias(alias: string): External

    graph(): amf.core.client.platform.model.domain.Graph

    withBase(base: string): External

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class DocumentsModel implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    declarationsPath(): amf.core.client.platform.model.StrField

    withRoot(documentMapping: DocumentMapping): DocumentsModel

    withKeyProperty(keyProperty: boolean): DocumentsModel

    keyProperty(): amf.core.client.platform.model.BoolField

    fragments(): Array<DocumentMapping>

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    root(): DocumentMapping

    selfEncoded(): amf.core.client.platform.model.BoolField

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withFragments(fragments: Array<DocumentMapping>): DocumentsModel

    library(): DocumentMapping

    withLibrary(library: DocumentMapping): DocumentsModel

    withSelfEncoded(selfEncoded: boolean): DocumentsModel

    withDeclarationsPath(declarationsPath: string): DocumentsModel

    withId(id: string): this


  }
  export class DocumentMapping implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withDeclaredNodes(declarations: Array<PublicNodeMapping>): DocumentMapping

    withDocumentName(name: string): DocumentMapping

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    documentName(): amf.core.client.platform.model.StrField

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withEncoded(encodedNode: string): DocumentMapping

    declaredNodes(): Array<PublicNodeMapping>

    withId(id: string): this

    encoded(): amf.core.client.platform.model.StrField


  }
  export class DialectDomainElement implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    setObjectProperty(propertyId: string, value: DialectDomainElement): this

    isAbstract(): amf.core.client.platform.model.BoolField

    withAbstract(isAbstract: boolean): DialectDomainElement

    setObjectCollectionProperty(propertyId: string, value: Array<DialectDomainElement>): this

    setLiteralProperty(propertyId: string, value: Array<any>): this

    localRefName(): string

    graph(): amf.core.client.platform.model.domain.Graph

    setLiteralProperty(propertyId: string, value: boolean): this

    getScalarValueByPropertyUri(propertyId: string): Array<any>

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    getScalarByPropertyUri(propertyId: string): Array<any>

    getTypeUris(): Array<string>

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    getPropertyUris(): Array<string>

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    setLiteralProperty(propertyId: string, value: string): this

    getObjectPropertyUri(propertyId: string): Array<DialectDomainElement>

    withDefinedby(nodeMapping: NodeMapping): DialectDomainElement

    definedBy(): NodeMapping

    withInstanceTypes(types: Array<string>): DialectDomainElement

    includeName(): string

    setLiteralProperty(propertyId: string, value: number): this

    withId(id: string): this


  }
  export class ClassTerm implements amf.core.client.platform.model.domain.DomainElement  {
    displayName: amf.core.client.platform.model.StrField
    name: amf.core.client.platform.model.StrField
    customDomainProperties: Array<DomainExtension>
    description: amf.core.client.platform.model.StrField
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    properties: Array<amf.core.client.platform.model.StrField>
    position: Range
    subClassOf: Array<amf.core.client.platform.model.StrField>
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    withName(name: string): ClassTerm

    withDescription(description: string): ClassTerm

    graph(): amf.core.client.platform.model.domain.Graph

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withProperties(properties: Array<string>): ClassTerm

    withDisplayName(displayName: string): ClassTerm

    withId(id: string): this

    withSubClassOf(superClasses: Array<string>): ClassTerm


  }
  export class AnnotationMapping implements amf.core.client.platform.model.domain.DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: amf.core.client.platform.model.BoolField
    id: string
    position: Range
    extendsNode: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    maximum(): amf.core.client.platform.model.DoubleField

    typeDiscriminator(): Map<string, string>

    withEnum(values: Array<any>): AnnotationMapping

    enum(): Array<amf.core.client.platform.model.AnyField>

    minCount(): amf.core.client.platform.model.IntField

    withName(name: string): AnnotationMapping

    literalRange(): amf.core.client.platform.model.StrField

    externallyLinkable(): amf.core.client.platform.model.BoolField

    withTypeDiscriminatorName(name: string): AnnotationMapping

    sorted(): amf.core.client.platform.model.BoolField

    withDomain(domainIri: string): AnnotationMapping

    minimum(): amf.core.client.platform.model.DoubleField

    pattern(): amf.core.client.platform.model.StrField

    graph(): amf.core.client.platform.model.domain.Graph

    withLiteralRange(range: string): AnnotationMapping

    withIsExternalLink(isExternalLink: boolean): amf.core.client.platform.model.domain.DomainElement

    withObjectRange(range: Array<string>): AnnotationMapping

    objectRange(): Array<amf.core.client.platform.model.StrField>

    domain(): amf.core.client.platform.model.StrField

    withPattern(pattern: string): AnnotationMapping

    withExtendsNode(extension: Array<amf.core.client.platform.model.domain.ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withSorted(sorted: boolean): AnnotationMapping

    withNodePropertyMapping(propertyId: string): AnnotationMapping

    allowMultiple(): amf.core.client.platform.model.BoolField

    withMinimum(min: number): AnnotationMapping

    withMaximum(max: number): AnnotationMapping

    nodePropertyMapping(): amf.core.client.platform.model.StrField

    typeDiscriminatorName(): amf.core.client.platform.model.StrField

    name(): amf.core.client.platform.model.StrField

    withAllowMultiple(allow: boolean): AnnotationMapping

    withExternallyLinkable(linkable: boolean): this

    withId(id: string): this

    withTypeDiscriminator(typesMapping: Map<string, string>): AnnotationMapping

    withMinCount(minCount: number): AnnotationMapping


  }
  export class Vocabulary implements amf.core.client.platform.model.document.BaseUnit, amf.core.client.platform.model.document.DeclaresModel  {
    name: amf.core.client.platform.model.StrField
    location: string
    description: amf.core.client.platform.model.StrField
    usage: amf.core.client.platform.model.StrField
    base: amf.core.client.platform.model.StrField
    sourceVendor: undefined | Vendor
    id: string
    raw: undefined | string
    modelVersion: amf.core.client.platform.model.StrField
    declares: Array<amf.core.client.platform.model.domain.DomainElement>
    externals: Array<External>
    imports: Array<VocabularyReference>

    constructor()

    objectPropertyTerms(): Array<ObjectPropertyTerm>

    findByType(typeId: string): Array<amf.core.client.platform.model.domain.DomainElement>

    cloneUnit(): amf.core.client.platform.model.document.BaseUnit

    withExternals(externals: Array<External>): Vocabulary

    withName(name: string): Vocabulary

    withReferences(references: Array<amf.core.client.platform.model.document.BaseUnit>): this

    withDeclaredElement(declared: amf.core.client.platform.model.domain.DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    datatypePropertyTerms(): Array<DatatypePropertyTerm>

    withBase(base: string): Vocabulary

    findById(id: string): undefined | amf.core.client.platform.model.domain.DomainElement

    classTerms(): Array<ClassTerm>

    withLocation(location: string): this

    withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): amf.core.client.platform.model.document.BaseUnit

    withDeclares(declares: Array<amf.core.client.platform.model.domain.DomainElement>): this

    references(): Array<amf.core.client.platform.model.document.BaseUnit>

    withImports(vocabularies: Array<VocabularyReference>): Vocabulary

    withId(id: string): this


  }
  export class DialectLibrary implements amf.core.client.platform.model.document.BaseUnit, amf.core.client.platform.model.document.DeclaresModel  {
    location: string
    usage: amf.core.client.platform.model.StrField
    sourceVendor: undefined | Vendor
    id: string
    raw: undefined | string
    modelVersion: amf.core.client.platform.model.StrField
    declares: Array<amf.core.client.platform.model.domain.DomainElement>
    externals: Array<External>

    constructor()

    findByType(typeId: string): Array<amf.core.client.platform.model.domain.DomainElement>

    cloneUnit(): amf.core.client.platform.model.document.BaseUnit

    withExternals(externals: Array<External>): DialectLibrary

    withReferences(references: Array<amf.core.client.platform.model.document.BaseUnit>): this

    withDeclaredElement(declared: amf.core.client.platform.model.domain.DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | amf.core.client.platform.model.domain.DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): amf.core.client.platform.model.document.BaseUnit

    withNodeMappings(nodeMappings: Array<NodeMapping>): DialectLibrary

    withDeclares(declares: Array<amf.core.client.platform.model.domain.DomainElement>): this

    nodeMappings(): Array<NodeMapping>

    references(): Array<amf.core.client.platform.model.document.BaseUnit>

    withId(id: string): this


  }
  export class DialectInstancePatch implements amf.core.client.platform.model.document.BaseUnit, amf.core.client.platform.model.document.EncodesModel, amf.core.client.platform.model.document.DeclaresModel  {
    location: string
    usage: amf.core.client.platform.model.StrField
    sourceVendor: undefined | Vendor
    id: string
    raw: undefined | string
    modelVersion: amf.core.client.platform.model.StrField
    encodes: amf.core.client.platform.model.domain.DomainElement
    declares: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    findByType(typeId: string): Array<amf.core.client.platform.model.domain.DomainElement>

    cloneUnit(): amf.core.client.platform.model.document.BaseUnit

    withReferences(references: Array<amf.core.client.platform.model.document.BaseUnit>): this

    withDeclaredElement(declared: amf.core.client.platform.model.domain.DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | amf.core.client.platform.model.domain.DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): amf.core.client.platform.model.document.BaseUnit

    withEncodes(encoded: amf.core.client.platform.model.domain.DomainElement): this

    withDeclares(declares: Array<amf.core.client.platform.model.domain.DomainElement>): this

    references(): Array<amf.core.client.platform.model.document.BaseUnit>

    withId(id: string): this


  }
  export class DialectInstanceLibrary implements amf.core.client.platform.model.document.BaseUnit, amf.core.client.platform.model.document.DeclaresModel  {
    location: string
    usage: amf.core.client.platform.model.StrField
    sourceVendor: undefined | Vendor
    id: string
    raw: undefined | string
    modelVersion: amf.core.client.platform.model.StrField
    declares: Array<amf.core.client.platform.model.domain.DomainElement>

    constructor()

    findByType(typeId: string): Array<amf.core.client.platform.model.domain.DomainElement>

    cloneUnit(): amf.core.client.platform.model.document.BaseUnit

    withReferences(references: Array<amf.core.client.platform.model.document.BaseUnit>): this

    withDeclaredElement(declared: amf.core.client.platform.model.domain.DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | amf.core.client.platform.model.domain.DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): amf.core.client.platform.model.document.BaseUnit

    withDeclares(declares: Array<amf.core.client.platform.model.domain.DomainElement>): this

    references(): Array<amf.core.client.platform.model.document.BaseUnit>

    withId(id: string): this


  }
  export class DialectInstanceFragment implements amf.core.client.platform.model.document.BaseUnit, amf.core.client.platform.model.document.EncodesModel  {
    location: string
    usage: amf.core.client.platform.model.StrField
    sourceVendor: undefined | Vendor
    id: string
    raw: undefined | string
    modelVersion: amf.core.client.platform.model.StrField
    encodes: DialectDomainElement

    constructor()

    findByType(typeId: string): Array<amf.core.client.platform.model.domain.DomainElement>

    cloneUnit(): amf.core.client.platform.model.document.BaseUnit

    withReferences(references: Array<amf.core.client.platform.model.document.BaseUnit>): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | amf.core.client.platform.model.domain.DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): amf.core.client.platform.model.document.BaseUnit

    withEncodes(encoded: DialectDomainElement): DialectInstanceFragment

    withEncodes(encoded: amf.core.client.platform.model.domain.DomainElement): this

    references(): Array<amf.core.client.platform.model.document.BaseUnit>

    withId(id: string): this


  }
  export class DialectInstance implements amf.core.client.platform.model.document.BaseUnit, amf.core.client.platform.model.document.EncodesModel, amf.core.client.platform.model.document.DeclaresModel  {
    location: string
    usage: amf.core.client.platform.model.StrField
    sourceVendor: undefined | Vendor
    id: string
    raw: undefined | string
    modelVersion: amf.core.client.platform.model.StrField
    encodes: DialectDomainElement
    declares: Array<amf.core.client.platform.model.domain.DomainElement>
    externals: Array<External>

    constructor()

    findByType(typeId: string): Array<amf.core.client.platform.model.domain.DomainElement>

    cloneUnit(): amf.core.client.platform.model.document.BaseUnit

    withExternals(externals: Array<External>): DialectInstance

    withReferences(references: Array<amf.core.client.platform.model.document.BaseUnit>): this

    withDeclaredElement(declared: amf.core.client.platform.model.domain.DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | amf.core.client.platform.model.domain.DomainElement

    withLocation(location: string): this

    withGraphDependencies(ids: Array<string>): DialectInstance

    withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): amf.core.client.platform.model.document.BaseUnit

    graphDependencies(): Array<amf.core.client.platform.model.StrField>

    withDefinedBy(dialectId: string): DialectInstance

    withEncodes(encoded: DialectDomainElement): DialectInstance

    withEncodes(encoded: amf.core.client.platform.model.domain.DomainElement): this

    definedBy(): amf.core.client.platform.model.StrField

    withDeclares(declares: Array<amf.core.client.platform.model.domain.DomainElement>): this

    references(): Array<amf.core.client.platform.model.document.BaseUnit>

    withId(id: string): this


  }
  export class DialectFragment implements amf.core.client.platform.model.document.BaseUnit, amf.core.client.platform.model.document.EncodesModel  {
    location: string
    usage: amf.core.client.platform.model.StrField
    sourceVendor: undefined | Vendor
    id: string
    raw: undefined | string
    modelVersion: amf.core.client.platform.model.StrField
    encodes: NodeMapping
    externals: Array<External>

    constructor()

    findByType(typeId: string): Array<amf.core.client.platform.model.domain.DomainElement>

    cloneUnit(): amf.core.client.platform.model.document.BaseUnit

    withExternals(externals: Array<External>): DialectFragment

    withReferences(references: Array<amf.core.client.platform.model.document.BaseUnit>): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | amf.core.client.platform.model.domain.DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): amf.core.client.platform.model.document.BaseUnit

    withEncodes(encoded: amf.core.client.platform.model.domain.DomainElement): this

    withEncodes(nodeMapping: NodeMapping): DialectFragment

    references(): Array<amf.core.client.platform.model.document.BaseUnit>

    withId(id: string): this


  }
  export class Dialect implements amf.core.client.platform.model.document.BaseUnit, amf.core.client.platform.model.document.EncodesModel, amf.core.client.platform.model.document.DeclaresModel  {
    name: amf.core.client.platform.model.StrField
    location: string
    usage: amf.core.client.platform.model.StrField
    nameAndVersion: string
    allHeaders: Array<string>
    sourceVendor: undefined | Vendor
    version: amf.core.client.platform.model.StrField
    id: string
    raw: undefined | string
    fragmentHeaders: Array<string>
    libraryHeader: undefined | string
    header: string
    modelVersion: amf.core.client.platform.model.StrField
    encodes: amf.core.client.platform.model.domain.DomainElement
    declares: Array<amf.core.client.platform.model.domain.DomainElement>
    externals: Array<External>

    constructor()

    findByType(typeId: string): Array<amf.core.client.platform.model.domain.DomainElement>

    cloneUnit(): amf.core.client.platform.model.document.BaseUnit

    withExternals(externals: Array<External>): Dialect

    withName(name: string): Dialect

    withReferences(references: Array<amf.core.client.platform.model.document.BaseUnit>): this

    withDeclaredElement(declared: amf.core.client.platform.model.domain.DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    isLibraryHeader(header: string): boolean

    findById(id: string): undefined | amf.core.client.platform.model.domain.DomainElement

    withLocation(location: string): this

    extensions(): Array<SemanticExtension>

    withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): amf.core.client.platform.model.document.BaseUnit

    withDocuments(documentsMapping: DocumentsModel): Dialect

    withVersion(version: string): Dialect

    withEncodes(encoded: amf.core.client.platform.model.domain.DomainElement): this

    documents(): DocumentsModel

    withDeclares(declares: Array<amf.core.client.platform.model.domain.DomainElement>): this

    references(): Array<amf.core.client.platform.model.document.BaseUnit>

    isFragmentHeader(header: string): boolean

    withId(id: string): this


  }
  export class AMLConfiguration extends amf.aml.client.platform.BaseAMLConfiguration {
    static predefined(): AMLConfiguration

    createClient(): AMLClient

    withParsingOptions(parsingOptions: ParsingOptions): AMLConfiguration

    withRenderOptions(renderOptions: RenderOptions): AMLConfiguration

    withErrorHandlerProvider(provider: ErrorHandlerProvider): AMLConfiguration

    withResourceLoader(rl: ResourceLoader): AMLConfiguration

    withResourceLoaders(rl: Array<ResourceLoader>): AMLConfiguration

    withUnitCache(cache: amf.core.client.platform.reference.ClientUnitCache): AMLConfiguration

    withTransformationPipeline(pipeline: TransformationPipeline): AMLConfiguration

    withEventListener(listener: amf.core.client.platform.config.AMFEventListener): AMLConfiguration

    withDialect(dialect: Dialect): AMLConfiguration

    merge(other: AMLConfiguration): AMLConfiguration

    withDialect(path: string): Promise<AMLConfiguration>

    forInstance(url: string): Promise<AMLConfiguration>
  }
  export class AMLClient extends amf.aml.client.platform.BaseAMLClient  {
    constructor(configuration: AMLConfiguration)

    getConfiguration(): AMLConfiguration


  }

  class ErrorHandlerProvider {
    errorHandler(): ClientErrorHandler
  }

  export interface TransformationStep            {
    transform(model: amf.core.client.platform.model.document.BaseUnit, errorHandler: ClientErrorHandler): amf.core.client.platform.model.document.BaseUnit


  }
  export interface TransformationPipeline            {
    readonly name: string
    steps: Array<TransformationStep>

  }
  export interface JsTransformationStep            {
    transform(model: amf.core.client.platform.model.document.BaseUnit, errorHandler: ClientErrorHandler): amf.core.client.platform.model.document.BaseUnit
  }

  export class RamlShapeRenderer            {
    static toRamlDatatype(element: AnyShape, config: AMFGraphConfiguration): string


  }
  export class JsonSchemaShapeRenderer            {
    static toJsonSchema(element: AnyShape, config: AMFGraphConfiguration): string

    static buildJsonSchema(element: AnyShape, config: AMFGraphConfiguration): string
  }

  export class ShapesConfiguration            {
    static predefined(): AMLConfiguration
  }

  export class AMLVocabularyResult extends AMFResult          {
    vocabulary: Vocabulary

  }
  export class AMLDialectResult extends AMFResult          {
    dialect: Dialect

  }
  export class AMLDialectInstanceResult extends AMFResult          {
    dialectInstance: DialectInstance
  }

  export class AMFLibraryResult extends AMFResult          {
    library: Module

  }
  export class AMFDocumentResult extends AMFResult          {
    document: Document
  }

  export class AMFResult          {
    conforms: boolean
    results: Array<ValidationResult>
    baseUnit: amf.core.client.platform.model.document.BaseUnit

  }


  export class ErrorHandler {
    static handler(obj: JsErrorHandler): ClientErrorHandler
    static provider(obj: JsErrorHandler): ErrorHandlerProvider
  }

  export interface ClientErrorHandler            {
    getResults: Array<ValidationResult>

    report(result: ValidationResult): void


  }
  export interface JsErrorHandler            {
    report(result: ValidationResult): void

    getResults(): Array<ValidationResult>
  }

  export interface ClientResourceLoader            {
    fetch(resource: string): Promise<Content>

    accepts(resource: string): boolean
  }

  export interface ResourceLoader            {
    fetch(resource: string): Promise<Content>

    accepts(resource: string): boolean
  }

  export class ValidationProfile            {
    profileName(): ProfileName

    baseProfile(): undefined | ProfileName
  }

  export class AMFConfiguration extends amf.aml.client.platform.BaseAMLConfiguration          {
    createClient(): AMFClient

    withParsingOptions(parsingOptions: ParsingOptions): AMFConfiguration

    withResourceLoader(rl: ResourceLoader): AMFConfiguration

    withResourceLoaders(rl: Array<ResourceLoader>): AMFConfiguration

    withUnitCache(cache: amf.core.client.platform.reference.ClientUnitCache): AMFConfiguration

    withTransformationPipeline(pipeline: TransformationPipeline): AMFConfiguration

    withRenderOptions(renderOptions: RenderOptions): AMFConfiguration

    withErrorHandlerProvider(provider: ErrorHandlerProvider): AMFConfiguration

    withEventListener(listener: amf.core.client.platform.config.AMFEventListener): AMFConfiguration

    merge(other: AMFConfiguration): AMFConfiguration

    withDialect(dialect: Dialect): AMFConfiguration

    withDialect(path: string): Promise<AMFConfiguration>
  }

  export class ShapePayloadValidatorFactory {
    createFor(shape: amf.core.client.platform.model.domain.Shape, mediaType: string, mode: amf.core.client.common.validation.ValidationMode): AMFShapePayloadValidator
    createFor(shape: amf.core.client.platform.model.domain.Shape, fragment: PayloadFragment): AMFShapePayloadValidator
  }

  export class AMFShapePayloadValidator            {
    validate(payload: string): Promise<AMFValidationReport>

    validate(payloadFragment: PayloadFragment): Promise<AMFValidationReport>

    syncValidate(payload: string): AMFValidationReport
  }
}
