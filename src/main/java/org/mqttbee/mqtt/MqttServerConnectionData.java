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

package org.mqttbee.mqtt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ServerConnectionData;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ServerConnectionData;
import org.mqttbee.mqtt.message.publish.MqttTopicAliasMapping;

/**
 * @author Silvio Giebl
 */
public class MqttServerConnectionData implements Mqtt5ServerConnectionData, Mqtt3ServerConnectionData {

    private final int receiveMaximum;
    private final int maximumPacketSize;
    private final @Nullable MqttTopicAliasMapping topicAliasMapping;
    private final @NotNull MqttQos maximumQos;
    private final boolean retainAvailable;
    private final boolean wildcardSubscriptionAvailable;
    private final boolean sharedSubscriptionAvailable;
    private final boolean subscriptionIdentifiersAvailable;

    public MqttServerConnectionData(
            final int receiveMaximum, final int maximumPacketSize, final int topicAliasMaximum,
            final @NotNull MqttQos maximumQos, final boolean retainAvailable,
            final boolean wildcardSubscriptionAvailable, final boolean sharedSubscriptionAvailable,
            final boolean subscriptionIdentifiersAvailable) {

        this.receiveMaximum = receiveMaximum;
        this.maximumPacketSize = maximumPacketSize;
        this.topicAliasMapping = (topicAliasMaximum == 0) ? null : new MqttTopicAliasMapping(topicAliasMaximum);
        this.maximumQos = maximumQos;
        this.retainAvailable = retainAvailable;
        this.wildcardSubscriptionAvailable = wildcardSubscriptionAvailable;
        this.sharedSubscriptionAvailable = sharedSubscriptionAvailable;
        this.subscriptionIdentifiersAvailable = subscriptionIdentifiersAvailable;
    }

    @Override
    public int getReceiveMaximum() {
        return receiveMaximum;
    }

    @Override
    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    @Override
    public int getTopicAliasMaximum() {
        return (topicAliasMapping == null) ? 0 : topicAliasMapping.getTopicAliasMaximum();
    }

    public @Nullable MqttTopicAliasMapping getTopicAliasMapping() {
        return topicAliasMapping;
    }

    @Override
    public @NotNull MqttQos getMaximumQos() {
        return maximumQos;
    }

    @Override
    public boolean isRetainAvailable() {
        return retainAvailable;
    }

    @Override
    public boolean isWildcardSubscriptionAvailable() {
        return wildcardSubscriptionAvailable;
    }

    @Override
    public boolean isSharedSubscriptionAvailable() {
        return sharedSubscriptionAvailable;
    }

    @Override
    public boolean areSubscriptionIdentifiersAvailable() {
        return subscriptionIdentifiersAvailable;
    }

}
