/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.api.mqtt.mqtt5.message.subscribe;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** @author David Katz */
class Mqtt5RetainHandlingTest {

  @Test
  void test_getCode_send() {
    assertEquals(0x00, Mqtt5RetainHandling.SEND.getCode());
    assertEquals(Mqtt5RetainHandling.SEND, Mqtt5RetainHandling.fromCode(0x00));
  }

  @Test
  void test_getCode_sendIfSubscriptionDoesNotExist() {
    assertEquals(0x01, Mqtt5RetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST.getCode());
    assertEquals(
        Mqtt5RetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST,
        Mqtt5RetainHandling.fromCode(0x01));
  }

  @Test
  void test_getCode_doNotSend() {
    assertEquals(0x02, Mqtt5RetainHandling.DO_NOT_SEND.getCode());
    assertEquals(Mqtt5RetainHandling.DO_NOT_SEND, Mqtt5RetainHandling.fromCode(0x02));
  }

  @Test
  void test_invalidCode() {
    assertNull(Mqtt5RetainHandling.fromCode(0xFF));
  }
}
