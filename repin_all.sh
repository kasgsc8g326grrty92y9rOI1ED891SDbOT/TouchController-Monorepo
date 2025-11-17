#!/bin/bash

set -e

REPIN=1 bazel run @maven//:pin
REPIN=1 bazel run @maven_fabric_1_20_6//:pin
REPIN=1 bazel run @maven_fabric_1_21//:pin
REPIN=1 bazel run @maven_fabric_1_21_1//:pin
REPIN=1 bazel run @maven_fabric_1_21_3//:pin
REPIN=1 bazel run @maven_fabric_1_21_4//:pin
REPIN=1 bazel run @maven_fabric_1_21_5//:pin
REPIN=1 bazel run @maven_fabric_1_21_6//:pin
REPIN=1 bazel run @maven_fabric_1_21_7//:pin
REPIN=1 bazel run @maven_fabric_1_21_8//:pin
REPIN=1 bazel run @maven_fabric_1_21_9//:pin
REPIN=1 bazel run @maven_fabric_1_21_10//:pin
bazel run @modrinth_pin//:pin
