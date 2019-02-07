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
 */

package org.mqttbee.mqtt;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.MqttClientSslConfigImplBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.List;
import java.util.Optional;

/**
 * Configuration for a secure transport to use by {@link MqttClient MQTT clients}.
 *
 * @author Christoph Schäbel
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface MqttClientSslConfig {

    long DEFAULT_HANDSHAKE_TIMEOUT_MS = 10_000;

    /**
     * Creates a builder for a secure transport configuration.
     *
     * @return the created builder for a secure transport configuration.
     */
    static @NotNull MqttClientSslConfigBuilder builder() {
        return new MqttClientSslConfigImplBuilder.Default();
    }

    /**
     * @return the optional user defined {@link KeyManagerFactory}.
     */
    @NotNull Optional<KeyManagerFactory> getKeyManagerFactory();

    /**
     * @return the optional user defined {@link TrustManagerFactory}.
     */
    @NotNull Optional<TrustManagerFactory> getTrustManagerFactory();

    /**
     * The optional user defined cipher suites. If absent, the default cipher suites of Netty (network communication
     * framework) will be used.
     *
     * @return the optional user defined cipher suites.
     */
    @NotNull Optional<@Immutable List<@NotNull String>> getCipherSuites();

    /**
     * The optional user defined protocols. If absent, the default protocols of Netty (network communication framework)
     * will be used.
     *
     * @return the optional user defined protocols.
     */
    @NotNull Optional<@Immutable List<@NotNull String>> getProtocols();

    /**
     * @return the handshake timeout in milliseconds.
     */
    long getHandshakeTimeoutMs();
}
