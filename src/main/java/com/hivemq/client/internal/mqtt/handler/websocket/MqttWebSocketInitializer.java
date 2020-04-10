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

package com.hivemq.client.internal.mqtt.handler.websocket;

import com.hivemq.client.internal.mqtt.MqttClientTransportConfigImpl;
import com.hivemq.client.internal.mqtt.MqttWebSocketConfigImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client.internal.mqtt.ioc.ConnectionScope;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttWebSocketInitializer {

    private static final @NotNull String HTTP_CODEC_NAME = "http.codec";
    private static final @NotNull String HTTP_AGGREGATOR_NAME = "http.aggregator";

    private final @NotNull MqttWebSocketCodec mqttWebSocketCodec;

    @Inject
    MqttWebSocketInitializer(final @NotNull MqttWebSocketCodec mqttWebSocketCodec) {
        this.mqttWebSocketCodec = mqttWebSocketCodec;
    }

    public void initChannel(
            final @NotNull Channel channel, final @NotNull MqttWebSocketConfigImpl webSocketConfig,
            final @NotNull MqttClientTransportConfigImpl transportConfig, final @NotNull Consumer<Channel> onSuccess,
            final @NotNull BiConsumer<Channel, Throwable> onError) {

        final URI uri;
        try {
            uri = new URI((transportConfig.getRawSslConfig() == null) ? "ws" : "wss", null,
                    transportConfig.getServerAddress().getHostString(), transportConfig.getServerAddress().getPort(),
                    "/" + webSocketConfig.getServerPath(), webSocketConfig.getQueryString(), null);
        } catch (final URISyntaxException e) {
            onError.accept(channel, e);
            return;
        }

        final WebSocketClientHandshaker handshaker =
                WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13,
                        webSocketConfig.getSubprotocol(), true, null, MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT,
                        true, false);

        channel.pipeline()
                .addLast(HTTP_CODEC_NAME, new HttpClientCodec())
                .addLast(HTTP_AGGREGATOR_NAME, new HttpObjectAggregator(65_535))
                .addLast(MqttWebsocketHandshakeHandler.NAME,
                        new MqttWebsocketHandshakeHandler(handshaker, onSuccess, onError))
                .addLast(MqttWebSocketCodec.NAME, mqttWebSocketCodec);
    }
}
