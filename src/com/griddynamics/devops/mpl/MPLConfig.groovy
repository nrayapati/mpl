//
// Copyright (c) 2019 Grid Dynamics International, Inc. All Rights Reserved
// https://www.griddynamics.com
//
// Classification level: Public
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// $Id: $
// @Project:     MPL
// @Description: Shared Jenkins Modular Pipeline Library
//

package com.griddynamics.devops.mpl

import com.cloudbees.groovy.cps.NonCPS

/**
 * Configuration object to provide the config interface
 *
 * @author Sergei Parshev <sparshev@griddynamics.com>
 */
class MPLConfig implements Serializable {
  /** Configuration Map or List storage */
  private Map config = [:]

  /**
   * Creating new MPLConfig
   *
   * It's impossible to create a new object of MPLConfig using consructor
   * because it's throwing com.cloudbees.groovy.cps.impl.CpsCallableInvocation
   * since constructors can't be CPS-transformed
   *
   * @param config  Map or List with configuration
   *
   * @return  MPLConfig object
   */
  @NonCPS
  static public MPLConfig create(config) {
    new MPLConfig(config)
  }

  /**
   * Clone of the existing MPLConfig
   *
   * @return  MPLConfig object clone
   */
  @NonCPS
  public MPLConfig clone() {
    new MPLConfig(config)
  }

  /**
   * MPLConfig constructor
   *
   * Private to disallow breaking the pipeline
   */
  private MPLConfig(config = [:]) {
    this.config = Helper.cloneValue(config)
  }

  /**
   * Get a value copy of the provided config key path
   *
   * Allowed path tokens:
   *  string - almost any charset not containing dot, key of the Map
   *  number - index of a List, if target is not Map
   *
   * @param key_path - path to the desired config value separated by dot
   * @return value of the key path or null if not found
   */
  public getProperty(String key_path) {
    def key_list = key_path.tokenize(".")

    def value = config
    for( def key in key_list ) {
      if( value in Map )
        value = value[key]
      else if( value in List && key.isInteger() && key.toInteger() >= 0 )
        value = value[key.toInteger()]
      else
        value = null

      if( value == null )
        break
    }
    return Helper.cloneValue(value)
  }

  /**
   * Set value for the provided config key path
   *
   * Allowed path tokens:
   *  string - almost any charset not containing dot, key of the Map
   *  number - index of a List, if target is not Map
   *
   * Not existing key will be created as Map (even if number key is provided)
   * Exception could be thrown if:
   *  - you trying to set sublevel of the existing key which is not the required type:
   *    + List requires positive integer: string keys will cause exception
   *    + Non-container variables (of course)
   *
   * @param key_path - path to the desired config value separated by dot
   */
  void setProperty(String key_path, val) {
    def key_list = key_path.tokenize(".")

    def key, parent
    def value = config
    for( def i = 0; i < key_list.size(); i++ ) {
      key = key_list[i]
      parent = value

      if( parent in List ) {
        if( key.isInteger() && key.toInteger() >= 0 )
          key = key.toInteger()
        else
          throw new MPLException("Invalid config key path '${key_list[i-1] = '<'+key_list[i-1]+'>'; key_list.join('.')}': List key value is not a positive integer")
      }

      if( parent in Map || parent in List )
        value = parent[key]
      else
        throw new MPLException("Invalid config key path '${key_list[i-1] = '<'+key_list[i-1]+'>'; key_list.join('.')}': object type '${parent?.getClass()}' is not suitable to set the nested variable")

      if( value == null )
        value = parent[key] = [:]
    }

    parent[key] = val
  }

  /**
   * Serializer to support unit testing via strings
   *
   * @return  String "MPLConfig(<config dump>)"
   */
  public String toString() {
    return "MPLConfig(${this.config.dump()})"
  }
}
