package org.mqttbee.api.mqtt5.message.disconnect;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5DisconnectEncoder;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;
import org.mqttbee.util.UnsignedDataTypes;

import static org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE;
import static org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DisconnectBuilder {

    private boolean withWillMessage = false;
    private long sessionExpiryInterval = Mqtt5DisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
    private Mqtt5UTF8StringImpl serverReference;
    private Mqtt5UTF8StringImpl reasonString;
    private Mqtt5UserPropertiesImpl userProperties = Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES;

    @NotNull
    public Mqtt5DisconnectBuilder withWillMessage(final boolean withWillMessage) {
        this.withWillMessage = withWillMessage;
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder withSessionExpiryInterval(final long sessionExpiryInterval) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(sessionExpiryInterval));
        this.sessionExpiryInterval = sessionExpiryInterval;
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder withServerReference(@NotNull final Mqtt5UTF8String serverReference) {
        Preconditions.checkNotNull(serverReference);
        this.serverReference = MustNotBeImplementedUtil.checkNotImplemented(serverReference, Mqtt5UTF8StringImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder withReasonString(@NotNull final Mqtt5UTF8String reasonString) {
        Preconditions.checkNotNull(reasonString);
        this.reasonString = MustNotBeImplementedUtil.checkNotImplemented(reasonString, Mqtt5UTF8StringImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5DisconnectBuilder withUserProperties(@NotNull final Mqtt5UserProperties userProperties) {
        Preconditions.checkNotNull(userProperties);
        this.userProperties =
                MustNotBeImplementedUtil.checkNotImplemented(userProperties, Mqtt5UserPropertiesImpl.class);
        return this;
    }

    @NotNull
    public Mqtt5Disconnect build() {
        final Mqtt5DisconnectReasonCode reasonCode =
                withWillMessage ? DISCONNECT_WITH_WILL_MESSAGE : NORMAL_DISCONNECTION;
        return new Mqtt5DisconnectImpl(reasonCode, sessionExpiryInterval, serverReference, reasonString, userProperties,
                Mqtt5DisconnectEncoder.PROVIDER);
    }

}
