#!/bin/bash
#
# Copyright 2023 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
if [[ -z "${GOOGLE_CLOUD_PROJECT}" ]]; then
  echo "GOOGLE_CLOUD_PROJECT environment variable not set"
  exit 1
fi

if [[ -z "${GOOGLE_APPLICATION_CREDENTIALS}" ]]; then
  echo "GOOGLE_APPLICATION_CREDENTIALS environment variable not set"
  exit 1
fi
echo "ENVIRONMENT VARIABLES VERIFIED"

echo "BUILDING SAMPLE APP IMAGE"
gradle clean jibDockerBuild --image "hello-autoinstrument-java-local:latest"


echo "RUNNING SAMPLE APP ON PORT 8080"
docker run \
      --rm \
      -e "GOOGLE_CLOUD_PROJECT=${GOOGLE_CLOUD_PROJECT}" \
      -e "GOOGLE_APPLICATION_CREDENTIALS=${GOOGLE_APPLICATION_CREDENTIALS}" \
      -v "${GOOGLE_APPLICATION_CREDENTIALS}:${GOOGLE_APPLICATION_CREDENTIALS}:ro" \
      -p 8080:8080 \
      "hello-autoinstrument-java-local:latest"
