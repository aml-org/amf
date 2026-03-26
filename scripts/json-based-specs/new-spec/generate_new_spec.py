#!/usr/bin/env python3
"""
Script to generate a new JsonSchemaBasedSpec module in the AMF project.

This script creates the full module structure, updates build.sbt, schemas.yaml,
and registers the new spec in amf-core (Spec.scala, ProfileNames.scala).

Usage:
    python3 generate_new_spec.py

The script will interactively prompt for all required inputs.
"""

import glob
import os
import re
import sys
import shutil
import textwrap

# Resolve project roots
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
AMF_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "..", ".."))
AMF_CORE_ROOT = os.path.abspath(os.path.join(AMF_ROOT, "..", "amf-core"))
APB_ROOT = os.path.abspath(os.path.join(AMF_ROOT, "..", "apb"))
SCHEMAS_YAML_PATH = os.path.join(AMF_ROOT, "scripts", "json-based-specs", "update-schema", "schemas.yaml")


# ---------------------------------------------------------------------------
# Naming helpers
# ---------------------------------------------------------------------------

def to_camel_case(name: str) -> str:
    """Convert 'my spec' or 'my-spec' to 'MySpec'."""
    return "".join(word.capitalize() for word in re.split(r"[\s\-_]+", name))


def to_lower_camel(name: str) -> str:
    """Convert 'my spec' to 'mySpec'."""
    cc = to_camel_case(name)
    return cc[0].lower() + cc[1:]


def to_package_segment(name: str) -> str:
    """Convert 'My Spec' to 'myspec' (no separators)."""
    return re.sub(r"[\s\-_]+", "", name).lower()


def to_module_dir(name: str) -> str:
    """Convert 'My Spec' to 'amf-my-spec'."""
    slug = re.sub(r"[\s_]+", "-", name).lower()
    return f"amf-{slug}"


def to_sbt_lazy_val(name: str) -> str:
    """Convert 'My Spec' to 'mySpec' for build.sbt lazy val."""
    return to_lower_camel(name)


def to_schema_file_name(name: str) -> str:
    """Convert 'My Spec' to 'schema_my_spec.json'."""
    slug = re.sub(r"[\s\-]+", "_", name).lower()
    return f"schema_{slug}.json"


def to_spec_object_name(name: str) -> str:
    """Convert 'My Spec' to 'MySpec' for case object in Spec.scala."""
    return to_camel_case(name)


def to_spec_constant_name(name: str) -> str:
    """Convert 'My Spec' to 'MY_SPEC' for @JSExport val."""
    return re.sub(r"[\s\-]+", "_", name).upper()


def to_profile_object_name(name: str) -> str:
    """Convert 'My Spec' to 'MySpecProfile'."""
    return to_camel_case(name) + "Profile"


def to_profile_names_constant(name: str) -> str:
    """Convert 'My Spec' to 'MY_SPEC' for ProfileNames."""
    return to_spec_constant_name(name)


def to_spec_id(name: str) -> str:
    """Convert 'my spec' to 'My Spec' for Spec.id."""
    return " ".join(word.capitalize() for word in re.split(r"[\s\-_]+", name))


def to_automatic_module_name(name: str) -> str:
    """Convert 'My Spec' to 'amf.my-spec' for AutomaticModuleName."""
    slug = re.sub(r"[\s_]+", "-", name).lower()
    return f"amf.{slug}"


def to_sbt_name(name: str) -> str:
    """Convert 'My Spec' to 'amf-my-spec' for sbt name := setting."""
    return to_module_dir(name)


def to_apb_slug(name: str) -> str:
    """Convert 'My Spec' to 'my-spec' for APB test resource directories."""
    return re.sub(r"[\s_]+", "-", name).lower()


# ---------------------------------------------------------------------------
# Input collection
# ---------------------------------------------------------------------------

def prompt(message: str, default: str = None) -> str:
    suffix = f" [{default}]" if default else ""
    result = input(f"{message}{suffix}: ").strip()
    return result if result else (default or "")


def prompt_yes_no(message: str, default: bool = True) -> bool:
    suffix = " [Y/n]" if default else " [y/N]"
    result = input(f"{message}{suffix}: ").strip().lower()
    if not result:
        return default
    return result in ("y", "yes")


def read_spec_local_path() -> str:
    """Read specLocalPath from schemas.yaml and return the resolved absolute path."""
    if not os.path.exists(SCHEMAS_YAML_PATH):
        print(f"Error: schemas.yaml not found at {SCHEMAS_YAML_PATH}")
        sys.exit(1)
    with open(SCHEMAS_YAML_PATH, "r") as f:
        for line in f:
            m = re.match(r'\s*specLocalPath:\s*"([^"]+)"', line)
            if m:
                return os.path.abspath(os.path.expanduser(m.group(1)))
    print("Error: specLocalPath not found in schemas.yaml repositories")
    sys.exit(1)


def find_asset_file(directory: str) -> str:
    """Find the asset.* file in a directory. Returns the path or None."""
    matches = glob.glob(os.path.join(directory, "asset.*"))
    if len(matches) == 1:
        return matches[0]
    if len(matches) > 1:
        print(f"  Warning: multiple asset.* files in {directory}, using first: {matches[0]}")
        return matches[0]
    return None


def collect_inputs() -> dict:
    print("=" * 60)
    print("  Generate New JsonSchemaBasedSpec Module")
    print("=" * 60)
    print()

    # Read specLocalPath from schemas.yaml
    spec_local_path = read_spec_local_path()
    print(f"  specLocalPath (from schemas.yaml): {spec_local_path}")
    print(f"  All schema and instance paths should be relative to this directory.")
    print()

    spec_name = prompt("Spec name (e.g. 'My Spec', 'Agent Card')")
    if not spec_name:
        print("Error: spec name is required")
        sys.exit(1)

    # Derive names
    camel = to_camel_case(spec_name)
    module_dir = to_module_dir(spec_name)
    pkg_segment = to_package_segment(spec_name)
    schema_file = to_schema_file_name(spec_name)
    apb_slug = to_apb_slug(spec_name)

    print(f"\n  Derived names:")
    print(f"    Module dir:    {module_dir}")
    print(f"    CamelCase:     {camel}")
    print(f"    Package:       amf.{pkg_segment}")
    print(f"    Schema file:   {schema_file}")
    print()

    # JSON Schema path (relative to specLocalPath)
    spec_schema_rel = prompt("Path to root JSON Schema file (relative to specLocalPath)")
    if not spec_schema_rel:
        print("Error: JSON Schema path is required")
        sys.exit(1)
    json_schema_path = os.path.join(spec_local_path, spec_schema_rel)
    if not os.path.exists(json_schema_path):
        print(f"  Warning: file not found: {json_schema_path}")

    # Media type
    print("\nMedia type options:")
    print("  1. application/json (default)")
    print("  2. application/yaml")
    media_choice = prompt("Choose media type", "1")
    if media_choice == "2":
        media_type = "application/yaml"
        media_type_scala = "`application/yaml`"
        parser_class = "YamlParser"
        parser_import = "org.yaml.parser.YamlParser"
        mime_const = "Mimes.`application/yaml`"
    else:
        media_type = "application/json"
        media_type_scala = "`application/json`"
        parser_class = "JsonParser"
        parser_import = "org.yaml.parser.JsonParser"
        mime_const = "Mimes.`application/json`"

    # ID Entry
    has_id_entry = prompt_yes_no("\nDoes this spec have an ID entry key?", default=False)
    id_entry_key = ""
    if has_id_entry:
        id_entry_key = prompt("ID entry key (e.g. 'schemaVersion', 'agentNetwork')")
        if not id_entry_key:
            print("Error: ID entry key is required when has_id_entry is True")
            sys.exit(1)

    # Valid instance directory (relative to specLocalPath)
    spec_valid_dir_rel = prompt("\nValid instances directory (relative to specLocalPath)")
    if not spec_valid_dir_rel:
        print("Error: valid instances directory is required")
        sys.exit(1)
    valid_abs_dir = os.path.join(spec_local_path, spec_valid_dir_rel)
    valid_instance_path = find_asset_file(valid_abs_dir) if os.path.isdir(valid_abs_dir) else None
    if not valid_instance_path:
        print(f"  Warning: no asset.* file found in {valid_abs_dir}")
        # Fallback so the rest of the script doesn't crash
        valid_instance_path = os.path.join(valid_abs_dir, "asset.json")

    # Invalid instance directory (relative to specLocalPath)
    spec_invalid_dir_rel = prompt("Invalid instances directory (relative to specLocalPath)")
    if not spec_invalid_dir_rel:
        print("Error: invalid instances directory is required")
        sys.exit(1)
    invalid_abs_dir = os.path.join(spec_local_path, spec_invalid_dir_rel)
    invalid_instance_path = find_asset_file(invalid_abs_dir) if os.path.isdir(invalid_abs_dir) else None
    if not invalid_instance_path:
        print(f"  Warning: no asset.* file found in {invalid_abs_dir}")
        invalid_instance_path = os.path.join(invalid_abs_dir, "asset.json")

    # Classifier for APB
    classifier = prompt("\nClassifier string for APB (e.g. 'agent-graph', 'mcp-metadata')")
    if not classifier:
        print("Error: classifier is required")
        sys.exit(1)

    # Determine file extension from valid instance
    _, valid_ext = os.path.splitext(valid_instance_path)
    _, invalid_ext = os.path.splitext(invalid_instance_path)

    return {
        "spec_name": spec_name,
        "camel": camel,
        "lower_camel": to_lower_camel(spec_name),
        "module_dir": module_dir,
        "pkg_segment": pkg_segment,
        "schema_file": schema_file,
        "json_schema_path": json_schema_path,
        "spec_schema_rel": spec_schema_rel,
        "spec_valid_dir_rel": spec_valid_dir_rel,
        "spec_invalid_dir_rel": spec_invalid_dir_rel,
        "media_type": media_type,
        "media_type_scala": media_type_scala,
        "parser_class": parser_class,
        "parser_import": parser_import,
        "mime_const": mime_const,
        "has_id_entry": has_id_entry,
        "id_entry_key": id_entry_key,
        "valid_instance_path": valid_instance_path,
        "invalid_instance_path": invalid_instance_path,
        "valid_ext": valid_ext,
        "invalid_ext": invalid_ext,
        "classifier": classifier,
        "apb_slug": apb_slug,
        "spec_id": to_spec_id(spec_name),
        "spec_object": to_spec_object_name(spec_name),
        "spec_constant": to_spec_constant_name(spec_name),
        "profile_object": to_profile_object_name(spec_name),
        "profile_constant": to_profile_names_constant(spec_name),
        "sbt_lazy_val": to_sbt_lazy_val(spec_name),
        "sbt_name": to_sbt_name(spec_name),
        "auto_module_name": to_automatic_module_name(spec_name),
    }


# ---------------------------------------------------------------------------
# File generation helpers
# ---------------------------------------------------------------------------

def write_file(path: str, content: str):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(content)
    print(f"  Created: {os.path.relpath(path, AMF_ROOT)}")


def copy_file(src: str, dest: str):
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    shutil.copy2(src, dest)
    print(f"  Copied:  {os.path.relpath(dest, AMF_ROOT)}")


# ---------------------------------------------------------------------------
# Module file generators
# ---------------------------------------------------------------------------

def gen_schema_scala(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.internal.plugins.parse.schema

        import amf.{c['pkg_segment']}.internal.spec.{c['camel']}SchemaContent
        import amf.shapes.internal.plugins.parser.schema.JsonSchemaBasedSpecSchema

        object {c['camel']}Schema extends JsonSchemaBasedSpecSchema {{

          override def schema: String = {c['camel']}SchemaContent.content

        }}
    """)


def gen_schema_loader_scala(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.internal.plugins.parse.schema

        import amf.shapes.internal.plugins.parser.schema.{{JsonSchemaBasedSpecSchema, JsonSchemaBasedSpecSchemaLoader}}

        object {c['camel']}SchemaLoader extends JsonSchemaBasedSpecSchemaLoader {{

          override protected def schemaProvider: JsonSchemaBasedSpecSchema = {c['camel']}Schema

        }}
    """)


def gen_entry_no_entry_scala(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.internal.plugins.parse.entry

        import amf.shapes.internal.plugins.parser.entry.IdEntryNoEntry


        object {c['camel']}IdEntry extends IdEntryNoEntry
    """)


def gen_entry_version_scala(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.internal.plugins.parse.entry

        import amf.shapes.internal.plugins.parser.entry.{{IdEntryVersion, IdVersion}}

        class {c['camel']}Entry(override val version: String) extends IdVersion(version)

        object {c['camel']}Entry extends IdEntryVersion {{

          override protected val idKey: String = "{c['id_entry_key']}"

          override protected def getIdVersionFromString(text: String): Option[IdVersion] = {{
            // No fixed versions at the moment, so any text could be a version
            Some(new {c['camel']}Entry(text))
          }}
        }}
    """)


def gen_id_entry_scala(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.internal.plugins.parse.entry

        import amf.shapes.internal.plugins.parser.entry.{{IdEntry, IdEntryVersion}}

        object {c['camel']}IdEntry extends IdEntry {{
          override protected def versionHandler: IdEntryVersion = {c['camel']}Entry
        }}
    """)


def gen_parse_plugin_scala(c: dict) -> str:
    if c["has_id_entry"]:
        entry_import = f"import amf.{c['pkg_segment']}.internal.plugins.parse.entry.{c['camel']}IdEntry"
        entry_call = f"{c['camel']}IdEntry(document).nonEmpty"
    else:
        entry_import = f"import amf.{c['pkg_segment']}.internal.plugins.parse.entry.{c['camel']}IdEntry"
        entry_call = f"{c['camel']}IdEntry(document).nonEmpty"

    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.internal.plugins.parse

        import amf.core.internal.parser.Root
        import amf.core.internal.remote.{{{c['spec_object']}, Spec}}
        {entry_import}
        import amf.{c['pkg_segment']}.internal.plugins.parse.schema.{c['camel']}SchemaLoader
        import amf.shapes.client.scala.model.document.JsonSchemaDocument
        import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecParsePlugin

        object {c['camel']}ParsePlugin extends JsonSchemaBasedSpecParsePlugin {{

          override protected val specSchema: JsonSchemaDocument = {c['camel']}SchemaLoader.doc

          override protected def existsSpecEntry(document: Root): Boolean = {entry_call}

          override def spec: Spec = {c['spec_object']}
        }}
    """)


def gen_render_plugin_scala(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.internal.plugins.render

        import amf.core.internal.remote.{{{c['spec_object']}, Spec}}
        import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecRenderPlugin

        object {c['camel']}RenderPlugin extends JsonSchemaBasedSpecRenderPlugin {{

          override protected def spec: Spec = {c['spec_object']}

        }}
    """)


def gen_validation_plugin_scala(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.internal.plugins.validation

        import amf.core.client.common.validation.{{ProfileName, ProfileNames}}
        import amf.core.client.scala.model.domain.Shape
        import amf.{c['pkg_segment']}.internal.plugins.parse.schema.{c['camel']}SchemaLoader
        import amf.shapes.internal.validation.plugin.JsonSchemaBasedSpecValidationPlugin

        class {c['camel']}ValidationPlugin extends JsonSchemaBasedSpecValidationPlugin {{

          override protected val schemaShape: Shape = {c['camel']}SchemaLoader.schema

          override protected def profile: ProfileName = ProfileNames.{c['profile_constant']}

        }}

        object {c['camel']}ValidationPlugin {{
          def apply(): {c['camel']}ValidationPlugin = new {c['camel']}ValidationPlugin()
        }}
    """)


def gen_scala_configuration(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.client.scala

        import amf.aml.client.scala.model.document.{{Dialect, DialectInstance}}
        import amf.aml.internal.registries.AMLRegistry
        import amf.core.client.scala.adoption.IdAdopterProvider
        import amf.core.client.scala.config._
        import amf.core.client.scala.errorhandling.ErrorHandlerProvider
        import amf.core.client.scala.execution.ExecutionEnvironment
        import amf.core.client.scala.model.domain.AnnotationGraphLoader
        import amf.core.client.scala.parse.AMFParsePlugin
        import amf.core.client.scala.resource.ResourceLoader
        import amf.core.client.scala.transform.TransformationPipeline
        import amf.core.client.scala.vocabulary.NamespaceAliases
        import amf.core.internal.metamodel.ModelDefaultBuilder
        import amf.core.internal.plugins.AMFPlugin
        import amf.core.internal.plugins.parse.DomainParsingFallback
        import amf.core.internal.registries.AMFRegistry
        import amf.core.internal.resource.AMFResolvers
        import amf.core.internal.validation.EffectiveValidations
        import amf.core.internal.validation.core.ValidationProfile
        import amf.{c['pkg_segment']}.internal.plugins.parse.{c['camel']}ParsePlugin
        import amf.{c['pkg_segment']}.internal.plugins.render.{c['camel']}RenderPlugin
        import amf.{c['pkg_segment']}.internal.plugins.validation.{c['camel']}ValidationPlugin
        import amf.shapes.client.scala.JsonSchemaBasedSpecConfiguration
        import amf.shapes.internal.plugins.parser.JsonSchemaBasedSpecGraphParsePlugin
        import amf.shapes.internal.plugins.render.JsonSchemaBasedSpecGraphRenderPlugin
        import amf.shapes.internal.transformation.{{
          JsonSchemaBasedSpecCachePipeline,
          JsonSchemaBasedSpecEditingPipeline,
          JsonSchemaBasedSpecTransformationPipeline
        }}

        import scala.concurrent.{{ExecutionContext, Future}}

        class {c['camel']}Configuration private[amf] (
            override private[amf] val resolvers: AMFResolvers,
            override private[amf] val errorHandlerProvider: ErrorHandlerProvider,
            override private[amf] val registry: AMLRegistry,
            override private[amf] val listeners: Set[AMFEventListener],
            override private[amf] val options: AMFOptions,
            override private[amf] val idAdopterProvider: IdAdopterProvider
        ) extends JsonSchemaBasedSpecConfiguration(
                resolvers,
                errorHandlerProvider,
                registry,
                listeners,
                options,
                idAdopterProvider
            ) {{

          private implicit val ec: ExecutionContext = this.getExecutionContext

          override protected[amf] def copy(
              resolvers: AMFResolvers = resolvers,
              errorHandlerProvider: ErrorHandlerProvider = errorHandlerProvider,
              registry: AMFRegistry = registry,
              listeners: Set[AMFEventListener] = listeners,
              options: AMFOptions = options,
              idAdopterProvider: IdAdopterProvider = idAdopterProvider
          ): {c['camel']}Configuration =
            new {c['camel']}Configuration(
                resolvers,
                errorHandlerProvider,
                registry.asInstanceOf[AMLRegistry],
                listeners,
                options,
                idAdopterProvider
            )

          override def baseUnitClient(): {c['camel']}BaseUnitClient = new {c['camel']}BaseUnitClient(this)

          override def withParsingOptions(parsingOptions: ParsingOptions): {c['camel']}Configuration =
            super._withParsingOptions(parsingOptions)

          override def withRenderOptions(renderOptions: RenderOptions): {c['camel']}Configuration =
            super._withRenderOptions(renderOptions)

          override def withResourceLoader(rl: ResourceLoader): {c['camel']}Configuration =
            super._withResourceLoader(rl)

          override def withResourceLoaders(rl: List[ResourceLoader]): {c['camel']}Configuration =
            super._withResourceLoaders(rl)

          override def withUnitCache(cache: UnitCache): {c['camel']}Configuration =
            super._withUnitCache(cache)

          override def withFallback(plugin: DomainParsingFallback): {c['camel']}Configuration = super._withFallback(plugin)

          override def withRootParsePlugin(amfParsePlugin: AMFParsePlugin): {c['camel']}Configuration =
            super._withRootParsePlugin(amfParsePlugin)

          override def withPlugin(amfPlugin: AMFPlugin[_]): {c['camel']}Configuration =
            super._withPlugin(amfPlugin)

          override def withReferenceParsePlugin(plugin: AMFParsePlugin): {c['camel']}Configuration =
            super._withReferenceParsePlugin(plugin)

          override def withRootParsePlugins(amfParsePlugin: List[AMFParsePlugin]): {c['camel']}Configuration =
            super._withRootParsePlugins(amfParsePlugin)

          override def withReferenceParsePlugins(amfPlugin: List[AMFParsePlugin]): {c['camel']}Configuration =
            super._withReferenceParsePlugins(amfPlugin)

          override def withPlugins(plugins: List[AMFPlugin[_]]): {c['camel']}Configuration =
            super._withPlugins(plugins)

          private[amf] override def withValidationProfile(profile: ValidationProfile): {c['camel']}Configuration =
            super._withValidationProfile(profile)

          private[amf] override def withValidationProfile(
              profile: ValidationProfile,
              effective: EffectiveValidations
          ): {c['camel']}Configuration =
            super._withValidationProfile(profile, effective)

          override def withTransformationPipeline(pipeline: TransformationPipeline): {c['camel']}Configuration =
            super._withTransformationPipeline(pipeline)

          override private[amf] def withTransformationPipelines(pipelines: List[TransformationPipeline]): {c['camel']}Configuration =
            super._withTransformationPipelines(pipelines)

          override def withErrorHandlerProvider(provider: ErrorHandlerProvider): {c['camel']}Configuration =
            super._withErrorHandlerProvider(provider)

          override def withEventListener(listener: AMFEventListener): {c['camel']}Configuration = super._withEventListener(listener)

          private[amf] override def withEntities(entities: Map[String, ModelDefaultBuilder]): {c['camel']}Configuration =
            super._withEntities(entities)

          override def withAliases(aliases: NamespaceAliases): {c['camel']}Configuration =
            super._withAliases(aliases)

          private[amf] override def withExtensions(dialect: Dialect): {c['camel']}Configuration = {{
            super.withExtensions(dialect).asInstanceOf[{c['camel']}Configuration]
          }}

          private[amf] override def withAnnotations(annotations: Map[String, AnnotationGraphLoader]): {c['camel']}Configuration =
            super._withAnnotations(annotations)

          override def withExecutionEnvironment(executionEnv: ExecutionEnvironment): {c['camel']}Configuration =
            super._withExecutionEnvironment(executionEnv)

          override def withDialect(dialect: Dialect): {c['camel']}Configuration =
            super.withDialect(dialect).asInstanceOf[{c['camel']}Configuration]

          override def withDialect(url: String): Future[{c['camel']}Configuration] =
            super.withDialect(url).map(_.asInstanceOf[{c['camel']}Configuration])(getExecutionContext)

          override def forInstance(url: String): Future[{c['camel']}Configuration] =
            super.forInstance(url).map(_.asInstanceOf[{c['camel']}Configuration])(getExecutionContext)

          override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): {c['camel']}Configuration =
            super._withIdAdopterProvider(idAdopterProvider)
        }}

        object {c['camel']}Configuration {{

          def {c['camel']}(): {c['camel']}Configuration =
            predefined()
              .withPlugins(
                  List(
                      {c['camel']}ParsePlugin,
                      {c['camel']}RenderPlugin,
                      {c['camel']}ValidationPlugin(),
                      JsonSchemaBasedSpecGraphRenderPlugin,
                      JsonSchemaBasedSpecGraphParsePlugin
                  )
              )
              .withTransformationPipelines(
                  List(
                      JsonSchemaBasedSpecTransformationPipeline(),
                      JsonSchemaBasedSpecEditingPipeline(),
                      JsonSchemaBasedSpecCachePipeline()
                  )
              )

          private def predefined(): {c['camel']}Configuration = {{
            val baseConfig = JsonSchemaBasedSpecConfiguration.base()
            new {c['camel']}Configuration(
                baseConfig.resolvers,
                baseConfig.errorHandlerProvider,
                baseConfig.registry,
                baseConfig.listeners,
                baseConfig.options,
                baseConfig.idAdopterProvider
            )
          }}
        }}
    """)


def gen_scala_base_unit_client(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.client.scala

        import amf.core.client.common.validation.{{ProfileName, ProfileNames}}
        import amf.core.client.scala.model.domain.Shape
        import amf.core.client.scala.parse.AMFParser
        import amf.{c['pkg_segment']}.internal.plugins.parse.schema.{c['camel']}SchemaLoader
        import amf.shapes.client.scala.JsonSchemaBasedSpecBaseUnitClient

        /** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
          * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
          */
        class {c['camel']}BaseUnitClient private[amf] (override protected val configuration: {c['camel']}Configuration)
            extends JsonSchemaBasedSpecBaseUnitClient(configuration) {{

          override protected def schemaShape: Shape = {c['camel']}SchemaLoader.schema

          override protected def profile: ProfileName = ProfileNames.{c['profile_constant']}
        }}
    """)


def gen_platform_configuration(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.client.platform

        import amf.aml.client.platform.model.document.{{Dialect, DialectInstance}}
        import amf.aml.client.platform.{{AMLBaseUnitClient, AMLConfigurationState}}
        import amf.aml.internal.convert.VocabulariesClientConverter.{{ClientFuture, ClientList}}
        import amf.core.client.platform.adoption.IdAdopterProvider
        import amf.core.client.platform.config.{{AMFEventListener, ParsingOptions, RenderOptions}}
        import amf.core.client.platform.errorhandling.ErrorHandlerProvider
        import amf.core.client.platform.execution.BaseExecutionEnvironment
        import amf.core.client.platform.reference.UnitCache
        import amf.core.client.platform.resource.ResourceLoader
        import amf.core.client.platform.transform.TransformationPipeline
        import amf.core.client.platform.validation.payload.AMFShapePayloadValidationPlugin
        import amf.core.internal.convert.ClientErrorHandlerConverter._
        import amf.core.internal.convert.PayloadValidationPluginConverter.PayloadValidationPluginMatcher
        import amf.core.internal.convert.TransformationPipelineConverter._
        import amf.{c['pkg_segment']}.client.scala.{{
          {c['camel']}BaseUnitClient => Internal{c['camel']}BaseUnitClient,
          {c['camel']}Configuration => Internal{c['camel']}Configuration
        }}
        import amf.{c['pkg_segment']}.internal.convert.{c['camel']}ClientConverters._
        import amf.shapes.client.platform.ShapesElementClient
        import amf.shapes.client.scala.{{ShapesConfiguration => InternalShapesConfiguration}}

        import scala.scalajs.js.annotation.{{JSExportAll, JSExportTopLevel}}

        @JSExportAll
        class {c['camel']}Configuration private[amf] (private[amf] override val _internal: Internal{c['camel']}Configuration)
            extends Base{c['camel']}Configuration(_internal) {{

          override def baseUnitClient(): AMLBaseUnitClient = new {c['camel']}BaseUnitClient(new Internal{c['camel']}BaseUnitClient(_internal))

          override def elementClient(): ShapesElementClient = new ShapesElementClient(
            _internal.asInstanceOf[InternalShapesConfiguration]
          )

          def configurationState(): AMLConfigurationState = new AMLConfigurationState(_internal.configurationState())

          override def withParsingOptions(parsingOptions: ParsingOptions): {c['camel']}Configuration =
            _internal.withParsingOptions(parsingOptions)

          override def withRenderOptions(renderOptions: RenderOptions): {c['camel']}Configuration =
            _internal.withRenderOptions(renderOptions)

          override def withErrorHandlerProvider(provider: ErrorHandlerProvider): {c['camel']}Configuration =
            _internal.withErrorHandlerProvider(() => provider.errorHandler())

          override def withResourceLoader(rl: ResourceLoader): {c['camel']}Configuration =
            _internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl))

          override def withResourceLoaders(rl: ClientList[ResourceLoader]): {c['camel']}Configuration =
            _internal.withResourceLoaders(rl.asInternal.toList)

          override def withUnitCache(cache: UnitCache): {c['camel']}Configuration =
            _internal.withUnitCache(UnitCacheMatcher.asInternal(cache))

          override def withTransformationPipeline(pipeline: TransformationPipeline): {c['camel']}Configuration =
            _internal.withTransformationPipeline(pipeline)

          override def withEventListener(listener: AMFEventListener): {c['camel']}Configuration =
            _internal.withEventListener(listener)

          override def withDialect(dialect: Dialect): {c['camel']}Configuration = _internal.withDialect(dialect)

          def withDialect(url: String): ClientFuture[{c['camel']}Configuration] = _internal.withDialect(url).asClient

          override def withExecutionEnvironment(executionEnv: BaseExecutionEnvironment): {c['camel']}Configuration =
            _internal.withExecutionEnvironment(executionEnv._internal)

          def forInstance(url: String): ClientFuture[{c['camel']}Configuration] = _internal.forInstance(url).asClient

          override def withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): {c['camel']}Configuration =
            _internal.withPlugin(PayloadValidationPluginMatcher.asInternal(plugin))

          override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): {c['camel']}Configuration =
            _internal.withIdAdopterProvider(idAdopterProvider)
        }}

        @JSExportAll
        @JSExportTopLevel("{c['camel']}Configuration")
        object {c['camel']}Configuration {{

          def {c['camel']}(): {c['camel']}Configuration = new {c['camel']}Configuration(Internal{c['camel']}Configuration.{c['camel']}())

        }}
    """)


def gen_platform_base_configuration(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.client.platform

        import amf.aml.client.platform.model.document.Dialect
        import amf.aml.internal.convert.VocabulariesClientConverter.{{ClientList, _}}
        import amf.core.client.platform.config.{{AMFEventListener, ParsingOptions, RenderOptions}}
        import amf.core.client.platform.errorhandling.ErrorHandlerProvider
        import amf.core.client.platform.reference.UnitCache
        import amf.core.client.platform.resource.ResourceLoader
        import amf.core.client.platform.transform.TransformationPipeline
        import amf.core.internal.convert.ClientErrorHandlerConverter._
        import amf.core.internal.convert.TransformationPipelineConverter._
        import amf.{c['pkg_segment']}.client.scala.{{{c['camel']}Configuration => Internal{c['camel']}Configuration}}
        import amf.shapes.client.platform.config.BaseJsonSchemaBasedSpecConfiguration

        import scala.concurrent.ExecutionContext
        import scala.scalajs.js.annotation.JSExportAll

        @JSExportAll
        class Base{c['camel']}Configuration private[amf](private[amf] override val _internal: Internal{c['camel']}Configuration)
            extends BaseJsonSchemaBasedSpecConfiguration(_internal) {{

          override protected implicit val ec: ExecutionContext = _internal.getExecutionContext

          override def withParsingOptions(parsingOptions: ParsingOptions): Base{c['camel']}Configuration =
            new Base{c['camel']}Configuration(_internal.withParsingOptions(parsingOptions))

          override def withRenderOptions(renderOptions: RenderOptions): Base{c['camel']}Configuration =
            new Base{c['camel']}Configuration(_internal.withRenderOptions(renderOptions))

          override def withErrorHandlerProvider(provider: ErrorHandlerProvider): Base{c['camel']}Configuration =
            new Base{c['camel']}Configuration(_internal.withErrorHandlerProvider(() => provider.errorHandler()))

          override def withResourceLoader(rl: ResourceLoader): Base{c['camel']}Configuration =
            new Base{c['camel']}Configuration(_internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl)))

          override def withResourceLoaders(rl: ClientList[ResourceLoader]): Base{c['camel']}Configuration =
            new Base{c['camel']}Configuration(_internal.withResourceLoaders(rl.asInternal.toList))

          override def withUnitCache(cache: UnitCache): Base{c['camel']}Configuration =
            new Base{c['camel']}Configuration(_internal.withUnitCache(UnitCacheMatcher.asInternal(cache)))

          override def withTransformationPipeline(pipeline: TransformationPipeline): Base{c['camel']}Configuration =
            new Base{c['camel']}Configuration(_internal.withTransformationPipeline(pipeline))

          override def withEventListener(listener: AMFEventListener): Base{c['camel']}Configuration =
            new Base{c['camel']}Configuration(_internal.withEventListener(listener))

          override def withDialect(dialect: Dialect): Base{c['camel']}Configuration =
            new Base{c['camel']}Configuration(_internal.withDialect(dialect))
        }}
    """)


def gen_platform_base_unit_client(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.client.platform

        import amf.{c['pkg_segment']}.client.scala.{{{c['camel']}BaseUnitClient => Internal{c['camel']}BaseUnitClient}}
        import amf.shapes.client.platform.config.JsonSchemaBasedSpecBaseUnitClient

        import scala.scalajs.js.annotation.JSExportAll

        @JSExportAll
        class {c['camel']}BaseUnitClient private[amf] (private val _internal: Internal{c['camel']}BaseUnitClient)
            extends JsonSchemaBasedSpecBaseUnitClient(_internal)
    """)


def gen_base_converter(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.internal.convert

        import amf.core.internal.convert.BidirectionalMatcher
        import amf.{c['pkg_segment']}.client.platform.{{{c['camel']}Configuration => Client{c['camel']}Configuration}}
        import amf.{c['pkg_segment']}.client.scala.{c['camel']}Configuration
        import amf.shapes.internal.convert.ShapesBaseConverter

        trait {c['camel']}BaseConverter
            extends ShapesBaseConverter
            with {c['camel']}ConfigurationConverter

        trait {c['camel']}ConfigurationConverter {{
          implicit object {c['camel']}ConfigurationMatcher
              extends BidirectionalMatcher[{c['camel']}Configuration, Client{c['camel']}Configuration] {{
            override def asClient(from: {c['camel']}Configuration): Client{c['camel']}Configuration = new Client{c['camel']}Configuration(from)

            override def asInternal(from: Client{c['camel']}Configuration): {c['camel']}Configuration = from._internal
          }}
        }}
    """)


def gen_client_converters(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.internal.convert

        import amf.core.internal.convert.CoreClientConverters

        object {c['camel']}ClientConverters extends {c['camel']}BaseConverter with {c['camel']}BaseClientConverter {{
          // Overriding to match type
          override type ClientOption[E] = CoreClientConverters.ClientOption[E]
          override type ClientList[E]   = CoreClientConverters.ClientList[E]
          override type ClientFuture[T] = CoreClientConverters.ClientFuture[T]
          override type ClientLoader    = CoreClientConverters.ClientLoader
          override type ClientReference = CoreClientConverters.ClientReference
        }}
    """)


def gen_base_client_converter(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf.{c['pkg_segment']}.internal.convert

        import amf.shapes.internal.convert.ShapesBaseClientConverter

        trait {c['camel']}BaseClientConverter extends {c['camel']}BaseConverter with ShapesBaseClientConverter
    """)


def gen_test_cycle_and_config(c: dict) -> str:
    valid_name = os.path.basename(c["valid_instance_path"])
    invalid_name = os.path.basename(c["invalid_instance_path"])
    golden_name = os.path.splitext(valid_name)[0] + ".jsonld"
    return textwrap.dedent(f"""\
        package amf

        import amf.{c['pkg_segment']}.client.scala.{c['camel']}Configuration
        import amf.{c['pkg_segment']}.internal.plugins.parse.schema.{c['camel']}SchemaLoader
        import amf.core.internal.remote.Spec
        import amf.shapes.test._

        class {c['camel']}CycleTest extends JsonSchemaBasedSpecCycleTestBase {{
          override def testConfig: JsonSchemaBasedSpecTestConfig = {c['camel']}TestConfig.config
        }}

        object {c['camel']}TestConfig {{
          val config: JsonSchemaBasedSpecTestConfig = JsonSchemaBasedSpecTestConfig(
            specName     = "{c['camel']}",
            basePath     = "{c['module_dir']}/shared/src/test/resources/instances/",
            configuration = {c['camel']}Configuration.{c['camel']}(),
            schemaLoader = {c['camel']}SchemaLoader,
            spec         = Spec.{c['spec_constant']},
            validInstances = Seq("valid/{valid_name}"),
            invalidInstances = Seq(InvalidInstance("invalid/{invalid_name}", Some(1))),
            cycleInstances = Seq(CycleInstance("valid/{valid_name}", "valid/{golden_name}"))
          )
        }}
    """)


def gen_test_schema_loader(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf

        import amf.shapes.test.JsonSchemaBasedSpecSchemaLoaderTestBase

        class {c['camel']}SchemaLoaderTest extends JsonSchemaBasedSpecSchemaLoaderTestBase {{
          override def testConfig = {c['camel']}TestConfig.config
        }}
    """)


def gen_test_validation(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf

        import amf.shapes.test.JsonSchemaBasedSpecValidationTestBase

        class {c['camel']}ValidationTest extends JsonSchemaBasedSpecValidationTestBase {{
          override def testConfig = {c['camel']}TestConfig.config
        }}
    """)


def gen_test_entry(c: dict) -> str:
    if c["has_id_entry"]:
        return gen_test_entry_with_id(c)
    else:
        return gen_test_entry_no_id(c)


def gen_test_entry_no_id(c: dict) -> str:
    is_yaml = c["valid_ext"] in (".yaml", ".yml")
    if is_yaml:
        parser_import = "org.yaml.parser.YamlParser"
        parser_class = "YamlParser"
        mime = 'Mimes.`application/yaml`'
    else:
        parser_import = "org.yaml.parser.JsonParser"
        parser_class = "JsonParser"
        mime = 'Mimes.`application/json`'

    return textwrap.dedent(f"""\
        package amf

        import amf.{c['pkg_segment']}.internal.plugins.parse.entry.{c['camel']}IdEntry
        import amf.core.client.scala.parse.document.{{SyamlParsedDocument, UnspecifiedReference}}
        import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
        import amf.core.internal.parser.Root
        import amf.core.internal.remote.Mimes
        import amf.shapes.internal.plugins.parser.entry.DefaultIdVersion
        import org.mulesoft.common.io.Fs
        import org.scalatest.matchers.should.Matchers
        import org.yaml.model.YDocument
        import {parser_import}

        class {c['camel']}ProtocolEntryTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {{

          private val basePath: String = "{c['module_dir']}/shared/src/test/resources/instances/entry/"

          test("{c['camel']} with string protocolVersion") {{
            val maybeVersion = {c['camel']}IdEntry.apply(getRoot(basePath + "none.json"))
            maybeVersion.nonEmpty shouldBe true
            maybeVersion.get shouldBe DefaultIdVersion
          }}

          private def getRoot(path: String): Root =
            Root(SyamlParsedDocument(getYDocument(path)), "", {mime}, Nil, UnspecifiedReference, "")

          private def getYDocument(path: String): YDocument = {{
            val content = Fs.syncFile(path).read()
            {parser_class}(content).documents().head
          }}
        }}
    """)


def gen_test_entry_with_id(c: dict) -> str:
    is_yaml = c["valid_ext"] in (".yaml", ".yml")
    if is_yaml:
        parser_import = "org.yaml.parser.YamlParser"
        parser_class = "YamlParser"
        mime = 'Mimes.`application/yaml`'
    else:
        parser_import = "org.yaml.parser.JsonParser"
        parser_class = "JsonParser"
        mime = 'Mimes.`application/json`'

    return textwrap.dedent(f"""\
        package amf

        import amf.{c['pkg_segment']}.internal.plugins.parse.entry.{c['camel']}IdEntry
        import amf.core.client.scala.parse.document.{{SyamlParsedDocument, UnspecifiedReference}}
        import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
        import amf.core.internal.parser.Root
        import amf.core.internal.remote.Mimes
        import org.mulesoft.common.io.Fs
        import org.scalatest.matchers.should.Matchers
        import org.yaml.model.YDocument
        import {parser_import}

        class {c['camel']}EntryTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {{

          private val basePath: String = "{c['module_dir']}/shared/src/test/resources/instances/entry/"

          test("{c['spec_id']} with valid entry") {{
            val maybeVersion = {c['camel']}IdEntry.apply(getRoot(basePath + "valid{c['valid_ext']}"))
            maybeVersion.nonEmpty shouldBe true
          }}

          test("{c['spec_id']} without entry") {{
            val maybeVersion = {c['camel']}IdEntry.apply(getRoot(basePath + "none{c['valid_ext']}"))
            maybeVersion.nonEmpty shouldBe false
          }}

          private def getRoot(path: String): Root =
            Root(SyamlParsedDocument(getYDocument(path)), "", {mime}, Nil, UnspecifiedReference, "")

          private def getYDocument(path: String): YDocument = {{
            val content = Fs.syncFile(path).read()
            {parser_class}(content).documents().head
          }}
        }}
    """)


def gen_test_source_spec(c: dict) -> str:
    return textwrap.dedent(f"""\
        package amf

        import amf.shapes.test.JsonSchemaBasedSpecSourceSpecTestBase

        class {c['camel']}SourceSpecTest extends JsonSchemaBasedSpecSourceSpecTestBase {{
          override def testConfig = {c['camel']}TestConfig.config
        }}
    """)


# ---------------------------------------------------------------------------
# Module creation
# ---------------------------------------------------------------------------

def create_module(c: dict):
    mod = os.path.join(AMF_ROOT, c["module_dir"])
    pkg = c["pkg_segment"]
    camel = c["camel"]

    print(f"\nCreating module: {c['module_dir']}")

    # shared/src/main/scala
    base_main = os.path.join(mod, "shared", "src", "main", "scala", "amf", pkg)

    # Schema
    write_file(os.path.join(base_main, "internal", "plugins", "parse", "schema", f"{camel}Schema.scala"),
               gen_schema_scala(c))
    write_file(os.path.join(base_main, "internal", "plugins", "parse", "schema", f"{camel}SchemaLoader.scala"),
               gen_schema_loader_scala(c))

    # Entry
    if c["has_id_entry"]:
        write_file(os.path.join(base_main, "internal", "plugins", "parse", "entry", f"{camel}Entry.scala"),
                   gen_entry_version_scala(c))
        write_file(os.path.join(base_main, "internal", "plugins", "parse", "entry", f"{camel}IdEntry.scala"),
                   gen_id_entry_scala(c))
    else:
        write_file(os.path.join(base_main, "internal", "plugins", "parse", "entry", f"{camel}IdEntry.scala"),
                   gen_entry_no_entry_scala(c))

    # Plugins
    write_file(os.path.join(base_main, "internal", "plugins", "parse", f"{camel}ParsePlugin.scala"),
               gen_parse_plugin_scala(c))
    write_file(os.path.join(base_main, "internal", "plugins", "render", f"{camel}RenderPlugin.scala"),
               gen_render_plugin_scala(c))
    write_file(os.path.join(base_main, "internal", "plugins", "validation", f"{camel}ValidationPlugin.scala"),
               gen_validation_plugin_scala(c))

    # Client scala
    write_file(os.path.join(base_main, "client", "scala", f"{camel}Configuration.scala"),
               gen_scala_configuration(c))
    write_file(os.path.join(base_main, "client", "scala", f"{camel}BaseUnitClient.scala"),
               gen_scala_base_unit_client(c))

    # Client platform
    write_file(os.path.join(base_main, "client", "platform", f"{camel}Configuration.scala"),
               gen_platform_configuration(c))
    write_file(os.path.join(base_main, "client", "platform", f"Base{camel}Configuration.scala"),
               gen_platform_base_configuration(c))
    write_file(os.path.join(base_main, "client", "platform", f"{camel}BaseUnitClient.scala"),
               gen_platform_base_unit_client(c))

    # Converters (shared)
    write_file(os.path.join(base_main, "internal", "convert", f"{camel}BaseConverter.scala"),
               gen_base_converter(c))
    write_file(os.path.join(base_main, "internal", "convert", f"{camel}ClientConverters.scala"),
               gen_client_converters(c))

    # Converters (js + jvm)
    for platform_dir in ["js", "jvm"]:
        write_file(os.path.join(mod, platform_dir, "src", "main", "scala", "amf", pkg,
                                "internal", "convert", f"{camel}BaseClientConverter.scala"),
                   gen_base_client_converter(c))

    # Resources - schema file placeholder
    resources_dir = os.path.join(mod, "shared", "src", "main", "resources")
    os.makedirs(resources_dir, exist_ok=True)
    schema_dest = os.path.join(resources_dir, c["schema_file"])
    # Will be populated by the schema bundler; create placeholder
    write_file(schema_dest, '{"$schema": "http://json-schema.org/draft-07/schema#", "type": "object"}\n')

    # Test files
    base_test = os.path.join(mod, "shared", "src", "test", "scala", "amf")

    # CycleTest + TestConfig (central config object used by all other tests)
    write_file(os.path.join(base_test, f"{camel}CycleTest.scala"),
               gen_test_cycle_and_config(c))
    write_file(os.path.join(base_test, f"{camel}SchemaLoaderTest.scala"),
               gen_test_schema_loader(c))
    write_file(os.path.join(base_test, f"{camel}ValidationTest.scala"),
               gen_test_validation(c))
    write_file(os.path.join(base_test, f"{camel}SourceSpecTest.scala"),
               gen_test_source_spec(c))

    if c["has_id_entry"]:
        write_file(os.path.join(base_test, f"{camel}EntryTest.scala"),
                   gen_test_entry_with_id(c))
    else:
        write_file(os.path.join(base_test, f"{camel}ProtocolEntryTest.scala"),
                   gen_test_entry_no_id(c))

    # Test resources
    test_res = os.path.join(mod, "shared", "src", "test", "resources", "instances")

    # Copy valid instance
    valid_name = os.path.basename(c["valid_instance_path"])
    valid_dest = os.path.join(test_res, "valid", valid_name)
    if os.path.exists(c["valid_instance_path"]):
        copy_file(c["valid_instance_path"], valid_dest)
    else:
        os.makedirs(os.path.dirname(valid_dest), exist_ok=True)
        print(f"  Warning: valid instance not found at {c['valid_instance_path']}, skipping copy")

    # Copy invalid instance
    invalid_name = os.path.basename(c["invalid_instance_path"])
    invalid_dest = os.path.join(test_res, "invalid", invalid_name)
    if os.path.exists(c["invalid_instance_path"]):
        copy_file(c["invalid_instance_path"], invalid_dest)
    else:
        os.makedirs(os.path.dirname(invalid_dest), exist_ok=True)
        print(f"  Warning: invalid instance not found at {c['invalid_instance_path']}, skipping copy")

    # Golden .jsonld for cycle test — placeholder, will be generated by running the CycleTest once
    golden_name = os.path.splitext(valid_name)[0] + ".jsonld"
    golden_dest = os.path.join(test_res, "valid", golden_name)
    os.makedirs(os.path.dirname(golden_dest), exist_ok=True)
    print(f"  Note:    Golden file {os.path.relpath(golden_dest, AMF_ROOT)} must be generated by running CycleTest")

    # Entry test resource
    entry_dir = os.path.join(test_res, "entry")
    os.makedirs(entry_dir, exist_ok=True)
    if not c["has_id_entry"]:
        # Copy valid instance as "none" entry test resource
        if os.path.exists(c["valid_instance_path"]):
            copy_file(c["valid_instance_path"], os.path.join(entry_dir, f"none{c['valid_ext']}"))
    else:
        # For ID entry, copy valid instance as "valid" and create a "none" without the ID key
        if os.path.exists(c["valid_instance_path"]):
            copy_file(c["valid_instance_path"], os.path.join(entry_dir, f"valid{c['valid_ext']}"))
        # Create a minimal "none" entry test file (empty object)
        none_ext = c["valid_ext"]
        if none_ext in (".yaml", ".yml"):
            write_file(os.path.join(entry_dir, f"none{none_ext}"), "key: value\n")
        else:
            write_file(os.path.join(entry_dir, f"none{none_ext}"), '{"key": "value"}\n')


# ---------------------------------------------------------------------------
# build.sbt modification
# ---------------------------------------------------------------------------

def update_build_sbt(c: dict):
    build_sbt_path = os.path.join(AMF_ROOT, "build.sbt")
    print(f"\nUpdating build.sbt")

    with open(build_sbt_path, "r") as f:
        content = f.read()

    sbt_var = c["sbt_lazy_val"]
    sbt_name = c["sbt_name"]
    mod_dir = c["module_dir"]
    schema = c["schema_file"]
    pkg = c["pkg_segment"]
    camel = c["camel"]
    auto_mod = c["auto_module_name"]
    spec_id = c["spec_id"]
    upper = spec_id.upper().replace(" ", "-")

    # Build the new module block
    module_block = textwrap.dedent(f"""\

    /** ********************************************** AMF-{upper} *********************************************
      */

    lazy val {sbt_var} = crossProject(JSPlatform, JVMPlatform)
      .settings(
        Seq(
          name := "{sbt_name}"
        )
      )
      .in(file("./{mod_dir}"))
      .settings(
        commonSettings ++ Seq(
          Compile / sourceGenerators += Def.task {{
            SourceGenerators.generateEmbeddedFileSource(
              inputFile     = (ThisBuild / baseDirectory).value / "{mod_dir}" / "shared" / "src" / "main" / "resources" / "{schema}",
              outputBaseDir = (Compile / sourceManaged).value,
              packageName   = "amf.{pkg}.internal.spec",
              objectName    = "{camel}SchemaContent"
            )
          }}.taskValue
        )
      )
      .dependsOn(shapes % "compile->compile;test->test")
      .jvmSettings(
        libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
        Compile / packageDoc / artifactPath := baseDirectory.value / "target" / "artifact" / "{sbt_name}-javadoc.jar",
        Compile / packageBin / mappings += file("amf-apicontract.versions") -> "amf-apicontract.versions"
      )
      .jsSettings(
        scalaJSLinkerConfig ~= {{ _.withModuleKind(ModuleKind.CommonJSModule) }},
        Compile / fullOptJS / artifactPath := baseDirectory.value / "target" / "artifact" / "{sbt_name}.js",
        npmDependencies ++= npmDeps
      )
      .settings(AutomaticModuleName.settings("{auto_mod}"))

    lazy val {sbt_var}JVM =
      {sbt_var}.jvm
        .in(file("./{mod_dir}/jvm"))
        .disablePlugins(SonarPlugin)

    lazy val {sbt_var}JS =
      {sbt_var}.js
        .in(file("./{mod_dir}/js"))
        .disablePlugins(SonarPlugin, ScoverageSbtPlugin)
    """)

    # Insert before the CLI section
    cli_marker = '/** ********************************************** AMF CLI'
    cli_pos = content.find(cli_marker)
    if cli_pos == -1:
        print("  Error: Could not find CLI section marker in build.sbt")
        return

    content = content[:cli_pos] + module_block + "\n" + content[cli_pos:]

    # Update cli dependsOn to include the new module
    # Pattern: .dependsOn(grpc, graphql, mcp, agentNetwork, ...)
    cli_depends_pattern = re.compile(r'(lazy val cli = crossProject.*?\.dependsOn\()([^)]+)(\))', re.DOTALL)
    match = cli_depends_pattern.search(content)
    if match:
        deps = match.group(2).strip()
        if sbt_var not in deps:
            new_deps = deps + ", " + sbt_var
            content = content[:match.start(2)] + new_deps + content[match.end(2):]
            print(f"  Added {sbt_var} to cli dependsOn")
    else:
        print("  Warning: Could not find cli dependsOn to update")

    # Update adhoc-cli dependsOn
    adhoc_pattern = f".dependsOn({sbt_var}JVM)"
    if adhoc_pattern not in content:
        # Find the last .dependsOn(xxxJVM) before .disablePlugins in adhoc-cli
        adhoc_disable = content.find('.disablePlugins(SonarPlugin, NpmOpsPlugin, ScoverageSbtPlugin)')
        if adhoc_disable != -1:
            # Find the last .dependsOn line before it
            last_dep_end = content.rfind("JVM)\n", 0, adhoc_disable)
            if last_dep_end != -1:
                insert_pos = last_dep_end + len("JVM)\n")
                content = content[:insert_pos] + f"  .dependsOn({sbt_var}JVM)\n" + content[insert_pos:]
                print(f"  Added {sbt_var}JVM to adhoc-cli dependsOn")

    with open(build_sbt_path, "w") as f:
        f.write(content)

    print("  Updated: build.sbt")


# ---------------------------------------------------------------------------
# schemas.yaml modification
# ---------------------------------------------------------------------------

def update_schemas_yaml(c: dict):
    print(f"\nUpdating schemas.yaml")

    with open(SCHEMAS_YAML_PATH, "r") as f:
        content = f.read()

    name_lower = c["spec_name"].lower()
    slug = c["apb_slug"]

    # Check if already present
    if f'name: "{name_lower}"' in content:
        print(f"  Skipped: '{name_lower}' already present in schemas.yaml")
        return

    new_entry = (
        f'  - name: "{name_lower}"\n'
        f'    amf-module: "{c["module_dir"]}"\n'
        f'    schema:\n'
        f'      spec: "{c["spec_schema_rel"]}"\n'
        f'      amf: "shared/src/main/resources/{c["schema_file"]}"\n'
        f'    instances:\n'
        f'      spec:\n'
        f'        valid: "{c["spec_valid_dir_rel"]}"\n'
        f'        invalid: "{c["spec_invalid_dir_rel"]}"\n'
        f'      amf:\n'
        f'        valid: "shared/src/test/resources/instances/valid"\n'
        f'        invalid: "shared/src/test/resources/instances/invalid"\n'
        f'      apb:\n'
        f'        valid:\n'
        f'          - "apb/shared/src/test/resources/spec/local/{slug}"\n'
        f'          - "apb/shared/src/test/resources/api-project/local/main-{slug}"\n'
        f'        invalid: "apb/shared/src/test/resources/spec/local/{slug}-invalid"\n'
    )

    content = content.rstrip() + "\n" + new_entry

    with open(SCHEMAS_YAML_PATH, "w") as f:
        f.write(content)

    print("  Updated: scripts/json-based-specs/update-schema/schemas.yaml")


# ---------------------------------------------------------------------------
# amf-core modifications
# ---------------------------------------------------------------------------

def update_amf_core_spec(c: dict):
    spec_path = os.path.join(AMF_CORE_ROOT, "shared", "src", "main", "scala", "amf",
                             "core", "internal", "remote", "Spec.scala")

    if not os.path.exists(spec_path):
        print(f"\n  Warning: amf-core Spec.scala not found at {spec_path}")
        print(f"  You need to manually add the spec to amf-core")
        return

    print(f"\nUpdating amf-core Spec.scala")

    with open(spec_path, "r") as f:
        content = f.read()

    spec_obj = c["spec_object"]
    spec_const = c["spec_constant"]
    spec_id = c["spec_id"]
    media_type_scala = c["media_type_scala"]

    # 1. Add case object at the end (before last empty line or EOF)
    case_object_block = textwrap.dedent(f"""\

    private[amf] case object {spec_obj} extends Spec {{
      override val id: String        = "{spec_id}"
      override val mediaType: String = {media_type_scala}
    }}
    """)

    # Find the last case object definition and insert after it
    last_case_obj = content.rfind("private[amf] case object")
    if last_case_obj != -1:
        # Find the end of that block (closing brace + newline)
        block_end = content.find("\n}", last_case_obj)
        if block_end != -1:
            insert_pos = block_end + 2  # after \n}
            content = content[:insert_pos] + case_object_block + content[insert_pos:]

    # 2. Add to unapply match
    unapply_marker = "case _                    => None"
    if unapply_marker in content:
        new_unapply_case = f"      case {spec_obj}.id        => Some({spec_obj})\n      "
        content = content.replace(unapply_marker, new_unapply_case + unapply_marker)

    # 3. Add @JSExport val
    # Find the last @JSExport val line in the Spec companion object
    last_jsexport = None
    for m in re.finditer(r'  @JSExport val \w+:\s+Spec\s+=\s+\w+\n', content):
        last_jsexport = m

    if last_jsexport:
        insert_pos = last_jsexport.end()
        jsexport_line = f"  @JSExport val {spec_const}: Spec{' ' * max(1, 20 - len(spec_const))}= {spec_obj}\n"
        content = content[:insert_pos] + jsexport_line + content[insert_pos:]

    with open(spec_path, "w") as f:
        f.write(content)

    print("  Updated: amf-core Spec.scala")


def update_amf_core_profile_names(c: dict):
    profile_path = os.path.join(AMF_CORE_ROOT, "shared", "src", "main", "scala", "amf",
                                "core", "client", "common", "validation", "ProfileNames.scala")

    if not os.path.exists(profile_path):
        print(f"\n  Warning: amf-core ProfileNames.scala not found at {profile_path}")
        print(f"  You need to manually add the profile to amf-core")
        return

    print(f"\nUpdating amf-core ProfileNames.scala")

    with open(profile_path, "r") as f:
        content = f.read()

    spec_obj = c["spec_object"]
    profile_obj = c["profile_object"]
    profile_const = c["profile_constant"]

    # 1. Add val in ProfileNames object
    # Find the last val XXX: ProfileName line
    last_profile_val = None
    for m in re.finditer(r'  val \w+:\s+ProfileName\s+=\s+\w+\n', content):
        last_profile_val = m

    if last_profile_val:
        insert_pos = last_profile_val.end()
        padding = max(1, 28 - len(profile_const))
        new_val = f"  val {profile_const}: ProfileName{' ' * padding}= {profile_obj}\n"
        content = content[:insert_pos] + new_val + content[insert_pos:]

    # 2. Add to specProfiles Seq
    # Find the closing ) of the specProfiles Seq
    spec_profiles_pattern = re.compile(r'(lazy val specProfiles.*?Seq\(.*?)(    \))', re.DOTALL)
    match = spec_profiles_pattern.search(content)
    if match:
        # Insert before the closing )
        insert_pos = match.start(2)
        content = content[:insert_pos] + f"      {profile_obj},\n" + content[insert_pos:]

    # 3. Add Profile object definition
    last_profile_obj = None
    for m in re.finditer(r'object \w+Profile extends ProfileName\([^)]+\) \{[^}]+\}\n', content):
        last_profile_obj = m

    if last_profile_obj:
        insert_pos = last_profile_obj.end()
        profile_def = textwrap.dedent(f"""\

        object {profile_obj} extends ProfileName({spec_obj}.id, AMFStyle) {{
          override def isOas(): Boolean  = false
          override def isRaml(): Boolean = false
        }}
        """)
        content = content[:insert_pos] + profile_def + content[insert_pos:]

    # 4. Add to unapply
    unapply_marker_pn = "case _                          => None"
    if unapply_marker_pn in content:
        new_case = f"      case {profile_obj}.p{' ' * max(1, 26 - len(profile_obj))}=> Some({profile_obj})\n      "
        content = content.replace(unapply_marker_pn, new_case + unapply_marker_pn)

    # 5. Add to apply
    apply_marker_pn = '    case custom               => new ProfileName(custom)'
    if apply_marker_pn in content:
        new_apply_case = f"    case {spec_obj}.id{' ' * max(1, 20 - len(spec_obj))}=> {profile_obj}\n"
        content = content.replace(apply_marker_pn, new_apply_case + apply_marker_pn)

    with open(profile_path, "w") as f:
        f.write(content)

    print("  Updated: amf-core ProfileNames.scala")


# ---------------------------------------------------------------------------
# APB modifications
# ---------------------------------------------------------------------------

def update_apb_classifier(c: dict):
    classifier_path = os.path.join(APB_ROOT, "apb-project", "shared", "src", "main", "scala",
                                   "org", "mulesoft", "apb", "project", "internal", "common", "Classifier.scala")

    if not os.path.exists(classifier_path):
        print(f"\n  Warning: APB Classifier.scala not found at {classifier_path}")
        print(f"  You need to manually add the classifier to APB")
        return

    print(f"\nUpdating APB Classifier.scala")

    with open(classifier_path, "r") as f:
        content = f.read()

    spec_constant = c["spec_constant"]
    classifier = c["classifier"]

    # Check if already present
    if f'val {spec_constant}' in content:
        print(f"  Skipped: {spec_constant} already present in Classifier.scala")
        return

    # Insert before POLICY_SCHEMA line
    policy_marker = '  val POLICY_SCHEMA: String'
    if policy_marker in content:
        padding = max(1, 19 - len(spec_constant))
        new_val = f'  val {spec_constant}: String{" " * padding}= "{classifier}"\n'
        content = content.replace(policy_marker, new_val + policy_marker)
    else:
        # Fallback: insert before closing brace
        content = content.rstrip().rstrip("}") + f'  val {spec_constant}: String = "{classifier}"\n}}\n'

    with open(classifier_path, "w") as f:
        f.write(content)

    print("  Updated: APB Classifier.scala")


def update_apb_config_provider(c: dict):
    config_path = os.path.join(APB_ROOT, "apb-project", "shared", "src", "main", "scala",
                               "org", "mulesoft", "apb", "project", "internal", "dependency",
                               "config", "ConfigProvider.scala")

    if not os.path.exists(config_path):
        print(f"\n  Warning: APB ConfigProvider.scala not found at {config_path}")
        print(f"  You need to manually add the config provider entry to APB")
        return

    print(f"\nUpdating APB ConfigProvider.scala")

    with open(config_path, "r") as f:
        content = f.read()

    camel = c["camel"]
    pkg = c["pkg_segment"]
    spec_constant = c["spec_constant"]

    # Check if already present
    if f'Classifier.{spec_constant}' in content:
        print(f"  Skipped: {spec_constant} already present in ConfigProvider.scala")
        return

    # 1. Add import
    import_line = f"import amf.{pkg}.client.scala.{camel}Configuration\n"
    # Insert after the last amf.*.client.scala import
    last_amf_import = None
    for m in re.finditer(r'import amf\.\w+\.client\.scala\.\w+Configuration\n', content):
        last_amf_import = m
    if last_amf_import:
        insert_pos = last_amf_import.end()
        content = content[:insert_pos] + import_line + content[insert_pos:]

    # 2. Add to fromClassifier match
    policy_classifier_marker = '    case Classifier.POLICY_SCHEMA'
    if policy_classifier_marker in content:
        padding = max(1, 19 - len(spec_constant))
        new_case = f'    case Classifier.{spec_constant}{" " * padding}=> ConfigurationAdapter.adapt({camel}Configuration.{camel}())\n'
        content = content.replace(policy_classifier_marker, new_case + policy_classifier_marker)

    # 3. Add to fromSpec match — insert before the case _ => throw
    from_spec_throw = '    case _ =>\n      throw UnrecognizedSpecException('
    if from_spec_throw in content:
        padding = max(1, 21 - len(spec_constant))
        new_spec_case = f'    case Spec.{spec_constant}{" " * padding}=> ConfigurationAdapter.adapt({camel}Configuration.{camel}())\n'
        content = content.replace(from_spec_throw, new_spec_case + from_spec_throw)

    # 4. Update the error message to include the new spec
    # Find the last Spec.XXX.id in the error message string and append the new one
    error_msg_pattern = re.compile(r'(\$\{Spec\.\w+\.id\})"')
    matches = list(error_msg_pattern.finditer(content))
    if matches:
        last_match = matches[-1]
        insert_pos = last_match.end() - 1  # before the closing "
        content = content[:insert_pos] + f', ${{Spec.{spec_constant}.id}}' + content[insert_pos:]

    with open(config_path, "w") as f:
        f.write(content)

    print("  Updated: APB ConfigProvider.scala")


def update_apb_build_sbt(c: dict):
    build_sbt_path = os.path.join(APB_ROOT, "build.sbt")

    if not os.path.exists(build_sbt_path):
        print(f"\n  Warning: APB build.sbt not found at {build_sbt_path}")
        print(f"  You need to manually update APB build.sbt")
        return

    print(f"\nUpdating APB build.sbt")

    with open(build_sbt_path, "r") as f:
        content = f.read()

    sbt_var = c["sbt_lazy_val"]
    sbt_name = c["sbt_name"]
    camel_var = f"amf{c['camel']}"

    # Check if already present
    if f'lazy val {camel_var}JVMRef' in content:
        print(f"  Skipped: {camel_var} refs already present in APB build.sbt")
        return

    # 1. Add lazy val refs/libs block — insert before customValidatorJVMRef
    custom_validator_marker = 'lazy val customValidatorJVMRef ='
    if custom_validator_marker in content:
        ref_block = (
            f'lazy val {camel_var}JVMRef = ProjectRef(workspaceDirectory / "amf", "{sbt_var}JVM")\n'
            f'lazy val {camel_var}JSRef  = ProjectRef(workspaceDirectory / "amf", "{sbt_var}JS")\n'
            f'lazy val {camel_var}LibJVM = "com.github.amlorg" %% "{sbt_name}"      % amfVersion changing () withSources ()\n'
            f'lazy val {camel_var}LibJS  = "com.github.amlorg" %% "{sbt_name}_sjs1" % amfVersion changing () withSources ()\n'
            f'\n'
        )
        content = content.replace(custom_validator_marker, ref_block + custom_validator_marker)

    # 2. Add .sourceDependency to apbProjectJVM — insert before amfGraphQLJVMRef line
    graphql_jvm_dep = '  .sourceDependency(amfGraphQLJVMRef, amfGraphQLLibJVM)'
    if graphql_jvm_dep in content and f'.sourceDependency({camel_var}JVMRef' not in content:
        new_dep = f'  .sourceDependency({camel_var}JVMRef, {camel_var}LibJVM)\n'
        content = content.replace(graphql_jvm_dep, new_dep + graphql_jvm_dep)

    # 3. Add .sourceDependency to apbProjectJS — insert before amfGraphQLJSRef line
    graphql_js_dep = '  .sourceDependency(amfGraphQLJSRef, amfGraphQLLibJS)'
    if graphql_js_dep in content and f'.sourceDependency({camel_var}JSRef' not in content:
        new_dep = f'  .sourceDependency({camel_var}JSRef, {camel_var}LibJS)\n'
        content = content.replace(graphql_js_dep, new_dep + graphql_js_dep)

    with open(build_sbt_path, "w") as f:
        f.write(content)

    print("  Updated: APB build.sbt")


def create_apb_test_resources(c: dict):
    print(f"\nCreating APB test resources")

    slug = c["apb_slug"]
    classifier = c["classifier"]
    spec_id = c["spec_id"]
    camel = c["camel"]

    # --- spec/local/<slug>/exchange.json ---
    spec_dir = os.path.join(APB_ROOT, "apb", "shared", "src", "test", "resources", "spec", "local", slug)
    exchange_content = textwrap.dedent(f"""\
        {{
          "main": "asset.yaml",
          "name": "An {spec_id} asset",
          "classifier": "{classifier}",
          "tags": [],
          "groupId": "xxxxxxxxxxxxxxx",
          "assetId": "test-{slug}",
          "version": "1.0.0",
          "descriptorVersion": "1.0.0",
          "dependencies": []
        }}
    """)
    write_file(os.path.join(spec_dir, "exchange.json"), exchange_content)

    # Copy valid instance as asset.yaml
    if os.path.exists(c["valid_instance_path"]):
        copy_file(c["valid_instance_path"], os.path.join(spec_dir, "asset.yaml"))
    else:
        print(f"  Warning: valid instance not found at {c['valid_instance_path']}, skipping asset.yaml copy")

    # --- spec/local/<slug>-invalid/exchange.json ---
    invalid_dir = os.path.join(APB_ROOT, "apb", "shared", "src", "test", "resources", "spec", "local", f"{slug}-invalid")
    write_file(os.path.join(invalid_dir, "exchange.json"), exchange_content)

    # Copy invalid instance as asset.yaml
    if os.path.exists(c["invalid_instance_path"]):
        copy_file(c["invalid_instance_path"], os.path.join(invalid_dir, "asset.yaml"))
    else:
        print(f"  Warning: invalid instance not found at {c['invalid_instance_path']}, skipping invalid asset.yaml copy")

    # --- api-project/local/main-<slug>/exchange.json ---
    api_project_dir = os.path.join(APB_ROOT, "apb", "shared", "src", "test", "resources", "api-project", "local", f"main-{slug}")
    api_exchange_content = textwrap.dedent(f"""\
        {{
          "main": "asset.yaml",
          "name": "An {spec_id} asset",
          "classifier": "{classifier}",
          "descriptorVersion": "1.0.0",
          "organizationId": "org.mulesoft.apb.tests",
          "groupId": "org.mulesoft.apb.tests",
          "assetId": "{slug}",
          "version": "1.0.0",
          "apiVersion": "v1",
          "projectType": "asset"
        }}
    """)
    write_file(os.path.join(api_project_dir, "exchange.json"), api_exchange_content)

    # Copy valid instance as asset.yaml
    if os.path.exists(c["valid_instance_path"]):
        copy_file(c["valid_instance_path"], os.path.join(api_project_dir, "asset.yaml"))
    else:
        print(f"  Warning: valid instance not found at {c['valid_instance_path']}, skipping api-project asset.yaml copy")


def update_apb_api_project_client_test(c: dict):
    test_path = os.path.join(APB_ROOT, "apb", "shared", "src", "test", "scala",
                             "org", "mulesoft", "apb", "client", "scala", "APIProjectClientTest.scala")

    if not os.path.exists(test_path):
        print(f"\n  Warning: APB APIProjectClientTest.scala not found")
        return

    print(f"\nUpdating APB APIProjectClientTest.scala")

    with open(test_path, "r") as f:
        content = f.read()

    spec_id = c["spec_id"]
    spec_constant = c["spec_constant"]
    slug = c["apb_slug"]

    test_name = f"Project with main asset {spec_id} should conform"
    if test_name in content:
        print(f"  Skipped: test already present")
        return

    test_block = textwrap.dedent(f"""\

      test("{test_name}") {{
        val client = readLoad("main-{slug}")
        for {{
          project <- client.project()
          report  <- client.validate()
        }} yield {{
          project.conforms shouldBe true
          project.project.apiContract().isInstanceOf[JsonLDInstanceDocument] shouldBe true
          project.project.apiContract().sourceSpec.isDefined shouldBe true
          project.project.apiContract().sourceSpec.get shouldBe Spec.{spec_constant}
          report.conforms shouldBe true
        }}
      }}
    """)

    # Insert before the gRPC test as anchor
    grpc_marker = '  test("Project with main asset gRPC should conform")'
    if grpc_marker in content:
        content = content.replace(grpc_marker, test_block + "\n" + grpc_marker)
    else:
        print("  Warning: Could not find gRPC test anchor in APIProjectClientTest.scala")

    with open(test_path, "w") as f:
        f.write(content)

    print("  Updated: APB APIProjectClientTest.scala")


def update_apb_e2e_test(c: dict):
    test_path = os.path.join(APB_ROOT, "apb", "shared", "src", "test", "scala",
                             "org", "mulesoft", "apb", "client", "scala", "E2EAPBContractClientTest.scala")

    if not os.path.exists(test_path):
        print(f"\n  Warning: APB E2EAPBContractClientTest.scala not found")
        return

    print(f"\nUpdating APB E2EAPBContractClientTest.scala")

    with open(test_path, "r") as f:
        content = f.read()

    spec_id = c["spec_id"]
    spec_constant = c["spec_constant"]
    slug = c["apb_slug"]

    test_name = f"Client should be able to build an {spec_id} asset"
    if test_name in content:
        print(f"  Skipped: test already present")
        return

    test_block = textwrap.dedent(f"""\

      test("{test_name}") {{
        val basePath = "file://apb/shared/src/test/resources/spec"
        for {{
          client <- readLoad(basePath, "{slug}")
          result <- client.build()
        }} yield {{
          result.conforms shouldBe true
          result.baseUnit.processingData.transformed.value() shouldBe true
          result.baseUnit.processingData.sourceSpec.value() shouldBe Spec.{spec_constant}.id
          result.baseUnit shouldBe a[JsonLDInstanceDocument]
        }}
      }}
    """)

    # Insert before the gRPC test as anchor
    grpc_marker = '  test("Client should be able to build an gRPC asset")'
    if grpc_marker in content:
        content = content.replace(grpc_marker, test_block + "\n" + grpc_marker)
    else:
        print("  Warning: Could not find gRPC test anchor in E2EAPBContractClientTest.scala")

    with open(test_path, "w") as f:
        f.write(content)

    print("  Updated: APB E2EAPBContractClientTest.scala")


def update_apb_for_spec_test(c: dict):
    test_path = os.path.join(APB_ROOT, "apb", "shared", "src", "test", "scala",
                             "org", "mulesoft", "apb", "client", "scala", "ForSpecAPBContractClientTest.scala")

    if not os.path.exists(test_path):
        print(f"\n  Warning: APB ForSpecAPBContractClientTest.scala not found")
        return

    print(f"\nUpdating APB ForSpecAPBContractClientTest.scala")

    with open(test_path, "r") as f:
        content = f.read()

    spec_id = c["spec_id"]
    spec_constant = c["spec_constant"]
    slug = c["apb_slug"]

    test_name = f"Client with {spec_id}"
    if test_name in content:
        print(f"  Skipped: test already present")
        return

    test_block = textwrap.dedent(f"""\

      test("{test_name}") {{
        val localPath = "{slug}"
        for {{
          client     <- readLoad(localPath)
          result     <- client.build()
          serialized <- client.serialize()
          diffed     <- diff(goldenPath(projectPath(basePath, localPath), client.mainFile), serialized)
        }} yield {{
          result.conforms shouldBe true
          result.baseUnit.processingData.transformed.value() shouldBe true
          result.baseUnit.processingData.sourceSpec.value() shouldBe Spec.{spec_constant}.id
          result.baseUnit shouldBe a[JsonLDInstanceDocument]
          diffed
        }}
      }}

      test("Client with invalid {spec_id} should not conform") {{
        val localPath = "{slug}-invalid"
        for {{
          client <- readLoad(localPath)
          result <- client.build()
          report <- client.validate()
        }} yield {{
          result.conforms shouldBe true
          report.conforms shouldBe false
          report.results.size shouldBe 1
        }}
      }}
    """)

    # Insert before the gRPC test as anchor
    grpc_marker = '  test("Client with gRPC")'
    if grpc_marker in content:
        content = content.replace(grpc_marker, test_block + "\n" + grpc_marker)
    else:
        print("  Warning: Could not find gRPC test anchor in ForSpecAPBContractClientTest.scala")

    with open(test_path, "w") as f:
        f.write(content)

    print("  Updated: APB ForSpecAPBContractClientTest.scala")


def update_apb(c: dict):
    if not os.path.exists(APB_ROOT):
        print(f"\n  Warning: APB project not found at {APB_ROOT}")
        print(f"  Skipping APB modifications")
        return

    update_apb_classifier(c)
    update_apb_config_provider(c)
    update_apb_build_sbt(c)
    create_apb_test_resources(c)
    update_apb_api_project_client_test(c)
    update_apb_e2e_test(c)
    update_apb_for_spec_test(c)


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    config = collect_inputs()

    print("\n" + "=" * 60)
    print("  Summary")
    print("=" * 60)
    print(f"  Spec name:        {config['spec_name']}")
    print(f"  Module dir:       {config['module_dir']}")
    print(f"  Package:          amf.{config['pkg_segment']}")
    print(f"  CamelCase:        {config['camel']}")
    print(f"  Spec object:      {config['spec_object']}")
    print(f"  Profile:          {config['profile_object']}")
    print(f"  Schema file:      {config['schema_file']}")
    print(f"  Spec schema:      {config['spec_schema_rel']} (relative to specLocalPath)")
    print(f"  Media type:       {config['media_type']}")
    print(f"  Has ID entry:     {config['has_id_entry']}")
    if config["has_id_entry"]:
        print(f"  ID entry key:     {config['id_entry_key']}")
    print(f"  Valid inst. dir:  {config['spec_valid_dir_rel']} (relative to specLocalPath)")
    print(f"  Invalid inst. dir: {config['spec_invalid_dir_rel']} (relative to specLocalPath)")
    print(f"  Classifier:       {config['classifier']}")
    print(f"  APB slug:         {config['apb_slug']}")
    print()

    if not prompt_yes_no("Proceed with generation?"):
        print("Aborted.")
        sys.exit(0)

    # Check if module already exists
    mod_path = os.path.join(AMF_ROOT, config["module_dir"])
    if os.path.exists(mod_path):
        if not prompt_yes_no(f"Module {config['module_dir']} already exists. Overwrite?", default=False):
            print("Aborted.")
            sys.exit(0)

    create_module(config)
    update_build_sbt(config)
    update_schemas_yaml(config)
    update_amf_core_spec(config)
    update_amf_core_profile_names(config)
    update_apb(config)

    print("\n" + "=" * 60)
    print("  Done!")
    print("=" * 60)
    print()
    print("Next steps:")
    print(f"  1. Run the schema bundler to populate the schema file:")
    print(f"     cd {AMF_ROOT}/scripts/json-based-specs/update-schema")
    print(f"     python3 bundle_all_schemas.py")
    print(f"  2. Compile:")
    print(f"     sbt {config['sbt_lazy_val']}JVM/compile")
    print(f"  3. Generate the golden .jsonld file for CycleTest:")
    print(f"     sbt \"{config['sbt_lazy_val']}JVM/testOnly amf.{config['camel']}CycleTest\"")
    print(f"     (will fail first run — copy temp output to valid/asset.jsonld)")
    print(f"  4. Test (amf):")
    print(f"     sbt {config['sbt_lazy_val']}JVM/test")
    print(f"  5. Review generated files (e.g. invalidInstances error count in TestConfig).")
    print(f"  6. Verify amf-core changes (Spec.scala, ProfileNames.scala).")
    print(f"  7. Compile and test (apb):")
    print(f"     sbt apbProjectJVM/compile")
    print(f"     sbt apbJVM/test")
    print(f"  8. Verify APB changes (Classifier.scala, ConfigProvider.scala, build.sbt).")
    print(f"  9. Generate the golden .jsonld file for ForSpecAPBContractClientTest.")
    print()


if __name__ == "__main__":
    main()
