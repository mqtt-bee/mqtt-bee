package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.Mqtt5ClientConnectionDataImpl;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5AuthEncoder;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5AuthProperty;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;

import static org.mqttbee.mqtt5.codec.decoder.Mqtt5MessageDecoderUtil.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5AuthDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 2; // reason code (1) + property length (min 1)

    @Inject
    Mqtt5AuthDecoder() {
    }

    @Override
    @Nullable
    public Mqtt5AuthImpl decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final Mqtt5ClientConnectionDataImpl clientConnectionData) {

        final Channel channel = clientConnectionData.getChannel();

        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("AUTH", channel);
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            disconnectRemainingLengthTooShort(channel);
            return null;
        }

        final Mqtt5AuthReasonCode reasonCode = Mqtt5AuthReasonCode.fromCode(in.readUnsignedByte());
        if (reasonCode == null) {
            disconnectWrongReasonCode("AUTH", channel);
            return null;
        }

        final int propertyLength = Mqtt5DataTypes.decodeVariableByteInteger(in);
        if (propertyLength < 0) {
            disconnectMalformedPropertyLength(channel);
            return null;
        }
        if (in.readableBytes() != propertyLength) {
            disconnectMustNotHavePayload("AUTH", channel);
            return null;
        }

        Mqtt5UTF8StringImpl method = null;
        ByteBuffer data = null;
        Mqtt5UTF8StringImpl reasonString = null;
        ImmutableList.Builder<Mqtt5UserPropertyImpl> userPropertiesBuilder = null;

        while (in.isReadable()) {

            final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);
            if (propertyIdentifier < 0) {
                disconnectMalformedPropertyIdentifier(channel);
                return null;
            }

            switch (propertyIdentifier) {
                case Mqtt5AuthProperty.AUTHENTICATION_METHOD:
                    method = decodeUTF8StringOnlyOnce(method, "authentication method", channel, in);
                    if (method == null) {
                        return null;
                    }
                    break;

                case Mqtt5AuthProperty.AUTHENTICATION_DATA:
                    data = decodeBinaryDataOnlyOnce(data, "authentication data", channel, in,
                            ChannelAttributes.useDirectBufferForAuth(channel));
                    if (data == null) {
                        return null;
                    }
                    break;

                case Mqtt5AuthProperty.REASON_STRING:
                    reasonString =
                            decodeReasonStringCheckProblemInformationRequested(reasonString, clientConnectionData, in);
                    if (reasonString == null) {
                        return null;
                    }
                    break;

                case Mqtt5AuthProperty.USER_PROPERTY:
                    userPropertiesBuilder = decodeUserPropertyCheckProblemInformationRequested(userPropertiesBuilder,
                            clientConnectionData, in);
                    if (userPropertiesBuilder == null) {
                        return null;
                    }
                    break;

                default:
                    disconnectWrongProperty("AUTH", channel);
                    return null;
            }
        }

        if (method == null) {
            disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "AUTH must not omit authentication method", channel);
            return null;
        }

        final Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.build(userPropertiesBuilder);

        return new Mqtt5AuthImpl(reasonCode, method, data, reasonString, userProperties, Mqtt5AuthEncoder.PROVIDER);
    }

}
