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

package org.mqttbee.mqtt.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.reactivex.SingleEmitter;
import io.reactivex.exceptions.Exceptions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.codec.encoder.MqttEncoder;
import org.mqttbee.mqtt.handler.websocket.MqttWebSocketClientProtocolHandler;
import org.mqttbee.mqtt.handler.websocket.WebSocketBinaryFrameDecoder;
import org.mqttbee.mqtt.handler.websocket.WebSocketBinaryFrameEncoder;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.handler.auth.MqttAuthHandler;
import org.mqttbee.mqtt.handler.auth.MqttDisconnectOnAuthHandler;
import org.mqttbee.mqtt.handler.connect.MqttConnectHandler;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectHandler;
import org.mqttbee.mqtt.ioc.ChannelComponent;

import java.net.URI;
import java.net.URISyntaxException;

import static org.mqttbee.mqtt.datatypes.MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT;

/**
 * Default channel initializer.
 *
 * @author Silvio Giebl
 */
public class MqttChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final String HTTP_CODEC = "http-codec";
    private static final String HTTP_AGGREGATOR = "http-aggregator";
    private static final String WEBSOCKETS_CLIENT = "websockets-client";
    private static final String WS_BINARYFRAME_CREATOR = "ws-binaryframe-creator";
    private static final String WS_BINARYFRAME_UNPACKER = "ws-binaryframe-unpacker";

    private final MqttConnect connect;
    private final SingleEmitter<Mqtt5ConnAck> connAckEmitter;
    final MqttClientData clientData;

    private ChannelComponent channelComponent;

    MqttChannelInitializer(
            @NotNull final MqttConnect connect, @NotNull final SingleEmitter<Mqtt5ConnAck> connAckEmitter,
            @NotNull final MqttClientData clientData) {

        this.connect = connect;
        this.connAckEmitter = connAckEmitter;
        this.clientData = clientData;
    }

    @Override
    protected void initChannel(final SocketChannel channel) {
        channelComponent = ChannelComponent.create(channel, clientData);
        if (clientData.usesWebSockets()) {
            initMqttOverWebSockets(channel.pipeline());
        } else {
            initMqttHandlers(channel.pipeline());
        }
    }

    void initMqttHandlers(@NotNull final ChannelPipeline pipeline) {
        pipeline.addLast(MqttEncoder.NAME, channelComponent.encoder());

        if (connect.getRawEnhancedAuthProvider() == null) {
            pipeline.addLast(MqttDisconnectOnAuthHandler.NAME, channelComponent.disconnectOnAuthHandler());
        } else {
            pipeline.addLast(MqttAuthHandler.NAME, channelComponent.authHandler());
        }

        pipeline.addLast(MqttConnectHandler.NAME, new MqttConnectHandler(connect, connAckEmitter, clientData));
        pipeline.addLast(MqttDisconnectHandler.NAME, channelComponent.disconnectHandler());

    }

    void initMqttOverWebSockets(@NotNull final ChannelPipeline pipeline) {
        try {
            final URI serverUri = new URI("ws", null,
                    clientData.getServerHost(), clientData.getServerPort(), "/" + clientData.getServerPath(), null, null);
            final MqttWebSocketClientProtocolHandler wsProtocolHandler = new MqttWebSocketClientProtocolHandler(serverUri);

            wsProtocolHandler.onWebSocketHandshakeComplete(() -> {
                pipeline.addLast(WS_BINARYFRAME_CREATOR, new WebSocketBinaryFrameEncoder());
                pipeline.addLast(WS_BINARYFRAME_UNPACKER, new WebSocketBinaryFrameDecoder());
                this.initMqttHandlers(pipeline);
            });

            pipeline.addLast(HTTP_CODEC, new HttpClientCodec());
            pipeline.addLast(HTTP_AGGREGATOR, new HttpObjectAggregator(MAXIMUM_PACKET_SIZE_LIMIT));
            pipeline.addLast(WEBSOCKETS_CLIENT, wsProtocolHandler);

        } catch (URISyntaxException e) {
            Exceptions.propagate(e);
        }
    }

    void init(@NotNull final SocketChannel channel) {
        channelComponent = ChannelComponent.create(channel, clientData);
    }

}
