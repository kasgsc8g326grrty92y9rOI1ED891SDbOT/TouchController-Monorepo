load("//rule/combine:texture.bzl", "TextureLibraryInfo")

def _texture_to_arg(texture):
    return ["--texture", texture.identifier, texture.texture.path, texture.metadata.path]

def _nine_patch_texture_to_arg(texture):
    return ["--ninepatch", texture.identifier, texture.texture.path, texture.metadata.path]

def _vanilla_pack_impl(ctx):
    texture_info = ctx.attr.dep[TextureLibraryInfo]
    output_file = ctx.actions.declare_file(ctx.attr.name + ".zip")

    args = ctx.actions.args()
    args.add(ctx.attr.namespace)
    args.add(output_file.path)
    args.add_all(texture_info.textures, map_each = _texture_to_arg)
    args.add_all(texture_info.ninepatch_textures, map_each = _nine_patch_texture_to_arg)

    ctx.actions.run(
        inputs = texture_info.files,
        outputs = [output_file],
        executable = ctx.executable._generator_bin,
        arguments = [args],
    )

    return [DefaultInfo(files = depset([output_file]))]


vanilla_pack = rule(
    implementation = _vanilla_pack_impl,
    attrs = {
        "dep": attr.label(
            providers = [TextureLibraryInfo],
            mandatory = True,
        ),
        "namespace": attr.string(
            mandatory = True,
        ),
        "_generator_bin": attr.label(
            default = Label("//rule/combine/minecraft/vanilla"),
            cfg = "exec",
            executable = True,
        )
    }
)