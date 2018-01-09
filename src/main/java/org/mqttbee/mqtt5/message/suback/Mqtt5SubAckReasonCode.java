package org.mqttbee.mqtt5.message.suback;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5ReasonCode;

/**
 * MQTT Reason Codes that can be used in SUBACK packets according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5SubAckReasonCode {

    GRANTED_QOS_0(0x00),
    GRANTED_QOS_1(0x01),
    GRANTED_QOS_2(0x02),
    UNSPECIFIED_ERROR(Mqtt5ReasonCode.UNSPECIFIED_ERROR),
    IMPLEMENTATION_SPECIFIC_ERROR(Mqtt5ReasonCode.IMPLEMENTATION_SPECIFIC_ERROR),
    NOT_AUTHORIZED(Mqtt5ReasonCode.NOT_AUTHORIZED),
    TOPIC_FILTER_INVALID(Mqtt5ReasonCode.TOPIC_FILTER_INVALID),
    PACKET_IDENTIFIER_IN_USE(Mqtt5ReasonCode.PACKET_IDENTIFIER_IN_USE),
    QUOTA_EXCEEDED(Mqtt5ReasonCode.QUOTA_EXCEEDED),
    SHARED_SUBSCRIPTION_NOT_SUPPORTED(Mqtt5ReasonCode.SHARED_SUBSCRIPTION_NOT_SUPPORTED),
    SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED(Mqtt5ReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED),
    WILDCARD_SUBSCRIPTION_NOT_SUPPORTED(Mqtt5ReasonCode.WILDCARD_SUBSCRIPTION_NOT_SUPPORTED);

    private final int code;

    Mqtt5SubAckReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5SubAckReasonCode(@NotNull final Mqtt5ReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    /**
     * @return the byte code of this SUBACK Reason Code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the SUBACK Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the SUBACK Reason Code belonging to the given byte code or null if the byte code is not a valid SUBACK
     * Reason Code code.
     */
    @Nullable
    public static Mqtt5SubAckReasonCode fromCode(final int code) {
        for (final Mqtt5SubAckReasonCode reasonCode : values()) {
            if (reasonCode.code == code) {
                return reasonCode;
            }
        }
        return null;
    }

}
