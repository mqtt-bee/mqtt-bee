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

package org.mqttbee.api.mqtt.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.MqttGlobalPublishFilter;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilderBase;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubscribeBuilder;
import org.mqttbee.mqtt.message.connect.mqtt3.Mqtt3ConnectBuilderImpl;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishBuilderImpl;
import org.mqttbee.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeBuilderImpl;
import org.mqttbee.mqtt.mqtt3.Mqtt3AsyncClientView;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Asynchronous API of a {@link Mqtt3Client} based on futures and callbacks.
 *
 * @author Silvio Giebl
 */
public interface Mqtt3AsyncClient extends Mqtt3Client {

    /**
     * Connects this client with the default Connect message.
     *
     * @return see {@link #connect(Mqtt3Connect)}.
     * @see #connect(Mqtt3Connect)
     */
    default @NotNull CompletableFuture<@NotNull Mqtt3ConnAck> connect() {
        return connect(Mqtt3Connect.DEFAULT);
    }

    /**
     * Connects this client with the given Connect message.
     *
     * @param connect the Connect message sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *         <li>completes normally with the ConnAck message if it does not contain an Error Code (connected
     *         successfully),</li>
     *         <li>completes exceptionally with a {@link org.mqttbee.api.mqtt.mqtt3.exceptions.Mqtt3MessageException
     *         Mqtt3MessageException} wrapping the ConnAck message if it contains an Error Code or</li>
     *         <li>completes exceptionally with a different exception if an error occurred before the Connect message
     *         was sent or before the ConnAck message was received.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt3ConnAck> connect(@NotNull Mqtt3Connect connect);

    /**
     * Fluent counterpart of {@link #connect(Mqtt3Connect)}.
     * <p>
     * Calling {@link Mqtt3ConnectBuilder.Send#send()} on the returned builder has the same effect as calling {@link
     * #connect(Mqtt3Connect)} with the result of {@link Mqtt3ConnectBuilder#build()}.
     *
     * @return the fluent builder for the Connect message.
     * @see #connect(Mqtt3Connect)
     */
    default @NotNull Mqtt3ConnectBuilder.Send<CompletableFuture<Mqtt3ConnAck>> connectWith() {
        return new Mqtt3ConnectBuilderImpl.SendImpl<>(this::connect);
    }

    /**
     * Subscribes this client with the given Subscribe message.
     * <p>
     * See {@link #publishes(MqttGlobalPublishFilter, Consumer)} or {@link #publishes(MqttGlobalPublishFilter, Consumer,
     * Executor)} to consume the incoming Publish messages. Alternatively, call {@link #subscribe(Mqtt3Subscribe,
     * Consumer)} or {@link #subscribe(Mqtt3Subscribe, Consumer, Executor)} to consume the incoming Publish messages
     * matching the subscriptions of the Subscribe message directly.
     *
     * @param subscribe the Subscribe messages sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *         <li>completes normally with the SubAck message if all subscriptions of the Subscribe message were
     *         successful (the SubAck message contains no Error Codes),</li>
     *         <li>completes exceptionally with a {@link org.mqttbee.api.mqtt.mqtt3.exceptions.Mqtt3MessageException
     *         Mqtt3MessageException} wrapping the SubAck message if it contains at least one Error Code or</li>
     *         <li>completes exceptionally with a different exception if an error occurred before the Subscribe message
     *         was sent or before a SubAck message was received.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(@NotNull Mqtt3Subscribe subscribe);

    /**
     * Subscribes this client with the given Subscribe message and consumes the the incoming Publish messages matching
     * the subscriptions of the Subscribe message with a callback.
     *
     * @param subscribe the Subscribe messages sent to the broker.
     * @param callback  the callback for consuming the incoming Publish messages matching the subscriptions of the
     *                  Subscribe message.
     * @return see {@link #subscribe(Mqtt3Subscribe)}.
     * @see #subscribe(Mqtt3Subscribe, Consumer, Executor)
     */
    @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            @NotNull Mqtt3Subscribe subscribe, @NotNull Consumer<@NotNull Mqtt3Publish> callback);

    /**
     * Subscribes this client with the given Subscribe message and consumes the the incoming Publish messages matching
     * the subscriptions of the Subscribe message with a callback.
     * <p>
     * The future is completed and the callback is executed on the given executor.
     *
     * @param subscribe the Subscribe messages sent to the broker.
     * @param callback  the callback for consuming the incoming Publish messages matching the subscriptions of the
     *                  Subscribe message.
     * @param executor  the executor where the future is completed and the callback is executed on.
     * @return see {@link #subscribe(Mqtt3Subscribe)}.
     * @see #subscribe(Mqtt3Subscribe, Consumer)
     */
    @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            @NotNull Mqtt3Subscribe subscribe, @NotNull Consumer<@NotNull Mqtt3Publish> callback,
            @NotNull Executor executor);

    /**
     * Fluent counterpart of {@link #subscribe(Mqtt3Subscribe)}, {@link #subscribe(Mqtt3Subscribe, Consumer)} and {@link
     * #subscribe(Mqtt3Subscribe, Consumer, Executor)}.
     * <p>
     * Calling {@link Mqtt3SubscribeAndCallbackBuilder.Complete#send()} on the returned builder has the same effect as
     * calling one of the following methods:
     * <ul>
     * <li>{@link #subscribe(Mqtt3Subscribe)} if no callback has been supplied to the builder</li>
     * <li>{@link #subscribe(Mqtt3Subscribe, Consumer)} if only a callback has been supplied to the builder</li>
     * <li>{@link #subscribe(Mqtt3Subscribe, Consumer, Executor)} if a callback and an executor have been supplied to
     * the builder</li>
     * </ul>
     *
     * @return the fluent builder for the Subscribe message.
     * @see #subscribe(Mqtt3Subscribe)
     * @see #subscribe(Mqtt3Subscribe, Consumer)
     * @see #subscribe(Mqtt3Subscribe, Consumer, Executor)
     */
    default @NotNull Mqtt3SubscribeAndCallbackBuilder.Start<CompletableFuture<Mqtt3SubAck>> subscribeWith() {
        return new Mqtt3AsyncClientView.Mqtt3SubscribeAndCallbackBuilderImpl<>(subscribeAndCallback -> {
            final Mqtt3Subscribe subscribe = subscribeAndCallback.getSubscribe();
            final Consumer<Mqtt3Publish> callback = subscribeAndCallback.getCallback();
            if (callback == null) {
                return subscribe(subscribe);
            }
            final Executor executor = subscribeAndCallback.getExecutor();
            if (executor == null) {
                return subscribe(subscribe, callback);
            }
            return subscribe(subscribe, callback, executor);
        });
    }

    /**
     * Globally consumes all incoming Publish messages matching the given filter.
     *
     * @param filter   the filter with which all incoming Publish messages are filtered.
     * @param callback the callback for all incoming Publish messages matching the given filter.
     * @see #publishes(MqttGlobalPublishFilter, Consumer, Executor)
     */
    void publishes(@NotNull MqttGlobalPublishFilter filter, @NotNull Consumer<@NotNull Mqtt3Publish> callback);

    /**
     * Globally consumes all incoming Publish messages matching the given filter.
     *
     * @param filter   the filter with which all incoming Publish messages are filtered.
     * @param callback the callback for all incoming Publish messages matching the given filter.
     * @param executor the executor where the callback is executed on.
     * @see #publishes(MqttGlobalPublishFilter, Consumer)
     */
    void publishes(
            @NotNull MqttGlobalPublishFilter filter, @NotNull Consumer<@NotNull Mqtt3Publish> callback,
            @NotNull Executor executor);

    /**
     * Unsubscribes this client with the given Unsubscribe message.
     *
     * @param unsubscribe the Unsubscribe message sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *         <li>completes normally when the corresponding UnsubAck message was received or</li>
     *         <li>completes exceptionally if an error occurred before the Unsubscribe message was sent or before a
     *         UnsubAck message was received.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<Void> unsubscribe(@NotNull Mqtt3Unsubscribe unsubscribe);

    /**
     * Fluent counterpart of {@link #unsubscribe(Mqtt3Unsubscribe)}.
     * <p>
     * Calling {@link Mqtt3UnsubscribeBuilder.Send.Complete#send()} on the returned builder has the same effect as
     * calling {@link #unsubscribe(Mqtt3Unsubscribe)} with the result of {@link Mqtt3UnsubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #unsubscribe(Mqtt3Unsubscribe)
     */
    default @NotNull Mqtt3UnsubscribeBuilder.Send.Start<CompletableFuture<Void>> unsubscribeWith() {
        return new Mqtt3UnsubscribeBuilderImpl.SendImpl<>(this::unsubscribe);
    }

    /**
     * Publishes the given Publish message.
     *
     * @param publish the Publish message sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *         <li>completes normally with the same Publish message (for context) if the Publish message was
     *         successfully published or</li>
     *         <li>completes exceptionally if an error occurred before the Publish
     *         message was sent or before an acknowledgement message was received.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt3Publish> publish(@NotNull Mqtt3Publish publish);

    /**
     * Fluent counterpart of {@link #publish(Mqtt3Publish)}.
     * <p>
     * Calling {@link Mqtt3PublishBuilder.Send.Complete#send()} on the returned builder has the same effect as calling
     * {@link #publish(Mqtt3Publish)} with the result of {@link Mqtt3PublishBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #publish(Mqtt3Publish)
     */
    default @NotNull Mqtt3PublishBuilder.Send<CompletableFuture<Mqtt3Publish>> publishWith() {
        return new Mqtt3PublishBuilderImpl.SendImpl<>(this::publish);
    }

    /**
     * Disconnects this client.
     *
     * @return the {@link CompletableFuture} which
     *         <ul>
     *         <li>completes when the client was successfully disconnected or</li>
     *         <li>errors if not disconnected gracefully.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<Void> disconnect();

    @Override
    default @NotNull Mqtt3AsyncClient toAsync() {
        return this;
    }

    // @formatter:off
    @DoNotImplement
    interface Mqtt3SubscribeAndCallbackBuilder<P> extends
            Mqtt3SubscribeBuilderBase<
                Mqtt3SubscribeAndCallbackBuilder<P>,
                Mqtt3SubscribeAndCallbackBuilder.Complete<P>> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete<P> extends
                Mqtt3SubscribeAndCallbackBuilder<P>,
                CallbackBuilder<P>,
                Mqtt3SubscribeBuilderBase.Complete<
                    Mqtt3SubscribeAndCallbackBuilder<P>,
                    Mqtt3SubscribeAndCallbackBuilder.Complete<P>> {
        // @formatter:on
        }

        // @formatter:off
        @DoNotImplement
        interface First<P> extends
                Mqtt3SubscribeBuilderBase.First<
                    Mqtt3SubscribeAndCallbackBuilder.First<P>,
                    Mqtt3SubscribeAndCallbackBuilder.First.Complete<P>> {
        // @formatter:on

            // @formatter:off
            @DoNotImplement
            interface Complete<P> extends
                    Mqtt3SubscribeAndCallbackBuilder.First<P>,
                    Mqtt3SubscribeAndCallbackBuilder.Complete<P>,
                    Mqtt3SubscribeBuilderBase.First.Complete<
                        Mqtt3SubscribeAndCallbackBuilder<P>,
                        Mqtt3SubscribeAndCallbackBuilder.Complete<P>,
                        Mqtt3SubscribeAndCallbackBuilder.First<P>,
                        Mqtt3SubscribeAndCallbackBuilder.First.Complete<P>> {
            // @formatter:on
            }
        }

        // @formatter:off
        @DoNotImplement
        interface Start<P> extends
                Mqtt3SubscribeAndCallbackBuilder<P>,
                Mqtt3SubscribeAndCallbackBuilder.First<P>,
                Mqtt3SubscribeBuilderBase.Start<
                    Mqtt3SubscribeAndCallbackBuilder<P>,
                    Mqtt3SubscribeAndCallbackBuilder.Complete<P>,
                    Mqtt3SubscribeAndCallbackBuilder.First<P>,
                    Mqtt3SubscribeAndCallbackBuilder.First.Complete<P>> {
        // @formatter:on
        }
    }

    @DoNotImplement
    interface CallbackBuilder<P> {

        @NotNull Ex<P> callback(@NotNull Consumer<Mqtt3Publish> callback);

        @NotNull P send();

        @DoNotImplement
        interface Ex<P> extends CallbackBuilder<P> {

            @NotNull Ex<P> executor(@NotNull Executor executor);
        }
    }
}
