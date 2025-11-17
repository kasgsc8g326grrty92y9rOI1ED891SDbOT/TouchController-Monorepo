def _llvm_mingw_toolchain_impl(rctx):
    build_file_header = rctx.read(Label("BUILD.llvm_mingw_toolchain.bazel"))
    build_file = build_file_header + "\n".join([
        "\n".join([
            "",
            "llvm_mingw_toolchain(",
            '    name = "%s_toolchain",' % name,
            '    target_cpu = "@platforms//cpu:%s",' % name,
            '    triple = "%s",' % triple,
            '    visibility = ["//visibility:public"],',
            ")",
        ])
        for name, triple in rctx.attr.targets.items()
    ])
    rctx.file(
        "BUILD.bazel",
        content = build_file,
        executable = False,
    )

    wrapper_files = ["ar", "as", "cpp", "gcc", "g++", "gcov", "ld", "nm", "objcopy", "ranlib", "strip"]
    for triple in rctx.attr.targets.values():
        for wrapper_file in wrapper_files:
            real_tool = triple + "-" + wrapper_file
            rctx.template(
                "wrapper/" + real_tool,
                Label("wrapper.sh"),
                substitutions = {
                    "%{REAL_TOOL}": real_tool,
                },
            )

    rctx.download_and_extract(
        url = "https://github.com/mstorsjo/llvm-mingw/releases/download/20251007/llvm-mingw-20251007-msvcrt-ubuntu-22.04-x86_64.tar.xz",
        sha256 = "5ddaa3399fae80b284e91a96e86eaadeb368021209b5d9d482f5a73611ea4d6e",
        strip_prefix = "llvm-mingw-20251007-msvcrt-ubuntu-22.04-x86_64",
        type = "tar.xz",
    )

llvm_mingw_toolchain = repository_rule(
    implementation = _llvm_mingw_toolchain_impl,
    attrs = {
        "targets": attr.string_dict(mandatory = True),
    },
)
