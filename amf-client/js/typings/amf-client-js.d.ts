
declare module "@mulesoft/amf-client-js" {

    type URI = string;
    type URL = string;

    namespace model {

        namespace document {

            export interface EncodesModel {
                encodes: model.domain.DomainElement
            }

            export interface DeclaresModel {
                declares: model.domain.DomainElement[]
            }

            export class BaseUnit {
                raw: string | null;
                withRaw(raw: string): this;
                references(): BaseUnit[];
                withReferences(newReferences: BaseUnit[]): this
                location: URL;
                usage(): string;
                findById(id: URI): model.domain.DomainElement | null;
                findByType(typeId: URI): model.domain.DomainElement[];
            }

            export class Document extends BaseUnit implements EncodesModel, DeclaresModel {
                declares: domain.DomainElement[];
                encodes: domain.DomainElement;
            }

            export class Fragment extends BaseUnit implements EncodesModel {
                encodes: model.domain.DomainElement;
            }

            export class Module extends BaseUnit implements  DeclaresModel {
                declares: domain.DomainElement[];
            }
        }

        namespace domain {

            export class DomainElement {
                customDomainProperties(): CustomDomainProperty[]
                withCustomDomainProperties(customDomainProperties: CustomDomainProperty[]): DomainElement
                position(): core.parser.Range
                getId(): URI;
                getTypeIds(): URI[];
                getPropertyIds(): URI[];
                getScalarByPropertyId(propertyId: URI): any[]
                getObjectByPropertyId(propertyId: URI): DomainElement[]
            }

            export class CustomDomainProperty extends DomainElement {
                name(): string;
                withName(name: string): this;
                displayName(): string;
                withDisplayName(displayName: string): this;
                description(): string;
                withDescription(description: string): this;
                domain(): string;
                withDomain(domain: string): this;
                schema(): Shape;
                withSchema(schema: Shape): this;
            }

            export class Shape extends DomainElement {
                name(): string;
                withName(name: string): this;
                displayName(): string;
                withDisplayName(displayName: string): this;
                default(): string;
                withDefault(defaultValue: string): this;
                values(): string[]
                withValues(values: string[]): this;
                inherits(): Shape[];
                withInherits(baseShapes: Shape[]): this;
            }

            export class PropertyShape extends DomainElement {
                pah(): string;
                range(): Shape;
                minCount: number;
                maxCount: number;
                withPath(path: String): this;
                withRange(range: Shape): this;
                withMinCount(minCount: number): this;
                withMaxCount(maxCount: number): this;
            }

            export class AnyShape extends Shape {}

            export class UnionShape extends AnyShape {
                anyOf(): AnyShape[];
                withAnyOf(anyOf: AnyShape[]): this;
            }

            export class RecursiveShape extends DomainElement {
                fixPoint(): string;
                withFixPoint(fixPoint: string): this;
            }

            export class DataNode extends DomainElement {
                name(): string;
                withName(name: string): this;
            }

            export class DomainExtension extends DomainElement {
                name(): string;
                definedBy(): CustomDomainProperty;
                extension(): DataNode;
                withName(name: string): this;
                withDefinedBy(definitor: CustomDomainProperty): this;
                withExtension(extension: DataNode): this;
            }

            export class AbstractDeclaration {
                name(): string;
                dataNode(): DataNode;
                variables(): string[];

                withName(name: string): this;
                withDataNode(dataNode: DataNode): this;
                withVariables(vars: string[]): this;
            }

            export class Trait extends AbstractDeclaration {}
            export class ResourceType extends AbstractDeclaration {}

            export class ParametrizedDeclaration extends DomainElement {
                name(): string;
                target(): URI;
                variables(): VariableValue[];
                withName(name: string): this;
                withVariables(variables: VariableValue[]): this;
                withTarget(targte: string): this;
            }

            export class Variable extends  DomainElement {
                name(): string
                value(): string
            }

            export class VariableValue extends DomainElement {
                name(): string;
                value(): string;
                withName(name: string): this;
                withValue(value: string): this;
            }
        }
    }

    namespace validation {

        export class AMFValidationReport {
            conforms: boolean;
            model: URI;
            profile: string;
            results: AMFValidationResult[];
        }

        export class AMFValidationResult {
            message: string;
            level: string;
            targetNode: URI;
            targetProperty: String|null;
            validationId: String;
            source: any;
        }
    }

    namespace core {

        namespace parser {

            export class Position {
                line: number;
                column: number;
            }

            export class Range {
                toString(): string
                start: core.parser.Position;
                end: core.parser.Position;
            }
        }


        namespace client {

            export interface JsHandler<T> {
                success(T): void;
                error(Throwable): void;
            }

            export interface StringHandler {
                success(string): void;
                error(Throwable): void;
            }

            export interface FileHandler {
                success(): void;
                error(Throwable): void;
            }

            export class Generator {
                generateFile(unit: model.document.BaseUnit, url: string, handler: FileHandler): void;
                generateString(unit: model.document.BaseUnit, handler: StringHandler): void;
                generateFile(unit: model.document.BaseUnit, url: string): Promise<void>;
                generateString(unit: model.document.BaseUnit): string;
            }

            export class Parser {
                parseFile(url: string, handler: JsHandler<model.document.BaseUnit>): void
                parseString(stream: string, handler: JsHandler<model.document.BaseUnit>): void
                parseFileAsync(url: string): Promise<model.document.BaseUnit>
                parseStringAsync(stream: string): Promise<model.document.BaseUnit>
                reportValidation(profileName: string, messageStyle: string): Promise<validation.AMFValidationReport>
                reportCustomValidation(profileName: string, customProfilePath: string): Promise<validation.AMFValidationReport>
            }

            export class Resolver {
                resolve(unit: model.document.BaseUnit): model.document.BaseUnit
            }
        }
    }

    namespace plugins {
        namespace document {

            export class WebApi {
                static register(): any;
                static validatePayload(shape: model.domain.Shape, payload: model.domain.DataNode): Promise<validation.AMFValidationReport>;
            }

            export class Vocabularies {
                static register(): any
            }
        }
        namespace features {
            export class AMFValidation {
                static register(): any
            }
        }
    }


    export class Core {
        static init(): Promise<any>
        static generator(vendor: string, mediaType: string): core.client.Generator
        static resolver(vendor: string): core.client.Resolver
        static parser(vendor: string, mediaType: string): core.client.Parser
        static validate(model: model.document.BaseUnit, profileName: string, messageStyle): Promise<validation.AMFValidationReport>
        static loadValidationProfile(url: string): Promise<string>
        static registerNamespace(alias: string, prefix: string): Boolean
    }
}