/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.lifecycle;

import com.hivemq.client.internal.mqtt.lifecycle.mqtt3.Mqtt3ClientReconnectorView;
import com.hivemq.client.internal.mqtt.mqtt3.Mqtt3ClientConfigView;
import com.hivemq.client.internal.mqtt.mqtt3.exceptions.Mqtt3ExceptionFactory;
import com.hivemq.client.mqtt.MqttClientConfig;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.lifecycle.MqttClientReconnector;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MqttDisconnectedListenerContext implements MqttClientDisconnectedListener.Context {

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttDisconnectSource source;
    private final @NotNull Throwable cause;
    private final @NotNull MqttClientReconnector reconnector;

    public MqttDisconnectedListenerContext(
            final @NotNull com.hivemq.client.internal.mqtt.MqttClientConfig clientConfig,
            final @NotNull MqttDisconnectSource source, final @NotNull Throwable cause,
            final @NotNull com.hivemq.client.internal.mqtt.lifecycle.MqttClientReconnector reconnector) {

        this.source = source;
        if (clientConfig.getMqttVersion() == MqttVersion.MQTT_3_1_1) {
            this.clientConfig = new Mqtt3ClientConfigView(clientConfig);
            this.cause = Mqtt3ExceptionFactory.map(cause);
            this.reconnector = new Mqtt3ClientReconnectorView(reconnector);
        } else {
            this.clientConfig = clientConfig;
            this.cause = cause;
            this.reconnector = reconnector;
        }
    }

    @Override
    public @NotNull MqttClientConfig getClientConfig() {
        return clientConfig;
    }

    @Override
    public @NotNull MqttDisconnectSource getSource() {
        return source;
    }

    @Override
    public @NotNull Throwable getCause() {
        return cause;
    }

    @Override
    public @NotNull MqttClientReconnector getReconnector() {
        return reconnector;
    }
}
