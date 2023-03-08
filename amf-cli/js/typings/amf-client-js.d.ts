declare module "amf-client-js" {
  export class AMFBaseUnitClient extends BaseAMLBaseUnitClient {
    getConfiguration(): AMFConfiguration;

    parseDocument(url: string): Promise<AMFDocumentResult>;

    parseLibrary(url: string): Promise<AMFLibraryResult>;
  }
  export class AMFConfiguration extends BaseShapesConfiguration {
    baseUnitClient(): AMFBaseUnitClient;

    configurationState(): AMFConfigurationState;

    elementClient(): AMFElementClient;

    forInstance(url: string): Promise<AMFConfiguration>;

    withDialect(dialect: Dialect): AMFConfiguration;

    withDialect(url: string): Promise<AMFConfiguration>;

    withErrorHandlerProvider(provider: ErrorHandlerProvider): AMFConfiguration;

    withEventListener(listener: AMFEventListener): AMFConfiguration;

    withParsingOptions(parsingOptions: ParsingOptions): AMFConfiguration;

    withRenderOptions(renderOptions: RenderOptions): AMFConfiguration;

    withResourceLoader(rl: ResourceLoader): AMFConfiguration;

    withResourceLoaders(rl: Array<ResourceLoader>): AMFConfiguration;

    withShapePayloadPlugin(
      plugin: AMFShapePayloadValidationPlugin
    ): AMFConfiguration;

    withTransformationPipeline(
      pipeline: TransformationPipeline
    ): AMFConfiguration;

    withUnitCache(cache: UnitCache): AMFConfiguration;
  }
  export class AMFConfigurationState extends AMLConfigurationState {}
  export class AMFDocumentResult extends AMFResult {
    document: Document;
  }
  export class AMFElementClient extends BaseShapesElementClient {
    asEndpoint<T>(unit: T, rt: ResourceType, profile: ProfileName): EndPoint;

    asOperation<T>(unit: T, tr: Trait, profile: ProfileName): Operation;

    getConfiguration(): AMFConfiguration;

    renderToBuilder<T>(
      element: DomainElement,
      builder: org.yaml.builder.JsOutputBuilder
    ): void;
  }
  export class AMFElementRenderer {
    static renderToBuilder<T>(
      element: DomainElement,
      builder: org.yaml.builder.JsOutputBuilder,
      config: AMFGraphConfiguration
    ): void;
  }
  export interface AMFEvent {
    readonly name: string;
  }
  export interface AMFEventListener {
    notifyEvent(event: AMFEvent): void;
  }
  export class AMFEventListenerFactory {
    static from(listener: JsAMFEventListener): AMFEventListener;
  }
  export class AMFGraphBaseUnitClient {
    getConfiguration(): AMFGraphConfiguration;

    parse(url: string): Promise<AMFParseResult>;

    parseContent(content: string): Promise<AMFParseResult>;

    parseContent(content: string, mediaType: string): Promise<AMFParseResult>;

    render(baseUnit: BaseUnit): string;

    render(baseUnit: BaseUnit, mediaType: string): string;

    renderGraphToBuilder<T>(
      baseUnit: BaseUnit,
      builder: org.yaml.builder.JsOutputBuilder
    ): T;

    setBaseUri(unit: BaseUnit, base: string): void;

    transform(baseUnit: BaseUnit): AMFResult;

    transform(baseUnit: BaseUnit, pipeline: string): AMFResult;

    validate(baseUnit: BaseUnit): Promise<AMFValidationReport>;
  }
  export class AMFGraphConfiguration {
    baseUnitClient(): AMFGraphBaseUnitClient;

    elementClient(): AMFGraphElementClient;

    static empty(): AMFGraphConfiguration;

    static predefined(): AMFGraphConfiguration;

    withErrorHandlerProvider(
      provider: ErrorHandlerProvider
    ): AMFGraphConfiguration;

    withEventListener(listener: AMFEventListener): AMFGraphConfiguration;

    withParsingOptions(parsingOptions: ParsingOptions): AMFGraphConfiguration;

    withRenderOptions(renderOptions: RenderOptions): AMFGraphConfiguration;

    withResourceLoader(rl: ResourceLoader): AMFGraphConfiguration;

    withResourceLoaders(rl: Array<ResourceLoader>): AMFGraphConfiguration;

    withShapePayloadPlugin(
      plugin: AMFShapePayloadValidationPlugin
    ): AMFGraphConfiguration;

    withTransformationPipeline(
      pipeline: TransformationPipeline
    ): AMFGraphConfiguration;

    withUnitCache(cache: UnitCache): AMFGraphConfiguration;
  }
  export class AMFGraphElementClient {
    getConfiguration(): AMFGraphConfiguration;

    payloadValidatorFor(
      shape: Shape,
      mediaType: string,
      mode: ValidationMode
    ): AMFShapePayloadValidator;

    payloadValidatorFor(
      shape: Shape,
      fragment: PayloadFragment
    ): AMFShapePayloadValidator;
  }
  export class AMFLibraryResult extends AMFResult {
    library: Module;
  }
  export class AMFObjectResult {
    results: Array<AMFValidationResult>;
  }
  export class AMFParseResult extends AMFResult {
    sourceSpec: Spec;
  }
  export class AMFParser {
    static parse(
      url: string,
      configuration: AMFGraphConfiguration
    ): Promise<AMFParseResult>;

    static parseContent(
      content: string,
      configuration: AMFGraphConfiguration
    ): Promise<AMFParseResult>;

    static parseContent(
      content: string,
      mediaType: string,
      configuration: AMFGraphConfiguration
    ): Promise<AMFParseResult>;

    static parseStartingPoint(
      graphUrl: string,
      startingPoint: string,
      configuration: AMFGraphConfiguration
    ): Promise<AMFObjectResult>;
  }
  export class AMFPayloadValidationPluginConverter {
    static toAMF(
      plugin: JsAMFPayloadValidationPlugin
    ): AMFShapePayloadValidationPlugin;
  }
  export class AMFRenderer {
    static render(
      baseUnit: BaseUnit,
      configuration: AMFGraphConfiguration
    ): string;

    static render(
      baseUnit: BaseUnit,
      mediaType: string,
      configuration: AMFGraphConfiguration
    ): string;

    static renderGraphToBuilder<T>(
      baseUnit: BaseUnit,
      builder: org.yaml.builder.JsOutputBuilder,
      configuration: AMFGraphConfiguration
    ): T;
  }
  export class AMFResult extends AMFObjectResult {
    baseUnit: BaseUnit;
    conforms: boolean;
    results: Array<AMFValidationResult>;

    merge(report: AMFValidationReport): AMFResult;

    toString(): string;
  }
  export class AMFSemanticSchemaResult extends AMFParseResult {
    baseUnit: Dialect;
    vocabulary: undefined | Vocabulary;
  }
  export interface AMFShapePayloadValidationPlugin {
    priority: PluginPriority;

    applies(element: ValidatePayloadRequest): boolean;

    validator(
      shape: Shape,
      mediaType: string,
      config: ShapeValidationConfiguration,
      validationMode: ValidationMode
    ): AMFShapePayloadValidator;
  }
  export interface AMFShapePayloadValidator {
    syncValidate(payload: string): AMFValidationReport;

    validate(payload: string): Promise<AMFValidationReport>;

    validate(payloadFragment: PayloadFragment): Promise<AMFValidationReport>;
  }
  export class AMFTransformer {
    static transform(
      unit: BaseUnit,
      configuration: AMFGraphConfiguration
    ): AMFResult;

    static transform(
      unit: BaseUnit,
      pipelineName: string,
      configuration: AMFGraphConfiguration
    ): AMFResult;
  }
  export class AMFValidationReport {
    conforms: boolean;
    model: string;
    profile: ProfileName;
    results: Array<AMFValidationResult>;

    constructor(
      model: string,
      profile: ProfileName,
      results: Array<AMFValidationResult>
    );

    toString(): string;

    toStringMaxed(max: number): string;
  }
  export class AMFValidationResult {
    location: undefined | string;
    message: string;
    position: Range;
    severityLevel: string;
    source: any;
    targetNode: string;
    targetProperty: string;
    validationId: string;

    constructor(
      message: string,
      level: string,
      targetNode: string,
      targetProperty: string,
      validationId: string,
      range: Range,
      location: string
    );
  }
  export class AMFValidator {
    static validate(
      baseUnit: BaseUnit,
      conf: AMFGraphConfiguration
    ): Promise<AMFValidationReport>;
  }
  export class AMLBaseUnitClient extends BaseAMLBaseUnitClient {
    getConfiguration(): AMLConfiguration;
  }
  export class AMLConfiguration extends BaseAMLConfiguration {
    baseUnitClient(): AMLBaseUnitClient;

    configurationState(): AMLConfigurationState;

    elementClient(): AMLElementClient;

    static empty(): AMLConfiguration;

    forInstance(url: string): Promise<AMLConfiguration>;

    static predefined(): AMLConfiguration;

    withDialect(dialect: Dialect): AMLConfiguration;

    withDialect(url: string): Promise<AMLConfiguration>;

    withErrorHandlerProvider(provider: ErrorHandlerProvider): AMLConfiguration;

    withEventListener(listener: AMFEventListener): AMLConfiguration;

    withParsingOptions(parsingOptions: ParsingOptions): AMLConfiguration;

    withRenderOptions(renderOptions: RenderOptions): AMLConfiguration;

    withResourceLoader(rl: ResourceLoader): AMLConfiguration;

    withResourceLoaders(rl: Array<ResourceLoader>): AMLConfiguration;

    withShapePayloadPlugin(
      plugin: AMFShapePayloadValidationPlugin
    ): AMLConfiguration;

    withTransformationPipeline(
      pipeline: TransformationPipeline
    ): AMLConfiguration;

    withUnitCache(cache: UnitCache): AMLConfiguration;
  }
  export class AMLConfigurationState {
    getDialect(name: string): Array<Dialect>;

    getDialect(name: string, version: string): undefined | Dialect;

    getDialects(): Array<Dialect>;

    getExtensions(): Array<SemanticExtension>;
  }
  export class AMLDialectInstanceResult extends AMFResult {
    dialectInstance: DialectInstance;
  }
  export class AMLDialectResult extends AMFResult {
    dialect: Dialect;
  }
  export class AMLElementClient extends BaseAMLElementClient {
    getConfiguration(): AMLConfiguration;

    renderToBuilder<T>(
      element: DomainElement,
      builder: org.yaml.builder.JsOutputBuilder
    ): void;
  }
  export class AMLVocabularyResult extends AMFResult {
    vocabulary: Vocabulary;
  }
  export class APIConfiguration {
    static API(): AMFConfiguration;

    static fromSpec(spec: Spec): AMFConfiguration;
  }
  export class APIContractProcessingData extends BaseUnitProcessingData {
    modelVersion: StrField;
    sourceSpec: StrField;

    constructor();

    withSourceSpec(spec: string): this;

    withSourceSpec(spec: Spec): this;
  }
  export class AbstractDeclaration implements DomainElement, Linkable {
    customDomainProperties: Array<DomainExtension>;
    dataNode: DataNode;
    description: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    name: StrField;
    position: Range;
    variables: Array<StrField>;

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): AbstractDeclaration;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDataNode(dataNode: DataNode): this;

    withDescription(description: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withName(name: string): this;

    withVariables(variables: Array<string>): this;
  }
  export class AbstractElementTransformer {
    static asEndpoint<T>(
      unit: T,
      rt: ResourceType,
      errorHandler: ClientErrorHandler,
      configuration: AMFGraphConfiguration,
      profile: ProfileName
    ): EndPoint;

    static asOperation<T>(
      unit: T,
      tr: Trait,
      errorHandler: ClientErrorHandler,
      configuration: AMFGraphConfiguration,
      profile: ProfileName
    ): Operation;
  }
  export class AbstractOperation implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    method: StrField;
    name: StrField;
    position: Range;
    request: AbstractRequest;
    response: AbstractResponse;
    responses: Array<AbstractResponse>;

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDescription(description: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withMethod(method: string): this;

    withName(name: string): this;

    withRequest(name: string): AbstractRequest;

    withRequest(request: AbstractRequest): this;

    withResponse(name: string): AbstractResponse;

    withResponses(responses: Array<AbstractResponse>): this;
  }
  export class AbstractParameter implements DomainElement {
    binding: StrField;
    customDomainProperties: Array<DomainExtension>;
    defaultValue: DataNode;
    description: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    parameterName: StrField;
    position: Range;
    required: BoolField;
    schema: Shape;

    annotations(): Annotations;

    cloneParameter(parent: string): this;

    graph(): Graph;

    withBinding(binding: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDefaultValue(defaultValue: DataNode): this;

    withDescription(description: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withObjectSchema(name: string): NodeShape;

    withParameterName(name: string): this;

    withRequired(required: boolean): this;

    withScalarSchema(name: string): ScalarShape;

    withSchema(schema: Shape): this;
  }
  export class AbstractPayload implements DomainElement, Linkable {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    mediaType: StrField;
    name: StrField;
    position: Range;
    schema: Shape;

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): Linkable;

    withArraySchema(name: string): ArrayShape;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withMediaType(mediaType: string): this;

    withName(name: string): this;

    withObjectSchema(name: string): NodeShape;

    withScalarSchema(name: string): ScalarShape;

    withSchema(schema: Shape): this;
  }
  export interface AbstractRequest extends DomainElement {
    name: StrField;
    queryParameters: Array<AbstractParameter>;

    withName(name: string): this;

    withQueryParameter(name: string): AbstractParameter;

    withQueryParameters(parameters: Array<AbstractParameter>): this;
  }
  export interface AbstractResponse extends DomainElement {
    name: StrField;
    payload: AbstractPayload;

    withName(name: string): this;

    withPayload(payload: AbstractPayload): AbstractPayload;
  }
  export class Ajv {
    readonly errors: undefined;

    addFormat(name: string, formatValidator: any): Ajv;

    addMetaSchema(metaSchema: undefined): Ajv;

    validate(schema: undefined, data: undefined): boolean;
  }
  export interface AmfObjectWrapper extends Annotable {
    annotations(): Annotations;
  }
  export class AmlDomainElementEmitter {
    static emitToBuilder<T>(
      element: DomainElement,
      amlConfig: BaseAMLConfiguration,
      builder: org.yaml.builder.JsOutputBuilder
    ): void;
  }
  export class Amqp091ChannelBinding implements ChannelBinding {
    customDomainProperties: Array<DomainExtension>;
    exchange: Amqp091ChannelExchange;
    extendsNode: Array<DomainElement>;
    id: string;
    is: StrField;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    position: Range;
    queue: Amqp091Queue;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): Amqp091ChannelBinding;

    withBindingVersion(bindingVersion: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExchange(exchange: Amqp091ChannelExchange): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIs(is: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withQueue(queue: Amqp091Queue): this;
  }
  export class Amqp091ChannelExchange implements DomainElement {
    autoDelete: BoolField;
    customDomainProperties: Array<DomainExtension>;
    durable: BoolField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    type: StrField;
    vHost: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withAutoDelete(autoDelete: boolean): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDurable(durable: boolean): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withType(type: string): this;

    withVHost(vHost: string): this;
  }
  export class Amqp091MessageBinding implements MessageBinding {
    contentEncoding: StrField;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    messageType: StrField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): Amqp091MessageBinding;

    withBindingVersion(bindingVersion: string): this;

    withContentEncoding(contentEncoding: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withMessageType(messageType: string): this;
  }
  export class Amqp091OperationBinding implements OperationBinding {
    ack: BoolField;
    bcc: Array<StrField>;
    cc: Array<StrField>;
    customDomainProperties: Array<DomainExtension>;
    deliveryMode: IntField;
    expiration: IntField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    mandatory: BoolField;
    position: Range;
    priority: IntField;
    replyTo: StrField;
    timestamp: BoolField;
    userId: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): Amqp091OperationBinding;

    withAck(ack: boolean): this;

    withBcc(bCC: Array<string>): this;

    withBindingVersion(bindingVersion: string): this;

    withCc(cC: Array<string>): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDeliveryMode(deliveryMode: number): this;

    withExpiration(expiration: number): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withMandatory(mandatory: boolean): this;

    withPriority(priority: number): this;

    withReplyTo(replyTo: string): this;

    withTimestamp(timestamp: boolean): this;

    withUserId(userId: string): this;
  }
  export class Amqp091Queue implements DomainElement {
    autoDelete: BoolField;
    customDomainProperties: Array<DomainExtension>;
    durable: BoolField;
    exclusive: BoolField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    vHost: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withAutoDelete(autoDelete: boolean): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDurable(durable: boolean): this;

    withExclusive(exclusive: boolean): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withVHost(vHost: string): this;
  }
  export interface Annotable {
    annotations(): Annotations;
  }
  export class AnnotationMapping implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;

    constructor();

    allowMultiple(): BoolField;

    annotations(): Annotations;

    domain(): Array<StrField>;

    enum(): Array<AnyField>;

    externallyLinkable(): BoolField;

    graph(): Graph;

    literalRange(): StrField;

    maximum(): DoubleField;

    minCount(): IntField;

    minimum(): DoubleField;

    name(): StrField;

    nodePropertyMapping(): StrField;

    objectRange(): Array<StrField>;

    pattern(): StrField;

    sorted(): BoolField;

    typeDiscriminator(): Map<string, string>;

    typeDiscriminatorName(): StrField;

    withAllowMultiple(allow: boolean): AnnotationMapping;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDomain(domains: Array<string>): AnnotationMapping;

    withEnum(values: Array<any>): AnnotationMapping;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withExternallyLinkable(linkable: boolean): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLiteralRange(range: string): AnnotationMapping;

    withMaximum(max: number): AnnotationMapping;

    withMinCount(minCount: number): AnnotationMapping;

    withMinimum(min: number): AnnotationMapping;

    withName(name: string): AnnotationMapping;

    withNodePropertyMapping(propertyId: string): AnnotationMapping;

    withObjectRange(range: Array<string>): AnnotationMapping;

    withPattern(pattern: string): AnnotationMapping;

    withSorted(sorted: boolean): AnnotationMapping;

    withTypeDiscriminator(typesMapping: Map<string, string>): AnnotationMapping;

    withTypeDiscriminatorName(name: string): AnnotationMapping;
  }
  export class AnnotationTypeDeclaration extends Fragment {
    constructor();
  }
  export class Annotations {
    autoGeneratedName: boolean;
    inheritanceProvenance: undefined | string;
    inlinedElement: boolean;
    isLocal: boolean;
    isTracked: boolean;
    resolvedLink: undefined | string;
    resolvedLinkTarget: undefined | string;

    constructor();

    custom(): Array<DomainExtension>;

    fragmentName(): undefined | string;

    isTrackedBy(trackId: string): boolean;

    lexical(): Range;

    location(): undefined | string;
  }
  export class AnyField implements ValueField<any> {
    isNull: boolean;
    nonNull: boolean;
    readonly option: undefined | any;

    annotations(): Annotations;

    is(other: any): boolean;

    is(accepts: undefined): boolean;

    remove(): void;

    toString(): string;

    value(): any;
  }
  export class AnyMapping implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;

    and(): Array<StrField>;

    annotations(): Annotations;

    components(): Array<StrField>;

    elseMapping(): StrField;

    graph(): Graph;

    ifMapping(): StrField;

    or(): Array<StrField>;

    thenMapping(): StrField;

    withAnd(andMappings: Array<string>): AnyMapping;

    withComponents(components: Array<string>): AnyMapping;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withElseMapping(elseMapping: string): AnyMapping;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIfMapping(ifMapping: string): AnyMapping;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withOr(orMappings: Array<string>): AnyMapping;

    withThenMapping(thenMapping: string): AnyMapping;
  }
  export class AnyShape implements Shape {
    and: Array<Shape>;
    comment: StrField;
    customDomainProperties: Array<DomainExtension>;
    customShapeProperties: Array<ShapeExtension>;
    customShapePropertyDefinitions: Array<PropertyShape>;
    defaultValue: DataNode;
    defaultValueStr: StrField;
    deprecated: BoolField;
    description: StrField;
    displayName: StrField;
    documentation: CreativeWork;
    elseShape: Shape;
    examples: Array<Example>;
    extendsNode: Array<DomainElement>;
    federationMetadata: ShapeFederationMetadata;
    hasExplicitName: boolean;
    id: string;
    ifShape: Shape;
    inherits: Array<Shape>;
    isExtension: BoolField;
    isExternalLink: BoolField;
    isLink: boolean;
    isNotExplicit: boolean;
    isStub: BoolField;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    name: StrField;
    not: Shape;
    or: Array<Shape>;
    position: Range;
    readOnly: BoolField;
    thenShape: Shape;
    values: Array<DataNode>;
    writeOnly: BoolField;
    xmlSerialization: XMLSerializer;
    xone: Array<Shape>;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    inlined(): boolean;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): AnyShape;

    trackedExample(trackId: string): undefined | Example;

    withAnd(subShapes: Array<Shape>): this;

    withComment(comment: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withCustomShapeProperties(
      customShapeProperties: Array<ShapeExtension>
    ): this;

    withCustomShapePropertyDefinition(name: string): PropertyShape;

    withCustomShapePropertyDefinitions(
      propertyDefinitions: Array<PropertyShape>
    ): this;

    withDefaultStr(value: string): this;

    withDefaultValue(defaultVal: DataNode): this;

    withDeprecated(deprecated: boolean): this;

    withDescription(description: string): this;

    withDisplayName(name: string): this;

    withDocumentation(documentation: CreativeWork): this;

    withElse(elseShape: Shape): this;

    withExample(mediaType: string): Example;

    withExamples(examples: Array<Example>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withFederationMetadata(metadata: ShapeFederationMetadata): this;

    withId(id: string): this;

    withIf(ifShape: Shape): this;

    withInherits(inherits: Array<Shape>): this;

    withIsExtension(value: boolean): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withIsStub(value: boolean): this;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withName(name: string): this;

    withNode(shape: Shape): this;

    withOr(subShapes: Array<Shape>): this;

    withReadOnly(readOnly: boolean): this;

    withThen(thenShape: Shape): this;

    withValues(values: Array<DataNode>): this;

    withWriteOnly(writeOnly: boolean): this;

    withXMLSerialization(xmlSerialization: XMLSerializer): this;

    withXone(subShapes: Array<Shape>): this;
  }
  export class Api<A> implements DomainElement {
    accepts: Array<StrField>;
    contentType: Array<StrField>;
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    documentations: Array<CreativeWork>;
    endPoints: Array<EndPoint>;
    extendsNode: Array<DomainElement>;
    id: string;
    identifier: StrField;
    isExternalLink: BoolField;
    license: License;
    name: StrField;
    position: Range;
    provider: Organization;
    schemes: Array<StrField>;
    security: Array<SecurityRequirement>;
    servers: Array<Server>;
    tags: Array<Tag>;
    termsOfService: StrField;
    version: StrField;

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDefaultServer(url: string): Server;

    withDocumentationTitle(title: string): CreativeWork;

    withDocumentationUrl(url: string): CreativeWork;

    withEndPoint(path: string): EndPoint;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withServer(url: string): Server;
  }
  export class ApiKeySettings extends Settings {
    in: StrField;
    name: StrField;

    constructor();

    withIn(inVal: string): this;

    withName(name: string): this;
  }
  export class ArrayNode implements DataNode {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    members: Array<DataNode>;
    name: StrField;
    position: Range;

    constructor();

    addMember(member: DataNode): this;

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;
  }
  export class ArrayShape extends DataArrangeShape {
    contains: Shape;
    items: Shape;
    maxContains: number;
    minContains: number;
    unevaluatedItems: boolean;
    unevaluatedItemsSchema: Shape;

    constructor();

    linkCopy(): ArrayShape;

    withContains(contains: Shape): this;

    withItems(items: Shape): this;

    withMaxContains(amount: number): this;

    withMinContains(amount: number): this;

    withUnevaluatedItems(value: boolean): this;

    withUnevaluatedItemsSchema(schema: Shape): this;
  }
  export class AsyncAPIConfiguration {
    static Async20(): AMFConfiguration;
  }
  export class AsyncApi extends Api<AsyncApi> {
    accepts: Array<StrField>;
    contentType: Array<StrField>;
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    documentations: Array<CreativeWork>;
    endPoints: Array<EndPoint>;
    extendsNode: Array<DomainElement>;
    id: string;
    identifier: StrField;
    isExternalLink: BoolField;
    license: License;
    name: StrField;
    position: Range;
    provider: Organization;
    schemes: Array<StrField>;
    security: Array<SecurityRequirement>;
    servers: Array<Server>;
    tags: Array<Tag>;
    termsOfService: StrField;
    version: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withAccepts(accepts: Array<string>): this;

    withContentType(contentType: Array<string>): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDefaultServer(url: string): Server;

    withDescription(description: string): this;

    withDocumentation(documentations: Array<CreativeWork>): this;

    withDocumentationTitle(title: string): CreativeWork;

    withDocumentationUrl(url: string): CreativeWork;

    withEndPoint(path: string): EndPoint;

    withEndPoints(endPoints: Array<EndPoint>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIdentifier(identifier: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLicense(license: License): this;

    withName(name: string): this;

    withProvider(provider: Organization): this;

    withSchemes(schemes: Array<string>): this;

    withSecurity(security: Array<SecurityRequirement>): this;

    withServer(url: string): Server;

    withServers(servers: Array<Server>): this;

    withTags(tags: Array<Tag>): this;

    withTermsOfService(terms: string): this;

    withVersion(version: string): this;
  }
  export class BaseAMLBaseUnitClient extends AMFGraphBaseUnitClient {
    parseDialect(url: string): Promise<AMLDialectResult>;

    parseDialectInstance(url: string): Promise<AMLDialectInstanceResult>;

    parseVocabulary(url: string): Promise<AMLVocabularyResult>;
  }
  export class BaseAMLConfiguration extends AMFGraphConfiguration {
    withDialect(dialect: Dialect): BaseAMLConfiguration;

    withErrorHandlerProvider(
      provider: ErrorHandlerProvider
    ): BaseAMLConfiguration;

    withEventListener(listener: AMFEventListener): BaseAMLConfiguration;

    withParsingOptions(parsingOptions: ParsingOptions): BaseAMLConfiguration;

    withRenderOptions(renderOptions: RenderOptions): BaseAMLConfiguration;

    withResourceLoader(rl: ResourceLoader): BaseAMLConfiguration;

    withResourceLoaders(rl: Array<ResourceLoader>): BaseAMLConfiguration;

    withTransformationPipeline(
      pipeline: TransformationPipeline
    ): BaseAMLConfiguration;

    withUnitCache(cache: UnitCache): BaseAMLConfiguration;
  }
  export class BaseAMLElementClient extends AMFGraphElementClient {
    renderToBuilder<T>(
      element: DomainElement,
      builder: org.yaml.builder.JsOutputBuilder
    ): void;
  }
  export interface BaseFileResourceLoader extends ResourceLoader {
    accepts(resource: string): boolean;

    fetch(resource: string): Promise<Content>;

    fetchFile(resource: string): Promise<Content>;
  }
  export class BaseHttpResourceLoader implements ResourceLoader {
    accepts(resource: string): boolean;

    fetch(resource: string): Promise<Content>;
  }
  export class BaseIri implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    iri: StrField;
    isExternalLink: BoolField;
    nulled: BoolField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIri(iri: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withNulled(nulled: boolean): this;
  }
  export class BaseShapesConfiguration extends BaseAMLConfiguration {
    withDialect(dialect: Dialect): BaseShapesConfiguration;

    withErrorHandlerProvider(
      provider: ErrorHandlerProvider
    ): BaseShapesConfiguration;

    withEventListener(listener: AMFEventListener): BaseShapesConfiguration;

    withParsingOptions(parsingOptions: ParsingOptions): BaseShapesConfiguration;

    withRenderOptions(renderOptions: RenderOptions): BaseShapesConfiguration;

    withResourceLoader(rl: ResourceLoader): BaseShapesConfiguration;

    withResourceLoaders(rl: Array<ResourceLoader>): BaseShapesConfiguration;

    withTransformationPipeline(
      pipeline: TransformationPipeline
    ): BaseShapesConfiguration;

    withUnitCache(cache: UnitCache): BaseShapesConfiguration;
  }
  export class BaseShapesElementClient extends BaseAMLElementClient {
    buildJsonSchema(element: AnyShape): string;

    renderExample(example: Example, mediaType: string): string;

    renderToBuilder<T>(
      element: DomainElement,
      builder: org.yaml.builder.JsOutputBuilder
    ): void;

    toJsonSchema(element: AnyShape): string;

    toRamlDatatype(element: AnyShape): string;
  }
  export interface BaseUnit extends AmfObjectWrapper {
    id: string;
    location: string;
    modelVersion: StrField;
    processingData: BaseUnitProcessingData;
    raw: undefined | string;
    sourceInformation: BaseUnitSourceInformation;
    sourceSpec: undefined | Spec;
    usage: StrField;

    annotations(): Annotations;

    cloneUnit(): BaseUnit;

    findById(id: string): undefined | DomainElement;

    findByType(typeId: string): Array<DomainElement>;

    pkg(): StrField;

    references(): Array<BaseUnit>;

    withId(id: string): this;

    withLocation(location: string): this;

    withPkg(pkg: string): this;

    withProcessingData(data: BaseUnitProcessingData): this;

    withRaw(raw: string): this;

    withReferenceAlias(
      alias: string,
      id: string,
      fullUrl: string,
      relativeUrl: string
    ): BaseUnit;

    withReferences(references: Array<BaseUnit>): this;

    withUsage(usage: string): this;
  }
  export class BaseUnitProcessingData implements AmfObjectWrapper {
    transformed: BoolField;

    constructor();

    annotations(): Annotations;

    withTransformed(value: boolean): this;
  }
  export class BaseUnitSourceInformation implements AmfObjectWrapper {
    additionalLocations: Array<LocationInformation>;
    rootLocation: StrField;

    constructor();

    annotations(): Annotations;

    withAdditionalLocations(locations: Array<LocationInformation>): this;

    withRootLocation(value: string): this;
  }
  export class BoolField implements ValueField<boolean> {
    isNull: boolean;
    nonNull: boolean;
    readonly option: undefined | boolean;

    annotations(): Annotations;

    is(other: boolean): boolean;

    is(accepts: undefined): boolean;

    remove(): void;

    toString(): string;

    value(): boolean;
  }
  export class CachedReference {
    content: BaseUnit;
    url: string;

    constructor(url: string, content: BaseUnit);
  }
  export class Callback implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    endpoint: EndPoint;
    expression: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withEndpoint(endpoint: EndPoint): this;

    withEndpoint(path: string): EndPoint;

    withExpression(expression: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;
  }
  export interface ChannelBinding extends DomainElement, Linkable {}
  export class ChannelBindings implements DomainElement, Linkable {
    bindings: Array<ChannelBinding>;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    name: StrField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): ChannelBindings;

    withBindings(bindings: Array<ChannelBinding>): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withName(name: string): this;
  }
  export class ClassTerm implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    displayName: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    properties: Array<StrField>;
    subClassOf: Array<StrField>;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDescription(description: string): ClassTerm;

    withDisplayName(displayName: string): ClassTerm;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): ClassTerm;

    withProperties(properties: Array<string>): ClassTerm;

    withSubClassOf(superClasses: Array<string>): ClassTerm;
  }
  export interface ClientErrorHandler {
    getResults: Array<AMFValidationResult>;

    report(result: AMFValidationResult): void;
  }
  export interface ClientResourceLoader {
    accepts(resource: string): boolean;

    fetch(resource: string): Promise<Content>;
  }
  export interface ClientUnitCache {
    fetch(url: string): Promise<CachedReference>;
  }
  export class ClientUnitCacheAdapter {
    static adapt(obj: ClientUnitCache): UnitCache;
  }
  export interface ClientWriter {
    append(s: string): this;

    close(): this;

    flush(): this;

    string(): string;
  }
  export class ComponentModule extends Module {
    name: StrField;
    version: StrField;

    constructor();

    withName(name: string): this;

    withVersion(version: string): this;
  }
  export class Content {
    constructor(stream: string, url: string);
    constructor(stream: string, url: string, mime: string);
    readonly url: string;
  }
  export class ContextMapping implements DomainElement {
    alias: StrField;
    coercion: StrField;
    containers: Array<StrField>;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    iri: StrField;
    isExternalLink: BoolField;
    nulled: BoolField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withAlias(alias: string): this;

    withCoercion(coersion: string): this;

    withContainers(containers: Array<string>): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIri(iri: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withNulled(nulled: boolean): this;
  }
  export class CorrelationId implements DomainElement, Linkable {
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    idLocation: StrField;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    name: StrField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): CorrelationId;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDescription(description: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIdLocation(idLocation: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withName(name: string): this;
  }
  export class CreativeWork implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;
    title: StrField;
    url: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDescription(description: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withTitle(title: string): this;

    withUrl(url: string): this;
  }
  export class CuriePrefix implements DomainElement {
    alias: StrField;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    iri: StrField;
    isExternalLink: BoolField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withAlias(alias: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIri(iri: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;
  }
  export class CustomDomainProperty implements DomainElement, Linkable {
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    displayName: StrField;
    domain: Array<StrField>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    name: StrField;
    position: Range;
    repeatable: BoolField;
    schema: Shape;
    serializationOrder: IntField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): CustomDomainProperty;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDescription(description: string): this;

    withDisplayName(displayName: string): this;

    withDomain(domain: Array<string>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withName(name: string): this;

    withRepeatable(repeatable: boolean): this;

    withSchema(schema: Shape): this;

    withSerializationOrder(order: number): this;
  }
  export interface CustomizableElement {
    customDomainProperties: Array<DomainExtension>;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;
  }
  export class DataArrangeShape extends AnyShape {
    maxItems: IntField;
    minItems: IntField;
    uniqueItems: BoolField;

    withMaxItems(maxItems: number): this;

    withMinItems(minItems: number): this;

    withUniqueItems(uniqueItems: boolean): this;
  }
  export interface DataNode extends DomainElement {
    name: StrField;

    withName(name: string): this;
  }
  export class DataType extends Fragment {
    constructor();
  }
  export class DataTypes {
    static readonly Any: string;
    static readonly AnyUri: string;
    static readonly Binary: string;
    static readonly Boolean: string;
    static readonly Byte: string;
    static readonly Date: string;
    static readonly DateTime: string;
    static readonly DateTimeOnly: string;
    static readonly Decimal: string;
    static readonly Double: string;
    static readonly File: string;
    static readonly Float: string;
    static readonly Integer: string;
    static readonly Long: string;
    static readonly Nil: string;
    static readonly Number: string;
    static readonly Password: string;
    static readonly String: string;
    static readonly Time: string;
  }
  export class DatatypePropertyTerm extends PropertyTerm {
    constructor();
  }
  export interface DeclaresModel extends AmfObjectWrapper {
    declares: Array<DomainElement>;

    annotations(): Annotations;

    withDeclaredElement(declared: DomainElement): this;

    withDeclares(declares: Array<DomainElement>): this;
  }
  export class DefaultExecutionEnvironment {
    static apply(): ExecutionEnvironment;
  }
  export class DefaultVocabulary implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    iri: StrField;
    isExternalLink: BoolField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIri(iri: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;
  }
  export class DetectedSyntaxMediaTypeEvent {}
  export class Dialect implements BaseUnit, EncodesModel, DeclaresModel {
    allHeaders: Array<string>;
    declares: Array<DomainElement>;
    encodes: DomainElement;
    externals: Array<External>;
    fragmentHeaders: Array<string>;
    header: string;
    id: string;
    libraryHeader: undefined | string;
    location: string;
    modelVersion: StrField;
    name: StrField;
    nameAndVersion: string;
    processingData: BaseUnitProcessingData;
    raw: undefined | string;
    sourceInformation: BaseUnitSourceInformation;
    sourceSpec: undefined | Spec;
    usage: StrField;
    version: StrField;

    constructor();

    annotations(): Annotations;

    cloneUnit(): BaseUnit;

    documents(): DocumentsModel;

    extensions(): Array<SemanticExtension>;

    findById(id: string): undefined | DomainElement;

    findByType(typeId: string): Array<DomainElement>;

    isFragmentHeader(header: string): boolean;

    isLibraryHeader(header: string): boolean;

    pkg(): StrField;

    references(): Array<BaseUnit>;

    withDeclaredElement(declared: DomainElement): this;

    withDeclares(declares: Array<DomainElement>): this;

    withDocuments(documentsMapping: DocumentsModel): Dialect;

    withEncodes(encoded: DomainElement): this;

    withExternals(externals: Array<External>): Dialect;

    withId(id: string): this;

    withLocation(location: string): this;

    withName(name: string): Dialect;

    withPkg(pkg: string): this;

    withProcessingData(data: BaseUnitProcessingData): this;

    withRaw(raw: string): this;

    withReferenceAlias(
      alias: string,
      id: string,
      fullUrl: string,
      relativeUrl: string
    ): BaseUnit;

    withReferences(references: Array<BaseUnit>): this;

    withUsage(usage: string): this;

    withVersion(version: string): Dialect;
  }
  export class DialectDomainElement implements DomainElement, Linkable {
    customDomainProperties: Array<DomainExtension>;
    declarationName: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    position: Range;

    constructor();

    annotations(): Annotations;

    containsProperty(property: PropertyMapping): boolean;

    definedBy(): NodeMapping;

    getObjectByProperty(iri: string): Array<DialectDomainElement>;

    getPropertyIris(): Array<string>;

    getTypeIris(): Array<string>;

    graph(): Graph;

    includeName(): string;

    isAbstract(): BoolField;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): DialectDomainElement;

    localRefName(): string;

    withAbstract(isAbstract: boolean): DialectDomainElement;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDeclarationName(name: string): DialectDomainElement;

    withDefinedby(nodeMapping: NodeMapping): DialectDomainElement;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withInstanceTypes(types: Array<string>): DialectDomainElement;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withLiteralProperty(propertyIri: string, value: boolean): this;

    withLiteralProperty(propertyIri: string, value: number): this;

    withLiteralProperty(propertyIri: string, value: Array<any>): this;

    withLiteralProperty(propertyIri: string, value: string): this;

    withObjectCollectionProperty(
      propertyIri: string,
      value: Array<DialectDomainElement>
    ): this;

    withObjectProperty(iri: string, value: DialectDomainElement): this;
  }
  export class DialectFragment implements BaseUnit, EncodesModel {
    encodes: NodeMapping;
    externals: Array<External>;
    id: string;
    location: string;
    modelVersion: StrField;
    processingData: BaseUnitProcessingData;
    raw: undefined | string;
    sourceInformation: BaseUnitSourceInformation;
    sourceSpec: undefined | Spec;
    usage: StrField;

    constructor();

    annotations(): Annotations;

    cloneUnit(): BaseUnit;

    findById(id: string): undefined | DomainElement;

    findByType(typeId: string): Array<DomainElement>;

    pkg(): StrField;

    references(): Array<BaseUnit>;

    withEncodes(encoded: DomainElement): this;

    withEncodes(nodeMapping: NodeMapping): DialectFragment;

    withExternals(externals: Array<External>): DialectFragment;

    withId(id: string): this;

    withLocation(location: string): this;

    withPkg(pkg: string): this;

    withProcessingData(data: BaseUnitProcessingData): this;

    withRaw(raw: string): this;

    withReferenceAlias(
      alias: string,
      id: string,
      fullUrl: string,
      relativeUrl: string
    ): BaseUnit;

    withReferences(references: Array<BaseUnit>): this;

    withUsage(usage: string): this;
  }
  export class DialectInstance
    implements BaseUnit, EncodesModel, DeclaresModel, DialectInstanceUnit
  {
    declares: Array<DomainElement>;
    encodes: DialectDomainElement;
    externals: Array<External>;
    id: string;
    location: string;
    modelVersion: StrField;
    processingData: DialectInstanceProcessingData;
    raw: undefined | string;
    sourceInformation: BaseUnitSourceInformation;
    sourceSpec: undefined | Spec;
    usage: StrField;

    constructor();

    annotations(): Annotations;

    cloneUnit(): BaseUnit;

    definedBy(): StrField;

    findById(id: string): undefined | DomainElement;

    findByType(typeId: string): Array<DomainElement>;

    graphDependencies(): Array<StrField>;

    pkg(): StrField;

    references(): Array<BaseUnit>;

    withDeclaredElement(declared: DomainElement): this;

    withDeclares(declares: Array<DomainElement>): this;

    withDefinedBy(dialectId: string): this;

    withEncodes(encoded: DialectDomainElement): DialectInstance;

    withEncodes(encoded: DomainElement): this;

    withExternals(externals: Array<External>): DialectInstance;

    withGraphDependencies(ids: Array<string>): this;

    withId(id: string): this;

    withLocation(location: string): this;

    withPkg(pkg: string): this;

    withProcessingData(data: DialectInstanceProcessingData): this;

    withProcessingData(data: BaseUnitProcessingData): this;

    withRaw(raw: string): this;

    withReferenceAlias(
      alias: string,
      id: string,
      fullUrl: string,
      relativeUrl: string
    ): BaseUnit;

    withReferences(references: Array<BaseUnit>): this;

    withUsage(usage: string): this;
  }
  export class DialectInstanceFragment
    implements BaseUnit, EncodesModel, DialectInstanceUnit
  {
    encodes: DialectDomainElement;
    id: string;
    location: string;
    modelVersion: StrField;
    processingData: DialectInstanceProcessingData;
    raw: undefined | string;
    sourceInformation: BaseUnitSourceInformation;
    sourceSpec: undefined | Spec;
    usage: StrField;

    constructor();

    annotations(): Annotations;

    cloneUnit(): BaseUnit;

    definedBy(): StrField;

    findById(id: string): undefined | DomainElement;

    findByType(typeId: string): Array<DomainElement>;

    graphDependencies(): Array<StrField>;

    pkg(): StrField;

    references(): Array<BaseUnit>;

    withDefinedBy(dialectId: string): this;

    withEncodes(encoded: DialectDomainElement): DialectInstanceFragment;

    withEncodes(encoded: DomainElement): this;

    withGraphDependencies(ids: Array<string>): this;

    withId(id: string): this;

    withLocation(location: string): this;

    withPkg(pkg: string): this;

    withProcessingData(data: DialectInstanceProcessingData): this;

    withProcessingData(data: BaseUnitProcessingData): this;

    withRaw(raw: string): this;

    withReferenceAlias(
      alias: string,
      id: string,
      fullUrl: string,
      relativeUrl: string
    ): BaseUnit;

    withReferences(references: Array<BaseUnit>): this;

    withUsage(usage: string): this;
  }
  export class DialectInstanceLibrary
    implements BaseUnit, DeclaresModel, DialectInstanceUnit
  {
    declares: Array<DomainElement>;
    id: string;
    location: string;
    modelVersion: StrField;
    processingData: DialectInstanceProcessingData;
    raw: undefined | string;
    sourceInformation: BaseUnitSourceInformation;
    sourceSpec: undefined | Spec;
    usage: StrField;

    constructor();

    annotations(): Annotations;

    cloneUnit(): BaseUnit;

    definedBy(): StrField;

    findById(id: string): undefined | DomainElement;

    findByType(typeId: string): Array<DomainElement>;

    graphDependencies(): Array<StrField>;

    pkg(): StrField;

    references(): Array<BaseUnit>;

    withDeclaredElement(declared: DomainElement): this;

    withDeclares(declares: Array<DomainElement>): this;

    withDefinedBy(dialectId: string): this;

    withGraphDependencies(ids: Array<string>): this;

    withId(id: string): this;

    withLocation(location: string): this;

    withPkg(pkg: string): this;

    withProcessingData(data: DialectInstanceProcessingData): this;

    withProcessingData(data: BaseUnitProcessingData): this;

    withRaw(raw: string): this;

    withReferenceAlias(
      alias: string,
      id: string,
      fullUrl: string,
      relativeUrl: string
    ): BaseUnit;

    withReferences(references: Array<BaseUnit>): this;

    withUsage(usage: string): this;
  }
  export class DialectInstancePatch
    implements BaseUnit, EncodesModel, DeclaresModel, DialectInstanceUnit
  {
    declares: Array<DomainElement>;
    encodes: DialectDomainElement;
    externals: Array<External>;
    id: string;
    location: string;
    modelVersion: StrField;
    processingData: DialectInstanceProcessingData;
    raw: undefined | string;
    sourceInformation: BaseUnitSourceInformation;
    sourceSpec: undefined | Spec;
    usage: StrField;

    constructor();

    annotations(): Annotations;

    cloneUnit(): BaseUnit;

    definedBy(): StrField;

    findById(id: string): undefined | DomainElement;

    findByType(typeId: string): Array<DomainElement>;

    graphDependencies(): Array<StrField>;

    pkg(): StrField;

    references(): Array<BaseUnit>;

    withDeclaredElement(declared: DomainElement): this;

    withDeclares(declares: Array<DomainElement>): this;

    withDefinedBy(dialectId: string): this;

    withEncodes(encoded: DialectDomainElement): DialectInstancePatch;

    withEncodes(encoded: DomainElement): this;

    withExternals(externals: Array<External>): DialectInstancePatch;

    withGraphDependencies(ids: Array<string>): this;

    withId(id: string): this;

    withLocation(location: string): this;

    withPkg(pkg: string): this;

    withProcessingData(data: DialectInstanceProcessingData): this;

    withProcessingData(data: BaseUnitProcessingData): this;

    withRaw(raw: string): this;

    withReferenceAlias(
      alias: string,
      id: string,
      fullUrl: string,
      relativeUrl: string
    ): BaseUnit;

    withReferences(references: Array<BaseUnit>): this;

    withUsage(usage: string): this;
  }
  export class DialectInstanceProcessingData extends BaseUnitProcessingData {
    constructor();

    definedBy(): StrField;

    graphDependencies(): Array<StrField>;

    withDefinedBy(dialectId: string): DialectInstanceProcessingData;

    withGraphDependencies(ids: Array<string>): DialectInstanceProcessingData;
  }
  export interface DialectInstanceUnit extends BaseUnit {
    processingData: DialectInstanceProcessingData;

    definedBy(): StrField;

    graphDependencies(): Array<StrField>;

    withDefinedBy(dialectId: string): this;

    withGraphDependencies(ids: Array<string>): this;

    withProcessingData(data: DialectInstanceProcessingData): this;
  }
  export class DialectLibrary implements BaseUnit, DeclaresModel {
    declares: Array<DomainElement>;
    externals: Array<External>;
    id: string;
    location: string;
    modelVersion: StrField;
    processingData: BaseUnitProcessingData;
    raw: undefined | string;
    sourceInformation: BaseUnitSourceInformation;
    sourceSpec: undefined | Spec;
    usage: StrField;

    constructor();

    annotations(): Annotations;

    cloneUnit(): BaseUnit;

    findById(id: string): undefined | DomainElement;

    findByType(typeId: string): Array<DomainElement>;

    nodeMappings(): Array<NodeMapping>;

    pkg(): StrField;

    references(): Array<BaseUnit>;

    withDeclaredElement(declared: DomainElement): this;

    withDeclares(declares: Array<DomainElement>): this;

    withExternals(externals: Array<External>): DialectLibrary;

    withId(id: string): this;

    withLocation(location: string): this;

    withNodeMappings(nodeMappings: Array<NodeMapping>): DialectLibrary;

    withPkg(pkg: string): this;

    withProcessingData(data: BaseUnitProcessingData): this;

    withRaw(raw: string): this;

    withReferenceAlias(
      alias: string,
      id: string,
      fullUrl: string,
      relativeUrl: string
    ): BaseUnit;

    withReferences(references: Array<BaseUnit>): this;

    withUsage(usage: string): this;
  }
  export class DiscriminatorValueMapping implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;
    targetShape: Shape;
    value: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withTargetShape(shape: Shape): this;

    withValue(value: string): this;
  }
  export class Document implements BaseUnit, EncodesModel, DeclaresModel {
    declares: Array<DomainElement>;
    encodes: DomainElement;
    id: string;
    location: string;
    modelVersion: StrField;
    processingData: BaseUnitProcessingData;
    raw: undefined | string;
    sourceInformation: BaseUnitSourceInformation;
    sourceSpec: undefined | Spec;
    usage: StrField;

    constructor();
    constructor(encoding: DomainElement);

    annotations(): Annotations;

    cloneUnit(): BaseUnit;

    findById(id: string): undefined | DomainElement;

    findByType(typeId: string): Array<DomainElement>;

    pkg(): StrField;

    references(): Array<BaseUnit>;

    withDeclaredElement(declared: DomainElement): this;

    withDeclares(declares: Array<DomainElement>): this;

    withEncodes(encoded: DomainElement): this;

    withId(id: string): this;

    withLocation(location: string): this;

    withPkg(pkg: string): this;

    withProcessingData(data: BaseUnitProcessingData): this;

    withRaw(raw: string): this;

    withReferenceAlias(
      alias: string,
      id: string,
      fullUrl: string,
      relativeUrl: string
    ): BaseUnit;

    withReferences(references: Array<BaseUnit>): this;

    withUsage(usage: string): this;
  }
  export class DocumentMapping implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;

    constructor();

    annotations(): Annotations;

    declaredNodes(): Array<PublicNodeMapping>;

    documentName(): StrField;

    encoded(): StrField;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDeclaredNodes(declarations: Array<PublicNodeMapping>): DocumentMapping;

    withDocumentName(name: string): DocumentMapping;

    withEncoded(encodedNode: string): DocumentMapping;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;
  }
  export class DocumentationItem extends Fragment {
    constructor();
  }
  export class DocumentsModel implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;

    constructor();

    annotations(): Annotations;

    declarationsPath(): StrField;

    fragments(): Array<DocumentMapping>;

    graph(): Graph;

    keyProperty(): BoolField;

    library(): DocumentMapping;

    root(): DocumentMapping;

    selfEncoded(): BoolField;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDeclarationsPath(declarationsPath: string): DocumentsModel;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withFragments(fragments: Array<DocumentMapping>): DocumentsModel;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withKeyProperty(keyProperty: boolean): DocumentsModel;

    withLibrary(library: DocumentMapping): DocumentsModel;

    withRoot(documentMapping: DocumentMapping): DocumentsModel;

    withSelfEncoded(selfEncoded: boolean): DocumentsModel;
  }
  export interface DomainElement extends AmfObjectWrapper, CustomizableElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;
  }
  export class DomainExtension implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    definedBy: CustomDomainProperty;
    extendsNode: Array<DomainElement>;
    extension: DataNode;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDefinedBy(property: CustomDomainProperty): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withExtension(node: DataNode): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;
  }
  export class DoubleField implements ValueField<number> {
    isNull: boolean;
    nonNull: boolean;
    readonly option: undefined | number;

    annotations(): Annotations;

    is(other: number): boolean;

    is(accepts: undefined): boolean;

    remove(): void;

    toString(): string;

    value(): number;
  }
  export class EmptyBinding
    implements ServerBinding, OperationBinding, ChannelBinding, MessageBinding
  {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    position: Range;
    type: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): EmptyBinding;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withType(type: string): this;
  }
  export interface EncodesModel extends AmfObjectWrapper {
    encodes: DomainElement;

    withEncodes(encoded: DomainElement): this;
  }
  export class Encoding implements DomainElement {
    allowReserved: BoolField;
    contentType: StrField;
    customDomainProperties: Array<DomainExtension>;
    explode: BoolField;
    extendsNode: Array<DomainElement>;
    headers: Array<Parameter>;
    id: string;
    isExternalLink: BoolField;
    position: Range;
    propertyName: StrField;
    style: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withAllowReserved(allowReserved: boolean): this;

    withContentType(contentType: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExplode(explode: boolean): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withHeader(name: string): Parameter;

    withHeaders(headers: Array<Parameter>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withPropertyName(propertyName: string): this;

    withStyle(style: string): this;
  }
  export class EndPoint implements DomainElement {
    bindings: ChannelBindings;
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    extendsNode: Array<DomainElement>;
    federationMetadata: EndPointFederationMetadata;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    operations: Array<Operation>;
    parameters: Array<Parameter>;
    parent: undefined | EndPoint;
    path: StrField;
    payloads: Array<Payload>;
    position: Range;
    relativePath: string;
    security: Array<SecurityRequirement>;
    servers: Array<Server>;
    summary: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withBindings(bindings: ChannelBindings): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDescription(description: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withOperation(method: string): Operation;

    withOperations(operations: Array<Operation>): this;

    withParameter(name: string): Parameter;

    withParameters(parameters: Array<Parameter>): this;

    withPath(path: string): this;

    withPayload(name: string): Payload;

    withPayloads(payloads: Array<Payload>): this;

    withSecurity(security: Array<SecurityRequirement>): this;

    withServer(url: string): Server;

    withServers(servers: Array<Server>): this;

    withSummary(summary: string): this;
  }
  export class EndPointFederationMetadata {
    constructor();
  }
  export class ErrorHandler {
    static handler(obj: JsErrorHandler): ClientErrorHandler;

    static provider(obj: JsErrorHandler): ErrorHandlerProvider;
  }
  export interface ErrorHandlerProvider {
    errorHandler(): ClientErrorHandler;
  }
  export class ErrorHandlerProvider {
    static default(): ErrorHandlerProvider;

    static ignoring(): ErrorHandlerProvider;

    static unhandled(): ErrorHandlerProvider;
  }
  export class EventNames {
    static readonly DetectedSyntaxMediaType: "DetectedSyntaxMediaType";
    static readonly FinishedASTRender: "FinishedASTRender";
    static readonly FinishedParse: "FinishedParse";
    static readonly FinishedSyntaxRender: "FinishedSyntaxRender";
    static readonly FinishedTransformation: "FinishedTransformation";
    static readonly FinishedTransformationStep: "FinishedTransformationStep";
    static readonly FinishedValidation: "FinishedValidation";
    static readonly FinishedValidationPlugin: "FinishedValidationPlugin";
    static readonly FoundReferences: "FoundReferences";
    static readonly ParsedModel: "ParsedModel";
    static readonly ParsedSyntax: "ParsedSyntax";
    static readonly SelectedParsePlugin: "SelectedParsePlugin";
    static readonly SkippedValidationPlugin: "SkippedValidationPlugin";
    static readonly StartedContentParse: "StartedContentParse";
    static readonly StartedParse: "StartedParse";
    static readonly StartedRender: "StartedRender";
    static readonly StartedRenderToWriter: "StartedRenderToWriter";
    static readonly StartedTransformation: "StartedTransformation";
    static readonly StartedTransformationStep: "StartedTransformationStep";
    static readonly StartingValidation: "StartingValidation";
    static readonly UnitCacheHit: "UnitCacheHit";
  }
  export class Example implements DomainElement, Linkable {
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    displayName: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    location: undefined | string;
    mediaType: StrField;
    name: StrField;
    position: Range;
    strict: BoolField;
    structuredValue: DataNode;
    value: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): Example;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDescription(description: string): this;

    withDisplayName(displayName: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withMediaType(mediaType: string): this;

    withName(name: string): this;

    withStrict(strict: boolean): this;

    withStructuredValue(value: DataNode): this;

    withValue(value: string): this;
  }
  export class ExecutionEnvironment {
    constructor();
  }
  export class Extension extends Document {
    constructor();
  }
  export class External implements DomainElement {
    alias: StrField;
    base: StrField;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withAlias(alias: string): External;

    withBase(base: string): External;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;
  }
  export class ExternalDomainElement implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    mediaType: StrField;
    position: Range;
    raw: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withMediaType(mediaType: string): this;

    withRaw(raw: string): this;
  }
  export class ExternalFragment extends Fragment {
    constructor();
  }
  export class ExternalPropertyShape implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    keyMappings: Array<PropertyKeyMapping>;
    name: StrField;
    position: Range;
    rangeName: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withKeyMappings(keyMappings: Array<PropertyKeyMapping>): this;

    withName(name: string): this;

    withRangeName(rangeName: string): this;
  }
  export class FileShape extends AnyShape {
    exclusiveMaximum: BoolField;
    exclusiveMaximumNumeric: DoubleField;
    exclusiveMinimum: BoolField;
    exclusiveMinimumNumeric: DoubleField;
    fileTypes: Array<StrField>;
    format: StrField;
    maxLength: IntField;
    maximum: DoubleField;
    minLength: IntField;
    minimum: DoubleField;
    multipleOf: DoubleField;
    pattern: StrField;

    constructor();

    linkCopy(): FileShape;

    withExclusiveMaximum(max: boolean): this;

    withExclusiveMaximumNumeric(max: number): this;

    withExclusiveMinimum(min: boolean): this;

    withExclusiveMinimumNumeric(min: number): this;

    withFileTypes(fileTypes: Array<string>): this;

    withFormat(format: string): this;

    withMaxLength(max: number): this;

    withMaximum(max: number): this;

    withMinLength(min: number): this;

    withMinimum(min: number): this;

    withMultipleOf(multiple: number): this;

    withPattern(pattern: string): this;
  }
  export class FinishedParsingEvent {
    unit: BaseUnit;
    url: string;
  }
  export class FinishedRenderingASTEvent {
    unit: BaseUnit;
  }
  export class FinishedRenderingSyntaxEvent {
    unit: BaseUnit;
  }
  export class FinishedTransformationEvent {
    unit: BaseUnit;
  }
  export class FinishedTransformationStepEvent {
    index: number;
    step: TransformationStep;
  }
  export class FinishedValidationEvent {
    result: AMFValidationReport;
  }
  export class FinishedValidationPluginEvent {
    result: AMFValidationReport;
  }
  export class FloatField implements ValueField<number> {
    isNull: boolean;
    nonNull: boolean;
    readonly option: undefined | number;

    annotations(): Annotations;

    is(other: number): boolean;

    is(accepts: undefined): boolean;

    remove(): void;

    toString(): string;

    value(): number;
  }
  export class FoundReferencesEvent {
    amount: number;
    root: string;
  }
  export class Fragment implements BaseUnit, EncodesModel {
    encodes: DomainElement;
    id: string;
    location: string;
    modelVersion: StrField;
    processingData: BaseUnitProcessingData;
    raw: undefined | string;
    sourceInformation: BaseUnitSourceInformation;
    sourceSpec: undefined | Spec;
    usage: StrField;

    annotations(): Annotations;

    cloneUnit(): BaseUnit;

    findById(id: string): undefined | DomainElement;

    findByType(typeId: string): Array<DomainElement>;

    pkg(): StrField;

    references(): Array<BaseUnit>;

    withEncodes(encoded: DomainElement): this;

    withId(id: string): this;

    withLocation(location: string): this;

    withPkg(pkg: string): this;

    withProcessingData(data: BaseUnitProcessingData): this;

    withRaw(raw: string): this;

    withReferenceAlias(
      alias: string,
      id: string,
      fullUrl: string,
      relativeUrl: string
    ): BaseUnit;

    withReferences(references: Array<BaseUnit>): this;

    withUsage(usage: string): this;
  }
  export class Graph {
    containsProperty(uri: string): boolean;

    getObjectByProperty(uri: string): Array<DomainElement>;

    properties(): Array<string>;

    propertyLexical(uri: string): Range;

    removeField(uri: string): this;

    scalarByProperty(uri: string): Array<any>;

    types(): Array<string>;

    propertyLexical(uri: String): Range;
  }
  export class GraphQLConfiguration {
    static GraphQL(): AMFConfiguration;
  }
  export class GraphQLFederationConfiguration {
    static GraphQLFederation(): AMFConfiguration;
  }
  export class GRPCConfiguration {
    static GRPC(): AMFConfiguration;
  }
  export class HighPriority extends PluginPriority {}
  export class HttpApiKeySettings extends Settings {
    in: StrField;
    name: StrField;

    constructor();

    withIn(inVal: string): this;

    withName(name: string): this;
  }
  export class HttpMessageBinding implements MessageBinding {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    headers: Shape;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): HttpMessageBinding;

    withBindingVersion(bindingVersion: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withHeaders(headers: Shape): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;
  }
  export class HttpOperationBinding implements OperationBinding {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    method: StrField;
    operationType: StrField;
    position: Range;
    query: Shape;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): HttpOperationBinding;

    withBindingVersion(bindingVersion: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withMethod(method: string): this;

    withOperationType(type: string): this;

    withQuery(query: Shape): this;
  }
  export class HttpSettings extends Settings {
    bearerFormat: StrField;
    scheme: StrField;

    constructor();

    withBearerFormat(bearerFormat: string): this;

    withScheme(scheme: string): this;
  }
  export class IntField implements ValueField<number> {
    isNull: boolean;
    nonNull: boolean;
    readonly option: undefined | number;

    annotations(): Annotations;

    is(other: number): boolean;

    is(accepts: undefined): boolean;

    remove(): void;

    toString(): string;

    value(): number;
  }
  export class IriTemplateMapping implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    linkExpression: StrField;
    position: Range;
    templateVariable: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkExpression(expression: string): this;

    withTemplateVariable(variable: string): this;
  }
  export interface JSONSchemaVersion {}
  export class JSONSchemaVersions {
    static readonly Draft04: JSONSchemaVersion;
    static readonly Draft07: JSONSchemaVersion;
    static readonly Draft201909: JSONSchemaVersion;
    static readonly Unspecified: JSONSchemaVersion;
  }
  export interface JsAMFEventListener {
    notifyEvent(event: AMFEvent): void;
  }
  export interface JsAMFPayloadValidationPlugin extends JsAMFPlugin {
    id: string;

    applies(element: ValidatePayloadRequest): boolean;

    validator(
      shape: Shape,
      mediaType: string,
      config: ShapeValidationConfiguration,
      validationMode: ValidationMode
    ): JsPayloadValidator;
  }
  export interface JsAMFPlugin {
    readonly ID: string;
  }
  export class JsBrowserHttpResourceLoader extends BaseHttpResourceLoader {
    constructor();

    fetch(resource: string): any;
  }
  export interface JsErrorHandler {
    getResults(): Array<AMFValidationResult>;

    report(result: AMFValidationResult): void;
  }
  export class JsPath {
    static readonly sep: string;
  }
  export interface JsPayloadValidator {
    syncValidate(payload: string): AMFValidationReport;

    validate(payload: string): Promise<AMFValidationReport>;

    validate(payloadFragment: PayloadFragment): Promise<AMFValidationReport>;
  }
  export class JsServerFileResourceLoader implements BaseFileResourceLoader {
    constructor();

    accepts(resource: string): boolean;

    ensureFileAuthority(str: string): string;

    fetch(resource: string): Promise<Content>;

    fetchFile(resource: string): any;
  }
  export class JsServerHttpResourceLoader extends BaseHttpResourceLoader {
    constructor();

    fetch(resource: string): any;
  }
  export interface JsTransformationStep {
    transform(
      model: BaseUnit,
      errorHandler: ClientErrorHandler,
      configuration: AMFGraphConfiguration
    ): BaseUnit;
  }
  export class JsonLDArray implements JsonLDElement {
    jsonLDElements: Array<JsonLDElement>;

    constructor();
    annotations(): Annotations;
  }
  export interface JsonLDElement extends Annotable {
    annotations(): Annotations;
  }
  export class JsonLDError implements JsonLDElement {
    constructor();

    annotations(): Annotations;
  }
  export class JsonLDInstanceDocument implements BaseUnit {
    encodes: Array<JsonLDElement>;
    id: string;
    location: string;
    modelVersion: StrField;
    processingData: BaseUnitProcessingData;
    raw: undefined | string;
    sourceInformation: BaseUnitSourceInformation;
    sourceSpec: undefined | Spec;
    usage: StrField;

    constructor();

    annotations(): Annotations;

    cloneUnit(): BaseUnit;

    findById(id: string): undefined | DomainElement;

    findByType(typeId: string): Array<DomainElement>;

    pkg(): StrField;

    references(): Array<BaseUnit>;

    withEncodes(encodes: Array<JsonLDElement>): this;

    withId(id: string): this;

    withLocation(location: string): this;

    withPkg(pkg: string): this;

    withProcessingData(data: BaseUnitProcessingData): this;

    withRaw(raw: string): this;

    withReferenceAlias(
      alias: string,
      id: string,
      fullUrl: string,
      relativeUrl: string
    ): BaseUnit;

    withReferences(references: Array<BaseUnit>): this;

    withUsage(usage: string): this;
  }
  export class JsonLDInstanceResult extends AMFParseResult {
    readonly instance: JsonLDInstanceDocument;
  }
  export class JsonLDObject implements DomainElement, JsonLDElement {
    componentId: string;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withBoolPropertyCollection(
      property: string,
      values: Array<boolean>
    ): JsonLDObject;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withFloatPropertyCollection(
      property: string,
      values: Array<number>
    ): JsonLDObject;

    withId(id: string): this;

    withIntPropertyCollection(
      property: string,
      values: Array<number>
    ): JsonLDObject;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withObjPropertyCollection(
      property: string,
      values: Array<JsonLDObject>
    ): JsonLDObject;

    withProperty(property: string, value: number): JsonLDObject;

    withProperty(property: string, value: JsonLDObject): JsonLDObject;

    withProperty(property: string, value: boolean): JsonLDObject;

    withProperty(property: string, value: string): JsonLDObject;

    withStringPropertyCollection(
      property: string,
      values: Array<string>
    ): JsonLDObject;
  }
  export class JsonLDScalar implements JsonLDElement {
    readonly dataType: string;
    readonly value: any;

    constructor(value: any, dataType: string);

    annotations(): any;
  }
  export class JsonLDSchemaConfiguration extends BaseShapesConfiguration {
    static JsonLDSchema(): JsonLDSchemaConfiguration;

    baseUnitClient(): JsonLDSchemaConfigurationClient;

    elementClient(): JsonLDSchemaElementClient;

    forInstance(url: string): Promise<JsonLDSchemaConfiguration>;

    withDialect(dialect: Dialect): JsonLDSchemaConfiguration;

    withDialect(url: string): Promise<JsonLDSchemaConfiguration>;

    withErrorHandlerProvider(
      provider: ErrorHandlerProvider
    ): JsonLDSchemaConfiguration;

    withEventListener(listener: AMFEventListener): JsonLDSchemaConfiguration;

    withExecutionEnvironment(
      executionEnv: ExecutionEnvironment
    ): JsonLDSchemaConfiguration;

    withParsingOptions(
      parsingOptions: ParsingOptions
    ): JsonLDSchemaConfiguration;

    withRenderOptions(renderOptions: RenderOptions): JsonLDSchemaConfiguration;

    withResourceLoader(rl: ResourceLoader): JsonLDSchemaConfiguration;

    withResourceLoaders(rl: Array<ResourceLoader>): JsonLDSchemaConfiguration;

    withTransformationPipeline(
      pipeline: TransformationPipeline
    ): JsonLDSchemaConfiguration;

    withUnitCache(cache: UnitCache): JsonLDSchemaConfiguration;
  }
  export class JsonLDSchemaConfigurationClient extends BaseAMLBaseUnitClient {
    getConfiguration(): JsonLDSchemaConfiguration;

    parseJsonLDInstance(
      url: string,
      jsonLDSchema: JsonSchemaDocument
    ): Promise<JsonLDInstanceResult>;

    parseJsonLDSchema(url: string): Promise<JsonLDSchemaResult>;
  }
  export class JsonLDSchemaElementClient extends BaseAMLElementClient {
    getConfiguration(): JsonLDSchemaConfiguration;

    renderToBuilder<T>(
      element: DomainElement,
      builder: org.yaml.builder.JsOutputBuilder
    ): void;
  }
  export class JsonLDSchemaResult extends AMFParseResult {
    readonly jsonDocument: JsonSchemaDocument;
  }
  export class JsonSchemaConfiguration {
    static JsonSchema(): ShapesConfiguration;
  }
  export class JsonSchemaDocument extends Document {
    schemaVersion: StrField;

    constructor();
  }
  export class JsonSchemaDraft201909 implements JSONSchemaVersion {}
  export class JsonSchemaDraft4 implements JSONSchemaVersion {}
  export class JsonSchemaDraft7 implements JSONSchemaVersion {}
  export class JsonSchemaShapeRenderer {
    static buildJsonSchema(
      element: AnyShape,
      config: AMFGraphConfiguration
    ): string;

    static toJsonSchema(
      element: AnyShape,
      config: AMFGraphConfiguration
    ): string;
  }
  export class KafkaMessageBinding implements MessageBinding {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    messageKey: Shape;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): KafkaMessageBinding;

    withBindingVersion(bindingVersion: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withKey(key: Shape): this;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;
  }
  export class KafkaOperationBinding implements OperationBinding {
    clientId: Shape;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    groupId: Shape;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): KafkaOperationBinding;

    withBindingVersion(bindingVersion: string): this;

    withClientId(clientId: Shape): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withGroupId(groupId: Shape): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;
  }
  export class Key implements DomainElement {
    components: Array<PropertyShapePath>;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isResolvable: BoolField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withComponents(components: Array<PropertyShapePath>): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withResolvable(isResolvable: boolean): this;
  }
  export interface KeyMapping extends DomainElement {
    source: any;
    target: any;

    withSource(source: any): this;

    withTarget(target: any): this;
  }
  export class License implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    url: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withUrl(url: string): this;
  }
  export class LinkNode implements DataNode {
    alias: StrField;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    link: StrField;
    name: StrField;
    position: Range;

    constructor();
    constructor(alias: string, value: string);

    annotations(): Annotations;

    graph(): Graph;

    withAlias(alias: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLink(link: string): this;

    withName(name: string): this;
  }
  export interface Linkable {
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;

    link<T>(): T;

    link<T>(label: string): T;

    linkCopy(): Linkable;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;
  }
  export class LocationInformation implements AmfObjectWrapper {
    locationValue: StrField;

    constructor();

    annotations(): Annotations;

    elements(): Array<StrField>;

    withElements(elements: Array<string>): this;

    withLocation(value: string): this;
  }
  export class LowPriority extends PluginPriority {}
  export class MatrixShape extends ArrayShape {
    constructor();

    withItems(items: Shape): this;
  }
  export class Message implements DomainElement, Linkable {
    bindings: MessageBindings;
    correlationId: CorrelationId;
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    displayName: StrField;
    documentation: CreativeWork;
    examples: Array<Example>;
    extendsNode: Array<DomainElement>;
    headerExamples: Array<Example>;
    headerSchema: NodeShape;
    id: string;
    isAbstract: BoolField;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    name: StrField;
    payloads: Array<Payload>;
    position: Range;
    summary: StrField;
    tags: Array<Tag>;
    title: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): Message;

    withAbstract(isAbstract: boolean): this;

    withBindings(bindings: MessageBindings): this;

    withCorrelationId(correlationId: CorrelationId): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDescription(description: string): this;

    withDisplayName(displayName: string): this;

    withDocumentation(documentation: CreativeWork): this;

    withExamples(examples: Array<Example>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withHeaderExamples(examples: Array<Example>): this;

    withHeaderSchema(schema: NodeShape): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withName(name: string): this;

    withPayload(): Payload;

    withPayload(mediaType: string): Payload;

    withPayload(mediaType: undefined | string): Payload;

    withPayloads(payloads: Array<Payload>): this;

    withSummary(summary: string): this;

    withTags(tags: Array<Tag>): this;

    withTitle(title: string): this;
  }
  export interface MessageBinding extends DomainElement, Linkable {}
  export class MessageBindings implements DomainElement, Linkable {
    bindings: Array<MessageBinding>;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    name: StrField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): MessageBindings;

    withBindings(bindings: Array<MessageBinding>): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withName(name: string): this;
  }
  export interface MessageStyle {
    profileName: ProfileName;
  }
  export class MessageStyles {
    static readonly AMF: MessageStyle;
    static readonly ASYNC: MessageStyle;
    static readonly OAS: MessageStyle;
    static readonly RAML: MessageStyle;
  }
  export class Module implements BaseUnit, DeclaresModel, CustomizableElement {
    customDomainProperties: Array<DomainExtension>;
    declares: Array<DomainElement>;
    id: string;
    location: string;
    modelVersion: StrField;
    processingData: BaseUnitProcessingData;
    raw: undefined | string;
    sourceInformation: BaseUnitSourceInformation;
    sourceSpec: undefined | Spec;
    usage: StrField;

    constructor();

    annotations(): Annotations;

    cloneUnit(): BaseUnit;

    findById(id: string): undefined | DomainElement;

    findByType(typeId: string): Array<DomainElement>;

    pkg(): StrField;

    references(): Array<BaseUnit>;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDeclaredElement(declared: DomainElement): this;

    withDeclares(declares: Array<DomainElement>): this;

    withId(id: string): this;

    withLocation(location: string): this;

    withPkg(pkg: string): this;

    withProcessingData(data: BaseUnitProcessingData): this;

    withRaw(raw: string): this;

    withReferenceAlias(
      alias: string,
      id: string,
      fullUrl: string,
      relativeUrl: string
    ): BaseUnit;

    withReferences(references: Array<BaseUnit>): this;

    withUsage(usage: string): this;
  }
  export class MqttMessageBinding implements MessageBinding {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): MqttMessageBinding;

    withBindingVersion(bindingVersion: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;
  }
  export class MqttOperationBinding implements OperationBinding {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    position: Range;
    qos: IntField;
    retain: BoolField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): MqttOperationBinding;

    withBindingVersion(bindingVersion: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withQos(qos: number): this;

    withRetain(retain: boolean): this;
  }
  export class MqttServerBinding implements ServerBinding {
    cleanSession: BoolField;
    clientId: StrField;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    keepAlive: IntField;
    lastWill: MqttServerLastWill;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): MqttServerBinding;

    withBindingVersion(bindingVersion: string): this;

    withCleanSession(cleanSession: boolean): this;

    withClientId(clientId: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withKeepAlive(keepAlive: number): this;

    withLastWill(lastWill: MqttServerLastWill): this;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;
  }
  export class MqttServerLastWill implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    message: StrField;
    position: Range;
    qos: IntField;
    retain: BoolField;
    topic: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withMessage(message: string): this;

    withQos(qos: number): this;

    withRetain(retain: boolean): this;

    withTopic(topic: string): this;
  }
  export class NamedExample extends Fragment {
    constructor();
  }
  export class NilShape extends AnyShape {
    constructor();

    linkCopy(): NilShape;
  }
  export class NodeMapping extends AnyMapping implements Linkable {
    closed: BoolField;
    idTemplate: StrField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    mergePolicy: StrField;
    name: StrField;
    nodetypeMapping: StrField;

    constructor();

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): NodeMapping;

    propertiesMapping(): Array<PropertyMapping>;

    withIdTemplate(idTemplate: string): NodeMapping;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withMergePolicy(mergePolicy: string): NodeMapping;

    withName(name: string): NodeMapping;

    withNodeTypeMapping(nodeType: string): NodeMapping;

    withPropertiesMapping(props: Array<PropertyMapping>): NodeMapping;
  }
  export class NodeShape extends AnyShape {
    additionalPropertiesKeySchema: Shape;
    additionalPropertiesSchema: Shape;
    closed: BoolField;
    dependencies: Array<PropertyDependencies>;
    discriminator: StrField;
    discriminatorMapping: Array<IriTemplateMapping>;
    discriminatorValue: StrField;
    discriminatorValueMapping: Array<DiscriminatorValueMapping>;
    externalProperties: Array<ExternalPropertyShape>;
    isAbstract: BoolField;
    isInputOnly: BoolField;
    keys: undefined;
    maxProperties: IntField;
    minProperties: IntField;
    properties: Array<PropertyShape>;
    propertyNames: Shape;
    schemaDependencies: Array<SchemaDependencies>;
    unevaluatedProperties: boolean;
    unevaluatedPropertiesSchema: Shape;

    constructor();

    linkCopy(): NodeShape;

    withAdditionalPropertiesKeySchema(
      additionalPropertiesKeySchema: Shape
    ): this;

    withAdditionalPropertiesSchema(additionalPropertiesSchema: Shape): this;

    withClosed(closed: boolean): this;

    withDependencies(dependencies: Array<PropertyDependencies>): this;

    withDependency(): PropertyDependencies;

    withDiscriminator(discriminator: string): this;

    withDiscriminatorMapping(mappings: Array<IriTemplateMapping>): this;

    withDiscriminatorValue(value: string): this;

    withExternalProperties(
      externalProperties: Array<ExternalPropertyShape>
    ): this;

    withInheritsObject(name: string): NodeShape;

    withInheritsScalar(name: string): ScalarShape;

    withIsAbstract(isAbstract: boolean): this;

    withIsInputOnly(isInputOnly: boolean): this;

    withKeys(keys: undefined): this;

    withMaxProperties(max: number): this;

    withMinProperties(min: number): this;

    withProperties(properties: Array<PropertyShape>): this;

    withProperty(name: string): PropertyShape;

    withPropertyNames(propertyNames: Shape): this;

    withSchemaDependencies(dependencies: Array<SchemaDependencies>): this;

    withUnevaluatedProperties(value: boolean): this;

    withUnevaluatedPropertiesSchema(schema: Shape): this;
  }
  export class NormalPriority extends PluginPriority {}
  export class OASConfiguration {
    static OAS(): AMFConfiguration;

    static OAS20(): AMFConfiguration;

    static OAS30(): AMFConfiguration;

    static OAS30Component(): AMFConfiguration;

    static OASComponent(): AMFConfiguration;

    static fromSpec(spec: Spec): AMFConfiguration;
  }
  export class OAuth1Settings extends Settings {
    authorizationUri: StrField;
    requestTokenUri: StrField;
    signatures: Array<StrField>;
    tokenCredentialsUri: StrField;

    constructor();

    withAuthorizationUri(authorizationUri: string): this;

    withRequestTokenUri(requestTokenUri: string): this;

    withSignatures(signatures: Array<string>): this;

    withTokenCredentialsUri(tokenCredentialsUri: string): this;
  }
  export class OAuth2Flow implements DomainElement {
    accessTokenUri: StrField;
    authorizationUri: StrField;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    flow: StrField;
    id: string;
    isExternalLink: BoolField;
    position: Range;
    refreshUri: StrField;
    scopes: Array<Scope>;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withAccessTokenUri(accessTokenUri: string): this;

    withAuthorizationUri(authorizationUri: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withFlow(flow: string): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withRefreshUri(refreshUri: string): this;

    withScopes(scopes: Array<Scope>): this;
  }
  export class OAuth2Settings extends Settings {
    authorizationGrants: Array<StrField>;
    flows: Array<OAuth2Flow>;

    constructor();

    withAuthorizationGrants(grants: Array<string>): this;

    withFlows(flows: Array<OAuth2Flow>): this;
  }
  export class ObjectNode implements DataNode {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    properties: Map<string, DataNode>;

    constructor();

    addProperty(property: string, node: DataNode): this;

    annotations(): Annotations;

    getProperty(property: string): undefined | DataNode;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;
  }
  export class ObjectPropertyTerm extends PropertyTerm {
    constructor();
  }
  export class OpenIdConnectSettings extends Settings {
    scopes: Array<Scope>;
    url: StrField;

    constructor();

    withScopes(scopes: Array<Scope>): this;

    withUrl(url: string): this;
  }
  export class Operation extends AbstractOperation implements Linkable {
    accepts: Array<StrField>;
    bindings: OperationBindings;
    callbacks: Array<Callback>;
    contentType: Array<StrField>;
    deprecated: BoolField;
    documentation: CreativeWork;
    federationMetadata: OperationFederationMetadata;
    isAbstract: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    operationId: StrField;
    request: Request;
    requests: Array<Request>;
    response: Response;
    responses: Array<Response>;
    schemes: Array<StrField>;
    security: Array<SecurityRequirement>;
    servers: Array<Server>;
    summary: StrField;
    tags: Array<Tag>;

    constructor();

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): Operation;

    withAbstract(abs: boolean): this;

    withAccepts(accepts: Array<string>): this;

    withBindings(bindings: OperationBindings): this;

    withCallback(name: string): Callback;

    withCallbacks(callbacks: Array<Callback>): this;

    withContentType(contentType: Array<string>): this;

    withDeprecated(deprecated: boolean): this;

    withDocumentation(documentation: CreativeWork): this;

    withFederationMetadata(
      federationMetadata: OperationFederationMetadata
    ): this;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withOperationId(operationId: string): this;

    withRequest(request: Request): this;

    withRequest(): Request;

    withRequests(requests: Array<Request>): this;

    withResponse(name: string): Response;

    withResponses(responses: Array<Response>): this;

    withSchemes(schemes: Array<string>): this;

    withSecurity(security: Array<SecurityRequirement>): this;

    withServer(name: string): Server;

    withServers(servers: Array<Server>): this;

    withSummary(summary: string): this;

    withTags(tags: Array<Tag>): this;
  }
  export interface OperationBinding extends DomainElement, Linkable {}
  export class OperationBindings implements DomainElement, Linkable {
    bindings: Array<OperationBinding>;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    name: StrField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): OperationBindings;

    withBindings(bindings: Array<OperationBinding>): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withName(name: string): this;
  }
  export class OperationFederationMetadata {
    federationMethod: StrField;
    keyMappings: Array<ParameterKeyMapping>;
    providedEntity: NodeShape;

    constructor();

    withFederationMethod(federationMethod: string): this;

    withKeyMappings(keyMappings: Array<ParameterKeyMapping>): this;

    withProvidedEntity(providedEntity: NodeShape): this;
  }
  export class Organization implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    email: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    url: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withEmail(email: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withUrl(url: string): this;
  }
  export class Overlay extends Document {
    constructor();
  }
  export class Parameter extends AbstractParameter {
    allowEmptyValue: BoolField;
    allowReserved: BoolField;
    deprecated: BoolField;
    examples: Array<Example>;
    explode: BoolField;
    federationMetadata: ParameterFederationMetadata;
    payloads: Array<Payload>;
    style: StrField;

    constructor();

    withAllowEmptyValue(allowEmptyValue: boolean): this;

    withAllowReserved(allowReserved: boolean): this;

    withDeprecated(deprecated: boolean): this;

    withExample(name: string): Example;

    withExamples(examples: Array<Example>): this;

    withExplode(explode: boolean): this;

    withPayload(mediaType: string): Payload;

    withPayloads(payloads: Array<Payload>): this;

    withStyle(style: string): this;
  }
  export class ParameterFederationMetadata {
    constructor();
  }
  export class ParameterKeyMapping implements KeyMapping {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;
    source: Parameter;
    target: PropertyShapePath;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withSource(source: Parameter): this;

    withTarget(target: PropertyShapePath): this;
  }
  export interface ParametrizedDeclaration extends DomainElement {
    name: StrField;
    target: AbstractDeclaration;
    variables: Array<VariableValue>;

    withName(name: string): this;

    withTarget(target: AbstractDeclaration): this;

    withVariables(variables: Array<VariableValue>): this;
  }
  export class ParametrizedResourceType implements ParametrizedDeclaration {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    target: AbstractDeclaration;
    variables: Array<VariableValue>;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withTarget(target: AbstractDeclaration): this;

    withVariables(variables: Array<VariableValue>): this;
  }
  export class ParametrizedSecurityScheme implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    extendsNode: Array<DomainElement>;
    hasNullSecurityScheme: boolean;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    scheme: SecurityScheme;
    settings: Settings;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withApiKeySettings(): ApiKeySettings;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDefaultSettings(): Settings;

    withDescription(description: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withHttpSettings(): HttpSettings;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withOAuth1Settings(): OAuth1Settings;

    withOAuth2Settings(): OAuth2Settings;

    withOpenIdConnectSettings(): OpenIdConnectSettings;

    withScheme(scheme: SecurityScheme): this;

    withSettings(settings: Settings): this;
  }
  export class ParametrizedTrait implements ParametrizedDeclaration {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    target: AbstractDeclaration;
    variables: Array<VariableValue>;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withTarget(target: AbstractDeclaration): this;

    withVariables(variables: Array<VariableValue>): this;
  }
  export class ParsedModelEvent {
    unit: BaseUnit;
    url: string;
  }
  export class ParsedSyntaxEvent {
    content: Content;
    url: string;
  }
  export class ParsingOptions {
    definedBaseUrl: undefined | string;
    getMaxJSONComplexity: undefined | number;
    getMaxJsonYamlDepth: undefined | number;
    getMaxYamlReferences: undefined | number;
    isAmfJsonLdSerialization: boolean;
    isTokens: boolean;

    constructor();

    setMaxJSONComplexity(value: number): ParsingOptions;

    setMaxJsonYamlDepth(value: number): ParsingOptions;

    setMaxYamlReferences(value: number): ParsingOptions;

    withAmfJsonLdSerialization(): ParsingOptions;

    withBaseUnitUrl(baseUnit: string): ParsingOptions;

    withTokens(): ParsingOptions;

    withoutAmfJsonLdSerialization(): ParsingOptions;

    withoutBaseUnitUrl(): ParsingOptions;

    withoutTokens(): ParsingOptions;
  }
  export class Path {
    static delimiter: string;
    static sep: string;

    static basename(p: string, ext: string): string;

    static dirname(p: string): string;

    static extname(p: string): string;

    static isAbsolute(path: string): boolean;

    static join(paths: undefined): string;

    static normalize(p: string): string;

    static relative(from: string, to: string): string;

    static resolve(pathSegments: undefined): string;
  }
  export class Payload extends AbstractPayload {
    encoding: Array<Encoding>;
    encodings: Array<Encoding>;
    examples: Array<Example>;
    required: BoolField;
    schemaMediaType: StrField;

    constructor();

    linkCopy(): Payload;

    withEncoding(name: string): Encoding;

    withEncoding(encoding: Array<Encoding>): this;

    withEncodings(encoding: Array<Encoding>): this;

    withExample(name: string): Example;

    withExamples(examples: Array<Example>): this;

    withRequired(required: boolean): this;

    withSchemaMediaType(mediaType: string): this;
  }
  export class PayloadFragment extends Fragment {
    dataNode: DataNode;
    mediaType: StrField;

    constructor(scalar: ScalarNode, mediaType: string);
    constructor(obj: ObjectNode, mediaType: string);
    constructor(arr: ArrayNode, mediaType: string);
  }
  export class PipelineId {
    static readonly Cache: "cache";
    static readonly Compatibility: "compatibility";
    static readonly Default: "default";
    static readonly Editing: "editing";
    static readonly Introspection: "introspection";
  }
  export class PipelineName {
    static from(targetMediaType: string, pipelineId: string): string;
  }
  export class PluginPriority {
    priority: number;

    constructor(priority: number);
  }
  export class Position {
    static FIRST: Position;
    static readonly ZERO: 0;
    column: number;
    isZero: boolean;
    line: number;
    offset: number;
    readonly offsetPart: "@";

    constructor(line: number, column: number, offset: number);

    static apply(line: number, column: number, offset: number): Position;

    static apply(offset: number): Position;

    static apply(lc: undefined): Position;

    compare(that: Position): number;

    compareTo(o: Position): number;

    equals(obj: any): boolean;

    hashCode(): number;

    lt(o: Position): boolean;

    max(other: Position): Position;

    min(other: Position): Position;
  }
  export class ProfileName {
    messageStyle: MessageStyle;
    profile: string;

    constructor(profile: string);

    static apply(profile: string): ProfileName;

    isOas(): boolean;

    isRaml(): boolean;

    toString(): string;
  }
  export class ProfileNames {
    static readonly AMF: ProfileName;
    static readonly AML: ProfileName;
    static readonly ASYNC: ProfileName;
    static readonly ASYNC20: ProfileName;
    static readonly GRAPHQL: ProfileName;
    static readonly GRAPHQL_FEDERATION: ProfileName;
    static readonly GRPC: ProfileName;
    static readonly JSONSCHEMA: ProfileName;
    static readonly OAS20: ProfileName;
    static readonly OAS30: ProfileName;
    static readonly PAYLOAD: ProfileName;
    static readonly RAML08: ProfileName;
    static readonly RAML10: ProfileName;
  }
  export class PropertyDependencies implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;
    source: StrField;
    target: Array<StrField>;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withPropertySource(propertySource: string): this;

    withPropertyTarget(propertyTarget: Array<string>): this;
  }
  export class PropertyKeyMapping implements KeyMapping {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;
    source: PropertyShape;
    target: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withSource(source: PropertyShape): this;

    withTarget(target: string): this;
  }
  export class PropertyMapping implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;

    constructor();

    allowMultiple(): BoolField;

    annotations(): Annotations;

    classification(): string;

    enum(): Array<AnyField>;

    externallyLinkable(): BoolField;

    graph(): Graph;

    literalRange(): StrField;

    mandatory(): BoolField;

    mapKeyProperty(): StrField;

    mapValueProperty(): StrField;

    maximum(): DoubleField;

    minCount(): IntField;

    minimum(): DoubleField;

    name(): StrField;

    nodePropertyMapping(): StrField;

    objectRange(): Array<StrField>;

    pattern(): StrField;

    sorted(): BoolField;

    typeDiscriminator(): Map<string, string>;

    typeDiscriminatorName(): StrField;

    withAllowMultiple(allow: boolean): PropertyMapping;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withEnum(values: Array<any>): PropertyMapping;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withExternallyLinkable(linkable: boolean): PropertyMapping;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLiteralRange(range: string): PropertyMapping;

    withMandatory(mandatory: boolean): PropertyMapping;

    withMapKeyProperty(key: string): PropertyMapping;

    withMapValueProperty(value: string): PropertyMapping;

    withMaximum(max: number): PropertyMapping;

    withMinCount(minCount: number): PropertyMapping;

    withMinimum(min: number): PropertyMapping;

    withName(name: string): PropertyMapping;

    withNodePropertyMapping(propertyId: string): PropertyMapping;

    withObjectRange(range: Array<string>): PropertyMapping;

    withPattern(pattern: string): PropertyMapping;

    withSorted(sorted: boolean): PropertyMapping;

    withTypeDiscriminator(typesMapping: Map<string, string>): PropertyMapping;

    withTypeDiscriminatorName(name: string): PropertyMapping;
  }
  export class PropertyShape implements Shape {
    and: Array<Shape>;
    customDomainProperties: Array<DomainExtension>;
    customShapeProperties: Array<ShapeExtension>;
    customShapePropertyDefinitions: Array<PropertyShape>;
    defaultValue: DataNode;
    defaultValueStr: StrField;
    deprecated: BoolField;
    description: StrField;
    displayName: StrField;
    elseShape: Shape;
    extendsNode: Array<DomainElement>;
    federationMetadata: ShapeFederationMetadata;
    hasExplicitName: boolean;
    id: string;
    ifShape: Shape;
    inherits: Array<Shape>;
    isExtension: BoolField;
    isExternalLink: BoolField;
    isLink: boolean;
    isStub: BoolField;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    maxCount: IntField;
    minCount: IntField;
    name: StrField;
    not: Shape;
    or: Array<Shape>;
    path: StrField;
    patternName: StrField;
    position: Range;
    provides: Array<PropertyShapePath>;
    range: Shape;
    readOnly: BoolField;
    requires: Array<PropertyShapePath>;
    serializationOrder: IntField;
    thenShape: Shape;
    values: Array<DataNode>;
    writeOnly: BoolField;
    xone: Array<Shape>;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): PropertyShape;

    withAnd(subShapes: Array<Shape>): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withCustomShapeProperties(
      customShapeProperties: Array<ShapeExtension>
    ): this;

    withCustomShapePropertyDefinition(name: string): PropertyShape;

    withCustomShapePropertyDefinitions(
      propertyDefinitions: Array<PropertyShape>
    ): this;

    withDefaultStr(value: string): this;

    withDefaultValue(defaultVal: DataNode): this;

    withDeprecated(deprecated: boolean): this;

    withDescription(description: string): this;

    withDisplayName(name: string): this;

    withElse(elseShape: Shape): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withFederationMetadata(metadata: ShapeFederationMetadata): this;

    withId(id: string): this;

    withIf(ifShape: Shape): this;

    withInherits(inherits: Array<Shape>): this;

    withIsExtension(value: boolean): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withIsStub(value: boolean): this;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withMaxCount(max: number): this;

    withMinCount(min: number): this;

    withName(name: string): this;

    withNode(shape: Shape): this;

    withOr(subShapes: Array<Shape>): this;

    withPath(path: string): this;

    withPatternName(pattern: string): this;

    withProvides(provides: Array<PropertyShapePath>): this;

    withRange(range: Shape): this;

    withReadOnly(readOnly: boolean): this;

    withRequires(requires: Array<PropertyShapePath>): this;

    withSerializationOrder(order: number): this;

    withThen(thenShape: Shape): this;

    withValues(values: Array<DataNode>): this;

    withWriteOnly(writeOnly: boolean): this;

    withXone(subShapes: Array<Shape>): this;
  }
  export class PropertyShapePath implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    path: Array<PropertyShape>;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withPath(path: Array<PropertyShape>): this;
  }
  export class PropertyTerm implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    displayName: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    range: StrField;
    subPropertyOf: Array<StrField>;

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDescription(description: string): PropertyTerm;

    withDisplayName(displayName: string): PropertyTerm;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): PropertyTerm;

    withRange(range: string): PropertyTerm;

    withSubClasOf(superProperties: Array<string>): PropertyTerm;
  }
  export class PublicNodeMapping implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    mappedNode(): StrField;

    name(): StrField;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withMappedNode(mappedNode: string): PublicNodeMapping;

    withName(name: string): PublicNodeMapping;
  }
  export class RAMLConfiguration {
    static RAML(): AMFConfiguration;

    static RAML08(): AMFConfiguration;

    static RAML10(): AMFConfiguration;

    static fromSpec(spec: Spec): AMFConfiguration;
  }
  export class RamlShapeRenderer {
    static toRamlDatatype(
      element: AnyShape,
      config: AMFGraphConfiguration
    ): string;
  }
  export class Range {
    static readonly ALL: 0;
    static readonly ZERO: 0;
    readonly columnFrom: number;
    readonly columnTo: number;
    end: Position;
    readonly lineFrom: number;
    readonly lineTo: number;
    start: Position;

    constructor(start: Position, end: Position);

    static apply(
      lineFrom: number,
      columnFrom: number,
      lineTo: number,
      columnTo: number
    ): Range;

    static apply(start: Position, delta: number): Range;

    static apply(start: undefined, end: undefined): Range;

    static apply(serialized: string): Range;

    compareTo(other: Range): number;

    contains(other: Range): boolean;

    extent(other: Range): Range;

    toString(): string;
  }
  export class RecursiveShape implements Shape {
    and: Array<Shape>;
    customDomainProperties: Array<DomainExtension>;
    customShapeProperties: Array<ShapeExtension>;
    customShapePropertyDefinitions: Array<PropertyShape>;
    defaultValue: DataNode;
    defaultValueStr: StrField;
    deprecated: BoolField;
    description: StrField;
    displayName: StrField;
    elseShape: Shape;
    extendsNode: Array<DomainElement>;
    federationMetadata: ShapeFederationMetadata;
    fixpoint: StrField;
    hasExplicitName: boolean;
    id: string;
    ifShape: Shape;
    inherits: Array<Shape>;
    isExtension: BoolField;
    isExternalLink: BoolField;
    isLink: boolean;
    isStub: BoolField;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    name: StrField;
    not: Shape;
    or: Array<Shape>;
    position: Range;
    readOnly: BoolField;
    thenShape: Shape;
    values: Array<DataNode>;
    writeOnly: BoolField;
    xone: Array<Shape>;

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): Linkable;

    withAnd(subShapes: Array<Shape>): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withCustomShapeProperties(
      customShapeProperties: Array<ShapeExtension>
    ): this;

    withCustomShapePropertyDefinition(name: string): PropertyShape;

    withCustomShapePropertyDefinitions(
      propertyDefinitions: Array<PropertyShape>
    ): this;

    withDefaultStr(value: string): this;

    withDefaultValue(defaultVal: DataNode): this;

    withDeprecated(deprecated: boolean): this;

    withDescription(description: string): this;

    withDisplayName(name: string): this;

    withElse(elseShape: Shape): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withFederationMetadata(metadata: ShapeFederationMetadata): this;

    withFixPoint(shapeId: string): this;

    withId(id: string): this;

    withIf(ifShape: Shape): this;

    withInherits(inherits: Array<Shape>): this;

    withIsExtension(value: boolean): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withIsStub(value: boolean): this;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withName(name: string): this;

    withNode(shape: Shape): this;

    withOr(subShapes: Array<Shape>): this;

    withReadOnly(readOnly: boolean): this;

    withThen(thenShape: Shape): this;

    withValues(values: Array<DataNode>): this;

    withWriteOnly(writeOnly: boolean): this;

    withXone(subShapes: Array<Shape>): this;
  }
  export class RenderOptions {
    isAmfJsonLdSerialization: boolean;
    isEmitNodeIds: boolean;
    isGovernanceMode: boolean;
    isPrettyPrint: boolean;
    isRawFieldEmission: boolean;
    isWithCompactUris: boolean;
    isWithCompactedEmission: boolean;
    isWithDocumentation: boolean;
    isWithSourceInformation: boolean;
    isWithSourceMaps: boolean;
    schemaVersion: JSONSchemaVersion;
    withGovernanceMode: RenderOptions;

    constructor();

    withAmfJsonLdSerialization(): RenderOptions;

    withCompactUris(): RenderOptions;

    withCompactedEmission(): RenderOptions;

    withDocumentation(): RenderOptions;

    withNodeIds(): RenderOptions;

    withPrettyPrint(): RenderOptions;

    withRawFieldEmission(): RenderOptions;

    withSchemaVersion(version: JSONSchemaVersion): RenderOptions;

    withSourceInformation(): RenderOptions;

    withSourceMaps(): RenderOptions;

    withoutAmfJsonLdSerialization(): RenderOptions;

    withoutCompactUris(): RenderOptions;

    withoutCompactedEmission(): RenderOptions;

    withoutDocumentation(): RenderOptions;

    withoutImplicitRamlTypes(): RenderOptions;

    withoutPrettyPrint(): RenderOptions;

    withoutSourceInformation(): RenderOptions;

    withoutSourceMaps(): RenderOptions;
  }
  export class Request extends Message implements AbstractRequest {
    cookieParameters: Array<Parameter>;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    headers: Array<Parameter>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    queryParameters: Array<Parameter>;
    queryString: Shape;
    required: BoolField;
    uriParameters: Array<Parameter>;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    linkCopy(): Request;

    withCookieParameter(name: string): Parameter;

    withCookieParameters(cookieParameters: Array<Parameter>): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withHeader(name: string): Parameter;

    withHeaders(headers: Array<Parameter>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withQueryParameter(name: string): Parameter;

    withQueryParameters(parameters: Array<Parameter>): this;

    withQueryString(queryString: Shape): this;

    withRequired(required: boolean): this;

    withUriParameter(name: string): Parameter;

    withUriParameters(uriParameters: Array<Parameter>): this;
  }
  export interface ResourceLoader {
    accepts(resource: string): boolean;

    fetch(resource: string): Promise<Content>;
  }
  export class ResourceLoaderFactory {
    static create(loader: ClientResourceLoader): any;
  }
  export class ResourceNotFound {
    readonly msj: string;

    constructor(msj: string);
  }
  export class ResourceType extends AbstractDeclaration {
    linkTarget: undefined | DomainElement;

    constructor();

    linkCopy(): ResourceType;
  }
  export class ResourceTypeFragment extends Fragment {
    constructor();
  }
  export class Response extends Message implements AbstractResponse {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    headers: Array<Parameter>;
    id: string;
    isExternalLink: BoolField;
    links: Array<TemplatedLink>;
    name: StrField;
    payload: Payload;
    position: Range;
    statusCode: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    // @ts-ignore
    linkCopy(): Response;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withHeader(name: string): Parameter;

    withHeaders(headers: Array<Parameter>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinks(links: Array<TemplatedLink>): this;

    withName(name: string): this;

    withPayload(payload: Payload): Payload;

    withPayload(): Payload;

    withPayload(mediaType: string): Payload;

    withPayload(mediaType: undefined | string): Payload;

    withStatusCode(statusCode: string): this;
  }
  export class ScalarNode implements DataNode {
    customDomainProperties: Array<DomainExtension>;
    dataType: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    value: StrField;

    constructor();
    constructor(value: string, dataType: string);

    annotations(): Annotations;

    static build(value: string, dataType: string): any;

    graph(): Graph;

    toString(): string;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDataType(dataType: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withValue(value: string): this;
  }
  export class ScalarRelaxedValidationMode extends ValidationMode {}
  export class ScalarShape extends AnyShape {
    dataType: StrField;
    encoding: StrField;
    exclusiveMaximum: BoolField;
    exclusiveMaximumNumeric: DoubleField;
    exclusiveMinimum: BoolField;
    exclusiveMinimumNumeric: DoubleField;
    format: StrField;
    maxLength: IntField;
    maximum: DoubleField;
    mediaType: StrField;
    minLength: IntField;
    minimum: DoubleField;
    multipleOf: DoubleField;
    pattern: StrField;
    schema: Shape;

    constructor();

    linkCopy(): ScalarShape;

    withDataType(dataType: string): this;

    withEncoding(encoding: string): this;

    withExclusiveMaximum(max: boolean): this;

    withExclusiveMaximumNumeric(max: number): this;

    withExclusiveMinimum(min: boolean): this;

    withExclusiveMinimumNumeric(min: number): this;

    withFormat(format: string): this;

    withMaxLength(max: number): this;

    withMaximum(max: number): this;

    withMediaType(mediaType: string): this;

    withMinLength(min: number): this;

    withMinimum(min: number): this;

    withMultipleOf(multiple: number): this;

    withPattern(pattern: string): this;

    withSchema(schema: Shape): this;

    withSerializationSchema(schema: Shape): this;
  }
  export class SchemaDependencies implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;
    source: StrField;
    target: Shape;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withPropertySource(propertySource: string): this;

    withSchemaTarget(schema: Shape): this;
  }
  export class SchemaShape extends AnyShape {
    location: undefined | string;
    mediaType: StrField;
    raw: StrField;

    constructor();

    linkCopy(): SchemaShape;

    withMediatype(mediaType: string): this;

    withRaw(text: string): this;
  }
  export class Scope implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDescription(description: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;
  }
  export class SecurityRequirement implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    schemes: Array<ParametrizedSecurityScheme>;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withScheme(): ParametrizedSecurityScheme;

    withSchemes(schemes: Array<ParametrizedSecurityScheme>): this;
  }
  export class SecurityScheme implements DomainElement, Linkable {
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    displayName: StrField;
    extendsNode: Array<DomainElement>;
    headers: Array<Parameter>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    name: StrField;
    position: Range;
    queryParameters: Array<Parameter>;
    queryString: Shape;
    responses: Array<Response>;
    settings: Settings;
    type: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): SecurityScheme;

    withApiKeySettings(): ApiKeySettings;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDefaultSettings(): Settings;

    withDescription(description: string): this;

    withDisplayName(displayName: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withHeader(name: string): Parameter;

    withHeaders(headers: Array<Parameter>): this;

    withHttpApiKeySettings(): HttpApiKeySettings;

    withHttpSettings(): HttpSettings;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withName(name: string): this;

    withOAuth1Settings(): OAuth1Settings;

    withOAuth2Settings(): OAuth2Settings;

    withOpenIdConnectSettings(): OpenIdConnectSettings;

    withQueryParameter(name: string): Parameter;

    withQueryParameters(queryParameters: Array<Parameter>): this;

    withQueryString(queryString: Shape): this;

    withResponse(name: string): Response;

    withResponses(responses: Array<Response>): this;

    withSettings(settings: Settings): this;

    withType(type: string): this;
  }
  export class SecuritySchemeFragment extends Fragment {
    constructor();
  }
  export class SelectedParsePluginEvent {}
  export class SemanticBaseUnitClient extends AMLBaseUnitClient {
    parseSemanticSchema(url: string): Promise<AMFSemanticSchemaResult>;

    parseSemanticSchemaContent(
      content: string
    ): Promise<AMFSemanticSchemaResult>;
  }
  export class SemanticContext implements DomainElement {
    base: undefined | BaseIri;
    curies: Array<CuriePrefix>;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    iri: StrField;
    isExternalLink: BoolField;
    mapping: Array<ContextMapping>;
    overrideMappings: Array<StrField>;
    position: Range;
    typeMappings: Array<StrField>;
    vocab: undefined | DefaultVocabulary;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withBase(base: BaseIri): this;

    withCuries(curies: Array<CuriePrefix>): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIri(iri: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withMapping(mapping: Array<ContextMapping>): this;

    withOverrideMappings(overrideMappings: Array<string>): this;

    withTypeMappings(typeMappings: Array<string>): this;

    withVocab(vocab: DefaultVocabulary): this;
  }
  export class SemanticExtension implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;

    constructor();

    annotations(): Annotations;

    extensionMappingDefinition(): StrField;

    extensionName(): StrField;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withExtensionMappingDefinition(
      annotationMapping: string
    ): SemanticExtension;

    withExtensionName(name: string): SemanticExtension;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;
  }
  export class SemanticJsonSchemaConfiguration extends BaseShapesConfiguration {
    baseUnitClient(): SemanticBaseUnitClient;

    configurationState(): AMLConfigurationState;

    static empty(): SemanticJsonSchemaConfiguration;

    forInstance(url: string): Promise<SemanticJsonSchemaConfiguration>;

    static predefined(): SemanticJsonSchemaConfiguration;

    withDialect(dialect: Dialect): SemanticJsonSchemaConfiguration;

    withDialect(url: string): Promise<SemanticJsonSchemaConfiguration>;

    withErrorHandlerProvider(
      provider: ErrorHandlerProvider
    ): SemanticJsonSchemaConfiguration;

    withEventListener(
      listener: AMFEventListener
    ): SemanticJsonSchemaConfiguration;

    withParsingOptions(
      parsingOptions: ParsingOptions
    ): SemanticJsonSchemaConfiguration;

    withRenderOptions(
      renderOptions: RenderOptions
    ): SemanticJsonSchemaConfiguration;

    withResourceLoader(rl: ResourceLoader): SemanticJsonSchemaConfiguration;

    withResourceLoaders(
      rl: Array<ResourceLoader>
    ): SemanticJsonSchemaConfiguration;

    withShapePayloadPlugin(
      plugin: AMFShapePayloadValidationPlugin
    ): SemanticJsonSchemaConfiguration;

    withTransformationPipeline(
      pipeline: TransformationPipeline
    ): SemanticJsonSchemaConfiguration;

    withUnitCache(cache: UnitCache): SemanticJsonSchemaConfiguration;
  }
  export class Server implements DomainElement {
    bindings: ServerBindings;
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    protocol: StrField;
    protocolVersion: StrField;
    security: Array<SecurityRequirement>;
    url: StrField;
    variables: Array<Parameter>;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withBindings(bindings: ServerBindings): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDescription(description: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withProtocol(protocol: string): this;

    withProtocolVersion(protocolVersion: string): this;

    withSecurity(security: Array<SecurityRequirement>): this;

    withUrl(url: string): this;

    withVariable(name: string): Parameter;

    withVariables(variables: Array<Parameter>): this;
  }
  export interface ServerBinding extends DomainElement, Linkable {}
  export class ServerBindings implements DomainElement, Linkable {
    bindings: Array<ServerBinding>;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    name: StrField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): ServerBindings;

    withBindings(bindings: Array<ServerBinding>): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withName(name: string): this;
  }
  export class Settings implements DomainElement {
    additionalProperties: DataNode;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withAdditionalProperties(properties: DataNode): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;
  }
  export class SeverityLevels {
    static readonly INFO: "Info";
    static readonly VIOLATION: "Violation";
    static readonly WARNING: "Warning";

    static unapply(arg: string): string;
  }
  export interface Shape extends DomainElement, Linkable {
    and: Array<Shape>;
    customShapeProperties: Array<ShapeExtension>;
    customShapePropertyDefinitions: Array<PropertyShape>;
    defaultValue: DataNode;
    defaultValueStr: StrField;
    deprecated: BoolField;
    description: StrField;
    displayName: StrField;
    elseShape: Shape;
    federationMetadata: ShapeFederationMetadata;
    hasExplicitName: boolean;
    ifShape: Shape;
    inherits: Array<Shape>;
    isExtension: BoolField;
    isStub: BoolField;
    name: StrField;
    not: Shape;
    or: Array<Shape>;
    readOnly: BoolField;
    thenShape: Shape;
    values: Array<DataNode>;
    writeOnly: BoolField;
    xone: Array<Shape>;

    withAnd(subShapes: Array<Shape>): this;

    withCustomShapeProperties(
      customShapeProperties: Array<ShapeExtension>
    ): this;

    withCustomShapePropertyDefinition(name: string): PropertyShape;

    withCustomShapePropertyDefinitions(
      propertyDefinitions: Array<PropertyShape>
    ): this;

    withDefaultStr(value: string): this;

    withDefaultValue(defaultVal: DataNode): this;

    withDeprecated(deprecated: boolean): this;

    withDescription(description: string): this;

    withDisplayName(name: string): this;

    withElse(elseShape: Shape): this;

    withFederationMetadata(metadata: ShapeFederationMetadata): this;

    withIf(ifShape: Shape): this;

    withInherits(inherits: Array<Shape>): this;

    withIsExtension(value: boolean): this;

    withIsStub(value: boolean): this;

    withName(name: string): this;

    withNode(shape: Shape): this;

    withOr(subShapes: Array<Shape>): this;

    withReadOnly(readOnly: boolean): this;

    withThen(thenShape: Shape): this;

    withValues(values: Array<DataNode>): this;

    withWriteOnly(writeOnly: boolean): this;

    withXone(subShapes: Array<Shape>): this;
  }
  export class ShapeExtension implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    definedBy: PropertyShape;
    extendsNode: Array<DomainElement>;
    extension: DataNode;
    id: string;
    isExternalLink: BoolField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDefinedBy(definedBy: PropertyShape): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withExtension(extension: DataNode): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;
  }
  export class ShapeFederationMetadata {
    constructor();
  }
  export class ShapeOperation extends AbstractOperation {
    federationMetadata: ShapeFederationMetadata;
    request: ShapeRequest;
    response: ShapeResponse;
    responses: Array<ShapeResponse>;

    constructor();

    withFederationMetadata(metadata: ShapeFederationMetadata): this;

    // @ts-ignore
    withRequest(request: ShapeRequest): this;

    withResponse(name: string): ShapeResponse;

    withResponses(responses: Array<ShapeResponse>): this;
  }
  export class ShapeParameter extends AbstractParameter {
    federationMetadata: ShapeFederationMetadata;

    constructor();

    withFederationMetadata(metadata: ShapeFederationMetadata): this;
  }
  export class ShapePayload extends AbstractPayload {
    constructor();

    linkCopy(): ShapePayload;
  }
  export class ShapeRequest implements AbstractRequest {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    queryParameters: Array<ShapeParameter>;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withQueryParameter(name: string): ShapeParameter;

    withQueryParameters(parameters: Array<ShapeParameter>): this;
  }
  export class ShapeResponse implements AbstractResponse {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    payload: ShapePayload;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withPayload(payload: ShapePayload): ShapePayload;
  }
  export class ShapeValidationConfiguration {
    readonly maxYamlReferences: undefined | number;

    static apply(config: AMFGraphConfiguration): ShapeValidationConfiguration;

    eh(): ClientErrorHandler;

    fetchContent(url: string): Promise<Content>;

    static predefined(): ShapeValidationConfiguration;
  }
  export class ShapesConfiguration extends BaseShapesConfiguration {
    baseUnitClient(): AMLBaseUnitClient;

    configurationState(): AMLConfigurationState;

    elementClient(): ShapesElementClient;

    static empty(): ShapesConfiguration;

    forInstance(url: string): Promise<ShapesConfiguration>;

    static predefined(): ShapesConfiguration;

    withDialect(dialect: Dialect): ShapesConfiguration;

    withDialect(url: string): Promise<ShapesConfiguration>;

    withErrorHandlerProvider(
      provider: ErrorHandlerProvider
    ): ShapesConfiguration;

    withEventListener(listener: AMFEventListener): ShapesConfiguration;

    withParsingOptions(parsingOptions: ParsingOptions): ShapesConfiguration;

    withRenderOptions(renderOptions: RenderOptions): ShapesConfiguration;

    withResourceLoader(rl: ResourceLoader): ShapesConfiguration;

    withResourceLoaders(rl: Array<ResourceLoader>): ShapesConfiguration;

    withShapePayloadPlugin(
      plugin: AMFShapePayloadValidationPlugin
    ): ShapesConfiguration;

    withTransformationPipeline(
      pipeline: TransformationPipeline
    ): ShapesConfiguration;

    withUnitCache(cache: UnitCache): ShapesConfiguration;
  }
  export class ShapesElementClient extends BaseShapesElementClient {
    getConfiguration(): ShapesConfiguration;
  }
  export class SkippedValidationPluginEvent {}
  export interface Spec {
    readonly id: string;
    isAsync: boolean;
    isOas: boolean;
    isRaml: boolean;
    readonly mediaType: string;
  }
  export class Spec {
    static readonly AMF: Spec;
    static readonly AML: Spec;
    static readonly ASYNC20: Spec;
    static readonly GRAPHQL: Spec;
    static readonly GRAPHQL_FEDERATION: Spec;
    static readonly GRPC: Spec;
    static readonly JSONDLSCHEMA: Spec;
    static readonly JSONSCHEMA: Spec;
    static readonly JSONSCHEMADIALECT: Spec;
    static readonly OAS20: Spec;
    static readonly OAS30: Spec;
    static readonly PAYLOAD: Spec;
    static readonly RAML08: Spec;
    static readonly RAML10: Spec;

    static apply(name: string): Spec;
  }
  export class StartedTransformationStepEvent {}
  export class StartingContentParsingEvent {
    content: Content;
    url: string;
  }
  export class StartingParsingEvent {
    url: string;
  }
  export class StartingRenderToWriterEvent {}
  export class StartingRenderingEvent {
    mediaType: undefined | string;
    unit: BaseUnit;
  }
  export class StartingTransformationEvent {
    pipeline: TransformationPipeline;
  }
  export class StartingValidationEvent {
    totalPlugins: number;
  }
  export interface Stats {
    atime: undefined;
    atimeMs: number;
    birthtime: undefined;
    birthtimeMs: number;
    blksize: number;
    blocks: number;
    ctime: undefined;
    ctimeMs: number;
    dev: number;
    gid: number;
    ino: number;
    mode: number;
    mtime: undefined;
    mtimeMs: number;
    nlink: number;
    rdev: number;
    size: number;
    uid: number;

    isBlockDevice(): boolean;

    isCharacterDevice(): boolean;

    isDirectory(): boolean;

    isFIFO(): boolean;

    isFile(): boolean;

    isSocket(): boolean;

    isSymbolicLink(): boolean;
  }
  export class StrField implements ValueField<string> {
    isNull: boolean;
    isNullOrEmpty: boolean;
    nonEmpty: boolean;
    nonNull: boolean;
    readonly option: undefined | string;

    annotations(): Annotations;

    is(other: string): boolean;

    is(accepts: undefined): boolean;

    remove(): void;

    toString(): string;

    value(): string;
  }
  export class StrictValidationMode extends ValidationMode {}
  export class Tag implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    documentation: CreativeWork;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDescription(description: string): this;

    withDocumentation(documentation: CreativeWork): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withVariables(documentation: CreativeWork): this;
  }
  export class TemplatedLink implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    mapping: Array<IriTemplateMapping>;
    name: StrField;
    operationId: StrField;
    operationRef: StrField;
    position: Range;
    requestBody: StrField;
    server: Server;
    template: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDescription(description: string): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withMapping(mapping: Array<IriTemplateMapping>): this;

    withName(name: string): this;

    withOperationId(operationId: string): this;

    withOperationRef(operationRef: string): this;

    withRequestBody(requestBody: string): this;

    withServer(server: Server): this;

    withTemplate(template: string): this;
  }
  export class Trait extends AbstractDeclaration {
    linkTarget: undefined | DomainElement;

    constructor();

    linkCopy(): Trait;
  }
  export class TraitFragment extends Fragment {
    constructor();
  }
  export interface TransformationPipeline {
    readonly name: string;
    steps: Array<TransformationStep>;
  }
  export class TransformationPipelineBuilder {
    append(newStage: TransformationStep): TransformationPipelineBuilder;

    build(): TransformationPipeline;

    static empty(pipelineName: string): TransformationPipelineBuilder;

    static fromPipeline(
      pipeline: TransformationPipeline
    ): TransformationPipelineBuilder;

    static fromPipeline(
      pipelineName: string,
      conf: AMFGraphConfiguration
    ): undefined | TransformationPipelineBuilder;

    prepend(newStage: TransformationStep): TransformationPipelineBuilder;

    withName(newName: string): TransformationPipelineBuilder;
  }
  export interface TransformationStep {
    transform(
      model: BaseUnit,
      errorHandler: ClientErrorHandler,
      configuration: AMFGraphConfiguration
    ): BaseUnit;
  }
  export class TransformationStepFactory {
    static from(step: JsTransformationStep): TransformationStep;
  }
  export class TupleShape extends DataArrangeShape {
    additionalItemsSchema: Shape;
    closedItems: BoolField;
    items: Array<Shape>;

    constructor();

    linkCopy(): TupleShape;

    withClosedItems(closedItems: boolean): this;

    withItems(items: Array<Shape>): this;
  }
  export class UnionNodeMapping extends AnyMapping implements Linkable {
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    name: StrField;

    constructor();

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): UnionNodeMapping;

    objectRange(): Array<StrField>;

    typeDiscriminator(): Map<string, string>;

    typeDiscriminatorName(): StrField;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withName(name: string): UnionNodeMapping;

    withObjectRange(range: Array<string>): UnionNodeMapping;

    withTypeDiscriminator(typesMapping: Map<string, string>): UnionNodeMapping;

    withTypeDiscriminatorName(name: string): UnionNodeMapping;
  }
  export class UnionShape extends AnyShape {
    anyOf: Array<Shape>;
    serializationSchema: Shape;

    constructor();

    withAnyOf(anyOf: Array<Shape>): UnionShape;

    withSerializationSchema(schema: Shape): this;
  }
  export interface UnitCache {
    fetch(url: string): Promise<CachedReference>;
  }
  export class UnitCacheHitEvent {}
  export class Unspecified implements JSONSchemaVersion {}
  export class ValidatePayloadRequest {
    config: ShapeValidationConfiguration;
    mediaType: string;
    shape: Shape;
  }
  export class ValidationCandidate {
    payload: PayloadFragment;
    shape: Shape;

    constructor(shape: Shape, payload: PayloadFragment);
  }
  export interface ValidationMode {}
  export class ValidationMode {
    static readonly ScalarRelaxedValidationMode: ValidationMode;
    static readonly StrictValidationMode: ValidationMode;
  }
  export class ValidationProfile {
    baseProfile(): undefined | ProfileName;

    profileName(): ProfileName;
  }
  export interface ValidationResult {
    readonly dataPath: string;
    readonly keyword: string;
    readonly message: string;
    readonly params: undefined;
    readonly schemaPath: string;
  }
  export class ValidationShapeSet {
    candidates: Array<ValidationCandidate>;
    defaultSeverity: string;

    constructor(
      candidates: Array<ValidationCandidate>,
      closure: Array<Shape>,
      defaultSeverity: string
    );
  }
  export interface ValueField<T> extends Annotable {
    isNull: boolean;
    nonNull: boolean;
    readonly option: undefined | T;

    is(other: T): boolean;

    is(accepts: undefined): boolean;

    remove(): void;

    toString(): string;

    value(): T;
  }
  export class VariableValue implements DomainElement {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    position: Range;
    value: DataNode;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withValue(value: DataNode): this;
  }
  export class Vocabulary implements BaseUnit, DeclaresModel {
    base: StrField;
    declares: Array<DomainElement>;
    description: StrField;
    externals: Array<External>;
    id: string;
    imports: Array<VocabularyReference>;
    location: string;
    modelVersion: StrField;
    name: StrField;
    processingData: BaseUnitProcessingData;
    raw: undefined | string;
    sourceInformation: BaseUnitSourceInformation;
    sourceSpec: undefined | Spec;
    usage: StrField;

    constructor();

    annotations(): Annotations;

    classTerms(): Array<ClassTerm>;

    cloneUnit(): BaseUnit;

    datatypePropertyTerms(): Array<DatatypePropertyTerm>;

    findById(id: string): undefined | DomainElement;

    findByType(typeId: string): Array<DomainElement>;

    objectPropertyTerms(): Array<ObjectPropertyTerm>;

    pkg(): StrField;

    references(): Array<BaseUnit>;

    withBase(base: string): Vocabulary;

    withDeclaredElement(declared: DomainElement): this;

    withDeclares(declares: Array<DomainElement>): this;

    withExternals(externals: Array<External>): Vocabulary;

    withId(id: string): this;

    withImports(vocabularies: Array<VocabularyReference>): Vocabulary;

    withLocation(location: string): this;

    withName(name: string): Vocabulary;

    withPkg(pkg: string): this;

    withProcessingData(data: BaseUnitProcessingData): this;

    withRaw(raw: string): this;

    withReferenceAlias(
      alias: string,
      id: string,
      fullUrl: string,
      relativeUrl: string
    ): BaseUnit;

    withReferences(references: Array<BaseUnit>): this;

    withUsage(usage: string): this;
  }
  export class VocabularyReference implements DomainElement {
    alias: StrField;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    position: Range;
    reference: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withAlias(alias: string): VocabularyReference;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withReference(reference: string): VocabularyReference;
  }
  export class WebAPIConfiguration {
    static WebAPI(): AMFConfiguration;

    static fromSpec(spec: Spec): AMFConfiguration;
  }
  export class WebApi extends Api<WebApi> {
    accepts: Array<StrField>;
    contentType: Array<StrField>;
    customDomainProperties: Array<DomainExtension>;
    description: StrField;
    documentations: Array<CreativeWork>;
    endPoints: Array<EndPoint>;
    extendsNode: Array<DomainElement>;
    id: string;
    identifier: StrField;
    isExternalLink: BoolField;
    license: License;
    name: StrField;
    position: Range;
    provider: Organization;
    schemes: Array<StrField>;
    security: Array<SecurityRequirement>;
    servers: Array<Server>;
    tags: Array<Tag>;
    termsOfService: StrField;
    version: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withAccepts(accepts: Array<string>): this;

    withContentType(contentType: Array<string>): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withDefaultServer(url: string): Server;

    withDescription(description: string): this;

    withDocumentation(documentations: Array<CreativeWork>): this;

    withDocumentationTitle(title: string): CreativeWork;

    withDocumentationUrl(url: string): CreativeWork;

    withEndPoint(path: string): EndPoint;

    withEndPoints(endPoints: Array<EndPoint>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIdentifier(identifier: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLicense(license: License): this;

    withName(name: string): this;

    withProvider(provider: Organization): this;

    withSchemes(schemes: Array<string>): this;

    withSecurity(security: Array<SecurityRequirement>): this;

    withServer(url: string): Server;

    withServers(servers: Array<Server>): this;

    withTags(tags: Array<Tag>): this;

    withTermsOfService(terms: string): this;

    withVersion(version: string): this;
  }
  export class WebSocketsChannelBinding implements ChannelBinding {
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    headers: Shape;
    id: string;
    isExternalLink: BoolField;
    isLink: boolean;
    linkLabel: StrField;
    linkTarget: undefined | DomainElement;
    method: StrField;
    position: Range;
    query: Shape;
    type: StrField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    link<T>(label: string): T;

    link<T>(): T;

    linkCopy(): WebSocketsChannelBinding;

    withBindingVersion(bindingVersion: string): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withHeaders(headers: Shape): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withLinkLabel(label: string): this;

    withLinkTarget(target: undefined): this;

    withMethod(method: string): this;

    withQuery(query: Shape): this;

    withType(type: string): this;
  }
  export class XMLSerializer implements DomainElement {
    attribute: BoolField;
    customDomainProperties: Array<DomainExtension>;
    extendsNode: Array<DomainElement>;
    id: string;
    isExternalLink: BoolField;
    name: StrField;
    namespace: StrField;
    position: Range;
    prefix: StrField;
    wrapped: BoolField;

    constructor();

    annotations(): Annotations;

    graph(): Graph;

    withAttribute(attribute: boolean): this;

    withCustomDomainProperties(extensions: Array<DomainExtension>): this;

    withExtendsNode(extension: Array<ParametrizedDeclaration>): this;

    withId(id: string): this;

    withIsExternalLink(isExternalLink: boolean): DomainElement;

    withName(name: string): this;

    withNamespace(namespace: string): this;

    withPrefix(prefix: string): this;

    withWrapped(wrapped: boolean): this;
  }
  namespace org {
    namespace mulesoft {
      namespace common {
        namespace io {
          export class LimitReachedException {
            constructor();
          }
        }
      }
    }
    namespace yaml {
      namespace builder {
        export class JsOutputBuilder {
          isDefined: boolean;
          result: undefined;

          constructor();

          static apply(): JsOutputBuilder;

          doc(f: undefined): any;

          list(f: undefined): any;

          obj(f: undefined): any;
        }
      }
    }
  }
}
