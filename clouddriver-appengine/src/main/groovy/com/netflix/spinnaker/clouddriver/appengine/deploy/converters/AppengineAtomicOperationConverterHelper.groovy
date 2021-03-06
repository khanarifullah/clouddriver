/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.appengine.deploy.converters

import com.fasterxml.jackson.databind.DeserializationFeature
import com.netflix.spinnaker.clouddriver.appengine.deploy.description.AbstractAppengineCredentialsDescription
import com.netflix.spinnaker.clouddriver.appengine.security.AppengineNamedAccountCredentials
import com.netflix.spinnaker.clouddriver.security.AbstractAtomicOperationsCredentialsConverter

class AppengineAtomicOperationConverterHelper {
  static <T extends AbstractAppengineCredentialsDescription> T convertDescription(Map input,
                                   AbstractAtomicOperationsCredentialsConverter<AppengineNamedAccountCredentials> credentialsSupport,
                                   Class<T> targetDescriptionType) {
    input.accountName = input.accountName ?: input.account ?: input.credentials

    if (input.accountName) {
      input.credentials = credentialsSupport.getCredentialsObject(input.accountName as String)
      input.account = input.accountName
    } else {
      throw new RuntimeException("Could not find App Engine account.")
    }

    def credentials = input.remove("credentials")

    def converted = credentialsSupport.objectMapper
      .copy()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .convertValue(input, targetDescriptionType)

    converted.credentials = credentials as AppengineNamedAccountCredentials
    converted
  }
}
