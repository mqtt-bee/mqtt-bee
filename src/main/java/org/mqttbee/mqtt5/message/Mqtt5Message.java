package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil;
import org.mqttbee.mqtt5.exceptions.Mqtt5MaximumPacketSizeExceededException;
import org.mqttbee.mqtt5.handler.Mqtt5ServerData;

import java.util.Optional;

import static org.mqttbee.mqtt5.message.Mqtt5Property.REASON_STRING;

/**
 * Interface for MQTT messages that can be encoded according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5Message {

    /**
     * Encodes this MQTT message.
     *
     * @param channel the channel where the given byte buffer will be written to.
     * @param out     the byte buffer to encode to.
     */
    void encode(@NotNull Channel channel, @NotNull ByteBuf out);

    /**
     * Creates a byte buffer with the correct size for this MQTT message.
     *
     * @param channel the channel where the allocated byte buffer will be written to.
     * @return the allocated byte buffer.
     */
    default ByteBuf allocateBuffer(@NotNull final Channel channel) {
        final int maximumPacketSize = Mqtt5ServerData.getMaximumPacketSize(channel);
        final int encodedLength = encodedLength(maximumPacketSize);
        if (encodedLength < 0) {
            throw new Mqtt5MaximumPacketSizeExceededException(this, maximumPacketSize);
        }
        return channel.alloc().ioBuffer(encodedLength, encodedLength);
    }

    /**
     * Returns the byte count of this MQTT message respecting the given maximum packet size. Calculation is only
     * performed if necessary.
     *
     * @param maxPacketSize the maximum packet size.
     * @return the encoded length of this MQTT message respecting the maximum packet size or -1 if the minimal encoded
     * length of this message is bigger than the maximum packet size.
     */
    int encodedLength(final int maxPacketSize);


    /**
     * Interface for MQTT messages with a properties field in its variable header.
     */
    interface Mqtt5MessageWithProperties extends Mqtt5Message {

        /**
         * Returns the remaining length byte count of this MQTT message respecting the given maximum packet size.
         * <p>
         * If the minimal encoded length of this MQTT message is bigger than the given maximum packet size the returned
         * value is unspecified.
         *
         * @param maxPacketSize the maximum packet size.
         * @return the encoded remaining length of this MQTT message respecting the maximum packet size.
         */
        int encodedRemainingLength(final int maxPacketSize);

        /**
         * Returns the property length byte count of this MQTT message respecting the given maximum packet size.
         * <p>
         * If the minimal encoded length of this MQTT message is bigger than the given maximum packet size the returned
         * value is unspecified.
         *
         * @param maxPacketSize the maximum packet size.
         * @return the encoded property length of this MQTT message respecting the maximum packet size.
         */
        int encodedPropertyLength(final int maxPacketSize);

    }


    /**
     * Base class for MQTT messages with a properties field with omissible user properties in its variable header.
     */
    abstract class Mqtt5MessageWithOmissibleUserProperties implements Mqtt5MessageWithProperties {

        private int encodedLength = -1;
        private int remainingLength = -1;
        private int propertyLength = -1;

        @Override
        public int encodedLength(final int maxPacketSize) {
            int encodedLength = maxEncodedLength();
            if (encodedLength <= maxPacketSize) {
                return encodedLength;
            }
            encodedLength = minEncodedLength();
            if (encodedLength <= maxPacketSize) {
                return encodedLength;
            }
            return -1;
        }

        /**
         * Returns the byte count of this MQTT message without omitting any properties. Calculation is only performed if
         * necessary.
         *
         * @return the encoded length of this MQTT message without omitting any properties.
         */
        private int maxEncodedLength() {
            if (encodedLength == -1) {
                encodedLength = Mqtt5MessageEncoderUtil.encodedPacketLength(maxEncodedRemainingLength());
            }
            return encodedLength;
        }

        /**
         * Returns the minimal byte count of this MQTT message. All properties which can be omitted are omitted.
         *
         * @return the minimal encoded length of this MQTT message.
         */
        private int minEncodedLength() {
            return Mqtt5MessageEncoderUtil.encodedPacketLength(minEncodedRemainingLength());
        }

        @Override
        public final int encodedRemainingLength(final int maxPacketSize) {
            return mustOmitProperties(maxPacketSize) ? minEncodedRemainingLength() : maxEncodedRemainingLength();
        }

        /**
         * Returns the remaining length byte count of this MQTT message without omitting any properties. Calculation is
         * only performed if necessary.
         *
         * @return the encoded remaining length of this MQTT message without omitting any properties.
         */
        private int maxEncodedRemainingLength() {
            if (remainingLength == -1) {
                remainingLength =
                        calculateEncodedRemainingLength() + encodedPropertyLengthWithHeader(maxEncodedPropertyLength());
            }
            return remainingLength;
        }

        /**
         * Returns the minimal remaining length byte count of this MQTT message. All properties which can be omitted are
         * omitted.
         *
         * @return the minimal encoded remaining length of this MQTT message.
         */
        private int minEncodedRemainingLength() {
            return maxEncodedRemainingLength() - encodedPropertyLengthWithHeader(maxEncodedPropertyLength()) +
                    encodedPropertyLengthWithHeader(minEncodedPropertyLength());
        }

        /**
         * Calculates the remaining length byte count of this MQTT message.
         *
         * @return the encoded remaining length of this MQTT message.
         */
        protected abstract int calculateEncodedRemainingLength();

        @Override
        public final int encodedPropertyLength(final int maxPacketSize) {
            return mustOmitProperties(maxPacketSize) ? minEncodedPropertyLength() : maxEncodedPropertyLength();
        }

        /**
         * Returns the property length byte count of this MQTT message without omitting any properties. Calculation is
         * only performed if necessary.
         *
         * @return the encoded property length of this MQTT message without omitting any properties.
         */
        public final int maxEncodedPropertyLength() {
            if (propertyLength == -1) {
                propertyLength = calculateEncodedPropertyLength();
            }
            return propertyLength;
        }

        /**
         * Returns the minimal property length byte count of this MQTT message. All properties which can be omitted are
         * omitted.
         *
         * @return the minimal encoded property length of this MQTT message.
         */
        private int minEncodedPropertyLength() {
            return maxEncodedPropertyLength() - omissiblePropertyLength();
        }

        /**
         * Calculates the property length byte count of this MQTT message.
         *
         * @return the encoded property length of this MQTT message.
         */
        protected abstract int calculateEncodedPropertyLength();

        /**
         * @return the encoded length of the properties that can be omitted.
         */
        int omissiblePropertyLength() {
            return getUserProperties().encodedLength();
        }

        /**
         * @return the user properties of this MQTT message.
         */
        @NotNull
        abstract Mqtt5UserPropertiesImpl getUserProperties();

        /**
         * Checks whether properties of this MQTT message must be omitted to fit the given maximum packet size.
         *
         * @param maxPacketSize the maximum packet size.
         * @return whether properties of this MQTT message must be omitted.
         */
        final boolean mustOmitProperties(final int maxPacketSize) {
            return maxEncodedLength() > maxPacketSize;
        }

        /**
         * Calculates the encoded property length with a prefixed header.
         *
         * @param propertyLength the encoded property length.
         * @return the encoded property length with a prefixed header.
         */
        int encodedPropertyLengthWithHeader(final int propertyLength) {
            return Mqtt5MessageEncoderUtil.encodedLengthWithHeader(propertyLength);
        }

        /**
         * Encodes the user properties of this MQTT message if they must not be omitted due to the given maximum packet
         * size.
         *
         * @param maxPacketSize the maximum packet size.
         * @param out           the byte buffer to encode to.
         */
        public final void encodeUserProperties(final int maxPacketSize, @NotNull final ByteBuf out) {
            if (!mustOmitProperties(maxPacketSize)) {
                getUserProperties().encode(out);
            }
        }

    }


    /**
     * Base class for MQTT messages with a properties field with user properties in its variable header.
     */
    abstract class Mqtt5MessageWithUserProperties extends Mqtt5MessageWithOmissibleUserProperties {

        private final Mqtt5UserPropertiesImpl userProperties;

        public Mqtt5MessageWithUserProperties(@NotNull final Mqtt5UserPropertiesImpl userProperties) {
            this.userProperties = userProperties;
        }

        @NotNull
        public Mqtt5UserPropertiesImpl getUserProperties() {
            return userProperties;
        }

    }


    /**
     * Base class for MQTT messages with a properties field with a reason string property in its variable header.
     */
    abstract class Mqtt5MessageWithReasonString extends Mqtt5MessageWithUserProperties {

        private final Mqtt5UTF8StringImpl reasonString;

        public Mqtt5MessageWithReasonString(
                @Nullable final Mqtt5UTF8StringImpl reasonString,
                @NotNull final Mqtt5UserPropertiesImpl userProperties) {
            super(userProperties);
            this.reasonString = reasonString;
        }

        @NotNull
        public Optional<Mqtt5UTF8String> getReasonString() {
            return Optional.ofNullable(reasonString);
        }

        @Nullable
        public Mqtt5UTF8StringImpl getRawReasonString() {
            return reasonString;
        }

        @Override
        int omissiblePropertyLength() {
            return super.omissiblePropertyLength() +
                    Mqtt5MessageEncoderUtil.nullablePropertyEncodedLength(reasonString);
        }

        /**
         * Encodes the reason string of this MQTT message if it must not be omitted due to the given maximum packet
         * size.
         *
         * @param maxPacketSize the maximum packet size.
         * @param out           the byte buffer to encode to.
         */
        public void encodeReasonString(final int maxPacketSize, @NotNull final ByteBuf out) {
            if (!mustOmitProperties(maxPacketSize)) {
                Mqtt5MessageEncoderUtil.encodeNullableProperty(REASON_STRING, reasonString, out);
            }
        }

    }


    /**
     * Base class for MQTT messages with a omissible properties field in its variable header.
     */
    abstract class Mqtt5MessageWithOmissibleProperties extends Mqtt5MessageWithReasonString {

        public Mqtt5MessageWithOmissibleProperties(
                @Nullable final Mqtt5UTF8StringImpl reasonString,
                @NotNull final Mqtt5UserPropertiesImpl userProperties) {
            super(reasonString, userProperties);
        }

        @Override
        int encodedPropertyLengthWithHeader(final int propertyLength) {
            return (propertyLength == 0) ? 0 : super.encodedPropertyLengthWithHeader(propertyLength);
        }

    }


    /**
     * Base class for MQTT message wrappers with user properties in its variable header.
     */
    abstract class Mqtt5MessageWithUserPropertiesWrapper<T extends WrappedMqtt5MessageWithUserProperties>
            extends Mqtt5MessageWithOmissibleUserProperties {

        private final T wrapped;

        public Mqtt5MessageWithUserPropertiesWrapper(@NotNull final T wrapped) {
            this.wrapped = wrapped;
        }

        /**
         * @return the wrapped MQTT message.
         */
        @NotNull
        public T getWrapped() {
            return wrapped;
        }

        @NotNull
        @Override
        Mqtt5UserPropertiesImpl getUserProperties() {
            return wrapped.getUserProperties();
        }

        @Override
        protected final int calculateEncodedRemainingLength() {
            return wrapped.encodedRemainingLengthWithoutProperties() + additionalRemainingLength();
        }

        /**
         * Calculates the additional remaining length byte count of this MQTT message.
         *
         * @return the additional remaining length of the wrapper.
         */
        protected abstract int additionalRemainingLength();

        @Override
        protected final int calculateEncodedPropertyLength() {
            return wrapped.encodedPropertyLength() + additionalPropertyLength();
        }

        /**
         * Calculates the additional property length byte count of this MQTT message.
         *
         * @return the additional property length of the wrapper.
         */
        protected abstract int additionalPropertyLength();

    }


    /**
     * Base class for wrapped MQTT messages with user properties in its variable header.
     */
    abstract class WrappedMqtt5MessageWithUserProperties {

        private final Mqtt5UserPropertiesImpl userProperties;

        private int remainingLengthWithoutProperties = -1;
        private int propertyLength = -1;

        public WrappedMqtt5MessageWithUserProperties(@NotNull final Mqtt5UserPropertiesImpl userProperties) {
            this.userProperties = userProperties;
        }

        @NotNull
        public Mqtt5UserPropertiesImpl getUserProperties() {
            return userProperties;
        }

        final int encodedRemainingLengthWithoutProperties() {
            if (remainingLengthWithoutProperties == -1) {
                remainingLengthWithoutProperties = calculateEncodedRemainingLengthWithoutProperties();
            }
            return remainingLengthWithoutProperties;
        }

        /**
         * Calculates the remaining length byte count without the properties of this MQTT message.
         *
         * @return the encoded remaining length without the properties of this MQTT message.
         */
        protected abstract int calculateEncodedRemainingLengthWithoutProperties();

        final int encodedPropertyLength() {
            if (propertyLength == -1) {
                propertyLength = calculateEncodedPropertyLength();
            }
            return propertyLength;
        }

        /**
         * Calculates the property length byte count of this MQTT message.
         *
         * @return the encoded property length of this MQTT message.
         */
        protected abstract int calculateEncodedPropertyLength();

    }

}
