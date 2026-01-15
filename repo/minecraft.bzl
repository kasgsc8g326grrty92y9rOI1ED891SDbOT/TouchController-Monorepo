"Module extension to download Minecraft artifacts"

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_file")
load("//private:bytes_util.bzl", "hex_sha1_to_sri")
load("//private:maven_coordinate.bzl", "convert_maven_coordinate", "convert_maven_coordinate_to_repo")

total_platforms = [
    "windows",
    "linux",
    "osx",
]
total_arches = [
    "32",
    "64",
]
platform_constraints = {
    "windows": "@platforms//os:windows",
    "linux": "@platforms//os:linux",
    "osx": "@platforms//os:macos",
}

def _version_to_repo_name(version, type):
    return "minecraft_%s_%s" % (version.replace(".", "_"), type)

def _split_hash(hash):
    return "{}/{}".format(hash[0:2], hash)

def _find_constraint(platform):
    constraint = None
    for prefix, constraint in platform_constraints.items():
        if platform.startswith(prefix):
            return constraint
    print("WARNING: No platform constraint found for %s" % platform)
    return None

def _minecraft_library_repo_impl(rctx):
    rctx.download(
        url = rctx.attr.url,
        output = rctx.attr.path,
        integrity = hex_sha1_to_sri(rctx.attr.sha1),
    )

    if rctx.attr.extract:
        build_content = """package(default_visibility = ["//visibility:public"])
load("@//repo/minecraft:extract_lib.bzl", "extract_lib")

extract_lib(
    name = "file",
    jar = "%s",
    excludes = %s,
)
""" % (rctx.attr.path, rctx.attr.extract_exclude)
    else:
        build_content = """package(default_visibility = ["//visibility:public"])

filegroup(
    name = "file",
    srcs = ["%s"],
)
""" % (rctx.attr.path)

    rctx.file("BUILD.bazel", build_content)

_minecraft_library_repo = repository_rule(
    implementation = _minecraft_library_repo_impl,
    attrs = {
        "url": attr.string(mandatory = True),
        "sha1": attr.string(mandatory = True),
        "path": attr.string(mandatory = True),
        "extract": attr.bool(default = False),
        "extract_exclude": attr.string_list(default = []),
    }
)

def _minecraft_repo_impl(rctx):
    version_entries = rctx.attr.version_entries
    version_libraries = rctx.attr.version_libraries
    library_extracts = rctx.attr.library_extracts
    library_platforms = {}

    for library, platforms_list in rctx.attr.library_platforms.items():
        platforms = {}
        for platform in platforms_list:
            (platform, classifier) = platform.split("#")
            platforms[platform] = classifier
        library_platforms[library] = platforms

    for version, entries in version_entries.items():
        build_content = [
            'load("@rules_java//java:defs.bzl", "java_import")',
            'load("@//repo/minecraft:extract_lib.bzl", "extract_manifest")',
            'package(default_visibility = ["//visibility:public"])',
            "",
        ]

        entry_dict = {}
        for version_entry in entries:
            entry, name = version_entry.split("#")
            entry_dict[entry] = name

        if "client" in entry_dict:
            common_libs = set()
            constraint_libs = {}
            extract_common_libs = set()
            constraint_extract_libs = {}

            for library_name in version_libraries.get(version, []):
                platforms = library_platforms.get(library_name, {})
                normalized_name = convert_maven_coordinate(library_name)

                def get_lib_label(classifier):
                    return "@minecraft_%s_%s//:file" % (normalized_name, classifier)

                if "common" in platforms:
                    lib_label = get_lib_label(platforms["common"])
                    common_libs.add(lib_label)
                    if library_name in library_extracts:
                        extract_common_libs.add(lib_label)

                for platform in platforms:
                    if platform == "common":
                        continue
                    lib_label = get_lib_label(platforms[platform])
                    constraint = _find_constraint(platform)
                    if not constraint:
                        continue
                    if not constraint in constraint_libs:
                        constraint_libs[constraint] = set()
                    constraint_libs[constraint].add(lib_label)
                    if library_name in library_extracts:
                        if not constraint in constraint_extract_libs:
                            constraint_extract_libs[constraint] = set()
                        constraint_extract_libs[constraint].add(lib_label)

            if constraint_libs:
                build_content += [
                    "java_import(",
                    '    name = "client_libraries",',
                    "    jars = select({",
                    '        "//conditions:default": [',
                    ",\n".join(['            "%s"' % lib for lib in common_libs]),
                    "        ],",
                    "    }) + select({",
                ]
                for constraint, libs in constraint_libs.items():
                    libs_to_add = libs.difference(common_libs)
                    if libs_to_add:
                        build_content += [
                            '        "%s": [' % constraint,
                            ",\n".join(['            "%s"' % lib for lib in libs_to_add]),
                            "        ],",
                        ]
                build_content += [
                    '        "//conditions:default": [],',
                    "    }),",
                    ")",
                ]
            else:
                build_content += [
                    "java_import(",
                    '    name = "client_libraries",',
                    "    jars = [",
                ]
                if common_libs:
                    build_content.append(",\n".join(['            "%s"' % lib for lib in common_libs]))
                build_content.append("        ])")

            if constraint_extract_libs:
                build_content += [
                    "extract_manifest(",
                    '    name = "client_extract_libraries",',
                    "    deps = select({",
                    '        "//conditions:default": [',
                    ",\n".join(['            "%s"' % lib for lib in extract_common_libs]),
                    "        ],",
                    "    }) + select({",
                ]
                for constraint, libs in constraint_extract_libs.items():
                    libs_to_add = libs.difference(extract_common_libs)
                    if libs_to_add:
                        build_content += [
                            '        "%s": [' % constraint,
                            ",\n".join(['            "%s"' % lib for lib in libs_to_add]),
                            "        ],",
                        ]
                build_content += [
                    '        "//conditions:default": [],',
                    "    }),",
                    ")",
                ]
            elif extract_common_libs:
                build_content += [
                    "extract_manifest(",
                    '    name = "client_extract_libraries",',
                    "    deps = [",
                ]
                if common_libs:
                    build_content.append(",\n".join(['            "%s"' % lib for lib in extract_common_libs]))
                build_content.append("        ])")

        for entry, name in entry_dict.items():
            repo = _version_to_repo_name(version, entry)
            if entry in ["client", "server"]:
                build_content += [
                    "java_import(",
                    '    name = "%s",' % entry,
                    '    jars = ["@%s//file"],' % repo,
                    '    deps = [":client_libraries"],' if entry == "client" else "",
                    ")",
                ]
            else:
                build_content += [
                    "alias(",
                    '    name = "%s",' % entry,
                    '    actual = "@%s//file",' % repo,
                    ")",
                ]

        rctx.file(
            "%s/BUILD.bazel" % version,
            content = "\n".join(build_content),
        )

    return None

minecraft_repo = repository_rule(
    implementation = _minecraft_repo_impl,
    attrs = {
        "version_entries": attr.string_list_dict(),
        "version_libraries": attr.string_list_dict(),
        "library_classifiers": attr.string_list_dict(),
        "library_platforms": attr.string_list_dict(),
        "library_extracts": attr.string_list_dict(),
    },
)

def _minecraft_assets_repo_impl(rctx):
    asset_sha1 = rctx.attr.asset_sha1
    asset_urls = rctx.attr.asset_urls
    version_assets = rctx.attr.version_assets

    build_content = [
        'package(default_visibility = ["//visibility:public"])',
        "",
    ]

    manifest_tokens = []
    for index_id, asset_url in asset_urls.items():
        manifest_tokens.append(rctx.download(
            url = asset_url,
            output = "indexes/%s.json" % index_id,
            integrity = hex_sha1_to_sri(asset_sha1[index_id]),
            block = False,
        ))
    for token in manifest_tokens:
        token.wait()

    object_entries = {}
    for manifest_id in asset_sha1.keys():
        manifest_path = "indexes/%s.json" % manifest_id
        manifest_text = rctx.read(manifest_path)
        asset_manifest = json.decode(manifest_text)
        map_to_resources = asset_manifest.get("map_to_resources", False)
        manifest_paths = {}
        for asset_name, asset_item in asset_manifest["objects"].items():
            asset_hash = asset_item["hash"]
            split_hash = _split_hash(asset_hash)
            asset_path = ("legacy/%s" % asset_name) if map_to_resources else ("objects/%s" % split_hash)
            manifest_paths[asset_hash] = asset_path
            object_entries[asset_path] = struct(
                hash = asset_hash,
                split_hash = split_hash,
            )
        build_content.append("filegroup(")
        build_content.append('    name = "objects_%s",' % manifest_id)
        build_content.append("    srcs = [")
        for asset_hash, asset_path in manifest_paths.items():
            build_content.append('        "%s",' % asset_path)
        build_content.append("    ],")
        build_content.append(")")

    object_tokens = []
    for asset_path, object_entry in object_entries.items():
        object_tokens.append(rctx.download(
            url = "https://resources.download.minecraft.net/%s" % object_entry.split_hash,
            output = asset_path,
            integrity = hex_sha1_to_sri(object_entry.hash),
            block = False,
        ))
    for token in object_tokens:
        token.wait()

    for version_id in version_assets.keys():
        version_manifest = version_assets[version_id]
        rctx.file(
            "versions/%s" % version_id,
            content = version_manifest,
        )
        build_content += [
            "alias(",
            '    name = "indexes_%s",' % version_id,
            '    actual = "indexes/%s.json",' % version_manifest,
            ")",
            "filegroup(",
            '    name = "assets_%s",' % version_id,
            "    srcs = [",
            '        ":indexes_%s",' % version_id,
            '        ":objects_%s",' % version_id,
            '        "versions/%s",' % version_id,
            "    ],",
            ")",
        ]
        if version_id != version_manifest:
            build_content += [
                "alias(",
                '    name = "objects_%s",' % version_id,
                '    actual = ":objects_%s",' % version_manifest,
                ")",
            ]

    build_content += [
        "alias(",
        '    name = "assets",',
        '    actual = ".",',
        ")",
    ]

    rctx.file(
        "BUILD.bazel",
        content = "\n".join(build_content),
    )

    return None

minecraft_assets_repo = repository_rule(
    implementation = _minecraft_assets_repo_impl,
    attrs = {
        "asset_sha1": attr.string_dict(),
        "asset_urls": attr.string_dict(),
        "version_assets": attr.string_dict(),
    },
)

def _download_version_manifest(mctx):
    manifest_url = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    manifest_path = "version_manifest.json"

    mctx.report_progress("Downloading version manifest")
    mctx.download(
        url = manifest_url,
        output = manifest_path,
    )
    manifest = json.decode(mctx.read(manifest_path))

    return manifest

def _append_version_entry(version, version_manifest, entry_type):
    downloads = version_manifest["downloads"].get(entry_type)
    if not downloads:
        fail("Type '%s' not found in version %s's data" % (entry_type, version))

    url = downloads["url"]
    filename = url.split("/")[-1]
    http_file(
        name = _version_to_repo_name(version, entry_type),
        url = url,
        integrity = hex_sha1_to_sri(downloads["sha1"]),
        downloaded_file_path = filename,
    )

def _append_library_entry(name, manifest):
    downloads = manifest["downloads"]
    artifact = downloads.get("artifact")
    manifest_classifiers = downloads.get("classifiers")

    classifiers = {}

    def add_artifact(id, artifact):
        classifiers[id] = struct(
            path = artifact["path"],
            url = artifact["url"],
            sha1 = artifact["sha1"],
        )

    if artifact:
        add_artifact("default", artifact)
    if manifest_classifiers:
        for classifier, artifact in manifest_classifiers.items():
            add_artifact(classifier, artifact)

    natives = manifest.get("natives")
    rules = manifest.get("rules")
    common = True
    allow_platforms = []
    if rules:
        for platform in total_platforms:
            allow = False
            for rule in rules:
                rule_pass = True
                os = rule.get("os")
                if os:
                    common = False
                    if os["name"] != platform:
                        rule_pass = False
                if rule_pass:
                    action = rule["action"]
                    if action == "allow":
                        allow = True
                    elif action == "disallow":
                        allow = False
            if allow:
                allow_platforms.append(platform)

    # platforms -> classifiers
    platforms = {}

    def add_native_platform(platform):
        classifier = natives.get(platform)
        if classifier:
            def add_classifier(classifier, suffix = ""):
                download = manifest_classifiers.get(classifier)
                if download:
                    platforms[platform + suffix] = classifier
                else:
                    print("WARNING: unknown classifier %s for version %s's library %s on platform %s" % (classifier, version, name, platform))

            if "${arch}" in classifier:
                for arch in total_arches:
                    real_classifier = classifier.replace("${arch}", arch)
                    add_classifier(real_classifier, "-" + arch)
            else:
                add_classifier(classifier)

    if "default" in classifiers:
        if common:
            platforms["common"] = "default"
        else:
            for platform in allow_platforms:
                platforms[platform] = "default"

    if natives:
        if rules:
            for platform in allow_platforms:
                add_native_platform(platform)
        else:
            for platform in natives.keys():
                add_native_platform(platform)

    extract = None
    if "extract" in manifest:
        extract_manifest = manifest["extract"]
        extract = struct(
            exclude = extract_manifest.get("exclude", []),
        )

    return struct(
        classifiers = classifiers,
        platforms = platforms,
        extract = extract,
    )

def _minecraft_impl(mctx):
    manifest = _download_version_manifest(mctx)

    # Deduplicate version entries
    version_inputs = {}
    for mod in mctx.modules:
        for version_tag in mod.tags.version:
            version = version_tag.version
            if version in version_inputs:
                entry = version_inputs
                version_inputs[version] = struct(
                    assets = version_inputs[version].assets or version_tag.assets,
                    client = version_inputs[version].client or version_tag.client,
                    server = version_inputs[version].server or version_tag.server,
                    client_mappings = version_inputs[version].client_mappings or version_tag.client_mappings,
                    server_mappings = version_inputs[version].server_mappings or version_tag.server_mappings,
                )
            else:
                version_inputs[version] = struct(
                    assets = version_tag.assets,
                    client = version_tag.client,
                    server = version_tag.server,
                    client_mappings = version_tag.client_mappings,
                    server_mappings = version_tag.server_mappings,
                )

    exclude_library_names = []
    for mod in mctx.modules:
        for exclude_library in mod.tags.exclude_library:
            for name in exclude_library.names:
                exclude_library_names.append(name)

    version_manifests = {}

    for version in version_inputs.keys():
        # Find version metadata
        version_manifest_entry = None
        for entry in manifest["versions"]:
            if entry["id"] == version:
                version_manifest_entry = entry
                break
        if not version_manifest_entry:
            fail("Version %s not found in manifest" % version)

        # Download version JSON
        version_manifest_path = "version_{}.json".format(version)
        mctx.report_progress("Downloading %s manifest" % version)
        version_manifests[version] = struct(
            path = version_manifest_path,
            token = mctx.download(
                url = version_manifest_entry["url"],
                output = version_manifest_path,
                integrity = hex_sha1_to_sri(version_manifest_entry["sha1"]),
                block = False,
            ),
        )

    version_entries = {}
    library_entries = {}
    asset_entries = {}

    for version, version_manifest in version_manifests.items():
        version_input = version_inputs[version]
        version_manifest.token.wait()
        version_manifest = json.decode(mctx.read(version_manifest.path))

        entries = []
        if version_input.client:
            _append_version_entry(version, version_manifest, "client")
            entries.append("client#file")
        if version_input.server:
            _append_version_entry(version, version_manifest, "server")
            entries.append("server#file")
        if version_input.client_mappings:
            _append_version_entry(version, version_manifest, "client_mappings")
            entries.append("client_mappings#file")
        if version_input.server_mappings:
            _append_version_entry(version, version_manifest, "server_mappings")
            entries.append("server_mappings#file")

        libraries = []
        if version_input.client:
            for library in version_manifest["libraries"]:
                name = library["name"]
                if name in exclude_library_names:
                    continue
                if not name in library_entries:
                    library_entries[name] = _append_library_entry(name, library)
                if name in libraries:
                    continue
                libraries.append(name)

        asset_manifest_id = None
        if version_input.assets:
            asset_info = version_manifest["assetIndex"]
            if asset_info == None:
                fail("No assets for version %s" % version)
            asset_id = asset_info["id"]
            asset_manifest_id = asset_id
            if not asset_id in asset_entries:
                asset_entries[asset_id] = struct(
                    sha1 = asset_info["sha1"],
                    url = asset_info["url"],
                )

        version_entries[version] = struct(
            entries = entries,
            libraries = libraries,
            asset_manifest_id = asset_manifest_id,
        )

    for library_name, library in library_entries.items():
        for classifier, artifact in library.classifiers.items():
            name = "%s_%s" % (convert_maven_coordinate_to_repo("minecraft", library_name), classifier)
            if library.extract:
                _minecraft_library_repo(
                    name = name,
                    url = artifact.url,
                    sha1 = artifact.sha1,
                    path = artifact.path,
                    extract = True,
                    extract_exclude = library.extract.exclude,
                )
            else:
                _minecraft_library_repo(
                    name = name,
                    url = artifact.url,
                    sha1 = artifact.sha1,
                    path = artifact.path,
                )

    library_classifiers = {}
    library_platforms = {}
    for name, library in library_entries.items():
        library_classifiers[name] = ["%s#%s" % (classifier, artifact) for classifier, artifact in library.classifiers.items()]
        library_platforms[name] = ["%s#%s" % (platform, classifier) for platform, classifier in library.platforms.items()]

    minecraft_repo(
        name = "minecraft",
        version_entries = {key: entry.entries for key, entry in version_entries.items()},
        version_libraries = {key: entry.libraries for key, entry in version_entries.items()},
        # library -> classifiers
        library_classifiers = library_classifiers,
        # library -> platform#classifier
        library_platforms = library_platforms,
        library_extracts = {key: entry.extract.exclude for key, entry in library_entries.items() if entry.extract},
    )

    minecraft_assets_repo(
        name = "minecraft_assets",
        asset_sha1 = {key: entry.sha1 for key, entry in asset_entries.items()},
        asset_urls = {key: entry.url for key, entry in asset_entries.items()},
        version_assets = {version: entry.asset_manifest_id for version, entry in version_entries.items() if entry.asset_manifest_id},
    )

version = tag_class(
    attrs = {
        "version": attr.string(
            doc = "The Minecraft version to be used",
        ),
        "assets": attr.bool(
            doc = "Download assets",
            default = False,
        ),
        "client": attr.bool(
            doc = "Download client",
            default = False,
        ),
        "server": attr.bool(
            doc = "Download server",
            default = False,
        ),
        "client_mappings": attr.bool(
            doc = "Download client mappings",
            default = False,
        ),
        "server_mappings": attr.bool(
            doc = "Download server mappings",
            default = False,
        ),
    },
)

exclude_library = tag_class(
    attrs = {
        "names": attr.string_list(
            doc = "Names to exclude",
            default = [],
        ),
    },
)

minecraft = module_extension(
    implementation = _minecraft_impl,
    tag_classes = {
        "version": version,
        "exclude_library": exclude_library,
    },
)
