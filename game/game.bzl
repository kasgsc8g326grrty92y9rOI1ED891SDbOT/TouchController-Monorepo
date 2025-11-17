"Macro to declare game versions"

load("@rules_java//java:java_import.bzl", "java_import")
load("//rule:extract_jar.bzl", "extract_jar")
load("//rule:merge_mapping.bzl", "merge_mapping", "merge_mapping_input")
load("//rule:remap_jar.bzl", "remap_jar")

def _game_version_impl(name, visibility, version, client_mappings, client, server, neoforge, intermediary, sodium_intermediary, iris_intermediary):
    intermediary_mapping = name + "_intermediary_mapping"
    intermediary_input = name + "_intermediary_input"
    named_input = name + "_named_input"
    merged_mapping = name + "_merged_mapping"
    client_intermediary = name + "_client_intermediary"
    client_named = name + "_client_named"
    client_neoforge = name + "_client_neoforge"
    server_jar_file = name + "_server_jar_file"
    server_jar = name + "_server_jar"
    server_named = name + "_server_named"
    sodium_named = name + "_sodium_named"
    iris_named = name + "_iris_named"

    extract_jar(
        name = intermediary_mapping,
        entry_path = "mappings/mappings.tiny",
        filename = "_mappings/intermediary.tiny",
        input = intermediary,
    )

    merge_mapping_input(
        name = intermediary_input,
        file = ":" + intermediary_mapping,
        format = "tinyv2",
        source_namespace = "official",
    )

    merge_mapping_input(
        name = named_input,
        file = client_mappings,
        format = "proguard",
        namespace_mappings = {
            "source": "named",
            "target": "official",
        },
        source_namespace = "official",
    )

    merge_mapping(
        name = merged_mapping,
        complete_namespace = {
            "intermediary": "official",
            "named": "intermediary",
        },
        inputs = [
            ":" + intermediary_input,
            ":" + named_input,
        ],
        output = "merged.tiny",
        output_source_namespace = "official",
        visibility = visibility,
    )

    remap_jar(
        name = client_intermediary,
        from_namespace = "official",
        inputs = [client],
        mapping = ":" + merged_mapping,
        to_namespace = "intermediary",
        visibility = visibility,
    )

    remap_jar(
        name = client_named,
        from_namespace = "official",
        inputs = [client],
        mapping = ":" + merged_mapping,
        to_namespace = "named",
        visibility = visibility,
    )

    if neoforge:
        native.alias(
            name = client_neoforge,
            actual = neoforge,
            visibility = visibility,
        )

    extract_jar(
        name = server_jar_file,
        entry_path = "META-INF/versions/%s/server-%s.jar" % (version, version),
        filename = "_minecraft/server.jar",
        input = server,
    )

    java_import(
        name = server_jar,
        jars = [
            ":" + server_jar_file,
        ],
        visibility = visibility,
    )

    remap_jar(
        name = server_named,
        from_namespace = "official",
        inputs = [":" + server_jar],
        mapping = ":" + merged_mapping,
        to_namespace = "named",
        visibility = visibility,
    )

    if sodium_intermediary:
        remap_jar(
            name = sodium_named,
            from_namespace = "intermediary",
            inputs = [sodium_intermediary],
            classpath = [":" + client_intermediary],
            mapping = ":" + merged_mapping,
            to_namespace = "named",
            visibility = visibility,
            mixin = True,
            remove_jar_in_jar = True,
        )

    if sodium_intermediary and iris_intermediary:
        remap_jar(
            name = iris_named,
            from_namespace = "intermediary",
            inputs = [iris_intermediary],
            classpath = [
                ":" + client_intermediary,
                ":" + sodium_named,
            ],
            mapping = ":" + merged_mapping,
            to_namespace = "named",
            visibility = visibility,
            mixin = True,
            remove_jar_in_jar = True,
        )

game_version = macro(
    implementation = _game_version_impl,
    attrs = {
        "version": attr.string(
            mandatory = True,
            doc = "Minecraft version",
            configurable = False,
        ),
        "client_mappings": attr.label(
            mandatory = True,
            allow_single_file = True,
            doc = "Client mappings file",
        ),
        "client": attr.label(
            mandatory = True,
            allow_single_file = True,
            doc = "Client JAR file",
            configurable = False,
        ),
        "server": attr.label(
            mandatory = True,
            allow_single_file = True,
            doc = "Server JAR file",
        ),
        "neoforge": attr.label(
            mandatory = False,
            doc = "NeoForge compiled target",
        ),
        "intermediary": attr.label(
            mandatory = True,
            doc = "Intermediary mappings",
        ),
        "sodium_intermediary": attr.label(
            mandatory = False,
            doc = "Sodium to be remapped from intermediary",
            configurable = False,
        ),
        "iris_intermediary": attr.label(
            mandatory = False,
            doc = "Sodium to be remapped from intermediary",
            configurable = False,
        ),
    },
)
