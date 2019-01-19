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

package org.mqttbee.mqtt.mqtt5.message.auth;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.datatypes.MqttUtf8String;
import org.mqttbee.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt.mqtt5.message.Mqtt5MessageType;

import java.util.Optional;

/**
 * MQTT 5 Auth message. This message is translated from and to a MQTT 5 AUTH packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Auth extends Mqtt5Message, Mqtt5EnhancedAuth {

    /**
     * @return the Reason Code of this Auth message.
     */
    @NotNull Mqtt5AuthReasonCode getReasonCode();

    /**
     * @return the optional reason string of this Auth message.
     */
    @NotNull Optional<MqttUtf8String> getReasonString();

    /**
     * @return the optional user properties of this Auth message.
     */
    @NotNull Mqtt5UserProperties getUserProperties();

    @Override
    default @NotNull Mqtt5MessageType getType() {
        return Mqtt5MessageType.AUTH;
    }
}
