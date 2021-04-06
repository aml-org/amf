declare module 'amf-client-js' {
  namespace reference {
    export interface ReferenceResolver    {
      fetch(url: string): Promise<client.remote.CachedReference>


    }
    export interface ClientReferenceResolver    {
      fetch(url: string): Promise<client.remote.CachedReference>


    }
  }
  namespace plugins {
    export interface ValidationMode    {
    }
    export class StrictValidationMode extends ValidationMode    {
    }
    export class ScalarRelaxedValidationMode extends ValidationMode    {
    }
    export interface ClientAMFPlugin    {
      readonly ID: string

      dependencies(): Array<ClientAMFPlugin>

      init(): Promise<ClientAMFPlugin>


    }
    export interface ClientAMFPayloadValidationPlugin extends ClientAMFPlugin    {
      readonly payloadMediaType: Array<string>

      canValidate(shape: model.domain.Shape, env: client.environment.Environment): boolean

      validator(s: model.domain.Shape, env: client.environment.Environment, validationMode: ValidationMode): ClientPayloadValidator


    }
    export interface ClientPayloadValidator    {
      readonly shape: model.domain.Shape
      readonly defaultSeverity: string
      readonly validationMode: ValidationMode
      readonly env: client.environment.Environment

      validate(payload: string, mediaType: string): Promise<client.validate.ValidationReport>

      validate(payloadFragment: model.domain.PayloadFragment): Promise<client.validate.ValidationReport>

      syncValidate(payload: string, mediaType: string): client.validate.ValidationReport

      isValid(payload: string, mediaType: string): Promise<boolean>


    }
  }
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
          export interface Stats          {
            dev: number
            ino: number
            mode: number
            nlink: number
            uid: number
            gid: number
            rdev: number
            size: number
            blksize: number
            blocks: number
            atime: undefined
            atimeMs: number
            mtime: undefined
            mtimeMs: number
            ctime: undefined
            ctimeMs: number
            birthtime: undefined
            birthtimeMs: number

            isFile(): boolean

            isDirectory(): boolean

            isBlockDevice(): boolean

            isCharacterDevice(): boolean

            isSymbolicLink(): boolean

            isFIFO(): boolean

            isSocket(): boolean


          }
          export class JsPath          {
            static readonly sep: string

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
      namespace writer {
        export interface ClientWriter        {
          append(s: string): this

          string(): string

          flush(): this

          close(): this


        }
      }
    }
  }
  namespace parser {
    export class ParsingOptions    {
      withoutAmfJsonLdSerialization: ParsingOptions
      withAmfJsonLdSerialization: ParsingOptions
      isAmfJsonLdSerilization: boolean

      constructor()

      withBaseUnitUrl(baseUnit: string): ParsingOptions

      withoutBaseUnitUrl(): ParsingOptions


    }
  }
  namespace validate {
    export class Validator    {
      validate(model: model.document.BaseUnit, profileName: ProfileName, messageStyle: amf.MessageStyle, env: client.environment.Environment, resolved: boolean): Promise<client.validate.ValidationReport>

      loadValidationProfile(url: string, env: client.environment.Environment): Promise<ProfileName>

      emitShapesGraph(profileName: ProfileName): string


    }
    export class PayloadValidator    {
      isValid(mediaType: string, payload: string): Promise<boolean>

      validate(mediaType: string, payload: string): Promise<client.validate.ValidationReport>

      validate(payloadFragment: model.domain.PayloadFragment): Promise<client.validate.ValidationReport>

      syncValidate(mediaType: string, payload: string): client.validate.ValidationReport


    }
  }
  namespace model {
    export interface ValueField<T>    {
      readonly option: undefined | T
      isNull: boolean
      nonNull: boolean
      toString: string

      value(): T

      is(other: T): boolean

      is(accepts: undefined): boolean

      remove(): void


    }
    export class StrField implements ValueField<string>    {
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
    export class IntField    {
      readonly option: undefined | number

      annotations(): Annotations

      value(): number

      remove(): void


    }
    export class FloatField    {
      readonly option: undefined | number

      annotations(): Annotations

      value(): number

      remove(): void


    }
    export class DoubleField    {
      readonly option: undefined | number

      annotations(): Annotations

      value(): number

      remove(): void


    }
    export class BoolField    {
      readonly option: undefined | boolean

      annotations(): Annotations

      value(): boolean

      remove(): void


    }
    export class AnyField implements ValueField<any>    {
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
    export class Annotations    {
      isLocal: boolean
      t: boolean
      resolvedLink: undefined | string
      resolvedLinkTarget: undefined | string
      inheritanceProvenance: undefined | string
      inlinedElement: boolean
      autoGeneratedName: boolean

      constructor()

      lexical(): core.parser.Range

      custom(): Array<domain.DomainExtension>

      fragmentName(): undefined | string

      location(): undefined | string

      isTrackedBy(trackId: string): boolean


    }
    export interface Annotable    {
      annotations(): Annotations


    }
    namespace document {
      export class Module implements BaseUnit, DeclaresModel, domain.CustomizableElement      {
        customDomainProperties: Array<domain.DomainExtension>
        location: string
        usage: StrField
        sourceVendor: undefined | core.Vendor
        id: string
        raw: undefined | string
        modelVersion: StrField
        declares: Array<domain.DomainElement>

        constructor()

        findByType(typeId: string): Array<domain.DomainElement>

        cloneUnit(): BaseUnit

        withReferences(references: Array<BaseUnit>): this

        withDeclaredElement(declared: domain.DomainElement): this

        withRaw(raw: string): this

        withUsage(usage: string): this

        findById(id: string): undefined | domain.DomainElement

        withLocation(location: string): this

        withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): BaseUnit

        withCustomDomainProperties(extensions: Array<domain.DomainExtension>): this

        withDeclares(declares: Array<domain.DomainElement>): this

        references(): Array<BaseUnit>

        withId(id: string): this


      }
      export class Fragment implements BaseUnit, EncodesModel      {
        location: string
        usage: StrField
        sourceVendor: undefined | core.Vendor
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
      export class ExternalFragment extends Fragment      {
        constructor()

      }
      export interface EncodesModel      {
        encodes: domain.DomainElement

        withEncodes(encoded: domain.DomainElement): this


      }
      export class Document implements BaseUnit, EncodesModel, DeclaresModel      {
        location: string
        usage: StrField
        sourceVendor: undefined | core.Vendor
        id: string
        raw: undefined | string
        modelVersion: StrField
        encodes: domain.DomainElement
        declares: Array<domain.DomainElement>

        constructor()
        constructor(encoding: domain.DomainElement)

        findByType(typeId: string): Array<domain.DomainElement>

        cloneUnit(): BaseUnit

        withReferences(references: Array<BaseUnit>): this

        withDeclaredElement(declared: domain.DomainElement): this

        withRaw(raw: string): this

        withUsage(usage: string): this

        findById(id: string): undefined | domain.DomainElement

        withLocation(location: string): this

        withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): BaseUnit

        withEncodes(encoded: domain.DomainElement): this

        withDeclares(declares: Array<domain.DomainElement>): this

        references(): Array<BaseUnit>

        withId(id: string): this


      }
      export interface DeclaresModel      {
        declares: Array<domain.DomainElement>

        withDeclaredElement(declared: domain.DomainElement): this

        withDeclares(declares: Array<domain.DomainElement>): this


      }
      export interface BaseUnit      {
        id: string
        raw: undefined | string
        location: string
        usage: StrField
        modelVersion: StrField
        sourceVendor: undefined | core.Vendor

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
      export class Vocabulary implements BaseUnit, DeclaresModel      {
        name: StrField
        location: string
        description: StrField
        usage: StrField
        base: StrField
        sourceVendor: undefined | core.Vendor
        id: string
        raw: undefined | string
        modelVersion: StrField
        declares: Array<domain.DomainElement>
        externals: Array<domain.External>
        imports: Array<domain.VocabularyReference>

        constructor()

        objectPropertyTerms(): Array<domain.ObjectPropertyTerm>

        findByType(typeId: string): Array<domain.DomainElement>

        cloneUnit(): BaseUnit

        withExternals(externals: Array<domain.External>): Vocabulary

        withName(name: string): Vocabulary

        withReferences(references: Array<BaseUnit>): this

        withDeclaredElement(declared: domain.DomainElement): this

        withRaw(raw: string): this

        withUsage(usage: string): this

        datatypePropertyTerms(): Array<domain.DatatypePropertyTerm>

        withBase(base: string): Vocabulary

        findById(id: string): undefined | domain.DomainElement

        classTerms(): Array<domain.ClassTerm>

        withLocation(location: string): this

        withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): BaseUnit

        withDeclares(declares: Array<domain.DomainElement>): this

        references(): Array<BaseUnit>

        withImports(vocabularies: Array<domain.VocabularyReference>): Vocabulary

        withId(id: string): this


      }
      export class DialectLibrary implements BaseUnit, DeclaresModel      {
        location: string
        usage: StrField
        sourceVendor: undefined | core.Vendor
        id: string
        raw: undefined | string
        modelVersion: StrField
        declares: Array<domain.DomainElement>
        externals: Array<domain.External>

        constructor()

        findByType(typeId: string): Array<domain.DomainElement>

        cloneUnit(): BaseUnit

        withExternals(externals: Array<domain.External>): DialectLibrary

        withReferences(references: Array<BaseUnit>): this

        withDeclaredElement(declared: domain.DomainElement): this

        withRaw(raw: string): this

        withUsage(usage: string): this

        findById(id: string): undefined | domain.DomainElement

        withLocation(location: string): this

        withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): BaseUnit

        withNodeMappings(nodeMappings: Array<domain.NodeMapping>): DialectLibrary

        withDeclares(declares: Array<domain.DomainElement>): this

        nodeMappings(): Array<domain.NodeMapping>

        references(): Array<BaseUnit>

        withId(id: string): this


      }
      export class DialectInstancePatch implements BaseUnit, EncodesModel, DeclaresModel      {
        location: string
        usage: StrField
        sourceVendor: undefined | core.Vendor
        id: string
        raw: undefined | string
        modelVersion: StrField
        encodes: domain.DomainElement
        declares: Array<domain.DomainElement>

        constructor()

        findByType(typeId: string): Array<domain.DomainElement>

        cloneUnit(): BaseUnit

        withReferences(references: Array<BaseUnit>): this

        withDeclaredElement(declared: domain.DomainElement): this

        withRaw(raw: string): this

        withUsage(usage: string): this

        findById(id: string): undefined | domain.DomainElement

        withLocation(location: string): this

        withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): BaseUnit

        withEncodes(encoded: domain.DomainElement): this

        withDeclares(declares: Array<domain.DomainElement>): this

        references(): Array<BaseUnit>

        withId(id: string): this


      }
      export class DialectInstanceLibrary implements BaseUnit, DeclaresModel      {
        location: string
        usage: StrField
        sourceVendor: undefined | core.Vendor
        id: string
        raw: undefined | string
        modelVersion: StrField
        declares: Array<domain.DomainElement>

        constructor()

        findByType(typeId: string): Array<domain.DomainElement>

        cloneUnit(): BaseUnit

        withReferences(references: Array<BaseUnit>): this

        withDeclaredElement(declared: domain.DomainElement): this

        withRaw(raw: string): this

        withUsage(usage: string): this

        findById(id: string): undefined | domain.DomainElement

        withLocation(location: string): this

        withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): BaseUnit

        withDeclares(declares: Array<domain.DomainElement>): this

        references(): Array<BaseUnit>

        withId(id: string): this


      }
      export class DialectInstanceFragment implements BaseUnit, EncodesModel      {
        location: string
        usage: StrField
        sourceVendor: undefined | core.Vendor
        id: string
        raw: undefined | string
        modelVersion: StrField
        encodes: domain.DialectDomainElement

        constructor()

        findByType(typeId: string): Array<domain.DomainElement>

        cloneUnit(): BaseUnit

        withReferences(references: Array<BaseUnit>): this

        withRaw(raw: string): this

        withUsage(usage: string): this

        findById(id: string): undefined | domain.DomainElement

        withLocation(location: string): this

        withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): BaseUnit

        withEncodes(encoded: domain.DialectDomainElement): DialectInstanceFragment

        withEncodes(encoded: domain.DomainElement): this

        references(): Array<BaseUnit>

        withId(id: string): this


      }
      export class DialectInstance implements BaseUnit, EncodesModel, DeclaresModel      {
        location: string
        usage: StrField
        sourceVendor: undefined | core.Vendor
        id: string
        raw: undefined | string
        modelVersion: StrField
        encodes: domain.DialectDomainElement
        declares: Array<domain.DomainElement>
        externals: Array<domain.External>

        constructor()

        findByType(typeId: string): Array<domain.DomainElement>

        cloneUnit(): BaseUnit

        withExternals(externals: Array<domain.External>): DialectInstance

        withReferences(references: Array<BaseUnit>): this

        withDeclaredElement(declared: domain.DomainElement): this

        withRaw(raw: string): this

        withUsage(usage: string): this

        findById(id: string): undefined | domain.DomainElement

        withLocation(location: string): this

        withGraphDependencies(ids: Array<string>): DialectInstance

        withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): BaseUnit

        graphDependencies(): Array<StrField>

        withDefinedBy(dialectId: string): DialectInstance

        withEncodes(encoded: domain.DialectDomainElement): DialectInstance

        withEncodes(encoded: domain.DomainElement): this

        definedBy(): StrField

        withDeclares(declares: Array<domain.DomainElement>): this

        references(): Array<BaseUnit>

        withId(id: string): this


      }
      export class DialectFragment implements BaseUnit, EncodesModel      {
        location: string
        usage: StrField
        sourceVendor: undefined | core.Vendor
        id: string
        raw: undefined | string
        modelVersion: StrField
        encodes: domain.NodeMapping
        externals: Array<domain.External>

        constructor()

        findByType(typeId: string): Array<domain.DomainElement>

        cloneUnit(): BaseUnit

        withExternals(externals: Array<domain.External>): DialectFragment

        withReferences(references: Array<BaseUnit>): this

        withRaw(raw: string): this

        withUsage(usage: string): this

        findById(id: string): undefined | domain.DomainElement

        withLocation(location: string): this

        withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): BaseUnit

        withEncodes(encoded: domain.DomainElement): this

        withEncodes(nodeMapping: domain.NodeMapping): DialectFragment

        references(): Array<BaseUnit>

        withId(id: string): this


      }
      export class Dialect implements BaseUnit, EncodesModel, DeclaresModel      {
        name: StrField
        location: string
        usage: StrField
        nameAndVersion: string
        allHeaders: Array<string>
        sourceVendor: undefined | core.Vendor
        version: StrField
        id: string
        raw: undefined | string
        fragmentHeaders: Array<string>
        libraryHeader: undefined | string
        header: string
        modelVersion: StrField
        encodes: domain.DomainElement
        declares: Array<domain.DomainElement>
        externals: Array<domain.External>

        constructor()

        findByType(typeId: string): Array<domain.DomainElement>

        cloneUnit(): BaseUnit

        withExternals(externals: Array<domain.External>): Dialect

        withName(name: string): Dialect

        withReferences(references: Array<BaseUnit>): this

        withDeclaredElement(declared: domain.DomainElement): this

        withRaw(raw: string): this

        withUsage(usage: string): this

        isLibraryHeader(header: string): boolean

        findById(id: string): undefined | domain.DomainElement

        withLocation(location: string): this

        withReferenceAlias(alias: string, fullUrl: string, relativeUrl: string): BaseUnit

        withDocuments(documentsMapping: domain.DocumentsModel): Dialect

        withVersion(version: string): Dialect

        withEncodes(encoded: domain.DomainElement): this

        documents(): domain.DocumentsModel

        withDeclares(declares: Array<domain.DomainElement>): this

        references(): Array<BaseUnit>

        isFragmentHeader(header: string): boolean

        withId(id: string): this


      }
    }
    namespace domain {
      export class XMLSerializer implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        prefix: StrField
        wrapped: BoolField
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        attribute: BoolField
        extendsNode: Array<DomainElement>
        namespace: StrField

        constructor()

        withName(name: string): this

        withWrapped(wrapped: boolean): this

        withPrefix(prefix: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withAttribute(attribute: boolean): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withNamespace(namespace: string): this

        withId(id: string): this


      }
      export class WebSocketsChannelBinding implements ChannelBinding      {
        method: StrField
        customDomainProperties: Array<DomainExtension>
        query: Shape
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        headers: Shape
        type: StrField
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        linkCopy(): WebSocketsChannelBinding

        withMethod(method: string): this

        withHeaders(headers: Shape): this

        graph(): Graph

        withBindingVersion(bindingVersion: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withType(type: string): this

        withId(id: string): this

        withQuery(query: Shape): this


      }
      export class WebApi extends Api<WebApi>      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        endPoints: Array<EndPoint>
        provider: Organization
        security: Array<SecurityRequirement>
        identifier: StrField
        description: StrField
        documentations: Array<CreativeWork>
        servers: Array<Server>
        schemes: Array<StrField>
        license: License
        isExternalLink: BoolField
        sourceVendor: undefined | core.Vendor
        termsOfService: StrField
        version: StrField
        id: string
        contentType: Array<StrField>
        accepts: Array<StrField>
        position: core.parser.Range
        extendsNode: Array<DomainElement>

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

        graph(): Graph

        withEndPoints(endPoints: Array<EndPoint>): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withTermsOfService(terms: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

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
      export class UnionShape extends AnyShape      {
        anyOf: Array<Shape>

        constructor()

        withAnyOf(anyOf: Array<Shape>): UnionShape


      }
      export class TupleShape extends DataArrangeShape      {
        items: Array<Shape>
        closedItems: BoolField
        additionalItemsSchema: Shape

        constructor()

        withItems(items: Array<Shape>): this

        withClosedItems(closedItems: boolean): this

        linkCopy(): TupleShape


      }
      export class Trait extends AbstractDeclaration      {
        linkTarget: undefined | DomainElement

        constructor()

        linkCopy(): Trait

        asOperation<T>(unit: T, profile: ProfileName): Operation


      }
      export class TemplatedLink implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        server: Server
        description: StrField
        mapping: Array<IriTemplateMapping>
        operationId: StrField
        isExternalLink: BoolField
        operationRef: StrField
        id: string
        requestBody: StrField
        template: StrField
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withRequestBody(requestBody: string): this

        withServer(server: Server): this

        withName(name: string): this

        withMapping(mapping: Array<IriTemplateMapping>): this

        withDescription(description: string): this

        graph(): Graph

        withTemplate(template: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withOperationId(operationId: string): this

        withId(id: string): this

        withOperationRef(operationRef: string): this


      }
      export class Tag implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        description: StrField
        documentation: CreativeWork
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withName(name: string): this

        withDescription(description: string): this

        withVariables(documentation: CreativeWork): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this


      }
      export class Settings implements DomainElement      {
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        additionalProperties: DataNode
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withAdditionalProperties(properties: DataNode): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this


      }
      export class OAuth1Settings extends Settings      {
        requestTokenUri: StrField
        authorizationUri: StrField
        tokenCredentialsUri: StrField
        signatures: Array<StrField>

        constructor()

        withRequestTokenUri(requestTokenUri: string): this

        withAuthorizationUri(authorizationUri: string): this

        withTokenCredentialsUri(tokenCredentialsUri: string): this

        withSignatures(signatures: Array<string>): this


      }
      export class OAuth2Settings extends Settings      {
        flows: Array<OAuth2Flow>
        authorizationGrants: Array<StrField>

        constructor()

        withFlows(flows: Array<OAuth2Flow>): this

        withAuthorizationGrants(grants: Array<string>): this


      }
      export class ApiKeySettings extends Settings      {
        name: StrField
        in: StrField

        constructor()

        withName(name: string): this

        withIn(inVal: string): this


      }
      export class HttpApiKeySettings extends Settings      {
        name: StrField
        in: StrField

        constructor()

        withName(name: string): this

        withIn(inVal: string): this


      }
      export class HttpSettings extends Settings      {
        scheme: StrField
        bearerFormat: StrField

        constructor()

        withScheme(scheme: string): this

        withBearerFormat(bearerFormat: string): this


      }
      export class OpenIdConnectSettings extends Settings      {
        url: StrField
        scopes: Array<Scope>

        constructor()

        withUrl(url: string): this

        withScopes(scopes: Array<Scope>): this


      }
      export class ServerBindings implements DomainElement, Linkable      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        bindings: Array<ServerBinding>
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        withBindings(bindings: Array<ServerBinding>): this

        linkCopy(): ServerBindings

        withName(name: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withId(id: string): this


      }
      export class Server implements DomainElement      {
        protocolVersion: StrField
        name: StrField
        customDomainProperties: Array<DomainExtension>
        security: Array<SecurityRequirement>
        url: StrField
        description: StrField
        bindings: ServerBindings
        variables: Array<Parameter>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>
        protocol: StrField

        constructor()

        withSecurity(security: Array<SecurityRequirement>): this

        withVariables(variables: Array<Parameter>): this

        withDescription(description: string): this

        withProtocol(protocol: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withVariable(name: string): Parameter

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withBindings(bindings: ServerBindings): this

        withProtocolVersion(protocolVersion: string): this

        withUrl(url: string): this

        withId(id: string): this


      }
      export class SecurityScheme implements DomainElement, Linkable      {
        displayName: StrField
        name: StrField
        customDomainProperties: Array<DomainExtension>
        description: StrField
        queryString: Shape
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        queryParameters: Array<Parameter>
        headers: Array<Parameter>
        type: StrField
        linkLabel: StrField
        extendsNode: Array<DomainElement>
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

        withQueryString(queryString: Shape): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withResponses(responses: Array<Response>): this

        withResponse(name: string): Response

        withLinkLabel(label: string): this

        withOpenIdConnectSettings(): OpenIdConnectSettings

        withQueryParameters(queryParameters: Array<Parameter>): this

        withHeader(name: string): Parameter

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

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
      export class SecurityRequirement implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        schemes: Array<ParametrizedSecurityScheme>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withName(name: string): this

        withScheme(): ParametrizedSecurityScheme

        withSchemes(schemes: Array<ParametrizedSecurityScheme>): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this


      }
      export class Scope implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        description: StrField
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withName(name: string): this

        withDescription(description: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this


      }
      export class SchemaShape extends AnyShape      {
        mediaType: StrField
        raw: StrField
        location: undefined | string

        constructor()

        withMediatype(mediaType: string): this

        withRaw(text: string): this

        linkCopy(): SchemaShape


      }
      export class SchemaDependencies implements DomainElement      {
        source: StrField
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        target: Shape
        extendsNode: Array<DomainElement>

        constructor()

        withSchemaTarget(schema: Shape): this

        withPropertySource(propertySource: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this


      }
      export class ScalarShape extends AnyShape      {
        dataType: StrField
        pattern: StrField
        minLength: IntField
        maxLength: IntField
        minimum: DoubleField
        maximum: DoubleField
        exclusiveMinimum: BoolField
        exclusiveMaximum: BoolField
        format: StrField
        multipleOf: DoubleField
        encoding: StrField
        mediaType: StrField
        schema: Shape

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

        withSchema(schema: Shape): this

        linkCopy(): ScalarShape


      }
      export class Response extends Message      {
        statusCode: StrField
        headers: Array<Parameter>
        links: Array<TemplatedLink>

        constructor()

        withStatusCode(statusCode: string): this

        withHeaders(headers: Array<Parameter>): this

        withLinks(links: Array<TemplatedLink>): this

        withHeader(name: string): Parameter

        linkCopy(): Response


      }
      export class ResourceType extends AbstractDeclaration      {
        linkTarget: undefined | DomainElement

        constructor()

        linkCopy(): ResourceType

        asEndpoint<T>(unit: T, profile: ProfileName): EndPoint


      }
      export class Request extends Message      {
        required: BoolField
        queryParameters: Array<Parameter>
        headers: Array<Parameter>
        queryString: Shape
        uriParameters: Array<Parameter>
        cookieParameters: Array<Parameter>

        constructor()

        withRequired(required: boolean): this

        withQueryParameters(parameters: Array<Parameter>): this

        withHeaders(headers: Array<Parameter>): this

        withQueryString(queryString: Shape): this

        withUriParameters(uriParameters: Array<Parameter>): this

        withCookieParameters(cookieParameters: Array<Parameter>): this

        withQueryParameter(name: string): Parameter

        withHeader(name: string): Parameter

        withUriParameter(name: string): Parameter

        withCookieParameter(name: string): Parameter

        linkCopy(): Request


      }
      export class PropertyDependencies implements DomainElement      {
        source: StrField
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        target: Array<StrField>
        extendsNode: Array<DomainElement>

        constructor()

        withPropertySource(propertySource: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withPropertyTarget(propertyTarget: Array<string>): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this


      }
      export class Payload implements DomainElement      {
        mediaType: StrField
        name: StrField
        customDomainProperties: Array<DomainExtension>
        examples: Array<Example>
        encoding: Array<Encoding>
        isExternalLink: BoolField
        id: string
        schema: Shape
        schemaMediaType: StrField
        position: core.parser.Range
        encodings: Array<Encoding>
        extendsNode: Array<DomainElement>

        constructor()

        withEncoding(name: string): Encoding

        withEncodings(encoding: Array<Encoding>): this

        withName(name: string): this

        withScalarSchema(name: string): ScalarShape

        withExample(name: string): Example

        withExamples(examples: Array<Example>): this

        withObjectSchema(name: string): NodeShape

        withSchema(schema: Shape): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withMediaType(mediaType: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withSchemaMediaType(mediaType: string): this

        withEncoding(encoding: Array<Encoding>): this

        withId(id: string): this


      }
      export class ParametrizedTrait implements ParametrizedDeclaration      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        variables: Array<VariableValue>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        target: AbstractDeclaration
        extendsNode: Array<DomainElement>

        constructor()

        withName(name: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withVariables(variables: Array<VariableValue>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withTarget(target: AbstractDeclaration): this

        withId(id: string): this


      }
      export class ParametrizedSecurityScheme implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        description: StrField
        isExternalLink: BoolField
        id: string
        scheme: SecurityScheme
        position: core.parser.Range
        extendsNode: Array<DomainElement>
        settings: Settings

        constructor()

        withName(name: string): this

        withDescription(description: string): this

        withHttpSettings(): HttpSettings

        withOAuth2Settings(): OAuth2Settings

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withOpenIdConnectSettings(): OpenIdConnectSettings

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withOAuth1Settings(): OAuth1Settings

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withDefaultSettings(): Settings

        withScheme(scheme: SecurityScheme): this

        withSettings(settings: Settings): this

        withId(id: string): this

        withApiKeySettings(): ApiKeySettings


      }
      export class ParametrizedResourceType implements ParametrizedDeclaration      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        variables: Array<VariableValue>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        target: AbstractDeclaration
        extendsNode: Array<DomainElement>

        constructor()

        withName(name: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withVariables(variables: Array<VariableValue>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withTarget(target: AbstractDeclaration): this

        withId(id: string): this


      }
      export class Parameter implements DomainElement      {
        name: StrField
        binding: StrField
        customDomainProperties: Array<DomainExtension>
        examples: Array<Example>
        style: StrField
        description: StrField
        payloads: Array<Payload>
        deprecated: BoolField
        allowReserved: BoolField
        isExternalLink: BoolField
        id: string
        schema: Shape
        explode: BoolField
        parameterName: StrField
        position: core.parser.Range
        required: BoolField
        allowEmptyValue: BoolField
        extendsNode: Array<DomainElement>

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

        withSchema(schema: Shape): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withAllowReserved(allowReserved: boolean): this

        withRequired(required: boolean): this

        withId(id: string): this

        withParameterName(name: string): this

        withBinding(binding: string): this

        withDeprecated(deprecated: boolean): this


      }
      export class Organization implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        email: StrField
        url: StrField
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withEmail(email: string): this

        withName(name: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withUrl(url: string): this

        withId(id: string): this


      }
      export class OperationBindings implements DomainElement, Linkable      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        bindings: Array<OperationBinding>
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        linkCopy(): OperationBindings

        withName(name: string): this

        withBindings(bindings: Array<OperationBinding>): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withId(id: string): this


      }
      export class Operation implements DomainElement, Linkable      {
        method: StrField
        name: StrField
        customDomainProperties: Array<DomainExtension>
        request: Request
        security: Array<SecurityRequirement>
        description: StrField
        bindings: OperationBindings
        tags: Array<Tag>
        documentation: CreativeWork
        deprecated: BoolField
        linkTarget: undefined | DomainElement
        operationId: StrField
        servers: Array<Server>
        schemes: Array<StrField>
        isLink: boolean
        isExternalLink: BoolField
        id: string
        contentType: Array<StrField>
        accepts: Array<StrField>
        position: core.parser.Range
        isAbstract: BoolField
        linkLabel: StrField
        callbacks: Array<Callback>
        requests: Array<Request>
        extendsNode: Array<DomainElement>
        summary: StrField
        responses: Array<Response>

        constructor()

        withSecurity(security: Array<SecurityRequirement>): this

        linkCopy(): Operation

        withName(name: string): this

        withMethod(method: string): this

        withDescription(description: string): this

        graph(): Graph

        withBindings(bindings: OperationBindings): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withResponses(responses: Array<Response>): this

        withResponse(name: string): Response

        withLinkLabel(label: string): this

        withTags(tags: Array<Tag>): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

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
      export class OAuth2Flow implements DomainElement      {
        customDomainProperties: Array<DomainExtension>
        accessTokenUri: StrField
        scopes: Array<Scope>
        isExternalLink: BoolField
        id: string
        flow: StrField
        authorizationUri: StrField
        position: core.parser.Range
        refreshUri: StrField
        extendsNode: Array<DomainElement>

        constructor()

        withFlow(flow: string): this

        withScopes(scopes: Array<Scope>): this

        withAuthorizationUri(authorizationUri: string): this

        withAccessTokenUri(accessTokenUri: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this

        withRefreshUri(refreshUri: string): this


      }
      export class NodeShape extends AnyShape      {
        minProperties: IntField
        maxProperties: IntField
        closed: BoolField
        discriminator: StrField
        discriminatorValue: StrField
        discriminatorMapping: Array<IriTemplateMapping>
        properties: Array<PropertyShape>
        additionalPropertiesSchema: Shape
        dependencies: Array<PropertyDependencies>
        schemaDependencies: Array<SchemaDependencies>
        propertyNames: Shape
        unevaluatedProperties: boolean
        unevaluatedPropertiesSchema: Shape

        constructor()

        withMinProperties(min: number): this

        withMaxProperties(max: number): this

        withClosed(closed: boolean): this

        withDiscriminator(discriminator: string): this

        withDiscriminatorValue(value: string): this

        withDiscriminatorMapping(mappings: Array<IriTemplateMapping>): this

        withProperties(properties: Array<PropertyShape>): this

        withAdditionalPropertiesSchema(additionalPropertiesSchema: Shape): this

        withDependencies(dependencies: Array<PropertyDependencies>): this

        withSchemaDependencies(dependencies: Array<SchemaDependencies>): this

        withPropertyNames(propertyNames: Shape): this

        withUnevaluatedProperties(value: boolean): this

        withUnevaluatedPropertiesSchema(schema: Shape): this

        withProperty(name: string): PropertyShape

        withDependency(): PropertyDependencies

        withInheritsObject(name: string): NodeShape

        withInheritsScalar(name: string): ScalarShape

        linkCopy(): NodeShape


      }
      export class NilShape extends AnyShape      {
        constructor()

        linkCopy(): NilShape


      }
      export class MqttServerBinding implements ServerBinding      {
        customDomainProperties: Array<DomainExtension>
        clientId: StrField
        keepAlive: IntField
        cleanSession: BoolField
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        linkLabel: StrField
        lastWill: MqttServerLastWill
        extendsNode: Array<DomainElement>

        constructor()

        withKeepAlive(keepAlive: number): this

        linkCopy(): MqttServerBinding

        withClientId(clientId: string): this

        withCleanSession(cleanSession: boolean): this

        graph(): Graph

        withBindingVersion(bindingVersion: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withId(id: string): this

        withLastWill(lastWill: MqttServerLastWill): this


      }
      export class MqttServerLastWill implements DomainElement      {
        customDomainProperties: Array<DomainExtension>
        retain: BoolField
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        message: StrField
        topic: StrField
        qos: IntField
        extendsNode: Array<DomainElement>

        constructor()

        withRetain(retain: boolean): this

        withMessage(message: string): this

        withTopic(topic: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withQos(qos: number): this

        withId(id: string): this


      }
      export class MqttOperationBinding implements OperationBinding      {
        customDomainProperties: Array<DomainExtension>
        retain: BoolField
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        qos: IntField
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        linkCopy(): MqttOperationBinding

        withRetain(retain: boolean): this

        graph(): Graph

        withBindingVersion(bindingVersion: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withQos(qos: number): this

        withId(id: string): this


      }
      export class MqttMessageBinding implements MessageBinding      {
        customDomainProperties: Array<DomainExtension>
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        linkCopy(): MqttMessageBinding

        graph(): Graph

        withBindingVersion(bindingVersion: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withId(id: string): this


      }
      export class MessageBindings implements DomainElement, Linkable      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        bindings: Array<MessageBinding>
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        linkCopy(): MessageBindings

        withName(name: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withBindings(bindings: Array<MessageBinding>): this

        withId(id: string): this


      }
      export class Message implements DomainElement, Linkable      {
        displayName: StrField
        name: StrField
        headerSchema: NodeShape
        customDomainProperties: Array<DomainExtension>
        examples: Array<Example>
        description: StrField
        bindings: MessageBindings
        tags: Array<Tag>
        documentation: CreativeWork
        payloads: Array<Payload>
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        correlationId: CorrelationId
        isAbstract: BoolField
        title: StrField
        linkLabel: StrField
        extendsNode: Array<DomainElement>
        headerExamples: Array<Example>
        summary: StrField

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

        graph(): Graph

        withHeaderExamples(examples: Array<Example>): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withTags(tags: Array<Tag>): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withSummary(summary: string): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withPayload(mediaType: undefined | string): Payload

        withDisplayName(displayName: string): this

        withCorrelationId(correlationId: CorrelationId): this

        withId(id: string): this

        withDocumentation(documentation: CreativeWork): this


      }
      export class MatrixShape extends ArrayShape      {
        constructor()

        withItems(items: Shape): this


      }
      export class License implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        url: StrField
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withName(name: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withUrl(url: string): this

        withId(id: string): this


      }
      export class KafkaOperationBinding implements OperationBinding      {
        customDomainProperties: Array<DomainExtension>
        clientId: Shape
        linkTarget: undefined | DomainElement
        groupId: Shape
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        linkCopy(): KafkaOperationBinding

        withGroupId(groupId: Shape): this

        withClientId(clientId: Shape): this

        graph(): Graph

        withBindingVersion(bindingVersion: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withId(id: string): this


      }
      export class KafkaMessageBinding implements MessageBinding      {
        customDomainProperties: Array<DomainExtension>
        messageKey: Shape
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        withKey(key: Shape): this

        linkCopy(): KafkaMessageBinding

        graph(): Graph

        withBindingVersion(bindingVersion: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withId(id: string): this


      }
      export class IriTemplateMapping implements DomainElement      {
        customDomainProperties: Array<DomainExtension>
        templateVariable: StrField
        isExternalLink: BoolField
        id: string
        linkExpression: StrField
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        graph(): Graph

        withTemplateVariable(variable: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withLinkExpression(expression: string): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this


      }
      export class HttpOperationBinding implements OperationBinding      {
        method: StrField
        customDomainProperties: Array<DomainExtension>
        operationType: StrField
        query: Shape
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        linkCopy(): HttpOperationBinding

        withMethod(method: string): this

        withOperationType(type: string): this

        graph(): Graph

        withBindingVersion(bindingVersion: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withId(id: string): this

        withQuery(query: Shape): this


      }
      export class HttpMessageBinding implements MessageBinding      {
        customDomainProperties: Array<DomainExtension>
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        headers: Shape
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        linkCopy(): HttpMessageBinding

        withHeaders(headers: Shape): this

        graph(): Graph

        withBindingVersion(bindingVersion: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withId(id: string): this


      }
      export class FileShape extends AnyShape      {
        fileTypes: Array<StrField>
        pattern: StrField
        minLength: IntField
        maxLength: IntField
        minimum: DoubleField
        maximum: DoubleField
        exclusiveMinimum: BoolField
        exclusiveMaximum: BoolField
        format: StrField
        multipleOf: DoubleField

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
      export class Example implements DomainElement, Linkable      {
        displayName: StrField
        mediaType: StrField
        name: StrField
        strict: BoolField
        toYaml: string
        customDomainProperties: Array<DomainExtension>
        location: undefined | string
        description: StrField
        structuredValue: DataNode
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        toJson: string
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>
        value: StrField

        constructor()

        linkCopy(): Example

        withName(name: string): this

        withStructuredValue(value: DataNode): this

        withDescription(description: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withMediaType(mediaType: string): this

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withValue(value: string): this

        withLinkTarget(target: undefined): this

        withStrict(strict: boolean): this

        withDisplayName(displayName: string): this

        withId(id: string): this


      }
      export class EndPoint implements DomainElement      {
        parent: undefined | EndPoint
        operations: Array<Operation>
        name: StrField
        customDomainProperties: Array<DomainExtension>
        path: StrField
        security: Array<SecurityRequirement>
        description: StrField
        bindings: ChannelBindings
        relativePath: string
        payloads: Array<Payload>
        servers: Array<Server>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>
        parameters: Array<Parameter>
        summary: StrField

        constructor()

        withSecurity(security: Array<SecurityRequirement>): this

        withPayloads(payloads: Array<Payload>): this

        withPath(path: string): this

        withParameter(name: string): Parameter

        withName(name: string): this

        withDescription(description: string): this

        withOperation(method: string): Operation

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withPayload(name: string): Payload

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withSummary(summary: string): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withServer(url: string): Server

        withOperations(operations: Array<Operation>): this

        withServers(servers: Array<Server>): this

        withParameters(parameters: Array<Parameter>): this

        withId(id: string): this

        withBindings(bindings: ChannelBindings): this


      }
      export class Encoding implements DomainElement      {
        customDomainProperties: Array<DomainExtension>
        style: StrField
        allowReserved: BoolField
        isExternalLink: BoolField
        id: string
        contentType: StrField
        explode: BoolField
        position: core.parser.Range
        propertyName: StrField
        headers: Array<Parameter>
        extendsNode: Array<DomainElement>

        constructor()

        withContentType(contentType: string): this

        withHeaders(headers: Array<Parameter>): this

        withExplode(explode: boolean): this

        withStyle(style: string): this

        withPropertyName(propertyName: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withHeader(name: string): Parameter

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withAllowReserved(allowReserved: boolean): this

        withId(id: string): this


      }
      export class EmptyBinding implements ServerBinding, OperationBinding, ChannelBinding, MessageBinding      {
        customDomainProperties: Array<DomainExtension>
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        type: StrField
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        linkCopy(): EmptyBinding

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withType(type: string): this

        withId(id: string): this


      }
      export class DataArrangeShape extends AnyShape      {
        minItems: IntField
        maxItems: IntField
        uniqueItems: BoolField

        withMinItems(minItems: number): this

        withMaxItems(maxItems: number): this

        withUniqueItems(uniqueItems: boolean): this


      }
      export class CreativeWork implements DomainElement      {
        customDomainProperties: Array<DomainExtension>
        url: StrField
        description: StrField
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        title: StrField
        extendsNode: Array<DomainElement>

        constructor()

        withDescription(description: string): this

        withTitle(title: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withUrl(url: string): this

        withId(id: string): this


      }
      export class CorrelationId implements DomainElement, Linkable      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        description: StrField
        idLocation: StrField
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        linkCopy(): CorrelationId

        withName(name: string): this

        withDescription(description: string): this

        withIdLocation(idLocation: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withId(id: string): this


      }
      export class ChannelBindings implements DomainElement, Linkable      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        bindings: Array<ChannelBinding>
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        linkCopy(): ChannelBindings

        withName(name: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withBindings(bindings: Array<ChannelBinding>): this

        withId(id: string): this


      }
      export class Callback implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        expression: StrField
        endpoint: EndPoint
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withExpression(expression: string): this

        withName(name: string): this

        withEndpoint(endpoint: EndPoint): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withEndpoint(path: string): EndPoint

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this


      }
      export interface ChannelBinding extends DomainElement, Linkable      {
      }
      export interface OperationBinding extends DomainElement, Linkable      {
      }
      export interface MessageBinding extends DomainElement, Linkable      {
      }
      export interface ServerBinding extends DomainElement, Linkable      {
      }
      export class AsyncApi extends Api<AsyncApi>      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        endPoints: Array<EndPoint>
        provider: Organization
        security: Array<SecurityRequirement>
        identifier: StrField
        description: StrField
        documentations: Array<CreativeWork>
        servers: Array<Server>
        schemes: Array<StrField>
        license: License
        isExternalLink: BoolField
        sourceVendor: undefined | core.Vendor
        termsOfService: StrField
        version: StrField
        id: string
        contentType: Array<StrField>
        accepts: Array<StrField>
        position: core.parser.Range
        extendsNode: Array<DomainElement>

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

        graph(): Graph

        withEndPoints(endPoints: Array<EndPoint>): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withTermsOfService(terms: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

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
      export class ArrayShape extends DataArrangeShape      {
        items: Shape
        contains: Shape
        minContains: number
        maxContains: number
        unevaluatedItems: boolean
        unevaluatedItemsSchema: Shape

        constructor()

        withItems(items: Shape): this

        withContains(contains: Shape): this

        withMinContains(amount: number): this

        withMaxContains(amount: number): this

        withUnevaluatedItemsSchema(schema: Shape): this

        withUnevaluatedItems(value: boolean): this

        linkCopy(): ArrayShape


      }
      export class Api<A> implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        endPoints: Array<EndPoint>
        provider: Organization
        security: Array<SecurityRequirement>
        identifier: StrField
        description: StrField
        documentations: Array<CreativeWork>
        servers: Array<Server>
        schemes: Array<StrField>
        license: License
        isExternalLink: BoolField
        sourceVendor: undefined | core.Vendor
        termsOfService: StrField
        version: StrField
        id: string
        contentType: Array<StrField>
        accepts: Array<StrField>
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        withDocumentationTitle(title: string): CreativeWork

        withEndPoint(path: string): EndPoint

        withDefaultServer(url: string): Server

        withDocumentationUrl(url: string): CreativeWork

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withServer(url: string): Server

        withId(id: string): this


      }
      export class AnyShape implements Shape      {
        defaultValueStr: StrField
        displayName: StrField
        name: StrField
        customDomainProperties: Array<DomainExtension>
        examples: Array<Example>
        xone: Array<Shape>
        readOnly: BoolField
        description: StrField
        documentation: CreativeWork
        deprecated: BoolField
        xmlSerialization: XMLSerializer
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
        comment: StrField
        not: Shape
        values: Array<DataNode>
        position: core.parser.Range
        isDefaultEmpty: boolean
        inherits: Array<Shape>
        linkLabel: StrField
        defaultValue: DataNode
        extendsNode: Array<DomainElement>
        and: Array<Shape>

        constructor()

        withValues(values: Array<DataNode>): this

        linkCopy(): AnyShape

        withOr(subShapes: Array<Shape>): this

        withName(name: string): this

        buildRamlDatatype(): string

        withDescription(description: string): this

        trackedExample(trackId: string): undefined | Example

        withIf(ifShape: Shape): this

        toRamlDatatype(): string

        validate(fragment: PayloadFragment): Promise<client.validate.ValidationReport>

        buildJsonSchema(): string

        withCustomShapePropertyDefinition(name: string): PropertyShape

        withExamples(examples: Array<Example>): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withCustomShapePropertyDefinitions(propertyDefinitions: Array<PropertyShape>): this

        withReadOnly(readOnly: boolean): this

        withInherits(inherits: Array<Shape>): this

        withAnd(subShapes: Array<Shape>): this

        parameterValidator(mediaType: string, env: client.environment.Environment): undefined | validate.PayloadValidator

        validateParameter(payload: string, env: client.environment.Environment): Promise<client.validate.ValidationReport>

        validate(fragment: PayloadFragment, env: client.environment.Environment): Promise<client.validate.ValidationReport>

        withComment(comment: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withWriteOnly(writeOnly: boolean): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        inlined(): boolean

        parameterValidator(mediaType: string): undefined | validate.PayloadValidator

        toJsonSchema(): string

        withLinkTarget(target: undefined): this

        withDisplayName(name: string): this

        withDefaultValue(defaultVal: DataNode): this

        withXMLSerialization(xmlSerialization: XMLSerializer): this

        validate(payload: string): Promise<client.validate.ValidationReport>

        validate(payload: string, env: client.environment.Environment): Promise<client.validate.ValidationReport>

        withThen(thenShape: Shape): this

        withDefaultStr(value: string): this

        validateParameter(payload: string): Promise<client.validate.ValidationReport>

        withCustomShapeProperties(customShapeProperties: Array<ShapeExtension>): this

        withId(id: string): this

        withElse(elseShape: Shape): this

        withExample(mediaType: string): Example

        buildJsonSchema(options: render.ShapeRenderOptions): string

        withXone(subShapes: Array<Shape>): this

        withDocumentation(documentation: CreativeWork): this

        withDeprecated(deprecated: boolean): this

        payloadValidator(mediaType: string, env: client.environment.Environment): undefined | validate.PayloadValidator

        withNode(shape: Shape): this

        payloadValidator(mediaType: string): undefined | validate.PayloadValidator


      }
      export class Amqp091OperationBinding implements OperationBinding      {
        priority: IntField
        customDomainProperties: Array<DomainExtension>
        timestamp: BoolField
        mandatory: BoolField
        replyTo: StrField
        deliveryMode: IntField
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        ack: BoolField
        bcc: Array<StrField>
        position: core.parser.Range
        cc: Array<StrField>
        userId: StrField
        linkLabel: StrField
        expiration: IntField
        extendsNode: Array<DomainElement>

        constructor()

        withDeliveryMode(deliveryMode: number): this

        linkCopy(): Amqp091OperationBinding

        withCc(cC: Array<string>): this

        withTimestamp(timestamp: boolean): this

        withReplyTo(replyTo: string): this

        graph(): Graph

        withBindingVersion(bindingVersion: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withMandatory(mandatory: boolean): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withBcc(bCC: Array<string>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withUserId(userId: string): this

        withAck(ack: boolean): this

        withPriority(priority: number): this

        withExpiration(expiration: number): this

        withId(id: string): this


      }
      export class Amqp091MessageBinding implements MessageBinding      {
        customDomainProperties: Array<DomainExtension>
        linkTarget: undefined | DomainElement
        isLink: boolean
        messageType: StrField
        isExternalLink: BoolField
        id: string
        contentEncoding: StrField
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        withMessageType(messageType: string): this

        linkCopy(): Amqp091MessageBinding

        withContentEncoding(contentEncoding: string): this

        graph(): Graph

        withBindingVersion(bindingVersion: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withId(id: string): this


      }
      export class Amqp091ChannelBinding implements ChannelBinding      {
        is: StrField
        customDomainProperties: Array<DomainExtension>
        queue: Amqp091Queue
        linkTarget: undefined | DomainElement
        isLink: boolean
        exchange: Amqp091ChannelExchange
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        linkCopy(): Amqp091ChannelBinding

        withQueue(queue: Amqp091Queue): this

        graph(): Graph

        withBindingVersion(bindingVersion: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withIs(is: string): this

        withExchange(exchange: Amqp091ChannelExchange): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withId(id: string): this


      }
      export class Amqp091ChannelExchange implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        autoDelete: BoolField
        isExternalLink: BoolField
        id: string
        vHost: StrField
        position: core.parser.Range
        type: StrField
        extendsNode: Array<DomainElement>
        durable: BoolField

        constructor()

        withName(name: string): this

        withDurable(durable: boolean): this

        withVHost(vHost: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withAutoDelete(autoDelete: boolean): this

        withType(type: string): this

        withId(id: string): this


      }
      export class Amqp091Queue implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        autoDelete: BoolField
        isExternalLink: BoolField
        id: string
        vHost: StrField
        exclusive: BoolField
        position: core.parser.Range
        extendsNode: Array<DomainElement>
        durable: BoolField

        constructor()

        withName(name: string): this

        withDurable(durable: boolean): this

        withVHost(vHost: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExclusive(exclusive: boolean): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withAutoDelete(autoDelete: boolean): this

        withId(id: string): this


      }
      export class Overlay extends document.Document      {
        constructor()

      }
      export class DocumentationItem extends document.Fragment      {
        constructor()

      }
      export class DataType extends document.Fragment      {
        constructor()

      }
      export class NamedExample extends document.Fragment      {
        constructor()

      }
      export class ResourceTypeFragment extends document.Fragment      {
        constructor()

      }
      export class TraitFragment extends document.Fragment      {
        constructor()

      }
      export class AnnotationTypeDeclaration extends document.Fragment      {
        constructor()

      }
      export class SecuritySchemeFragment extends document.Fragment      {
        constructor()

      }
      export class Extension extends document.Document      {
        constructor()

      }
      export class Variable      {
        name: string
        value: DataNode

        constructor(name: string, value: DataNode)

      }
      export class VariableValue implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
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
      export class ShapeExtension implements DomainElement      {
        customDomainProperties: Array<DomainExtension>
        definedBy: PropertyShape
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>
        extension: DataNode

        constructor()

        withDefinedBy(definedBy: PropertyShape): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withExtension(extension: DataNode): this

        withId(id: string): this


      }
      export interface Shape extends DomainElement, Linkable      {
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
      export class ScalarNode implements DataNode      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        toString: undefined
        dataType: StrField
        extendsNode: Array<DomainElement>
        value: StrField

        constructor()
        constructor(value: string, dataType: string)

        withName(name: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withDataType(dataType: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withValue(value: string): this

        withId(id: string): this


      }
      export class RecursiveShape implements Shape      {
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
        position: core.parser.Range
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

        parameterValidator(mediaType: string, env: client.environment.Environment): undefined | validate.PayloadValidator

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withWriteOnly(writeOnly: boolean): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        parameterValidator(mediaType: string): undefined | validate.PayloadValidator

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

        payloadValidator(mediaType: string, env: client.environment.Environment): undefined | validate.PayloadValidator

        withNode(shape: Shape): this

        payloadValidator(mediaType: string): undefined | validate.PayloadValidator


      }
      export class PropertyShape implements Shape      {
        defaultValueStr: StrField
        displayName: StrField
        name: StrField
        customDomainProperties: Array<DomainExtension>
        path: StrField
        xone: Array<Shape>
        readOnly: BoolField
        description: StrField
        deprecated: BoolField
        customShapePropertyDefinitions: Array<PropertyShape>
        or: Array<Shape>
        elseShape: Shape
        linkTarget: undefined | DomainElement
        maxCount: IntField
        isLink: boolean
        isExternalLink: BoolField
        customShapeProperties: Array<ShapeExtension>
        thenShape: Shape
        id: string
        range: Shape
        ifShape: Shape
        writeOnly: BoolField
        patternName: StrField
        not: Shape
        values: Array<DataNode>
        position: core.parser.Range
        inherits: Array<Shape>
        linkLabel: StrField
        defaultValue: DataNode
        extendsNode: Array<DomainElement>
        and: Array<Shape>
        minCount: IntField

        constructor()

        withValues(values: Array<DataNode>): this

        withPath(path: string): this

        linkCopy(): PropertyShape

        withOr(subShapes: Array<Shape>): this

        withName(name: string): this

        withRange(range: Shape): this

        withDescription(description: string): this

        withMaxCount(max: number): this

        withIf(ifShape: Shape): this

        withCustomShapePropertyDefinition(name: string): PropertyShape

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withCustomShapePropertyDefinitions(propertyDefinitions: Array<PropertyShape>): this

        withReadOnly(readOnly: boolean): this

        withPatternName(pattern: string): this

        withInherits(inherits: Array<Shape>): this

        withAnd(subShapes: Array<Shape>): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withWriteOnly(writeOnly: boolean): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withDisplayName(name: string): this

        withDefaultValue(defaultVal: DataNode): this

        withThen(thenShape: Shape): this

        withMinCount(min: number): this

        withDefaultStr(value: string): this

        withCustomShapeProperties(customShapeProperties: Array<ShapeExtension>): this

        withId(id: string): this

        withElse(elseShape: Shape): this

        withXone(subShapes: Array<Shape>): this

        withDeprecated(deprecated: boolean): this

        withNode(shape: Shape): this


      }
      export interface ParametrizedDeclaration extends DomainElement      {
        name: StrField
        target: AbstractDeclaration
        variables: Array<VariableValue>

        withName(name: string): this

        withTarget(target: AbstractDeclaration): this

        withVariables(variables: Array<VariableValue>): this


      }
      export class ObjectNode implements DataNode      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        properties: Map<string, DataNode>
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withName(name: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        getProperty(property: string): undefined | DataNode

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this

        addProperty(property: string, node: DataNode): this


      }
      export interface Linkable      {
        linkTarget: undefined | DomainElement
        isLink: boolean
        linkLabel: StrField

        linkCopy(): Linkable

        withLinkTarget(target: undefined): this

        withLinkLabel(label: string): this


      }
      export class Graph      {
        types(): Array<string>

        properties(): Array<string>

        scalarByProperty(id: string): Array<any>

        getObjectByPropertyId(id: string): Array<DomainElement>

        remove(uri: string): this


      }
      export class ExternalDomainElement implements DomainElement      {
        mediaType: StrField
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        raw: StrField
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withRaw(raw: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withMediaType(mediaType: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this


      }
      export class DomainExtension implements DomainElement      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        definedBy: CustomDomainProperty
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>
        extension: DataNode

        constructor()

        withName(name: string): this

        withDefinedBy(property: CustomDomainProperty): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this

        withExtension(node: DataNode): this


      }
      export interface DomainElement extends CustomizableElement      {
        customDomainProperties: Array<DomainExtension>
        extendsNode: Array<DomainElement>
        id: string
        position: core.parser.Range
        isExternalLink: BoolField

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withId(id: string): this

        withIsExternalLink(isExternalLink: boolean): DomainElement

        graph(): Graph


      }
      export interface DataNode extends DomainElement      {
        name: StrField

        withName(name: string): this


      }
      export interface CustomizableElement      {
        customDomainProperties: Array<DomainExtension>

        withCustomDomainProperties(extensions: Array<DomainExtension>): this


      }
      export class CustomDomainProperty implements DomainElement, Linkable      {
        displayName: StrField
        name: StrField
        customDomainProperties: Array<DomainExtension>
        description: StrField
        domain: Array<StrField>
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        schema: Shape
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        linkCopy(): CustomDomainProperty

        withName(name: string): this

        withDescription(description: string): this

        withDomain(domain: Array<string>): this

        withSchema(schema: Shape): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        withDisplayName(displayName: string): this

        withId(id: string): this


      }
      export class ArrayNode implements DataNode      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        members: Array<DataNode>
        extendsNode: Array<DomainElement>

        constructor()

        withName(name: string): this

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this

        addMember(member: DataNode): this


      }
      export class AbstractDeclaration implements DomainElement, Linkable      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        description: StrField
        dataNode: DataNode
        variables: Array<StrField>
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
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
      export class PayloadFragment extends document.Fragment      {
        mediaType: StrField
        dataNode: DataNode

        constructor(scalar: ScalarNode, mediaType: string)
        constructor(obj: ObjectNode, mediaType: string)
        constructor(arr: ArrayNode, mediaType: string)

      }
      export class VocabularyReference implements DomainElement      {
        reference: StrField
        customDomainProperties: Array<DomainExtension>
        alias: StrField
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withAlias(alias: string): VocabularyReference

        graph(): Graph

        withReference(reference: string): VocabularyReference

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this


      }
      export class UnionNodeMapping implements DomainElement, Linkable      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        typeDiscriminator(): Map<string, string>

        linkCopy(): UnionNodeMapping

        withName(name: string): UnionNodeMapping

        withTypeDiscriminatorName(name: string): UnionNodeMapping

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withObjectRange(range: Array<string>): UnionNodeMapping

        withLinkLabel(label: string): this

        objectRange(): Array<StrField>

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withLinkTarget(target: undefined): this

        typeDiscriminatorName(): StrField

        withId(id: string): this

        withTypeDiscriminator(typesMapping: Map<string, string>): any


      }
      export class PublicNodeMapping implements DomainElement      {
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withName(name: string): any

        mappedNode(): StrField

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withMappedNode(mappedNode: string): any

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        name(): StrField

        withId(id: string): this


      }
      export class DocumentMapping implements DomainElement      {
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withDeclaredNodes(declarations: Array<PublicNodeMapping>): any

        withDocumentName(name: string): any

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        documentName(): StrField

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withEncoded(encodedNode: string): any

        declaredNodes(): Array<PublicNodeMapping>

        withId(id: string): this

        encoded(): StrField


      }
      export class DocumentsModel implements DomainElement      {
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        declarationsPath(): StrField

        withRoot(documentMapping: DocumentMapping): any

        withKeyProperty(keyProperty: boolean): DocumentsModel

        keyProperty(): BoolField

        fragments(): Array<DocumentMapping>

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        root(): DocumentMapping

        selfEncoded(): BoolField

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withFragments(fragments: Array<DocumentMapping>): any

        library(): DocumentMapping

        withLibrary(library: DocumentMapping): any

        withSelfEncoded(selfEncoded: boolean): DocumentsModel

        withDeclarationsPath(declarationsPath: string): DocumentsModel

        withId(id: string): this


      }
      export class PropertyTerm implements DomainElement      {
        displayName: StrField
        name: StrField
        customDomainProperties: Array<DomainExtension>
        description: StrField
        subPropertyOf: Array<StrField>
        isExternalLink: BoolField
        id: string
        range: StrField
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        withName(name: string): any

        withDescription(description: string): any

        graph(): Graph

        withSubClasOf(superProperties: Array<string>): any

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withDisplayName(displayName: string): any

        withId(id: string): this

        withRange(range: string): any


      }
      export class ObjectPropertyTerm extends PropertyTerm      {
        constructor()

      }
      export class DatatypePropertyTerm extends PropertyTerm      {
        constructor()

      }
      export class PropertyMapping implements DomainElement      {
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        maximum(): DoubleField

        typeDiscriminator(): Map<string, string>

        classification(): string

        withEnum(values: Array<any>): any

        enum(): Array<AnyField>

        minCount(): IntField

        withMapKeyProperty(key: string): any

        withName(name: string): any

        literalRange(): StrField

        externallyLinkable(): BoolField

        withTypeDiscriminatorName(name: string): any

        sorted(): BoolField

        minimum(): DoubleField

        pattern(): StrField

        graph(): Graph

        withLiteralRange(range: string): any

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withObjectRange(range: Array<string>): any

        objectRange(): Array<StrField>

        withPattern(pattern: string): any

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withMapValueProperty(value: string): any

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withSorted(sorted: boolean): any

        withNodePropertyMapping(propertyId: string): any

        allowMultiple(): BoolField

        withMinimum(min: number): any

        withMaximum(max: number): any

        nodePropertyMapping(): StrField

        typeDiscriminatorName(): StrField

        name(): StrField

        withAllowMultiple(allow: boolean): any

        mapValueProperty(): StrField

        mapKeyProperty(): StrField

        withExternallyLinkable(linkable: boolean): any

        withId(id: string): this

        withTypeDiscriminator(typesMapping: Map<string, string>): any

        withMinCount(minCount: number): any


      }
      export class NodeMapping implements DomainElement, Linkable      {
        name: StrField
        customDomainProperties: Array<DomainExtension>
        idTemplate: StrField
        linkTarget: undefined | DomainElement
        isLink: boolean
        isExternalLink: BoolField
        id: string
        mergePolicy: StrField
        nodetypeMapping: StrField
        position: core.parser.Range
        linkLabel: StrField
        extendsNode: Array<DomainElement>

        constructor()

        withIdTemplate(idTemplate: string): any

        linkCopy(): NodeMapping

        withName(name: string): NodeMapping

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withLinkLabel(label: string): this

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withNodeTypeMapping(nodeType: string): NodeMapping

        withLinkTarget(target: undefined): this

        withPropertiesMapping(props: Array<PropertyMapping>): NodeMapping

        propertiesMapping(): Array<PropertyMapping>

        withMergePolicy(mergePolicy: string): NodeMapping

        withId(id: string): this


      }
      export class External implements DomainElement      {
        customDomainProperties: Array<DomainExtension>
        base: StrField
        alias: StrField
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        withAlias(alias: string): External

        graph(): Graph

        withBase(base: string): External

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withId(id: string): this


      }
      export class DialectDomainElement implements DomainElement      {
        customDomainProperties: Array<DomainExtension>
        isExternalLink: BoolField
        id: string
        position: core.parser.Range
        extendsNode: Array<DomainElement>

        constructor()

        setObjectProperty(propertyId: string, value: DialectDomainElement): this

        isAbstract(): BoolField

        withAbstract(isAbstract: boolean): DialectDomainElement

        setObjectCollectionProperty(propertyId: string, value: Array<DialectDomainElement>): this

        setLiteralProperty(propertyId: string, value: Array<any>): this

        localRefName(): string

        graph(): Graph

        setLiteralProperty(propertyId: string, value: boolean): this

        getScalarValueByPropertyUri(propertyId: string): Array<any>

        withIsExternalLink(isExternalLink: boolean): DomainElement

        getScalarByPropertyUri(propertyId: string): Array<any>

        getTypeUris(): Array<string>

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

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
      export class ClassTerm implements DomainElement      {
        displayName: StrField
        name: StrField
        customDomainProperties: Array<DomainExtension>
        description: StrField
        isExternalLink: BoolField
        id: string
        properties: Array<StrField>
        position: core.parser.Range
        subClassOf: Array<StrField>
        extendsNode: Array<DomainElement>

        constructor()

        withName(name: string): any

        withDescription(description: string): any

        graph(): Graph

        withIsExternalLink(isExternalLink: boolean): DomainElement

        withExtendsNode(extension: Array<ParametrizedDeclaration>): this

        withCustomDomainProperties(extensions: Array<DomainExtension>): this

        withProperties(properties: Array<string>): any

        withDisplayName(displayName: string): any

        withId(id: string): this

        withSubClassOf(superClasses: Array<string>): any


      }
    }
  }
  namespace resource {
    export interface ResourceLoader    {
      fetch(resource: string): Promise<client.remote.Content>

      accepts(resource: string): boolean


    }
    export class BaseHttpResourceLoader implements ResourceLoader    {
      fetch(resource: string): Promise<client.remote.Content>

      accepts(resource: string): boolean


    }
    export interface BaseFileResourceLoader extends ResourceLoader    {
      fetch(resource: string): Promise<client.remote.Content>

      fetchFile(resource: string): Promise<client.remote.Content>

      accepts(resource: string): boolean


    }
    export interface ClientResourceLoader    {
      fetch(resource: string): Promise<client.remote.Content>

      accepts(resource: string): boolean


    }
  }
  namespace parse {
    export class Parser    {
      parseFileAsync(url: string): Promise<model.document.BaseUnit>

      parseFileAsync(url: string, options: parser.ParsingOptions): Promise<model.document.BaseUnit>

      parseStringAsync(stream: string): Promise<model.document.BaseUnit>

      parseStringAsync(stream: string, options: parser.ParsingOptions): Promise<model.document.BaseUnit>

      parseStringAsync(url: string, stream: string): Promise<model.document.BaseUnit>

      parseStringAsync(url: string, stream: string, options: parser.ParsingOptions): Promise<model.document.BaseUnit>

      reportValidation(profile: ProfileName, messageStyle: amf.MessageStyle): Promise<client.validate.ValidationReport>

      reportValidation(profile: ProfileName): Promise<client.validate.ValidationReport>

      reportCustomValidation(profile: ProfileName, customProfilePath: string): Promise<client.validate.ValidationReport>


    }
  }
  namespace resolve {
    export interface ClientErrorHandler    {
      reportConstraint(id: string, node: string, property: undefined | string, message: string, range: undefined, level: string, location: undefined | string): void


    }
  }
  namespace amf {
    export interface MessageStyle    {
      profileName: ProfileName

    }
    namespace core {
      namespace remote {
        namespace server {
          export class Path          {
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
          export class JsServerPlatform          {
            instance(): JsServerPlatform

            platform(): string


          }
        }
        namespace browser {
          export class JsBrowserPlatform          {
            instance(): JsBrowserPlatform


          }
        }
      }
    }
    namespace plugins {
      namespace document {
        export class Vocabularies        {
          register(): void

          registerDialect(url: string): Promise<model.document.Dialect>

          registerDialect(url: string, env: client.environment.Environment): Promise<model.document.Dialect>

          registerDialect(url: string, dialectText: string): Promise<model.document.Dialect>

          registerDialect(url: string, dialectText: string, env: client.environment.Environment): Promise<model.document.Dialect>


        }
        namespace webapi {
          namespace validation {
            namespace remote {
              export interface ValidationResult              {
                readonly keyword: string
                readonly dataPath: string
                readonly schemaPath: string
                readonly params: undefined
                readonly message: string

              }
              export class Ajv              {
                readonly errors: undefined

                validate(schema: undefined, data: undefined): boolean

                addMetaSchema(metaSchema: undefined): Ajv

                addFormat(name: string, formatValidator: any): Ajv


              }
            }
          }
        }
      }
      namespace features {
        export class AMFValidation        {
          register(): void


        }
        export class AMFCustomValidation        {
          register(): void


        }
      }
    }
  }
  namespace render {
    export class ShapeRenderOptions    {
      withoutDocumentation: ShapeRenderOptions
      withCompactedEmission: ShapeRenderOptions
      errorHandler: resolve.ClientErrorHandler
      isWithDocumentation: boolean
      isWithCompactedEmission: boolean

      constructor()

      withErrorHandler(errorHandler: resolve.ClientErrorHandler): ShapeRenderOptions


    }
    export class Renderer    {
      generateFile(unit: model.document.BaseUnit, url: string): Promise<void>

      generateFile(unit: model.document.BaseUnit, url: string, options: RenderOptions): Promise<void>

      generateString(unit: model.document.BaseUnit): Promise<string>

      generateString(unit: model.document.BaseUnit, options: RenderOptions): Promise<string>

      generateToWriter(unit: model.document.BaseUnit, options: RenderOptions, writer: org.mulesoft.common.io.LimitedStringBuffer): Promise<void>

      generateToWriter(unit: model.document.BaseUnit, writer: org.mulesoft.common.io.LimitedStringBuffer): Promise<void>


    }
    export class RenderOptions    {
      withCompactedEmission: RenderOptions
      withoutCompactedEmission: RenderOptions
      withPrettyPrint: RenderOptions
      withoutPrettyPrint: RenderOptions
      withSourceMaps: RenderOptions
      withoutSourceMaps: RenderOptions
      withCompactUris: RenderOptions
      withoutCompactUris: RenderOptions
      withFlattenedJsonLd: RenderOptions
      withoutFlattenedJsonLd: RenderOptions
      isFlattenedJsonLd: boolean
      withoutAmfJsonLdSerialization: RenderOptions
      withAmfJsonLdSerialization: RenderOptions
      withNodeIds: RenderOptions
      isWithCompactedEmission: boolean
      isWithCompactUris: boolean
      isWithSourceMaps: boolean
      isAmfJsonLdSerilization: boolean
      errorHandler: resolve.ClientErrorHandler
      isPrettyPrint: boolean
      isEmitNodeIds: boolean

      constructor()

      withErrorHandler(errorHandler: resolve.ClientErrorHandler): RenderOptions


    }
  }
  namespace core {
    export class Vendor    {
      static readonly RAML: Vendor
      static readonly RAML08: Vendor
      static readonly RAML10: Vendor
      static readonly OAS: Vendor
      static readonly OAS20: Vendor
      static readonly OAS30: Vendor
      static readonly ASYNC: Vendor
      static readonly ASYNC20: Vendor
      static readonly AMF: Vendor
      static readonly PAYLOAD: Vendor
      static readonly AML: Vendor
      static readonly JSONSCHEMA: Vendor

      apply(name: string): Vendor


    }
    namespace parser {
      export class Position      {
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
      export class Range      {
        toString: string
        start: Position
        end: Position

        constructor(start: Position, end: Position)

        extent(other: Range): Range

        contains(other: Range): boolean


      }
    }
    namespace validation {
      export class SeverityLevels      {
        static readonly WARNING: 'Warning'
        static readonly INFO: 'Info'
        static readonly VIOLATION: 'Violation'

        unapply(arg: string): string


      }
    }
  }
  namespace handler {
    export interface JsErrorHandler    {
      reportConstraint(id: string, node: string, property: undefined | string, message: string, range: undefined, level: string, location: undefined | string): void


    }
  }
  namespace client {
    export class DefaultEnvironment    {
      apply(): environment.Environment


    }
    export class DefaultExecutionEnvironment    {
      apply(): execution.ExecutionEnvironment


    }
    namespace environment {
      export class Environment      {
        loaders: Array<resource.ResourceLoader>
        reference: undefined | reference.ReferenceResolver

        constructor()

        addClientLoader(loader: resource.ClientResourceLoader): Environment

        withClientResolver(resolver: reference.ClientReferenceResolver): Environment

        add(loader: resource.ClientResourceLoader): Environment

        withLoaders(loaders: Array<resource.ClientResourceLoader>): Environment

        withResolver(resolver: reference.ClientReferenceResolver): Environment


      }
    }
    namespace execution {
      export class ExecutionEnvironment      {
        constructor()

      }
    }
    namespace plugins {
      export class ValidationShapeSet      {
        candidates: Array<ValidationCandidate>
        defaultSeverity: string

        constructor(candidates: Array<ValidationCandidate>, closure: Array<model.domain.Shape>, defaultSeverity: string)

      }
      export class ValidationCandidate      {
        shape: model.domain.Shape
        payload: model.domain.PayloadFragment

        constructor(shape: model.domain.Shape, payload: model.domain.PayloadFragment)

      }
    }
    namespace remote {
      export class Content      {
        constructor(stream: string, url: string)
        constructor(stream: string, url: string, mime: string)

      }
      export class CachedReference      {
        url: string
        content: model.document.BaseUnit
        resolved: boolean

        constructor(url: string, content: model.document.BaseUnit, resolved: boolean)

      }
    }
    namespace validate {
      export class ValidationResult      {
        message: string
        level: string
        targetNode: string
        targetProperty: string
        validationId: string
        source: any
        lexical: core.parser.Range
        location: undefined | string

        constructor(message: string, level: string, targetNode: string, targetProperty: string, validationId: string, position: core.parser.Range, location: string)

      }
      export class PayloadParsingResult      {
        fragment: model.domain.PayloadFragment
        results: Array<ValidationResult>

        constructor(fragment: model.domain.PayloadFragment, results: Array<ValidationResult>)

      }
      export class ValidationReport      {
        conforms: boolean
        model: string
        profile: ProfileName
        results: Array<ValidationResult>
        toString: string

        constructor(conforms: boolean, model: string, profile: ProfileName, results: Array<ValidationResult>)

      }
    }
  }
  export class Raml10Resolver extends Resolver  {
    constructor()

  }
  export class Raml08Resolver extends Resolver  {
    constructor()

  }
  export class Oas30Resolver extends Resolver  {
    constructor()

  }
  export class Oas20Resolver extends Resolver  {
    constructor()

  }
  export class Async20Resolver extends Resolver  {
    constructor()

  }
  export class YamlPayloadRenderer extends render.Renderer  {
    constructor()
    constructor(env: client.environment.Environment)

  }
  export class WebApiDomainElementEmitter  {
    emitToBuilder<T>(element: model.domain.DomainElement, emissionStructure: core.Vendor, eh: resolve.ClientErrorHandler, builder: org.yaml.builder.JsOutputBuilder): void


  }
  export class Raml10Renderer extends render.Renderer  {
    constructor()
    constructor(env: client.environment.Environment)

  }
  export class Raml08Renderer extends render.Renderer  {
    constructor()
    constructor(env: client.environment.Environment)

  }
  export class Oas30Renderer extends render.Renderer  {
    constructor()
    constructor(env: client.environment.Environment)

  }
  export class Oas20Renderer extends render.Renderer  {
    constructor()
    constructor(env: client.environment.Environment)

  }
  export class JsonldRenderer extends render.Renderer  {
    constructor()
    constructor(env: client.environment.Environment)

  }
  export class JsonPayloadRenderer extends render.Renderer  {
    constructor()
    constructor(env: client.environment.Environment)

  }
  export class Async20Renderer extends render.Renderer  {
    constructor()

  }
  export class YamlPayloadParser extends parse.Parser  {
    constructor()
    constructor(environment: client.environment.Environment)

  }
  export class RamlParser extends parse.Parser  {
    constructor()
    constructor(environment: client.environment.Environment)

  }
  export class Raml10Parser extends parse.Parser  {
    constructor()
    constructor(environment: client.environment.Environment)

  }
  export class Raml08Parser extends parse.Parser  {
    constructor()
    constructor(environment: client.environment.Environment)

  }
  export class Oas30YamlParser extends parse.Parser  {
    constructor()
    constructor(environment: client.environment.Environment)

  }
  export class Oas30Parser extends parse.Parser  {
    constructor()
    constructor(environment: client.environment.Environment)

  }
  export class Oas20YamlParser extends parse.Parser  {
    constructor()
    constructor(environment: client.environment.Environment)

  }
  export class Oas20Parser extends parse.Parser  {
    constructor()
    constructor(environment: client.environment.Environment)

  }
  export class JsonPayloadParser extends parse.Parser  {
    constructor()
    constructor(environment: client.environment.Environment)

  }
  export class Async20Parser extends parse.Parser  {
    constructor()
    constructor(env: client.environment.Environment)

  }
  export class Async20JsonParser extends parse.Parser  {
    constructor()
    constructor(env: client.environment.Environment)

  }
  export class AMF  {
    init(): Promise<void>

    raml10Parser(): Raml10Parser

    ramlParser(): RamlParser

    raml10Generator(): Raml10Renderer

    raml08Parser(): Raml08Parser

    raml08Generator(): Raml08Renderer

    oas20Parser(): Oas20Parser

    oas20Generator(): Oas20Renderer

    amfGraphParser(): AmfGraphParser

    amfGraphGenerator(): AmfGraphRenderer

    validate(model: model.document.BaseUnit, profileName: ProfileName, messageStyle: amf.MessageStyle): Promise<client.validate.ValidationReport>

    validate(model: model.document.BaseUnit, profileName: ProfileName, messageStyle: amf.MessageStyle, env: client.environment.Environment): Promise<client.validate.ValidationReport>

    validateResolved(model: model.document.BaseUnit, profileName: ProfileName, messageStyle: amf.MessageStyle): Promise<client.validate.ValidationReport>

    validateResolved(model: model.document.BaseUnit, profileName: ProfileName, messageStyle: amf.MessageStyle, env: client.environment.Environment): Promise<client.validate.ValidationReport>

    loadValidationProfile(url: string): Promise<ProfileName>

    loadValidationProfile(url: string, env: client.environment.Environment): Promise<ProfileName>

    emitShapesGraph(profileName: ProfileName): string

    registerNamespace(alias: string, prefix: string): boolean

    registerDialect(url: string): Promise<model.document.Dialect>

    registerDialect(url: string, env: client.environment.Environment): Promise<model.document.Dialect>

    resolveRaml10(unit: model.document.BaseUnit): model.document.BaseUnit

    resolveRaml08(unit: model.document.BaseUnit): model.document.BaseUnit

    resolveOas20(unit: model.document.BaseUnit): model.document.BaseUnit

    resolveAmfGraph(unit: model.document.BaseUnit): model.document.BaseUnit

    jsonPayloadParser(): JsonPayloadParser

    yamlPayloadParser(): YamlPayloadParser


  }
  export class Core  {
    init(): Promise<void>

    parser(vendor: string, mediaType: string): parse.Parser

    parser(vendor: string, mediaType: string, env: client.environment.Environment): parse.Parser

    generator(vendor: string, mediaType: string): render.Renderer

    resolver(vendor: string): Resolver

    validate(model: model.document.BaseUnit, profileName: ProfileName, messageStyle: amf.MessageStyle): Promise<client.validate.ValidationReport>

    validate(model: model.document.BaseUnit, profileName: ProfileName, messageStyle: amf.MessageStyle, env: client.environment.Environment): Promise<client.validate.ValidationReport>

    loadValidationProfile(url: string): Promise<ProfileName>

    loadValidationProfile(url: string, env: client.environment.Environment): Promise<ProfileName>

    emitShapesGraph(profileName: ProfileName): string

    registerNamespace(alias: string, prefix: string): boolean

    registerPlugin(plugin: plugins.ClientAMFPlugin): void

    registerPayloadPlugin(plugin: plugins.ClientAMFPayloadValidationPlugin): void


  }
  export class ResolutionPipeline  {
    static readonly DEFAULT_PIPELINE: 'default'
    static readonly EDITING_PIPELINE: 'editing'
    static readonly COMPATIBILITY_PIPELINE: 'compatibility'
    static readonly CACHE_PIPELINE: 'cache'

  }
  export class ResourceNotFound  {
    readonly msj: string

    constructor(msj: string)

  }
  export class Resolver  {
    constructor(vendor: string)

    resolve(unit: model.document.BaseUnit): model.document.BaseUnit

    resolve(unit: model.document.BaseUnit, pipeline: string): model.document.BaseUnit

    resolve(unit: model.document.BaseUnit, pipeline: string, errorHandler: resolve.ClientErrorHandler): model.document.BaseUnit


  }
  export class AmfGraphResolver extends Resolver  {
    constructor()

  }
  export class AmfGraphRenderer extends render.Renderer  {
    constructor()
    constructor(env: client.environment.Environment)

    generateToBuilder<T>(unit: model.document.BaseUnit, builder: org.yaml.builder.JsOutputBuilder): Promise<void>

    generateToBuilder<T>(unit: model.document.BaseUnit, options: render.RenderOptions, builder: org.yaml.builder.JsOutputBuilder): Promise<void>


  }
  export class ValidationMode  {
    static readonly StrictValidationMode: ValidationMode
    static readonly ScalarRelaxedValidationMode: ValidationMode

  }
  export class AmfGraphParser extends parse.Parser  {
    constructor()
    constructor(environment: client.environment.Environment)

  }
  export class ProfileNames  {
    static readonly AMF: ProfileName
    static readonly OAS: ProfileName
    static readonly OAS20: ProfileName
    static readonly OAS30: ProfileName
    static readonly RAML: ProfileName
    static readonly RAML10: ProfileName
    static readonly RAML08: ProfileName
    static readonly ASYNC: ProfileName
    static readonly ASYNC20: ProfileName
    static readonly AML: ProfileName
    static readonly PAYLOAD: ProfileName

  }
  export class ProfileName  {
    profile: string
    messageStyle: amf.MessageStyle
    toString: string

    constructor(profile: string)

  }
  export class MessageStyles  {
    static readonly RAML: amf.MessageStyle
    static readonly OAS: amf.MessageStyle
    static readonly ASYNC: amf.MessageStyle
    static readonly AMF: amf.MessageStyle

  }
  export class JsServerHttpResourceLoader extends resource.BaseHttpResourceLoader  {
    constructor()

    fetch(resource: string): any


  }
  export class JsServerFileResourceLoader implements resource.BaseFileResourceLoader  {
    constructor()

    fetch(resource: string): Promise<client.remote.Content>

    accepts(resource: string): boolean

    fetchFile(resource: string): any

    ensureFileAuthority(str: string): string


  }
  export class JsBrowserHttpResourceLoader extends resource.BaseHttpResourceLoader  {
    constructor()

    fetch(resource: string): any


  }
  export class SHACLValidator  {
    constructor()

    validate(data: string, dataMediaType: string, shapes: string, shapesMediaType: string): any

    report(data: string, dataMediaType: string, shapes: string, shapesMediaType: string): any


  }
  export class AmlDomainElementEmitter  {
    emitToBuilder<T>(element: model.domain.DomainElement, emissionStructure: model.document.Dialect, eh: resolve.ClientErrorHandler, builder: org.yaml.builder.JsOutputBuilder): void


  }
  export class Aml10Renderer extends render.Renderer  {
    readonly mediaType: string

    constructor()
    constructor(mediaType: string)

  }
  export class Aml10Parser extends parse.Parser  {
    constructor()
    constructor(mediaType: string)
    constructor(env: client.environment.Environment)
    constructor(mediaType: string, env: client.environment.Environment)

  }
}