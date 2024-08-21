package com.chrzanowy.events.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventEntity(UUID eventUuid, OffsetDateTime eventTime, String eventBody) {

}
