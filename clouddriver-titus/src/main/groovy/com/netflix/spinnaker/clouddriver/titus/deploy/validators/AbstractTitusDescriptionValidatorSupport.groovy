/*
 * Copyright 2015 Netflix, Inc.
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

package com.netflix.spinnaker.clouddriver.titus.deploy.validators

import com.netflix.spinnaker.clouddriver.deploy.DescriptionValidator
import com.netflix.spinnaker.clouddriver.deploy.ValidationErrors
import com.netflix.spinnaker.clouddriver.security.AccountCredentials
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsProvider
import com.netflix.spinnaker.clouddriver.titus.credentials.NetflixTitusCredentials
import com.netflix.spinnaker.clouddriver.titus.deploy.description.AbstractTitusCredentialsDescription

abstract class AbstractTitusDescriptionValidatorSupport<T extends AbstractTitusCredentialsDescription> extends DescriptionValidator<T> {

  private final String descriptionName

  AbstractTitusDescriptionValidatorSupport(String descriptionName) {
    this.descriptionName = descriptionName
  }

  @Override
  void validate(List<T> priorDescriptions, T description, ValidationErrors errors) {
    if (!description.credentials) {
      errors.rejectValue "credentials", "${descriptionName}.credentials.empty"
    } else {
      if (!(description?.credentials instanceof NetflixTitusCredentials)) {
        errors.rejectValue("credentials", "${descriptionName}.credentials.invalid")
      }
    }
  }

  static <T> void validateRegion(T description, String regionName, String errorKey, ValidationErrors errors) {
    validateRegions(description, regionName ? [regionName] : [], errorKey, errors, "region")
  }

  static <T> void validateRegions(T description, Collection<String> regionNames, String errorKey, ValidationErrors errors, String attributeName = "regions") {
    if (!regionNames) {
      errors.rejectValue(attributeName, "${errorKey}.${attributeName}.empty")
    } else {
      def allowedRegions = description.credentials?.regions?.name
      if (allowedRegions && !allowedRegions.containsAll(regionNames)) {
        errors.rejectValue(attributeName, "${errorKey}.${attributeName}.not.configured")
      }
    }
  }

  static <T> void validateAsgName(T description, ValidationErrors errors) {
    def key = description.getClass().simpleName
    if (!description.asgName) {
      errors.rejectValue("asgName", "${key}.asgName.empty")
    }
  }

  static <T> void validateAsgNameAndRegionAndInstanceIds(T description, ValidationErrors errors) {
    def key = description.class.simpleName
    if (description.asgName) {
      validateAsgName(description, errors)
    }

    validateRegion(description, description.region, key, errors)
    if (!description.instanceIds) {
      errors.rejectValue("instanceIds", "${key}.instanceIds.empty")
    } else {
      description.instanceIds.each {
        if (!it) {
          errors.rejectValue("instanceIds", "${key}.instanceId.invalid")
        }
      }
    }
  }

}
