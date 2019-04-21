package io.jqtt.broker.protocol.session;

import io.jqtt.broker.protocol.model.ClientId;
import lombok.NonNull;

public interface SessionManager {
    Boolean exists(@NonNull ClientId clientId);
}
