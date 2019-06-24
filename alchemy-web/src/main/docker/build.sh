#!/bin/sh

################################################################################
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
# limitations under the License.
################################################################################

usage() {
  cat <<HERE
Usage:
  build.sh [--image-name <image> ]
  build.sh --help

  If the --image-name flag is not used the built image name will be 'alchemy'.
HERE
  exit 1
}

while [[ $# -ge 1 ]]
do
key="$1"
  case $key in
    --image-name)
    IMAGE_NAME="$2"
    shift
    ;;
    --help)
    usage
    ;;
    *)
    # unknown option
    ;;
  esac
  shift
done

IMAGE_NAME=${IMAGE_NAME:-alchemy}

# TMPDIR must be contained within the working directory so it is part of the
# Docker context. (i.e. it can't be mktemp'd in /tmp)
TMPDIR=_TMP_

cleanup() {
    rm -rf "${TMPDIR}"
}
trap cleanup EXIT

mkdir -p "${TMPDIR}"

DIST_FILE="../../../target/*.jar"
ALCHEMY_FILE="${TMPDIR}/alchemy.jar"
echo "Using target dist: ${DIST_FILE}"
cp ${DIST_FILE}  ${ALCHEMY_FILE}
echo "Using ALCHEMY_FILE: ${ALCHEMY_FILE}"
docker build --build-arg alchemy_dist="${ALCHEMY_FILE}" -t "${IMAGE_NAME}" .
docker tag alchemy  quay.app.2dfire.com/congbai/alchemy
docker push quay.app.2dfire.com/congbai/alchemy
