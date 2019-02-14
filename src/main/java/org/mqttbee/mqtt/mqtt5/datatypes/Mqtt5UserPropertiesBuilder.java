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

package org.mqttbee.mqtt.mqtt5.datatypes;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;

/**
 * Builder for {@link Mqtt5UserProperties}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5UserPropertiesBuilder extends Mqtt5UserPropertiesBuilderBase<Mqtt5UserPropertiesBuilder> {

    /**
     * Builds the {@link Mqtt5UserProperties}.
     *
     * @return the built {@link Mqtt5UserProperties}.
     */
    @NotNull Mqtt5UserProperties build();

    /**
     * Builder for {@link Mqtt5UserProperties} that are applied to a parent.
     *
     * @param <P> the type of the result when the built {@link Mqtt5UserProperties} are applied to the parent.
     */
    @DoNotImplement
    interface Nested<P> extends Mqtt5UserPropertiesBuilderBase<Nested<P>> {

        /**
         * Builds the {@link Mqtt5UserProperties} and applies them to the parent.
         *
         * @return the result when the built {@link Mqtt5UserProperties} are applied to the parent.
         */
        @NotNull P applyUserProperties();
    }
}
