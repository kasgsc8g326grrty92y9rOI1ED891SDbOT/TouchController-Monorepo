#!/bin/bash -e
export PATH="/usr/bin:/sbin:/bin:$PATH"
self_path="$(realpath "$0")"
wrapper_path="$(dirname "$self_path")"
toolchain_path="$(realpath "$wrapper_path"/..)"

new_args=()
for arg in "$@"; do
    if [ "$arg" = "-lunwind" ]; then
        new_args+=("-l:libunwind.a")
    else
        new_args+=("$arg")
    fi
done

exec "$toolchain_path/bin/%{REAL_TOOL}" "${new_args[@]}"
