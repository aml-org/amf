declare module 'amf-client-js' {
  export class AMFObjectResult  {
    results: Array<AMFValidationResult>

  }
  export class AbstractPayload implements DomainElement, Linkable  {
    mediaType: StrField
    name: StrField
    customDomainProperties: Array<DomainExtension>
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    schema: Shape
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    link<T>(label: string): T

    linkCopy(): Linkable

    withName(name: string): this

    withScalarSchema(name: string): ScalarShape

    withArraySchema(name: string): ArrayShape

    withObjectSchema(name: string): NodeShape

    withSchema(schema: Shape): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withMediaType(mediaType: string): this

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withId(id: string): this

    annotations(): Annotations;


  }
  export class FinishedRenderingSyntaxEvent  {
    unit: BaseUnit

  }
  export interface JsErrorHandler  {
    report(result: AMFValidationResult): void

    getResults(): Array<AMFValidationResult>


  }
  export class BaseHttpResourceLoader implements ResourceLoader  {
    fetch(resource: string): Promise<Content>

    accepts(resource: string): boolean


  }
  export interface DialectInstanceUnit extends BaseUnit  {
    processingData: DialectInstanceProcessingData

    withProcessingData(data: DialectInstanceProcessingData): this

    definedBy(): StrField

    graphDependencies(): Array<StrField>

    withDefinedBy(dialectId: string): this

    withGraphDependencies(ids: Array<string>): this


  }
  export class StartingParsingEvent  {
    url: string

  }
  export class JsonSchemaDraft7 implements JSONSchemaVersion  {
  }
  export interface JsPayloadValidator  {
    validate(payload: string): Promise<AMFValidationReport>

    validate(payloadFragment: PayloadFragment): Promise<AMFValidationReport>

    syncValidate(payload: string): AMFValidationReport


  }
  export interface Shape extends DomainElement, Linkable  {
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
    isExtension: BoolField
    isStub: BoolField
    federationMetadata: ShapeFederationMetadata
    hasExplicitName: boolean

    annotations(): Annotations

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

    withIsExtension(value: boolean): this

    withIsStub(value: boolean): this

    withFederationMetadata(metadata: ShapeFederationMetadata): this


  }
  export class AMFParseResult extends AMFResult  {
    sourceSpec: Spec

  }
  export class ValidationProfile  {
    profileName(): ProfileName

    baseProfile(): undefined | ProfileName


  }
  export class StartingRenderToWriterEvent  {
  }
  export interface ChannelBinding extends DomainElement, Linkable  {
  }
  export interface ResourceLoader  {
    fetch(resource: string): Promise<Content>

    accepts(resource: string): boolean


  }
  export class AMFConfigurationState extends AMLConfigurationState  {
  }
  export interface ClientUnitCache  {
    fetch(url: string): Promise<CachedReference>


  }
  export class ValidatePayloadRequest  {
    shape: Shape
    mediaType: string
    config: ShapeValidationConfiguration

  }
  export interface JsAMFPlugin  {
    readonly ID: string

  }
  export interface AbstractRequest extends DomainElement  {
    queryParameters: Array<AbstractParameter>
    name: StrField

    withQueryParameters(parameters: Array<AbstractParameter>): this

    withQueryParameter(name: string): AbstractParameter

    withName(name: string): this


  }
  export interface BaseFileResourceLoader extends ResourceLoader  {
    fetch(resource: string): Promise<Content>

    fetchFile(resource: string): Promise<Content>

    accepts(resource: string): boolean


  }
  export class StartedTransformationStepEvent  {
  }
  export class BoolField implements ValueField<boolean>  {
    readonly option: undefined | boolean
    isNull: boolean
    nonNull: boolean

    toString(): string

    annotations(): Annotations

    value(): boolean

    remove(): void

    is(other: boolean): boolean

    is(accepts: undefined): boolean


  }
  export class AMLConfigurationState  {
    getDialects(): Array<Dialect>

    getDialect(name: string): Array<Dialect>

    getDialect(name: string, version: string): undefined | Dialect

    getExtensions(): Array<SemanticExtension>


  }
  export class AMFSemanticSchemaResult extends AMFParseResult  {
    baseUnit: Dialect
    vocabulary: undefined | Vocabulary

  }
  export class SkippedValidationPluginEvent  {
  }
  export class AMFConfiguration extends BaseShapesConfiguration  {
    baseUnitClient(): AMFBaseUnitClient

    elementClient(): AMFElementClient

    configurationState(): AMFConfigurationState

    withParsingOptions(parsingOptions: ParsingOptions): AMFConfiguration

    withResourceLoader(rl: ResourceLoader): AMFConfiguration

    withResourceLoaders(rl: Array<ResourceLoader>): AMFConfiguration

    withUnitCache(cache: UnitCache): AMFConfiguration

    withTransformationPipeline(pipeline: TransformationPipeline): AMFConfiguration

    withRenderOptions(renderOptions: RenderOptions): AMFConfiguration

    withErrorHandlerProvider(provider: ErrorHandlerProvider): AMFConfiguration

    withEventListener(listener: AMFEventListener): AMFConfiguration

    withDialect(dialect: Dialect): AMFConfiguration

    withDialect(url: string): Promise<AMFConfiguration>

    forInstance(url: string): Promise<AMFConfiguration>

    withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): AMFConfiguration


  }
  export interface JsAMFEventListener  {
    notifyEvent(event: AMFEvent): void


  }
  export class FinishedValidationEvent  {
    result: AMFValidationReport

  }
  export interface BaseUnit  {
    id: string
    raw: undefined | string
    location: string
    usage: StrField
    modelVersion: StrField
    sourceSpec: undefined | Spec
    processingData: BaseUnitProcessingData
    sourceInformation: BaseUnitSourceInformation

    references(): Array<BaseUnit>

    pkg(): StrField

    withReferences(references: Array<BaseUnit>): this

    withPkg(pkg: string): this

    withId(id: string): this

    withRaw(raw: string): this

    withLocation(location: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | DomainElement

    findByType(typeId: string): Array<DomainElement>

    cloneUnit(): BaseUnit

    withReferenceAlias(alias: string, id: string, fullUrl: string, relativeUrl: string): BaseUnit

    withProcessingData(data: BaseUnitProcessingData): this


  }
  export class AMLDialectInstanceResult extends AMFResult  {
    dialectInstance: DialectInstance

  }
  export interface OperationBinding extends DomainElement, Linkable  {
  }
  export class FinishedParsingEvent  {
    url: string
    unit: BaseUnit

  }
  export interface EncodesModel  {
    encodes: DomainElement

    withEncodes(encoded: DomainElement): this


  }
  export class AMLBaseUnitClient extends BaseAMLBaseUnitClient  {
    getConfiguration(): AMLConfiguration


  }
  export class JsonSchemaDraft4 implements JSONSchemaVersion  {
  }
  export interface AMFShapePayloadValidationPlugin  {
    priority: PluginPriority

    applies(element: ValidatePayloadRequest): boolean

    validator(shape: Shape, mediaType: string, config: ShapeValidationConfiguration, validationMode: ValidationMode): AMFShapePayloadValidator


  }
  export class DataArrangeShape extends AnyShape  {
    minItems: IntField
    maxItems: IntField
    uniqueItems: BoolField

    withMinItems(minItems: number): this

    withMaxItems(maxItems: number): this

    withUniqueItems(uniqueItems: boolean): this


  }
  export class BaseAMLConfiguration extends AMFGraphConfiguration  {
    withParsingOptions(parsingOptions: ParsingOptions): BaseAMLConfiguration

    withRenderOptions(renderOptions: RenderOptions): BaseAMLConfiguration

    withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseAMLConfiguration

    withResourceLoader(rl: ResourceLoader): BaseAMLConfiguration

    withResourceLoaders(rl: Array<ResourceLoader>): BaseAMLConfiguration

    withUnitCache(cache: UnitCache): BaseAMLConfiguration

    withTransformationPipeline(pipeline: TransformationPipeline): BaseAMLConfiguration

    withEventListener(listener: AMFEventListener): BaseAMLConfiguration

    withDialect(dialect: Dialect): BaseAMLConfiguration


  }
  export class AMFGraphBaseUnitClient  {
    getConfiguration(): AMFGraphConfiguration

    parse(url: string): Promise<AMFParseResult>

    parseContent(content: string): Promise<AMFParseResult>

    parseContent(content: string, mediaType: string): Promise<AMFParseResult>

    transform(baseUnit: BaseUnit): AMFResult

    transform(baseUnit: BaseUnit, pipeline: string): AMFResult

    render(baseUnit: BaseUnit): string

    render(baseUnit: BaseUnit, mediaType: string): string

    renderGraphToBuilder<T>(baseUnit: BaseUnit, builder: org.yaml.builder.JsOutputBuilder): T

    validate(baseUnit: BaseUnit): Promise<AMFValidationReport>

    setBaseUri(unit: BaseUnit, base: string): void


  }
  export interface UnitCache  {
    fetch(url: string): Promise<CachedReference>


  }
  export class IntField implements ValueField<number>  {
    readonly option: undefined | number
    isNull: boolean
    nonNull: boolean

    toString(): string

    annotations(): Annotations

    value(): number

    remove(): void

    is(other: number): boolean

    is(accepts: undefined): boolean


  }
  export class AnyField implements ValueField<any>  {
    readonly option: undefined | any
    isNull: boolean
    nonNull: boolean

    toString(): string

    annotations(): Annotations

    value(): any

    remove(): void

    is(other: any): boolean

    is(accepts: undefined): boolean


  }
  export interface Annotable  {
    annotations(): Annotations


  }
  export class AbstractDeclaration implements DomainElement, Linkable  {
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

    annotations(): Annotations

    link<T>(label: string): T

    linkCopy(): AbstractDeclaration

    withName(name: string): this

    withDescription(description: string): this

    withDataNode(dataNode: DataNode): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withVariables(variables: Array<string>): this

    withId(id: string): this


  }
  export class AMFLibraryResult extends AMFResult  {
    library: Module

  }
  export interface ClientWriter  {
    append(s: string): this

    string(): string

    flush(): this

    close(): this


  }
  export class AMFElementClient extends BaseShapesElementClient  {
    getConfiguration(): AMFConfiguration

    renderToBuilder<T>(element: DomainElement, builder: org.yaml.builder.JsOutputBuilder): void

    asEndpoint<T>(unit: T, rt: ResourceType, profile: ProfileName): EndPoint

    asOperation<T>(unit: T, tr: Trait, profile: ProfileName): Operation


  }
  export interface JsAMFPayloadValidationPlugin extends JsAMFPlugin  {
    id: string

    applies(element: ValidatePayloadRequest): boolean

    validator(shape: Shape, mediaType: string, config: ShapeValidationConfiguration, validationMode: ValidationMode): JsPayloadValidator


  }
  export class Unspecified implements JSONSchemaVersion  {
  }
  export class BaseShapesConfiguration extends BaseAMLConfiguration  {
    withParsingOptions(parsingOptions: ParsingOptions): BaseShapesConfiguration

    withRenderOptions(renderOptions: RenderOptions): BaseShapesConfiguration

    withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseShapesConfiguration

    withResourceLoader(rl: ResourceLoader): BaseShapesConfiguration

    withResourceLoaders(rl: Array<ResourceLoader>): BaseShapesConfiguration

    withUnitCache(cache: UnitCache): BaseShapesConfiguration

    withTransformationPipeline(pipeline: TransformationPipeline): BaseShapesConfiguration

    withEventListener(listener: AMFEventListener): BaseShapesConfiguration

    withDialect(dialect: Dialect): BaseShapesConfiguration


  }
  export class BaseAMLElementClient extends AMFGraphElementClient  {
    renderToBuilder<T>(element: DomainElement, builder: org.yaml.builder.JsOutputBuilder): void


  }
  export class UnitCacheHitEvent  {
  }
  export interface TransformationStep  {
    transform(model: BaseUnit, errorHandler: ClientErrorHandler, configuration: AMFGraphConfiguration): BaseUnit


  }
  export class DoubleField implements ValueField<number>  {
    readonly option: undefined | number
    isNull: boolean
    nonNull: boolean

    toString(): string

    annotations(): Annotations

    value(): number

    remove(): void

    is(other: number): boolean

    is(accepts: undefined): boolean


  }
  export class AMFResult extends AMFObjectResult  {
    conforms: boolean
    results: Array<AMFValidationResult>
    baseUnit: BaseUnit
    toString: string

    merge(report: AMFValidationReport): AMFResult


  }
  export interface ServerBinding extends DomainElement, Linkable  {
  }
  export interface Linkable  {
    linkTarget: undefined | DomainElement
    isLink: boolean
    linkLabel: StrField

    linkCopy(): Linkable

    withLinkTarget(target: undefined): this

    withLinkLabel(label: string): this

    link<T>(): T

    link<T>(label: string): T


  }
  export class FloatField implements ValueField<number>  {
    readonly option: undefined | number
    isNull: boolean
    nonNull: boolean

    toString(): string

    annotations(): Annotations

    value(): number

    remove(): void

    is(other: number): boolean

    is(accepts: undefined): boolean


  }
  export interface ClientResourceLoader  {
    fetch(resource: string): Promise<Content>

    accepts(resource: string): boolean


  }
  export class Path  {
    static sep: string
    static delimiter: string

    static normalize(p: string): string

    static join(paths: undefined): string

    static resolve(pathSegments: undefined): string

    static isAbsolute(path: string): boolean

    static relative(from: string, to: string): string

    static dirname(p: string): string

    static basename(p: string, ext: string): string

    static extname(p: string): string


  }
  export interface ParametrizedDeclaration extends DomainElement  {
    name: StrField
    target: AbstractDeclaration
    variables: Array<VariableValue>

    withName(name: string): this

    withTarget(target: AbstractDeclaration): this

    withVariables(variables: Array<VariableValue>): this


  }
  export class StartingContentParsingEvent  {
    url: string
    content: Content

  }
  export interface JSONSchemaVersion  {
  }
  export interface AMFShapePayloadValidator  {
    validate(payload: string): Promise<AMFValidationReport>

    validate(payloadFragment: PayloadFragment): Promise<AMFValidationReport>

    syncValidate(payload: string): AMFValidationReport


  }
  export class AMFBaseUnitClient extends BaseAMLBaseUnitClient  {
    getConfiguration(): AMFConfiguration

    parseDocument(url: string): Promise<AMFDocumentResult>

    parseLibrary(url: string): Promise<AMFLibraryResult>


  }
  export class FinishedValidationPluginEvent  {
    result: AMFValidationReport

  }
  export class SemanticBaseUnitClient extends AMLBaseUnitClient  {
    parseSemanticSchema(url: string): Promise<AMFSemanticSchemaResult>

    parseSemanticSchemaContent(content: string): Promise<AMFSemanticSchemaResult>


  }
  export interface ValidationResult  {
    readonly keyword: string
    readonly dataPath: string
    readonly schemaPath: string
    readonly params: undefined
    readonly message: string

  }
  export interface AMFEvent  {
    readonly name: string

  }
  export class AMFDocumentResult extends AMFResult  {
    document: Document

  }
  export class AbstractOperation implements DomainElement  {
    method: StrField
    name: StrField
    customDomainProperties: Array<DomainExtension>
    request: AbstractRequest
    description: StrField
    isExternalLink: BoolField
    response: AbstractResponse
    id: string
    position: Range
    extendsNode: Array<DomainElement>
    responses: Array<AbstractResponse>

    withResponses(responses: Array<AbstractResponse>): this

    withName(name: string): this

    withMethod(method: string): this

    withDescription(description: string): this

    withRequest(name: string): AbstractRequest

    withRequest(request: AbstractRequest): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withResponse(name: string): AbstractResponse

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this

    annotations(): Annotations;


  }
  export class DetectedSyntaxMediaTypeEvent  {
  }
  export class RecursiveShape implements Shape  {
    defaultValueStr: StrField
    displayName: StrField
    name: StrField
    customDomainProperties: Array<DomainExtension>
    isExtension: BoolField
    xone: Array<Shape>
    readOnly: BoolField
    isStub: BoolField
    description: StrField
    fixpoint: StrField
    deprecated: BoolField
    customShapePropertyDefinitions: Array<PropertyShape>
    or: Array<Shape>
    elseShape: Shape
    linkTarget: undefined | DomainElement
    hasExplicitName: boolean
    isLink: boolean
    isExternalLink: BoolField
    customShapeProperties: Array<ShapeExtension>
    thenShape: Shape
    federationMetadata: ShapeFederationMetadata
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

    annotations(): Annotations

    link<T>(label: string): T

    withValues(values: Array<DataNode>): this

    linkCopy(): Linkable

    withOr(subShapes: Array<Shape>): this

    withName(name: string): this

    withDescription(description: string): this

    withIf(ifShape: Shape): this

    withIsExtension(value: boolean): this

    withCustomShapePropertyDefinition(name: string): PropertyShape

    withIsStub(value: boolean): this

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

    withFederationMetadata(metadata: ShapeFederationMetadata): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

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
  export class FoundReferencesEvent  {
    root: string
    amount: number

  }
  export class AMFGraphElementClient  {
    getConfiguration(): AMFGraphConfiguration

    payloadValidatorFor(shape: Shape, mediaType: string, mode: ValidationMode): AMFShapePayloadValidator

    payloadValidatorFor(shape: Shape, fragment: PayloadFragment): AMFShapePayloadValidator


  }
  export interface MessageBinding extends DomainElement, Linkable  {
  }
  export class Ajv  {
    readonly errors: undefined

    validate(schema: undefined, data: undefined): boolean

    addMetaSchema(metaSchema: undefined): Ajv

    addFormat(name: string, formatValidator: any): Ajv


  }
  export class StartingRenderingEvent  {
    unit: BaseUnit
    mediaType: undefined | string

  }
  export class JsonSchemaDraft201909 implements JSONSchemaVersion  {
  }
  export class AMLElementClient extends BaseAMLElementClient  {
    renderToBuilder<T>(element: DomainElement, builder: org.yaml.builder.JsOutputBuilder): void

    getConfiguration(): AMLConfiguration


  }
  export class FinishedTransformationEvent  {
    unit: BaseUnit

  }
  export class StrField implements ValueField<string>  {
    isNull: boolean
    nonNull: boolean
    readonly option: undefined | string
    nonEmpty: boolean
    isNullOrEmpty: boolean

    toString(): string

    annotations(): Annotations

    value(): string

    remove(): void

    is(other: string): boolean

    is(accepts: undefined): boolean


  }
  export class AMLVocabularyResult extends AMFResult  {
    vocabulary: Vocabulary

  }
  export class AMLDialectResult extends AMFResult  {
    dialect: Dialect

  }
  export class AnyMapping implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    annotations(): Annotations

    withIfMapping(ifMapping: string): AnyMapping

    or(): Array<StrField>

    ifMapping(): StrField

    and(): Array<StrField>

    components(): Array<StrField>

    withComponents(components: Array<string>): AnyMapping

    graph(): Graph

    thenMapping(): StrField

    withOr(orMappings: Array<string>): AnyMapping

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withThenMapping(thenMapping: string): AnyMapping

    elseMapping(): StrField

    withElseMapping(elseMapping: string): AnyMapping

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withAnd(andMappings: Array<string>): AnyMapping

    withId(id: string): this


  }
  export interface JsTransformationStep  {
    transform(model: BaseUnit, errorHandler: ClientErrorHandler, configuration: AMFGraphConfiguration): BaseUnit


  }
  export interface ClientErrorHandler  {
    getResults: Array<AMFValidationResult>

    report(result: AMFValidationResult): void


  }
  export class ScalarRelaxedValidationMode extends ValidationMode  {
  }
  export class FinishedTransformationStepEvent  {
    step: TransformationStep
    index: number

  }
  export class Fragment implements BaseUnit, EncodesModel  {
    location: string
    usage: StrField
    id: string
    raw: undefined | string
    processingData: BaseUnitProcessingData
    sourceSpec: undefined | Spec
    sourceInformation: BaseUnitSourceInformation
    modelVersion: StrField
    encodes: DomainElement

    findByType(typeId: string): Array<DomainElement>

    cloneUnit(): BaseUnit

    withReferences(references: Array<BaseUnit>): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, id: string, fullUrl: string, relativeUrl: string): BaseUnit

    withEncodes(encoded: DomainElement): this

    pkg(): StrField

    withPkg(pkg: string): this

    references(): Array<BaseUnit>

    withProcessingData(data: BaseUnitProcessingData): this

    withId(id: string): this


  }
  export class BaseShapesElementClient extends BaseAMLElementClient  {
    toJsonSchema(element: AnyShape): string

    buildJsonSchema(element: AnyShape): string

    toRamlDatatype(element: AnyShape): string

    renderExample(example: Example, mediaType: string): string

    renderToBuilder<T>(element: DomainElement, builder: org.yaml.builder.JsOutputBuilder): void


  }
  export interface DomainElement extends CustomizableElement  {
    customDomainProperties: Array<DomainExtension>
    extendsNode: Array<DomainElement>
    id: string
    position: Range
    isExternalLink: BoolField

    annotations(): Annotations

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withId(id: string): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    graph(): Graph


  }
  export class StartingTransformationEvent  {
    pipeline: TransformationPipeline

  }
  export class PropertyTerm implements DomainElement  {
    displayName: StrField
    name: StrField
    customDomainProperties: Array<DomainExtension>
    description: StrField
    subPropertyOf: Array<StrField>
    isExternalLink: BoolField
    id: string
    range: StrField
    position: Range
    extendsNode: Array<DomainElement>

    annotations(): Annotations

    withName(name: string): PropertyTerm

    withDescription(description: string): PropertyTerm

    graph(): Graph

    withSubClasOf(superProperties: Array<string>): PropertyTerm

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withDisplayName(displayName: string): PropertyTerm

    withId(id: string): this

    withRange(range: string): PropertyTerm


  }
  export interface CustomizableElement  {
    customDomainProperties: Array<DomainExtension>

    withCustomDomainProperties(extensions: Array<DomainExtension>): this


  }
  export class BaseAMLBaseUnitClient extends AMFGraphBaseUnitClient  {
    parseDialect(url: string): Promise<AMLDialectResult>

    parseDialectInstance(url: string): Promise<AMLDialectInstanceResult>

    parseVocabulary(url: string): Promise<AMLVocabularyResult>


  }
  export class SelectedParsePluginEvent  {
  }
  export class Graph  {
    types(): Array<string>

    properties(): Array<string>

    containsProperty(uri: string): boolean

    getObjectByProperty(uri: string): Array<DomainElement>

    scalarByProperty(uri: string): Array<any>

    removeField(uri: string): this


  }
  export interface TransformationPipeline  {
    readonly name: string
    steps: Array<TransformationStep>

  }
  export class JsPath  {
    static readonly sep: string

  }
  export interface Stats  {
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
  export class StartingValidationEvent  {
    totalPlugins: number

  }
  export interface AbstractResponse extends DomainElement  {
    payload: AbstractPayload
    name: StrField

    withPayload(payload: AbstractPayload): AbstractPayload

    withName(name: string): this


  }
  export interface KeyMapping extends DomainElement  {
    source: any
    target: any

    withSource(source: any): this

    withTarget(target: any): this


  }
  export class StrictValidationMode extends ValidationMode  {
  }
  export class AbstractParameter implements DomainElement  {
    annotations(): Annotations
    name: StrField
    binding: StrField
    customDomainProperties: Array<DomainExtension>
    description: StrField
    isExternalLink: BoolField
    id: string
    schema: Shape
    parameterName: StrField
    position: Range
    required: BoolField
    defaultValue: DataNode
    extendsNode: Array<DomainElement>

    withDefaultValue(defaultValue: DataNode): this

    withName(name: string): this

    withScalarSchema(name: string): ScalarShape

    withDescription(description: string): this

    withObjectSchema(name: string): NodeShape

    withSchema(schema: Shape): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    cloneParameter(parent: string): this

    withRequired(required: boolean): this

    withId(id: string): this

    withParameterName(name: string): this

    withBinding(binding: string): this


  }
  export class ShapesElementClient extends BaseShapesElementClient  {
    getConfiguration(): ShapesConfiguration


  }
  export class AbstractElementTransformer  {
    static asEndpoint<T>(unit: T, rt: ResourceType, errorHandler: ClientErrorHandler, configuration: AMFGraphConfiguration, profile: ProfileName): EndPoint

    static asOperation<T>(unit: T, tr: Trait, errorHandler: ClientErrorHandler, configuration: AMFGraphConfiguration, profile: ProfileName): Operation


  }
  export interface ValueField<T> extends Annotable  {
    readonly option: undefined | T
    isNull: boolean
    nonNull: boolean

    toString(): string

    value(): T

    is(other: T): boolean

    is(accepts: undefined): boolean

    remove(): void


  }
  export class ParsedModelEvent  {
    url: string
    unit: BaseUnit

  }
  export interface DataNode extends DomainElement  {
    name: StrField

    withName(name: string): this


  }
  export class Api<A> implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    endPoints: Array<EndPoint>
    provider: Organization
    security: Array<SecurityRequirement>
    identifier: StrField
    description: StrField
    tags: Array<Tag>
    documentations: Array<CreativeWork>
    servers: Array<Server>
    schemes: Array<StrField>
    license: License
    isExternalLink: BoolField
    termsOfService: StrField
    version: StrField
    id: string
    contentType: Array<StrField>
    accepts: Array<StrField>
    position: Range
    extendsNode: Array<DomainElement>

    annotations(): Annotations

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
  export interface AMFEventListener  {
    notifyEvent(event: AMFEvent): void


  }
  export class ParsedSyntaxEvent  {
    url: string
    content: Content

  }
  export interface DeclaresModel  {
    declares: Array<DomainElement>

    withDeclaredElement(declared: DomainElement): this

    withDeclares(declares: Array<DomainElement>): this


  }
  export class FinishedRenderingASTEvent  {
    unit: BaseUnit

  }
  export interface MessageStyle  {
    profileName: ProfileName

  }
  export interface ErrorHandlerProvider  {
    errorHandler(): ClientErrorHandler


  }
  export interface Spec  {
    readonly id: string
    isRaml: boolean
    isOas: boolean
    isAsync: boolean
    readonly mediaType: string

  }
  export interface ValidationMode  {
  }
  namespace org {
    namespace mulesoft {
      namespace common {
        namespace io {
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

          static apply(): JsOutputBuilder


        }
      }
    }
  }
  export class AMFTransformer  {
    static transform(unit: BaseUnit, configuration: AMFGraphConfiguration): AMFResult

    static transform(unit: BaseUnit, pipelineName: string, configuration: AMFGraphConfiguration): AMFResult


  }
  export class Message implements DomainElement, Linkable  {
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
    position: Range
    correlationId: CorrelationId
    isAbstract: BoolField
    title: StrField
    linkLabel: StrField
    extendsNode: Array<DomainElement>
    headerExamples: Array<Example>
    summary: StrField

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

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

    link<T>(): T

    withLinkTarget(target: undefined): this

    withPayload(mediaType: undefined | string): Payload

    withDisplayName(displayName: string): this

    withCorrelationId(correlationId: CorrelationId): this

    withId(id: string): this

    withDocumentation(documentation: CreativeWork): this


  }
  export class ApiKeySettings extends Settings  {
    name: StrField
    in: StrField

    constructor()

    withName(name: string): this

    withIn(inVal: string): this


  }
  export class AnnotationMapping implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    maximum(): DoubleField

    typeDiscriminator(): Map<string, string>

    withEnum(values: Array<any>): AnnotationMapping

    enum(): Array<AnyField>

    minCount(): IntField

    withName(name: string): AnnotationMapping

    literalRange(): StrField

    externallyLinkable(): BoolField

    withTypeDiscriminatorName(name: string): AnnotationMapping

    withDomain(domains: Array<string>): AnnotationMapping

    sorted(): BoolField

    minimum(): DoubleField

    pattern(): StrField

    graph(): Graph

    withLiteralRange(range: string): AnnotationMapping

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withObjectRange(range: Array<string>): AnnotationMapping

    objectRange(): Array<StrField>

    domain(): Array<StrField>

    withPattern(pattern: string): AnnotationMapping

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withSorted(sorted: boolean): AnnotationMapping

    withNodePropertyMapping(propertyId: string): AnnotationMapping

    allowMultiple(): BoolField

    withMinimum(min: number): AnnotationMapping

    withMaximum(max: number): AnnotationMapping

    nodePropertyMapping(): StrField

    typeDiscriminatorName(): StrField

    name(): StrField

    withAllowMultiple(allow: boolean): AnnotationMapping

    withExternallyLinkable(linkable: boolean): this

    withId(id: string): this

    withTypeDiscriminator(typesMapping: Map<string, string>): AnnotationMapping

    withMinCount(minCount: number): AnnotationMapping


  }
  export class DialectLibrary implements BaseUnit, DeclaresModel  {
    location: string
    usage: StrField
    id: string
    raw: undefined | string
    processingData: BaseUnitProcessingData
    sourceSpec: undefined | Spec
    sourceInformation: BaseUnitSourceInformation
    modelVersion: StrField
    declares: Array<DomainElement>
    externals: Array<External>

    constructor()

    findByType(typeId: string): Array<DomainElement>

    cloneUnit(): BaseUnit

    withExternals(externals: Array<External>): DialectLibrary

    withReferences(references: Array<BaseUnit>): this

    withDeclaredElement(declared: DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, id: string, fullUrl: string, relativeUrl: string): BaseUnit

    withNodeMappings(nodeMappings: Array<NodeMapping>): DialectLibrary

    pkg(): StrField

    withPkg(pkg: string): this

    withDeclares(declares: Array<DomainElement>): this

    nodeMappings(): Array<NodeMapping>

    references(): Array<BaseUnit>

    withProcessingData(data: BaseUnitProcessingData): this

    withId(id: string): this


  }
  export class TupleShape extends DataArrangeShape  {
    items: Array<Shape>
    closedItems: BoolField
    additionalItemsSchema: Shape

    constructor()

    withItems(items: Array<Shape>): this

    withClosedItems(closedItems: boolean): this

    linkCopy(): TupleShape


  }
  export class PropertyKeyMapping implements KeyMapping  {
    source: PropertyShape
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    target: string
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withSource(source: PropertyShape): this

    withTarget(target: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class DialectInstanceProcessingData extends BaseUnitProcessingData  {
    constructor()

    definedBy(): StrField

    graphDependencies(): Array<StrField>

    withDefinedBy(dialectId: string): DialectInstanceProcessingData

    withGraphDependencies(ids: Array<string>): DialectInstanceProcessingData


  }
  export class ValidationCandidate  {
    shape: Shape
    payload: PayloadFragment

    constructor(shape: Shape, payload: PayloadFragment)

  }
  export class DialectFragment implements BaseUnit, EncodesModel  {
    location: string
    usage: StrField
    id: string
    raw: undefined | string
    processingData: BaseUnitProcessingData
    sourceSpec: undefined | Spec
    sourceInformation: BaseUnitSourceInformation
    modelVersion: StrField
    encodes: NodeMapping
    externals: Array<External>

    constructor()

    findByType(typeId: string): Array<DomainElement>

    cloneUnit(): BaseUnit

    withExternals(externals: Array<External>): DialectFragment

    withReferences(references: Array<BaseUnit>): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, id: string, fullUrl: string, relativeUrl: string): BaseUnit

    withEncodes(encoded: DomainElement): this

    withEncodes(nodeMapping: NodeMapping): DialectFragment

    pkg(): StrField

    withPkg(pkg: string): this

    references(): Array<BaseUnit>

    withProcessingData(data: BaseUnitProcessingData): this

    withId(id: string): this


  }
  export class FileShape extends AnyShape  {
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
  export class RamlShapeRenderer  {
    static toRamlDatatype(element: AnyShape, config: AMFGraphConfiguration): string


  }
  export class NamedExample extends Fragment  {
    constructor()

  }
  export class ObjectPropertyTerm extends PropertyTerm  {
    constructor()

  }
  export class SecurityScheme implements DomainElement, Linkable  {
    displayName: StrField
    name: StrField
    customDomainProperties: Array<DomainExtension>
    description: StrField
    queryString: Shape
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    queryParameters: Array<Parameter>
    headers: Array<Parameter>
    type: StrField
    linkLabel: StrField
    extendsNode: Array<DomainElement>
    settings: Settings
    responses: Array<Response>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

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

    link<T>(): T

    withLinkTarget(target: undefined): this

    withHttpApiKeySettings(): HttpApiKeySettings

    withDisplayName(displayName: string): this

    withSettings(settings: Settings): this

    withType(type: string): this

    withId(id: string): this

    withApiKeySettings(): ApiKeySettings


  }
  export class UnionShape extends AnyShape  {
    anyOf: Array<Shape>
    serializationSchema: Shape

    constructor()

    withAnyOf(anyOf: Array<Shape>): UnionShape

    withSerializationSchema(schema: Shape): this


  }
  export class OpenIdConnectSettings extends Settings  {
    url: StrField
    scopes: Array<Scope>

    constructor()

    withUrl(url: string): this

    withScopes(scopes: Array<Scope>): this


  }
  export class ParsingOptions  {
    isAmfJsonLdSerialization: boolean
    definedBaseUrl: undefined | string
    getMaxYamlReferences: undefined | number
    getMaxJSONComplexity: undefined | number

    constructor()

    withoutAmfJsonLdSerialization(): ParsingOptions

    withAmfJsonLdSerialization(): ParsingOptions

    withBaseUnitUrl(baseUnit: string): ParsingOptions

    withoutBaseUnitUrl(): ParsingOptions

    setMaxYamlReferences(value: number): ParsingOptions

    setMaxJSONComplexity(value: number): ParsingOptions


  }
  export class Response extends Message implements AbstractResponse {
    statusCode: StrField
    name: StrField
    customDomainProperties: Array<DomainExtension>
    payload: Payload
    isExternalLink: BoolField
    links: Array<TemplatedLink>
    id: string
    position: Range
    headers: Array<Parameter>
    extendsNode: Array<DomainElement>

    constructor()

    withStatusCode(statusCode: string): this

    withHeaders(headers: Array<Parameter>): this

    withStatusCode(statusCode: string): this

    // @ts-ignore
    linkCopy(): Response

    withName(name: string): this

    graph(): Graph

    withPayload(payload: Payload): Payload

    withPayload(): Payload

    withPayload(mediaType: string): Payload

    withPayload(mediaType: undefined | string): Payload

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withHeader(name: string): Parameter

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLinks(links: Array<TemplatedLink>): this

    withId(id: string): this


  }
  export class GraphQLConfiguration  {
    static GraphQL(): AMFConfiguration


  }
  export class GraphQLFederationConfiguration  {
    static GraphQLFederation(): AMFConfiguration


  }
  export class Document implements BaseUnit, EncodesModel, DeclaresModel  {
    location: string
    usage: StrField
    id: string
    raw: undefined | string
    processingData: BaseUnitProcessingData
    sourceSpec: undefined | Spec
    sourceInformation: BaseUnitSourceInformation
    modelVersion: StrField
    encodes: DomainElement
    declares: Array<DomainElement>

    constructor()
    constructor(encoding: DomainElement)

    findByType(typeId: string): Array<DomainElement>

    cloneUnit(): BaseUnit

    withReferences(references: Array<BaseUnit>): this

    withDeclaredElement(declared: DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, id: string, fullUrl: string, relativeUrl: string): BaseUnit

    withEncodes(encoded: DomainElement): this

    pkg(): StrField

    withPkg(pkg: string): this

    withDeclares(declares: Array<DomainElement>): this

    references(): Array<BaseUnit>

    withProcessingData(data: BaseUnitProcessingData): this

    withId(id: string): this


  }

  export class JsonSchemaDocument implements Document {
    location: string
    usage: StrField
    id: string
    raw: undefined | string
    processingData: BaseUnitProcessingData
    sourceSpec: undefined | Spec
    sourceInformation: BaseUnitSourceInformation
    modelVersion: StrField
    encodes: DomainElement
    declares: Array<DomainElement>
    schemaVersion: StrField

    constructor()
    constructor(encoding: DomainElement)

    findByType(typeId: string): Array<DomainElement>

    cloneUnit(): BaseUnit

    withReferences(references: Array<BaseUnit>): this

    withDeclaredElement(declared: DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, id: string, fullUrl: string, relativeUrl: string): BaseUnit

    withEncodes(encoded: DomainElement): this

    pkg(): StrField

    withPkg(pkg: string): this

    withDeclares(declares: Array<DomainElement>): this

    references(): Array<BaseUnit>

    withProcessingData(data: BaseUnitProcessingData): this

    withId(id: string): this



  }

  export class Amqp091OperationBinding implements OperationBinding  {
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
    position: Range
    cc: Array<StrField>
    userId: StrField
    linkLabel: StrField
    expiration: IntField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

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

    link<T>(): T

    withLinkTarget(target: undefined): this

    withUserId(userId: string): this

    withAck(ack: boolean): this

    withPriority(priority: number): this

    withExpiration(expiration: number): this

    withId(id: string): this


  }
  export class ObjectNode implements DataNode  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    properties: Map<string, DataNode>
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    getProperty(property: string): undefined | DataNode

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this

    addProperty(property: string, node: DataNode): this


  }
  export class ValidationShapeSet  {
    candidates: Array<ValidationCandidate>
    defaultSeverity: string

    constructor(candidates: Array<ValidationCandidate>, closure: Array<Shape>, defaultSeverity: string)

  }
  export class TransformationStepFactory  {
    static from(step: JsTransformationStep): TransformationStep


  }
  export class PropertyShape implements Shape  {
    defaultValueStr: StrField
    displayName: StrField
    name: StrField
    serializationOrder: IntField
    customDomainProperties: Array<DomainExtension>
    isExtension: BoolField
    path: StrField
    xone: Array<Shape>
    readOnly: BoolField
    isStub: BoolField
    description: StrField
    provides: Array<PropertyShapePath>
    deprecated: BoolField
    customShapePropertyDefinitions: Array<PropertyShape>
    or: Array<Shape>
    elseShape: Shape
    linkTarget: undefined | DomainElement
    maxCount: IntField
    hasExplicitName: boolean
    isLink: boolean
    isExternalLink: BoolField
    customShapeProperties: Array<ShapeExtension>
    thenShape: Shape
    federationMetadata: ShapeFederationMetadata
    id: string
    range: Shape
    ifShape: Shape
    writeOnly: BoolField
    patternName: StrField
    not: Shape
    values: Array<DataNode>
    position: Range
    requires: Array<PropertyShapePath>
    inherits: Array<Shape>
    linkLabel: StrField
    defaultValue: DataNode
    extendsNode: Array<DomainElement>
    and: Array<Shape>
    minCount: IntField

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    withRequires(requires: Array<PropertyShapePath>): this

    withValues(values: Array<DataNode>): this

    withPath(path: string): this

    linkCopy(): PropertyShape

    withOr(subShapes: Array<Shape>): this

    withName(name: string): this

    withRange(range: Shape): this

    withDescription(description: string): this

    withMaxCount(max: number): this

    withIf(ifShape: Shape): this

    withIsExtension(value: boolean): this

    withSerializationOrder(order: number): this

    withCustomShapePropertyDefinition(name: string): PropertyShape

    withIsStub(value: boolean): this

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

    withFederationMetadata(metadata: ShapeFederationMetadata): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withProvides(provides: Array<PropertyShapePath>): this

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
  export class MessageStyles  {
    static readonly RAML: MessageStyle
    static readonly OAS: MessageStyle
    static readonly ASYNC: MessageStyle
    static readonly AMF: MessageStyle

  }
  export class PublicNodeMapping implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): PublicNodeMapping

    mappedNode(): StrField

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withMappedNode(mappedNode: string): PublicNodeMapping

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    name(): StrField

    withId(id: string): this


  }
  export class Scope implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    description: StrField
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    withDescription(description: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class HighPriority extends PluginPriority  {
  }
  export class AMFEventListenerFactory  {
    static from(listener: JsAMFEventListener): AMFEventListener


  }
  export class ShapeExtension implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    definedBy: PropertyShape
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>
    extension: DataNode

    constructor()

    annotations(): Annotations

    withDefinedBy(definedBy: PropertyShape): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withExtension(extension: DataNode): this

    withId(id: string): this


  }
  export class TraitFragment extends Fragment  {
    constructor()

  }
  export class ClassTerm implements DomainElement  {
    displayName: StrField
    name: StrField
    customDomainProperties: Array<DomainExtension>
    description: StrField
    isExternalLink: BoolField
    id: string
    properties: Array<StrField>
    position: Range
    subClassOf: Array<StrField>
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): ClassTerm

    withDescription(description: string): ClassTerm

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withProperties(properties: Array<string>): ClassTerm

    withDisplayName(displayName: string): ClassTerm

    withId(id: string): this

    withSubClassOf(superClasses: Array<string>): ClassTerm


  }
  export class OAuth2Flow implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    accessTokenUri: StrField
    scopes: Array<Scope>
    isExternalLink: BoolField
    id: string
    flow: StrField
    authorizationUri: StrField
    position: Range
    refreshUri: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

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
  export class OperationBindings implements DomainElement, Linkable  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    bindings: Array<OperationBinding>
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    linkCopy(): OperationBindings

    withName(name: string): this

    withBindings(bindings: Array<OperationBinding>): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class ExternalFragment extends Fragment  {
    constructor()

  }
  export class AMFValidationResult  {
    message: string
    severityLevel: string
    targetNode: string
    targetProperty: string
    validationId: string
    source: any
    position: Range
    location: undefined | string

    constructor(message: string, level: string, targetNode: string, targetProperty: string, validationId: string, range: Range, location: string)

  }
  export class MqttOperationBinding implements OperationBinding  {
    customDomainProperties: Array<DomainExtension>
    retain: BoolField
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    qos: IntField
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    linkCopy(): MqttOperationBinding

    withRetain(retain: boolean): this

    graph(): Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withQos(qos: number): this

    withId(id: string): this


  }
  export class SchemaDependencies implements DomainElement  {
    source: StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    target: Shape
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withSchemaTarget(schema: Shape): this

    withPropertySource(propertySource: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class Payload extends AbstractPayload  {
    encoding: Array<Encoding>
    encodings: Array<Encoding>
    examples: Array<Example>
    extendsNode: Array<DomainElement>
    id: string
    isExternalLink: BoolField
    position: Range
    schema: Shape
    schemaMediaType: StrField

    annotations(): Annotations

    constructor()

    graph(): Graph

    linkCopy(): Payload

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withEncoding(encoding: Array<Encoding>): this

    withEncoding(name: string): Encoding

    withEncodings(encoding: Array<Encoding>): this

    withExample(name: string): Example

    withExamples(examples: Array<Example>): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withId(id: string): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withMediaType(mediaType: string): this

    withName(name: string): this

    withObjectSchema(name: string): NodeShape

    withScalarSchema(name: string): ScalarShape

    withSchema(schema: Shape): this

    withSchemaMediaType(mediaType: string): this


  }
  export class RAMLConfiguration  {
    static RAML10(): AMFConfiguration

    static RAML08(): AMFConfiguration

    static RAML(): AMFConfiguration

    static fromSpec(spec: Spec): AMFConfiguration


  }
  export class JsonSchemaConfiguration {
    static JsonSchema(): ShapesConfiguration
  }
  export class Extension extends Document  {
    constructor()

  }
  export class MqttServerLastWill implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    retain: BoolField
    isExternalLink: BoolField
    id: string
    position: Range
    message: StrField
    topic: StrField
    qos: IntField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

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
  export class AsyncApi extends Api<AsyncApi>  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    endPoints: Array<EndPoint>
    provider: Organization
    security: Array<SecurityRequirement>
    identifier: StrField
    description: StrField
    tags: Array<Tag>
    documentations: Array<CreativeWork>
    servers: Array<Server>
    schemes: Array<StrField>
    license: License
    isExternalLink: BoolField
    termsOfService: StrField
    version: StrField
    id: string
    contentType: Array<StrField>
    accepts: Array<StrField>
    position: Range
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

    withTags(tags: Array<Tag>): this

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
    static readonly GRPC: ProfileName
    static readonly GRAPHQL: ProfileName
    static readonly GRAPHQL_FEDERATION: ProfileName
    static readonly JSONSCHEMA: ProfileName

  }
  export class OAuth2Settings extends Settings  {
    flows: Array<OAuth2Flow>
    authorizationGrants: Array<StrField>

    constructor()

    withFlows(flows: Array<OAuth2Flow>): this

    withAuthorizationGrants(grants: Array<string>): this


  }
  export class ShapeOperation extends AbstractOperation  {
    request: ShapeRequest
    response: ShapeResponse
    name: StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this

    // @ts-ignore
    withRequest(request: ShapeRequest): this

    withResponse(name: string): ShapeResponse

    withResponses(responses: Array<ShapeResponse>): this

    withFederationMetadata(metadata: ShapeFederationMetadata): this


  }
  export class Callback implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    expression: StrField
    endpoint: EndPoint
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

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
  export class Module implements BaseUnit, DeclaresModel, CustomizableElement  {
    customDomainProperties: Array<DomainExtension>
    location: string
    usage: StrField
    id: string
    raw: undefined | string
    processingData: BaseUnitProcessingData
    sourceSpec: undefined | Spec
    sourceInformation: BaseUnitSourceInformation
    modelVersion: StrField
    declares: Array<DomainElement>

    constructor()

    findByType(typeId: string): Array<DomainElement>

    cloneUnit(): BaseUnit

    withReferences(references: Array<BaseUnit>): this

    withDeclaredElement(declared: DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, id: string, fullUrl: string, relativeUrl: string): BaseUnit

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    pkg(): StrField

    withPkg(pkg: string): this

    withDeclares(declares: Array<DomainElement>): this

    references(): Array<BaseUnit>

    withProcessingData(data: BaseUnitProcessingData): this

    withId(id: string): this


  }
  export class PropertyDependencies implements DomainElement  {
    source: StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    target: Array<StrField>
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withPropertySource(propertySource: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withPropertyTarget(propertyTarget: Array<string>): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class DialectInstance implements BaseUnit, EncodesModel, DeclaresModel, DialectInstanceUnit  {
    location: string
    usage: StrField
    id: string
    raw: undefined | string
    processingData: DialectInstanceProcessingData
    sourceSpec: undefined | Spec
    sourceInformation: BaseUnitSourceInformation
    modelVersion: StrField
    encodes: DialectDomainElement
    declares: Array<DomainElement>
    externals: Array<External>

    constructor()

    findByType(typeId: string): Array<DomainElement>

    withProcessingData(data: DialectInstanceProcessingData): this

    cloneUnit(): BaseUnit

    withExternals(externals: Array<External>): DialectInstance

    withReferences(references: Array<BaseUnit>): this

    withDeclaredElement(declared: DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | DomainElement

    withLocation(location: string): this

    withGraphDependencies(ids: Array<string>): this

    withReferenceAlias(alias: string, id: string, fullUrl: string, relativeUrl: string): BaseUnit

    graphDependencies(): Array<StrField>

    withDefinedBy(dialectId: string): this

    withEncodes(encoded: DialectDomainElement): DialectInstance

    withEncodes(encoded: DomainElement): this

    definedBy(): StrField

    pkg(): StrField

    withPkg(pkg: string): this

    withDeclares(declares: Array<DomainElement>): this

    references(): Array<BaseUnit>

    withId(id: string): this


  }
  export class PipelineId  {
    static readonly Default: 'default'
    static readonly Editing: 'editing'
    static readonly Compatibility: 'compatibility'
    static readonly Cache: 'cache'

  }
  export class SecurityRequirement implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    schemes: Array<ParametrizedSecurityScheme>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    withScheme(): ParametrizedSecurityScheme

    withSchemes(schemes: Array<ParametrizedSecurityScheme>): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class ComponentModule extends Module  {
    name: StrField
    version: StrField

    constructor()

    withName(name: string): this

    withVersion(version: string): this


  }
  export class MqttServerBinding implements ServerBinding  {
    customDomainProperties: Array<DomainExtension>
    clientId: StrField
    keepAlive: IntField
    cleanSession: BoolField
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    lastWill: MqttServerLastWill
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

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

    link<T>(): T

    withLinkTarget(target: undefined): this

    withId(id: string): this

    withLastWill(lastWill: MqttServerLastWill): this


  }
  export class NilShape extends AnyShape  {
    constructor()

    linkCopy(): NilShape


  }
  export class ShapeResponse implements AbstractResponse  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    payload: ShapePayload
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    graph(): Graph

    withPayload(payload: ShapePayload): ShapePayload

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class BaseUnitSourceInformation  {
    rootLocation: StrField
    additionalLocations: Array<LocationInformation>

    constructor()

    withRootLocation(value: string): this

    withAdditionalLocations(locations: Array<LocationInformation>): this


  }
  export class APIContractProcessingData extends BaseUnitProcessingData  {
    modelVersion: StrField
    sourceSpec: StrField

    constructor()

    withSourceSpec(spec: string): this

    withSourceSpec(spec: Spec): this


  }
  export class EndPoint implements DomainElement  {
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
    position: Range
    extendsNode: Array<DomainElement>
    parameters: Array<Parameter>
    summary: StrField

    constructor()

    annotations(): Annotations

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
  export class SeverityLevels  {
    static readonly WARNING: 'Warning'
    static readonly INFO: 'Info'
    static readonly VIOLATION: 'Violation'

    static unapply(arg: string): string


  }
  export class AMFElementRenderer  {
    static renderToBuilder<T>(element: DomainElement, builder: org.yaml.builder.JsOutputBuilder, config: AMFGraphConfiguration): void


  }
  export class WebApi extends Api<WebApi>  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    endPoints: Array<EndPoint>
    provider: Organization
    security: Array<SecurityRequirement>
    identifier: StrField
    description: StrField
    tags: Array<Tag>
    documentations: Array<CreativeWork>
    servers: Array<Server>
    schemes: Array<StrField>
    license: License
    isExternalLink: BoolField
    termsOfService: StrField
    version: StrField
    id: string
    contentType: Array<StrField>
    accepts: Array<StrField>
    position: Range
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

    withTags(tags: Array<Tag>): this

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
  export class JsServerFileResourceLoader implements BaseFileResourceLoader  {
    constructor()

    fetch(resource: string): Promise<Content>

    accepts(resource: string): boolean

    fetchFile(resource: string): any

    ensureFileAuthority(str: string): string


  }
  export class ResourceTypeFragment extends Fragment  {
    constructor()

  }
  export class JSONSchemaVersions  {
    static readonly Unspecified: JSONSchemaVersion
    static readonly Draft04: JSONSchemaVersion
    static readonly Draft07: JSONSchemaVersion
    static readonly Draft201909: JSONSchemaVersion

  }
  export class ChannelBindings implements DomainElement, Linkable  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    bindings: Array<ChannelBinding>
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    linkCopy(): ChannelBindings

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withBindings(bindings: Array<ChannelBinding>): this

    withId(id: string): this


  }
  export class Key implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    components: Array<PropertyShapePath>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>
    isResolvable: BoolField

    constructor()

    annotations(): Annotations

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withComponents(components: Array<PropertyShapePath>): this

    withResolvable(isResolvable: boolean): this

    withId(id: string): this


  }
  export class JsonSchemaShapeRenderer  {
    static toJsonSchema(element: AnyShape, config: AMFGraphConfiguration): string

    static buildJsonSchema(element: AnyShape, config: AMFGraphConfiguration): string


  }
  export class PropertyShapePath implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    path: Array<PropertyShape>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withPath(path: Array<PropertyShape>): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class DialectDomainElement implements DomainElement, Linkable  {
    customDomainProperties: Array<DomainExtension>
    linkTarget: undefined | DomainElement
    isLink: boolean
    declarationName: StrField
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    withObjectCollectionProperty(propertyIri: string, value: Array<DialectDomainElement>): this

    linkCopy(): DialectDomainElement

    isAbstract(): BoolField

    withAbstract(isAbstract: boolean): DialectDomainElement

    withDeclarationName(name: string): DialectDomainElement

    localRefName(): string

    withObjectProperty(iri: string, value: DialectDomainElement): this

    withLiteralProperty(propertyIri: string, value: boolean): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    getTypeIris(): Array<string>

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    containsProperty(property: PropertyMapping): boolean

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withLiteralProperty(propertyIri: string, value: number): this

    withDefinedby(nodeMapping: NodeMapping): DialectDomainElement

    getPropertyIris(): Array<string>

    definedBy(): NodeMapping

    withLiteralProperty(propertyIri: string, value: Array<any>): this

    withInstanceTypes(types: Array<string>): DialectDomainElement

    getObjectByProperty(iri: string): Array<DialectDomainElement>

    includeName(): string

    withLiteralProperty(propertyIri: string, value: string): this

    withId(id: string): this


  }
  export class MessageBindings implements DomainElement, Linkable  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    bindings: Array<MessageBinding>
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    linkCopy(): MessageBindings

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withBindings(bindings: Array<MessageBinding>): this

    withId(id: string): this


  }
  export class Dialect implements BaseUnit, EncodesModel, DeclaresModel  {
    name: StrField
    location: string
    usage: StrField
    nameAndVersion: string
    allHeaders: Array<string>
    version: StrField
    id: string
    raw: undefined | string
    processingData: BaseUnitProcessingData
    fragmentHeaders: Array<string>
    libraryHeader: undefined | string
    header: string
    sourceSpec: undefined | Spec
    sourceInformation: BaseUnitSourceInformation
    modelVersion: StrField
    encodes: DomainElement
    declares: Array<DomainElement>
    externals: Array<External>

    constructor()

    findByType(typeId: string): Array<DomainElement>

    cloneUnit(): BaseUnit

    withExternals(externals: Array<External>): Dialect

    withName(name: string): Dialect

    withReferences(references: Array<BaseUnit>): this

    withDeclaredElement(declared: DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    isLibraryHeader(header: string): boolean

    findById(id: string): undefined | DomainElement

    withLocation(location: string): this

    withReferenceAlias(alias: string, id: string, fullUrl: string, relativeUrl: string): BaseUnit

    extensions(): Array<SemanticExtension>

    withDocuments(documentsMapping: DocumentsModel): Dialect

    withVersion(version: string): Dialect

    withEncodes(encoded: DomainElement): this

    pkg(): StrField

    documents(): DocumentsModel

    withPkg(pkg: string): this

    withDeclares(declares: Array<DomainElement>): this

    references(): Array<BaseUnit>

    isFragmentHeader(header: string): boolean

    withProcessingData(data: BaseUnitProcessingData): this

    withId(id: string): this


  }
  export class ScalarShape extends AnyShape  {
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

    withSerializationSchema(schema: Shape): this

    linkCopy(): ScalarShape


  }
  export class Amqp091ChannelExchange implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    autoDelete: BoolField
    isExternalLink: BoolField
    id: string
    vHost: StrField
    position: Range
    type: StrField
    extendsNode: Array<DomainElement>
    durable: BoolField

    constructor()

    annotations(): Annotations

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
  export class ParametrizedTrait implements ParametrizedDeclaration  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    variables: Array<VariableValue>
    isExternalLink: BoolField
    id: string
    position: Range
    target: AbstractDeclaration
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withVariables(variables: Array<VariableValue>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withTarget(target: AbstractDeclaration): this

    withId(id: string): this


  }
  export class JsBrowserHttpResourceLoader extends BaseHttpResourceLoader  {
    constructor()

    fetch(resource: string): any


  }
  export class Request extends Message implements AbstractRequest  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    queryString: Shape
    isExternalLink: BoolField
    id: string
    uriParameters: Array<Parameter>
    position: Range
    queryParameters: Array<Parameter>
    headers: Array<Parameter>
    required: BoolField
    cookieParameters: Array<Parameter>
    extendsNode: Array<DomainElement>

    constructor()

    graph(): Graph

    linkCopy(): Request

    withCookieParameter(name: string): Parameter

    withCookieParameters(cookieParameters: Array<Parameter>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withHeader(name: string): Parameter

    withHeaders(headers: Array<Parameter>): this

    withId(id: string): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withName(name: string): this

    withQueryParameter(name: string): Parameter

    withQueryParameters(parameters: Array<Parameter>): this

    withQueryString(queryString: Shape): this

    withRequired(required: boolean): this

    withUriParameter(name: string): Parameter

    withUriParameters(uriParameters: Array<Parameter>): this

    withPayload(): Payload

    withPayload(mediaType: string): Payload

    withPayload(mediaType: undefined | string): Payload

  }
  export class CreativeWork implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    url: StrField
    description: StrField
    isExternalLink: BoolField
    id: string
    position: Range
    title: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withDescription(description: string): this

    withTitle(title: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withUrl(url: string): this

    withId(id: string): this


  }
  export class ShapeRequest implements AbstractRequest  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    queryParameters: Array<ShapeParameter>
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    withQueryParameter(name: string): ShapeParameter

    withQueryParameters(parameters: Array<ShapeParameter>): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class LowPriority extends PluginPriority  {
  }
  export class OASConfiguration  {
    static OAS20(): AMFConfiguration

    static OAS30(): AMFConfiguration

    static OAS30Component(): AMFConfiguration

    static OAS(): AMFConfiguration

    static OASComponent(): AMFConfiguration

    static fromSpec(spec: Spec): AMFConfiguration


  }
  export class Content  {
    constructor(stream: string, url: string)
    constructor(stream: string, url: string, mime: string)
    readonly url: string

  }
  export class DatatypePropertyTerm extends PropertyTerm  {
    constructor()

  }
  export class ResourceNotFound  {
    readonly msj: string

    constructor(msj: string)

  }
  export class Parameter extends AbstractParameter  {
    allowEmptyValue: BoolField
    allowReserved: BoolField
    binding: StrField
    customDomainProperties: Array<DomainExtension>
    deprecated: BoolField
    description: StrField
    examples: Array<Example>
    explode: BoolField
    extendsNode: Array<DomainElement>
    id: string
    isExternalLink: BoolField
    name: StrField
    parameterName: StrField
    payloads: Array<Payload>
    position: Range
    required: BoolField
    schema: Shape
    style: StrField

    annotations(): Annotations

    constructor()

    graph(): Graph

    withAllowEmptyValue(allowEmptyValue: boolean): this

    withAllowReserved(allowReserved: boolean): this

    withBinding(binding: string): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withDeprecated(deprecated: boolean): this

    withDescription(description: string): this

    withExample(name: string): Example

    withExamples(examples: Array<Example>): this

    withExplode(explode: boolean): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withId(id: string): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withName(name: string): this

    withObjectSchema(name: string): NodeShape

    withParameterName(name: string): this

    withPayload(mediaType: string): Payload

    withPayloads(payloads: Array<Payload>): this

    withRequired(required: boolean): this

    withScalarSchema(name: string): ScalarShape

    withSchema(schema: Shape): this

    withStyle(style: string): this

  }
  export class DocumentationItem extends Fragment  {
    constructor()

  }
  export class EventNames  {
    static readonly StartedParse: 'StartedParse'
    static readonly StartedContentParse: 'StartedContentParse'
    static readonly ParsedSyntax: 'ParsedSyntax'
    static readonly ParsedModel: 'ParsedModel'
    static readonly FinishedParse: 'FinishedParse'
    static readonly StartedTransformation: 'StartedTransformation'
    static readonly FinishedTransformationStep: 'FinishedTransformationStep'
    static readonly StartedTransformationStep: 'StartedTransformationStep'
    static readonly FinishedTransformation: 'FinishedTransformation'
    static readonly StartingValidation: 'StartingValidation'
    static readonly FinishedValidationPlugin: 'FinishedValidationPlugin'
    static readonly FinishedValidation: 'FinishedValidation'
    static readonly StartedRender: 'StartedRender'
    static readonly StartedRenderToWriter: 'StartedRenderToWriter'
    static readonly FinishedASTRender: 'FinishedASTRender'
    static readonly FinishedSyntaxRender: 'FinishedSyntaxRender'
    static readonly FoundReferences: 'FoundReferences'
    static readonly SelectedParsePlugin: 'SelectedParsePlugin'
    static readonly DetectedSyntaxMediaType: 'DetectedSyntaxMediaType'
    static readonly SkippedValidationPlugin: 'SkippedValidationPlugin'
    static readonly UnitCacheHit: 'UnitCacheHit'

  }
  export class AnyShape implements Shape  {
    isNotExplicit: boolean
    defaultValueStr: StrField
    displayName: StrField
    name: StrField
    customDomainProperties: Array<DomainExtension>
    isExtension: BoolField
    examples: Array<Example>
    xone: Array<Shape>
    readOnly: BoolField
    isStub: BoolField
    description: StrField
    documentation: CreativeWork
    deprecated: BoolField
    xmlSerialization: XMLSerializer
    customShapePropertyDefinitions: Array<PropertyShape>
    or: Array<Shape>
    elseShape: Shape
    linkTarget: undefined | DomainElement
    hasExplicitName: boolean
    isLink: boolean
    isExternalLink: BoolField
    customShapeProperties: Array<ShapeExtension>
    thenShape: Shape
    federationMetadata: ShapeFederationMetadata
    id: string
    ifShape: Shape
    writeOnly: BoolField
    comment: StrField
    not: Shape
    values: Array<DataNode>
    position: Range
    inherits: Array<Shape>
    linkLabel: StrField
    defaultValue: DataNode
    extendsNode: Array<DomainElement>
    and: Array<Shape>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    withValues(values: Array<DataNode>): this

    linkCopy(): AnyShape

    withOr(subShapes: Array<Shape>): this

    withName(name: string): this

    withDescription(description: string): this

    trackedExample(trackId: string): undefined | Example

    withIf(ifShape: Shape): this

    withIsExtension(value: boolean): this

    withCustomShapePropertyDefinition(name: string): PropertyShape

    withIsStub(value: boolean): this

    withExamples(examples: Array<Example>): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withCustomShapePropertyDefinitions(propertyDefinitions: Array<PropertyShape>): this

    withReadOnly(readOnly: boolean): this

    withInherits(inherits: Array<Shape>): this

    withAnd(subShapes: Array<Shape>): this

    withComment(comment: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withWriteOnly(writeOnly: boolean): this

    withFederationMetadata(metadata: ShapeFederationMetadata): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    inlined(): boolean

    withLinkTarget(target: undefined): this

    withDisplayName(name: string): this

    withDefaultValue(defaultVal: DataNode): this

    withXMLSerialization(xmlSerialization: XMLSerializer): this

    withThen(thenShape: Shape): this

    withDefaultStr(value: string): this

    withCustomShapeProperties(customShapeProperties: Array<ShapeExtension>): this

    withId(id: string): this

    withElse(elseShape: Shape): this

    withExample(mediaType: string): Example

    withXone(subShapes: Array<Shape>): this

    withDocumentation(documentation: CreativeWork): this

    withDeprecated(deprecated: boolean): this

    withNode(shape: Shape): this


  }
  export class MatrixShape extends ArrayShape  {
    constructor()

    withItems(items: Shape): this


  }
  export class ServerBindings implements DomainElement, Linkable  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    bindings: Array<ServerBinding>
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    withBindings(bindings: Array<ServerBinding>): this

    linkCopy(): ServerBindings

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class NodeShape extends AnyShape  {
    isAbstract: BoolField
    isInputOnly: BoolField
    minProperties: IntField
    maxProperties: IntField
    closed: BoolField
    discriminator: StrField
    discriminatorValue: StrField
    discriminatorMapping: Array<IriTemplateMapping>
    discriminatorValueMapping: Array<DiscriminatorValueMapping>
    properties: Array<PropertyShape>
    additionalPropertiesSchema: Shape
    additionalPropertiesKeySchema: Shape
    dependencies: Array<PropertyDependencies>
    schemaDependencies: Array<SchemaDependencies>
    propertyNames: Shape
    unevaluatedProperties: boolean
    unevaluatedPropertiesSchema: Shape
    keys: undefined
    externalProperties: Array<ExternalPropertyShape>

    constructor()

    annotations(): Annotations

    withIsAbstract(isAbstract: boolean): this

    withIsInputOnly(isInputOnly: boolean): this

    withMinProperties(min: number): this

    withMaxProperties(max: number): this

    withClosed(closed: boolean): this

    withDiscriminator(discriminator: string): this

    withDiscriminatorValue(value: string): this

    withDiscriminatorMapping(mappings: Array<IriTemplateMapping>): this

    withProperties(properties: Array<PropertyShape>): this

    withAdditionalPropertiesSchema(additionalPropertiesSchema: Shape): this

    withAdditionalPropertiesKeySchema(additionalPropertiesKeySchema: Shape): this

    withDependencies(dependencies: Array<PropertyDependencies>): this

    withSchemaDependencies(dependencies: Array<SchemaDependencies>): this

    withPropertyNames(propertyNames: Shape): this

    withUnevaluatedProperties(value: boolean): this

    withUnevaluatedPropertiesSchema(schema: Shape): this

    withProperty(name: string): PropertyShape

    withDependency(): PropertyDependencies

    withInheritsObject(name: string): NodeShape

    withInheritsScalar(name: string): ScalarShape

    withKeys(keys: undefined): this

    withExternalProperties(externalProperties: Array<ExternalPropertyShape>): this

    linkCopy(): NodeShape


  }
  export class DialectInstancePatch implements BaseUnit, EncodesModel, DeclaresModel, DialectInstanceUnit  {
    location: string
    usage: StrField
    id: string
    raw: undefined | string
    processingData: DialectInstanceProcessingData
    sourceSpec: undefined | Spec
    sourceInformation: BaseUnitSourceInformation
    modelVersion: StrField
    encodes: DialectDomainElement
    declares: Array<DomainElement>
    externals: Array<External>

    constructor()

    findByType(typeId: string): Array<DomainElement>

    withProcessingData(data: DialectInstanceProcessingData): this

    cloneUnit(): BaseUnit

    withExternals(externals: Array<External>): DialectInstancePatch

    withReferences(references: Array<BaseUnit>): this

    withDeclaredElement(declared: DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | DomainElement

    withLocation(location: string): this

    withGraphDependencies(ids: Array<string>): this

    withReferenceAlias(alias: string, id: string, fullUrl: string, relativeUrl: string): BaseUnit

    graphDependencies(): Array<StrField>

    withDefinedBy(dialectId: string): this

    withEncodes(encoded: DialectDomainElement): DialectInstancePatch

    withEncodes(encoded: DomainElement): this

    definedBy(): StrField

    pkg(): StrField

    withPkg(pkg: string): this

    withDeclares(declares: Array<DomainElement>): this

    references(): Array<BaseUnit>

    withId(id: string): this


  }
  export class Overlay extends Document  {
    constructor()

  }
  export class License implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    url: StrField
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withUrl(url: string): this

    withId(id: string): this


  }
  export class Tag implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    description: StrField
    documentation: CreativeWork
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    withDescription(description: string): this

    withVariables(documentation: CreativeWork): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this

    withDocumentation(documentation: CreativeWork): this


  }
  export class AMFValidationReport  {
    conforms: boolean
    model: string
    profile: ProfileName
    results: Array<AMFValidationResult>
    toString: string

    constructor(model: string, profile: ProfileName, results: Array<AMFValidationResult>)

    toStringMaxed(max: number): string


  }
  export class ArrayNode implements DataNode  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    members: Array<DataNode>
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this

    addMember(member: DataNode): this


  }
  export class Organization implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    email: StrField
    url: StrField
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withEmail(email: string): this

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withUrl(url: string): this

    withId(id: string): this


  }
  export class ParametrizedResourceType implements ParametrizedDeclaration  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    variables: Array<VariableValue>
    isExternalLink: BoolField
    id: string
    position: Range
    target: AbstractDeclaration
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withVariables(variables: Array<VariableValue>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withTarget(target: AbstractDeclaration): this

    withId(id: string): this


  }
  export class VocabularyReference implements DomainElement  {
    reference: StrField
    customDomainProperties: Array<DomainExtension>
    alias: StrField
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withAlias(alias: string): VocabularyReference

    graph(): Graph

    withReference(reference: string): VocabularyReference

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class AMFRenderer  {
    static render(baseUnit: BaseUnit, configuration: AMFGraphConfiguration): string

    static render(baseUnit: BaseUnit, mediaType: string, configuration: AMFGraphConfiguration): string

    static renderGraphToBuilder<T>(baseUnit: BaseUnit, builder: org.yaml.builder.JsOutputBuilder, configuration: AMFGraphConfiguration): T


  }
  export class AnnotationTypeDeclaration extends Fragment  {
    constructor()

  }
  export class DocumentsModel implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    declarationsPath(): StrField

    withRoot(documentMapping: DocumentMapping): DocumentsModel

    withKeyProperty(keyProperty: boolean): DocumentsModel

    keyProperty(): BoolField

    fragments(): Array<DocumentMapping>

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    root(): DocumentMapping

    selfEncoded(): BoolField

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withFragments(fragments: Array<DocumentMapping>): DocumentsModel

    library(): DocumentMapping

    withLibrary(library: DocumentMapping): DocumentsModel

    withSelfEncoded(selfEncoded: boolean): DocumentsModel

    withDeclarationsPath(declarationsPath: string): DocumentsModel

    withId(id: string): this


  }
  export class Annotations  {
    isLocal: boolean
    isTracked: boolean
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
  export class KafkaMessageBinding implements MessageBinding  {
    customDomainProperties: Array<DomainExtension>
    messageKey: Shape
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    withKey(key: Shape): this

    linkCopy(): KafkaMessageBinding

    graph(): Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class DefaultExecutionEnvironment  {
    static apply(): ExecutionEnvironment


  }
  export class DialectInstanceFragment implements BaseUnit, EncodesModel, DialectInstanceUnit  {
    location: string
    usage: StrField
    id: string
    raw: undefined | string
    processingData: DialectInstanceProcessingData
    sourceSpec: undefined | Spec
    sourceInformation: BaseUnitSourceInformation
    modelVersion: StrField
    encodes: DialectDomainElement

    constructor()

    findByType(typeId: string): Array<DomainElement>

    withProcessingData(data: DialectInstanceProcessingData): this

    cloneUnit(): BaseUnit

    withReferences(references: Array<BaseUnit>): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | DomainElement

    withLocation(location: string): this

    withGraphDependencies(ids: Array<string>): this

    withReferenceAlias(alias: string, id: string, fullUrl: string, relativeUrl: string): BaseUnit

    graphDependencies(): Array<StrField>

    withDefinedBy(dialectId: string): this

    withEncodes(encoded: DialectDomainElement): DialectInstanceFragment

    withEncodes(encoded: DomainElement): this

    definedBy(): StrField

    pkg(): StrField

    withPkg(pkg: string): this

    references(): Array<BaseUnit>

    withId(id: string): this


  }
  export class BaseUnitProcessingData  {
    transformed: BoolField

    constructor()

    withTransformed(value: boolean): this


  }
  export class LinkNode implements DataNode  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    alias: StrField
    isExternalLink: BoolField
    id: string
    link: StrField
    position: Range
    extendsNode: Array<DomainElement>

    constructor()
    constructor(alias: string, value: string)

    annotations(): Annotations

    withAlias(alias: string): this

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withLink(link: string): this

    withId(id: string): this


  }
  export class NodeMapping extends AnyMapping implements Linkable  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    closed: BoolField
    idTemplate: StrField
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    mergePolicy: StrField
    nodetypeMapping: StrField
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    withIdTemplate(idTemplate: string): NodeMapping

    linkCopy(): NodeMapping

    withName(name: string): NodeMapping

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withNodeTypeMapping(nodeType: string): NodeMapping

    link<T>(): T

    withLinkTarget(target: undefined): this

    withPropertiesMapping(props: Array<PropertyMapping>): NodeMapping

    propertiesMapping(): Array<PropertyMapping>

    withMergePolicy(mergePolicy: string): NodeMapping

    withId(id: string): this


  }
  export class HttpSettings extends Settings  {
    scheme: StrField
    bearerFormat: StrField

    constructor()

    withScheme(scheme: string): this

    withBearerFormat(bearerFormat: string): this


  }
  export class ShapeFederationMetadata  {
    constructor()

  }
  export class CustomDomainProperty implements DomainElement, Linkable  {
    displayName: StrField
    name: StrField
    serializationOrder: IntField
    customDomainProperties: Array<DomainExtension>
    description: StrField
    domain: Array<StrField>
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    schema: Shape
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    linkCopy(): CustomDomainProperty

    withName(name: string): this

    withDescription(description: string): this

    withDomain(domain: Array<string>): this

    withSerializationOrder(order: number): this

    withSchema(schema: Shape): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withDisplayName(displayName: string): this

    withId(id: string): this


  }
  export class OperationFederationMetadata  {
    providedEntity: NodeShape
    federationMethod: StrField
    keyMappings: Array<ParameterKeyMapping>

    constructor()

    withProvidedEntity(providedEntity: NodeShape): this

    withFederationMethod(federationMethod: string): this

    withKeyMappings(keyMappings: Array<ParameterKeyMapping>): this


  }
  export class EmptyBinding implements ServerBinding, OperationBinding, ChannelBinding, MessageBinding  {
    customDomainProperties: Array<DomainExtension>
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    type: StrField
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    linkCopy(): EmptyBinding

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withType(type: string): this

    withId(id: string): this


  }
  export class XMLSerializer implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    prefix: StrField
    wrapped: BoolField
    isExternalLink: BoolField
    id: string
    position: Range
    attribute: BoolField
    extendsNode: Array<DomainElement>
    namespace: StrField

    constructor()

    annotations(): Annotations

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
  export class ErrorHandler  {
    static handler(obj: JsErrorHandler): ClientErrorHandler

    static provider(obj: JsErrorHandler): ErrorHandlerProvider


  }
  export class Vocabulary implements BaseUnit, DeclaresModel  {
    name: StrField
    location: string
    description: StrField
    usage: StrField
    base: StrField
    id: string
    raw: undefined | string
    processingData: BaseUnitProcessingData
    sourceSpec: undefined | Spec
    sourceInformation: BaseUnitSourceInformation
    modelVersion: StrField
    declares: Array<DomainElement>
    externals: Array<External>
    imports: Array<VocabularyReference>

    constructor()

    objectPropertyTerms(): Array<ObjectPropertyTerm>

    findByType(typeId: string): Array<DomainElement>

    cloneUnit(): BaseUnit

    withExternals(externals: Array<External>): Vocabulary

    withName(name: string): Vocabulary

    withReferences(references: Array<BaseUnit>): this

    withDeclaredElement(declared: DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    datatypePropertyTerms(): Array<DatatypePropertyTerm>

    withBase(base: string): Vocabulary

    findById(id: string): undefined | DomainElement

    classTerms(): Array<ClassTerm>

    withLocation(location: string): this

    withReferenceAlias(alias: string, id: string, fullUrl: string, relativeUrl: string): BaseUnit

    pkg(): StrField

    withPkg(pkg: string): this

    withDeclares(declares: Array<DomainElement>): this

    references(): Array<BaseUnit>

    withImports(vocabularies: Array<VocabularyReference>): Vocabulary

    withProcessingData(data: BaseUnitProcessingData): this

    withId(id: string): this


  }
  export class KafkaOperationBinding implements OperationBinding  {
    customDomainProperties: Array<DomainExtension>
    clientId: Shape
    linkTarget: undefined | DomainElement
    groupId: Shape
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    linkCopy(): KafkaOperationBinding

    withGroupId(groupId: Shape): this

    withClientId(clientId: Shape): this

    graph(): Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class VariableValue implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>
    value: DataNode

    constructor()

    annotations(): Annotations

    withName(name: string): this

    graph(): Graph

    withValue(value: DataNode): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class AMFParser  {
    static parse(url: string, configuration: AMFGraphConfiguration): Promise<AMFParseResult>

    static parseContent(content: string, configuration: AMFGraphConfiguration): Promise<AMFParseResult>

    static parseContent(content: string, mediaType: string, configuration: AMFGraphConfiguration): Promise<AMFParseResult>

    static parseStartingPoint(graphUrl: string, startingPoint: string, configuration: AMFGraphConfiguration): Promise<AMFObjectResult>


  }
  export class PropertyMapping implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    maximum(): DoubleField

    typeDiscriminator(): Map<string, string>

    classification(): string

    withEnum(values: Array<any>): PropertyMapping

    enum(): Array<AnyField>

    minCount(): IntField

    withMapKeyProperty(key: string): PropertyMapping

    withName(name: string): PropertyMapping

    literalRange(): StrField

    externallyLinkable(): BoolField

    withTypeDiscriminatorName(name: string): PropertyMapping

    sorted(): BoolField

    minimum(): DoubleField

    pattern(): StrField

    graph(): Graph

    withLiteralRange(range: string): PropertyMapping

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withObjectRange(range: Array<string>): PropertyMapping

    objectRange(): Array<StrField>

    withMandatory(mandatory: boolean): PropertyMapping

    withPattern(pattern: string): PropertyMapping

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withMapValueProperty(value: string): PropertyMapping

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withSorted(sorted: boolean): PropertyMapping

    withNodePropertyMapping(propertyId: string): PropertyMapping

    allowMultiple(): BoolField

    withMinimum(min: number): PropertyMapping

    withMaximum(max: number): PropertyMapping

    nodePropertyMapping(): StrField

    typeDiscriminatorName(): StrField

    mandatory(): BoolField

    name(): StrField

    withAllowMultiple(allow: boolean): PropertyMapping

    mapValueProperty(): StrField

    mapKeyProperty(): StrField

    withExternallyLinkable(linkable: boolean): PropertyMapping

    withId(id: string): this

    withTypeDiscriminator(typesMapping: Map<string, string>): PropertyMapping

    withMinCount(minCount: number): PropertyMapping


  }
  export class Server implements DomainElement  {
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
    position: Range
    extendsNode: Array<DomainElement>
    protocol: StrField

    constructor()

    annotations(): Annotations

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
  export class CorrelationId implements DomainElement, Linkable  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    description: StrField
    idLocation: StrField
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    linkCopy(): CorrelationId

    withName(name: string): this

    withDescription(description: string): this

    withIdLocation(idLocation: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class APIConfiguration  {
    static API(): AMFConfiguration

    static fromSpec(spec: Spec): AMFConfiguration


  }
  export class HttpMessageBinding implements MessageBinding  {
    customDomainProperties: Array<DomainExtension>
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    headers: Shape
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    linkCopy(): HttpMessageBinding

    withHeaders(headers: Shape): this

    graph(): Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class AmlDomainElementEmitter  {
    static emitToBuilder<T>(element: DomainElement, amlConfig: BaseAMLConfiguration, builder: org.yaml.builder.JsOutputBuilder): void


  }
  export class Amqp091ChannelBinding implements ChannelBinding  {
    is: StrField
    customDomainProperties: Array<DomainExtension>
    queue: Amqp091Queue
    linkTarget: undefined | DomainElement
    isLink: boolean
    exchange: Amqp091ChannelExchange
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

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

    link<T>(): T

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class DialectInstanceLibrary implements BaseUnit, DeclaresModel, DialectInstanceUnit  {
    location: string
    usage: StrField
    id: string
    raw: undefined | string
    processingData: DialectInstanceProcessingData
    sourceSpec: undefined | Spec
    sourceInformation: BaseUnitSourceInformation
    modelVersion: StrField
    declares: Array<DomainElement>

    constructor()

    findByType(typeId: string): Array<DomainElement>

    withProcessingData(data: DialectInstanceProcessingData): this

    cloneUnit(): BaseUnit

    withReferences(references: Array<BaseUnit>): this

    withDeclaredElement(declared: DomainElement): this

    withRaw(raw: string): this

    withUsage(usage: string): this

    findById(id: string): undefined | DomainElement

    withLocation(location: string): this

    withGraphDependencies(ids: Array<string>): this

    withReferenceAlias(alias: string, id: string, fullUrl: string, relativeUrl: string): BaseUnit

    graphDependencies(): Array<StrField>

    withDefinedBy(dialectId: string): this

    definedBy(): StrField

    pkg(): StrField

    withPkg(pkg: string): this

    withDeclares(declares: Array<DomainElement>): this

    references(): Array<BaseUnit>

    withId(id: string): this


  }
  export class JsServerHttpResourceLoader extends BaseHttpResourceLoader  {
    constructor()

    fetch(resource: string): any


  }
  export class SecuritySchemeFragment extends Fragment  {
    constructor()

  }
  export class MqttMessageBinding implements MessageBinding  {
    customDomainProperties: Array<DomainExtension>
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    linkCopy(): MqttMessageBinding

    graph(): Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class AMFPayloadValidationPluginConverter  {
    static toAMF(plugin: JsAMFPayloadValidationPlugin): AMFShapePayloadValidationPlugin


  }
  export class AsyncAPIConfiguration  {
    static Async20(): AMFConfiguration


  }
  export class Amqp091MessageBinding implements MessageBinding  {
    customDomainProperties: Array<DomainExtension>
    linkTarget: undefined | DomainElement
    isLink: boolean
    messageType: StrField
    isExternalLink: BoolField
    id: string
    contentEncoding: StrField
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    withMessageType(messageType: string): this

    linkCopy(): Amqp091MessageBinding

    withContentEncoding(contentEncoding: string): this

    graph(): Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withId(id: string): this


  }
  export class Operation extends AbstractOperation implements Linkable  {
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
    federationMetadata: OperationFederationMetadata
    response: Response
    isExternalLink: BoolField
    id: string
    contentType: Array<StrField>
    accepts: Array<StrField>
    position: Range
    isAbstract: BoolField
    linkLabel: StrField
    callbacks: Array<Callback>
    requests: Array<Request>
    extendsNode: Array<DomainElement>
    summary: StrField
    responses: Array<Response>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    withSecurity(security: Array<SecurityRequirement>): this

    linkCopy(): Operation

    withFederationMetadata(federationMetadata: OperationFederationMetadata): this

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

    link<T>(): T

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

    withRequests(requests: Array<Request>): this

    withId(id: string): this

    withRequest(): Request

    withRequests(requests: Array<Request>): this

    withDocumentation(documentation: CreativeWork): this

    withDeprecated(deprecated: boolean): this


  }
  export class ExecutionEnvironment  {
    constructor()

  }
  export class External implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    base: StrField
    alias: StrField
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withAlias(alias: string): External

    graph(): Graph

    withBase(base: string): External

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class ResourceType extends AbstractDeclaration  {
    linkTarget: undefined | DomainElement

    constructor()

    linkCopy(): ResourceType


  }
  export class DiscriminatorValueMapping implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    targetShape: Shape
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>
    value: StrField

    constructor()

    annotations(): Annotations

    withTargetShape(shape: Shape): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withValue(value: string): this

    withId(id: string): this


  }
  export class SchemaShape extends AnyShape  {
    mediaType: StrField
    raw: StrField
    location: undefined | string

    constructor()

    withMediatype(mediaType: string): this

    withRaw(text: string): this

    linkCopy(): SchemaShape


  }
  export class HttpOperationBinding implements OperationBinding  {
    method: StrField
    customDomainProperties: Array<DomainExtension>
    operationType: StrField
    query: Shape
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    linkCopy(): HttpOperationBinding

    withMethod(method: string): this

    withOperationType(type: string): this

    graph(): Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withId(id: string): this

    withQuery(query: Shape): this


  }
  export class UnionNodeMapping extends AnyMapping implements Linkable  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

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

    link<T>(): T

    withLinkTarget(target: undefined): this

    typeDiscriminatorName(): StrField

    withId(id: string): this

    withTypeDiscriminator(typesMapping: Map<string, string>): UnionNodeMapping


  }
  export class HttpApiKeySettings extends Settings  {
    name: StrField
    in: StrField

    constructor()

    withName(name: string): this

    withIn(inVal: string): this


  }
  export class TemplatedLink implements DomainElement  {
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
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

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
  export class Encoding implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    style: StrField
    allowReserved: BoolField
    isExternalLink: BoolField
    id: string
    contentType: StrField
    explode: BoolField
    position: Range
    propertyName: StrField
    headers: Array<Parameter>
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

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
  export class Amqp091Queue implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    autoDelete: BoolField
    isExternalLink: BoolField
    id: string
    vHost: StrField
    exclusive: BoolField
    position: Range
    extendsNode: Array<DomainElement>
    durable: BoolField

    constructor()

    annotations(): Annotations

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
  export class ShapeParameter extends AbstractParameter  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class SemanticExtension implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    extensionName(): StrField

    extensionMappingDefinition(): StrField

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtensionMappingDefinition(annotationMapping: string): SemanticExtension

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withExtensionName(name: string): SemanticExtension

    withId(id: string): this


  }
  export class DomainExtension implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    definedBy: CustomDomainProperty
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>
    extension: DataNode

    constructor()

    annotations(): Annotations

    withName(name: string): this

    withDefinedBy(property: CustomDomainProperty): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this

    withExtension(node: DataNode): this


  }
  export class IriTemplateMapping implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    templateVariable: StrField
    isExternalLink: BoolField
    id: string
    linkExpression: StrField
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    graph(): Graph

    withTemplateVariable(variable: string): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withLinkExpression(expression: string): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class RenderOptions  {
    withGovernanceMode: RenderOptions
    isWithDocumentation: boolean
    isWithCompactedEmission: boolean
    schemaVersion: JSONSchemaVersion
    isWithCompactUris: boolean
    isWithSourceMaps: boolean
    isWithSourceInformation: boolean
    isAmfJsonLdSerialization: boolean
    isPrettyPrint: boolean
    isEmitNodeIds: boolean
    isRawFieldEmission: boolean
    isGovernanceMode: boolean

    constructor()

    withPrettyPrint(): RenderOptions

    withoutPrettyPrint(): RenderOptions

    withSourceMaps(): RenderOptions

    withoutSourceMaps(): RenderOptions

    withSourceInformation(): RenderOptions

    withoutSourceInformation(): RenderOptions

    withCompactUris(): RenderOptions

    withoutCompactUris(): RenderOptions

    withoutAmfJsonLdSerialization(): RenderOptions

    withAmfJsonLdSerialization(): RenderOptions

    withNodeIds(): RenderOptions

    withDocumentation(): RenderOptions

    withoutDocumentation(): RenderOptions

    withCompactedEmission(): RenderOptions

    withoutCompactedEmission(): RenderOptions

    withSchemaVersion(version: JSONSchemaVersion): RenderOptions

    withRawFieldEmission(): RenderOptions


  }
  export class DocumentMapping implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withDeclaredNodes(declarations: Array<PublicNodeMapping>): DocumentMapping

    withDocumentName(name: string): DocumentMapping

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    documentName(): StrField

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withEncoded(encodedNode: string): DocumentMapping

    declaredNodes(): Array<PublicNodeMapping>

    withId(id: string): this

    encoded(): StrField


  }
  export class PluginPriority  {
    priority: number

    constructor(priority: number)

  }
  export class PayloadFragment extends Fragment  {
    mediaType: StrField
    dataNode: DataNode

    constructor(scalar: ScalarNode, mediaType: string)
    constructor(obj: ObjectNode, mediaType: string)
    constructor(arr: ArrayNode, mediaType: string)

  }
  export class PipelineName  {
    static from(targetMediaType: string, pipelineId: string): string


  }
  export class OAuth1Settings extends Settings  {
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
  export class NormalPriority extends PluginPriority  {
  }
  export class WebAPIConfiguration  {
    static WebAPI(): AMFConfiguration

    static fromSpec(spec: Spec): AMFConfiguration


  }
  export class ResourceLoaderFactory  {
    static create(loader: ClientResourceLoader): any


  }
  export class ShapePayload extends AbstractPayload  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this

    linkCopy(): ShapePayload


  }
  export class DataTypes  {
    static readonly String: string
    static readonly Integer: string
    static readonly Number: string
    static readonly Long: string
    static readonly Double: string
    static readonly Float: string
    static readonly Decimal: string
    static readonly Boolean: string
    static readonly Date: string
    static readonly Time: string
    static readonly DateTime: string
    static readonly DateTimeOnly: string
    static readonly File: string
    static readonly Byte: string
    static readonly Binary: string
    static readonly Password: string
    static readonly Any: string
    static readonly AnyUri: string
    static readonly Nil: string

  }
  export class Example implements DomainElement, Linkable  {
    displayName: StrField
    mediaType: StrField
    name: StrField
    strict: BoolField
    customDomainProperties: Array<DomainExtension>
    location: undefined | string
    description: StrField
    structuredValue: DataNode
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    linkLabel: StrField
    extendsNode: Array<DomainElement>
    value: StrField

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

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

    link<T>(): T

    withValue(value: string): this

    withLinkTarget(target: undefined): this

    withStrict(strict: boolean): this

    withDisplayName(displayName: string): this

    withId(id: string): this


  }
  export class ParameterKeyMapping implements KeyMapping  {
    source: Parameter
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    target: PropertyShapePath
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withTarget(target: PropertyShapePath): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this

    withSource(source: Parameter): this


  }
  export class WebSocketsChannelBinding implements ChannelBinding  {
    method: StrField
    customDomainProperties: Array<DomainExtension>
    query: Shape
    linkTarget: undefined | DomainElement
    isLink: boolean
    isExternalLink: BoolField
    id: string
    position: Range
    headers: Shape
    type: StrField
    linkLabel: StrField
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    link<T>(label: string): T

    linkCopy(): WebSocketsChannelBinding

    withMethod(method: string): this

    withHeaders(headers: Shape): this

    graph(): Graph

    withBindingVersion(bindingVersion: string): this

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withLinkLabel(label: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    link<T>(): T

    withLinkTarget(target: undefined): this

    withType(type: string): this

    withId(id: string): this

    withQuery(query: Shape): this


  }
  export class DataType extends Fragment  {
    constructor()

  }
  export class ArrayShape extends DataArrangeShape  {
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
  export class ExternalDomainElement implements DomainElement  {
    mediaType: StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    raw: StrField
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withRaw(raw: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withMediaType(mediaType: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class AMFValidator  {
    static validate(baseUnit: BaseUnit, conf: AMFGraphConfiguration): Promise<AMFValidationReport>


  }
  export class ExternalPropertyShape implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    rangeName: StrField
    isExternalLink: BoolField
    id: string
    position: Range
    extendsNode: Array<DomainElement>
    keyMappings: Array<PropertyKeyMapping>

    constructor()

    annotations(): Annotations

    withName(name: string): this

    withRangeName(rangeName: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withKeyMappings(keyMappings: Array<PropertyKeyMapping>): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class LocationInformation  {
    locationValue: StrField

    constructor()

    withLocation(value: string): this

    elements(): Array<StrField>

    withElements(elements: Array<string>): this


  }
  export class Trait extends AbstractDeclaration  {
    linkTarget: undefined | DomainElement

    constructor()

    linkCopy(): Trait


  }
  export class ParametrizedSecurityScheme implements DomainElement  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    description: StrField
    isExternalLink: BoolField
    id: string
    scheme: SecurityScheme
    position: Range
    extendsNode: Array<DomainElement>
    hasNullSecurityScheme: boolean
    settings: Settings

    constructor()

    annotations(): Annotations

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
  export class CachedReference  {
    url: string
    content: BaseUnit

    constructor(url: string, content: BaseUnit)

  }
  export class Settings implements DomainElement  {
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    additionalProperties: DataNode
    position: Range
    extendsNode: Array<DomainElement>

    constructor()

    annotations(): Annotations

    withAdditionalProperties(properties: DataNode): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withId(id: string): this


  }
  export class ErrorHandlerProvider  {
    static unhandled(): ErrorHandlerProvider

    static default(): ErrorHandlerProvider

    static ignoring(): ErrorHandlerProvider


  }
  export class Spec  {
    static readonly RAML08: Spec
    static readonly RAML10: Spec
    static readonly OAS20: Spec
    static readonly OAS30: Spec
    static readonly ASYNC20: Spec
    static readonly AMF: Spec
    static readonly PAYLOAD: Spec
    static readonly AML: Spec
    static readonly JSONSCHEMA: Spec
    static readonly GRPC: Spec
    static readonly GRAPHQL: Spec
    static readonly GRAPHQL_FEDERATION: Spec
    static readonly JSONSCHEMADIALECT: Spec

    static apply(name: string): Spec


  }
  export class ValidationMode  {
    static readonly StrictValidationMode: ValidationMode
    static readonly ScalarRelaxedValidationMode: ValidationMode

  }
  export class AMFGraphConfiguration  {
    baseUnitClient(): AMFGraphBaseUnitClient

    elementClient(): AMFGraphElementClient

    withParsingOptions(parsingOptions: ParsingOptions): AMFGraphConfiguration

    withRenderOptions(renderOptions: RenderOptions): AMFGraphConfiguration

    withErrorHandlerProvider(provider: ErrorHandlerProvider): AMFGraphConfiguration

    withResourceLoader(rl: ResourceLoader): AMFGraphConfiguration

    withResourceLoaders(rl: Array<ResourceLoader>): AMFGraphConfiguration

    withUnitCache(cache: UnitCache): AMFGraphConfiguration

    withTransformationPipeline(pipeline: TransformationPipeline): AMFGraphConfiguration

    withEventListener(listener: AMFEventListener): AMFGraphConfiguration

    withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): AMFGraphConfiguration

    static empty(): AMFGraphConfiguration

    static predefined(): AMFGraphConfiguration


  }
  export class ShapesConfiguration extends BaseShapesConfiguration  {
    baseUnitClient(): AMLBaseUnitClient

    elementClient(): ShapesElementClient

    configurationState(): AMLConfigurationState

    withParsingOptions(parsingOptions: ParsingOptions): ShapesConfiguration

    withRenderOptions(renderOptions: RenderOptions): ShapesConfiguration

    withErrorHandlerProvider(provider: ErrorHandlerProvider): ShapesConfiguration

    withResourceLoader(rl: ResourceLoader): ShapesConfiguration

    withResourceLoaders(rl: Array<ResourceLoader>): ShapesConfiguration

    withUnitCache(cache: UnitCache): ShapesConfiguration

    withTransformationPipeline(pipeline: TransformationPipeline): ShapesConfiguration

    withEventListener(listener: AMFEventListener): ShapesConfiguration

    withDialect(dialect: Dialect): ShapesConfiguration

    withDialect(url: string): Promise<ShapesConfiguration>

    forInstance(url: string): Promise<ShapesConfiguration>

    withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): ShapesConfiguration

    static empty(): ShapesConfiguration

    static predefined(): ShapesConfiguration


  }
  export class TransformationPipelineBuilder  {
    build(): TransformationPipeline

    withName(newName: string): TransformationPipelineBuilder

    append(newStage: TransformationStep): TransformationPipelineBuilder

    prepend(newStage: TransformationStep): TransformationPipelineBuilder

    static empty(pipelineName: string): TransformationPipelineBuilder

    static fromPipeline(pipeline: TransformationPipeline): TransformationPipelineBuilder

    static fromPipeline(pipelineName: string, conf: AMFGraphConfiguration): undefined | TransformationPipelineBuilder


  }
  export class ProfileName  {
    profile: string
    messageStyle: MessageStyle

    constructor(profile: string)

    toString(): string

    isOas(): boolean

    isRaml(): boolean

    static apply(profile: string): ProfileName


  }
  export class ScalarNode implements DataNode  {
    name: StrField
    customDomainProperties: Array<DomainExtension>
    isExternalLink: BoolField
    id: string
    position: Range
    toString: undefined
    dataType: StrField
    extendsNode: Array<DomainElement>
    value: StrField

    constructor()
    constructor(value: string, dataType: string)

    annotations(): Annotations

    withName(name: string): this

    graph(): Graph

    withIsExternalLink(isExternalLink: boolean): DomainElement

    withDataType(dataType: string): this

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this

    withCustomDomainProperties(extensions: Array<DomainExtension>): this

    withValue(value: string): this

    static build(value: string, dataType: string): any

    withId(id: string): this


  }
  export class AMLConfiguration extends BaseAMLConfiguration  {
    baseUnitClient(): AMLBaseUnitClient

    elementClient(): AMLElementClient

    configurationState(): AMLConfigurationState

    withParsingOptions(parsingOptions: ParsingOptions): AMLConfiguration

    withRenderOptions(renderOptions: RenderOptions): AMLConfiguration

    withErrorHandlerProvider(provider: ErrorHandlerProvider): AMLConfiguration

    withResourceLoader(rl: ResourceLoader): AMLConfiguration

    withResourceLoaders(rl: Array<ResourceLoader>): AMLConfiguration

    withUnitCache(cache: UnitCache): AMLConfiguration

    withTransformationPipeline(pipeline: TransformationPipeline): AMLConfiguration

    withEventListener(listener: AMFEventListener): AMLConfiguration

    withDialect(dialect: Dialect): AMLConfiguration

    withDialect(url: string): Promise<AMLConfiguration>

    forInstance(url: string): Promise<AMLConfiguration>

    withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): AMLConfiguration

    static empty(): AMLConfiguration

    static predefined(): AMLConfiguration


  }
  export class SemanticJsonSchemaConfiguration extends BaseShapesConfiguration  {
    baseUnitClient(): SemanticBaseUnitClient

    configurationState(): AMLConfigurationState

    withParsingOptions(parsingOptions: ParsingOptions): SemanticJsonSchemaConfiguration

    withRenderOptions(renderOptions: RenderOptions): SemanticJsonSchemaConfiguration

    withErrorHandlerProvider(provider: ErrorHandlerProvider): SemanticJsonSchemaConfiguration

    withResourceLoader(rl: ResourceLoader): SemanticJsonSchemaConfiguration

    withResourceLoaders(rl: Array<ResourceLoader>): SemanticJsonSchemaConfiguration

    withUnitCache(cache: UnitCache): SemanticJsonSchemaConfiguration

    withTransformationPipeline(pipeline: TransformationPipeline): SemanticJsonSchemaConfiguration

    withEventListener(listener: AMFEventListener): SemanticJsonSchemaConfiguration

    withDialect(dialect: Dialect): SemanticJsonSchemaConfiguration

    withDialect(url: string): Promise<SemanticJsonSchemaConfiguration>

    forInstance(url: string): Promise<SemanticJsonSchemaConfiguration>

    withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): SemanticJsonSchemaConfiguration

    static empty(): SemanticJsonSchemaConfiguration

    static predefined(): SemanticJsonSchemaConfiguration


  }
  export class ShapeValidationConfiguration  {
    readonly getResults: ClientErrorHandler
    readonly maxYamlReferences: undefined | number

    report(result: AMFValidationResult): void

    eh(): ClientErrorHandler

    fetchContent(url: string): Promise<Content>

    static predefined(): ShapeValidationConfiguration

    static apply(config: AMFGraphConfiguration): ShapeValidationConfiguration


  }
  export class Range  {
    toString: string
    readonly lineFrom: number
    readonly columnFrom: number
    readonly lineTo: number
    readonly columnTo: number
    start: Position
    end: Position
    static readonly ZERO: 0
    static readonly ALL: 0

    constructor(start: Position, end: Position)

    extent(other: Range): Range

    contains(other: Range): boolean

    compareTo(other: Range): number

    static apply(lineFrom: number, columnFrom: number, lineTo: number, columnTo: number): Range

    static apply(start: Position, delta: number): Range

    static apply(start: undefined, end: undefined): Range

    static apply(serialized: string): Range


  }
  export class Position  {
    isZero: boolean
    readonly offsetPart: '@'
    line: number
    column: number
    offset: number
    static readonly ZERO: 0
    static FIRST: Position

    constructor(line: number, column: number, offset: number)

    lt(o: Position): boolean

    min(other: Position): Position

    max(other: Position): Position

    compare(that: Position): number

    compareTo(o: Position): number

    equals(obj: any): boolean

    hashCode(): number

    static apply(line: number, column: number, offset: number): Position

    static apply(offset: number): Position

    static apply(lc: undefined): Position


  }
}
