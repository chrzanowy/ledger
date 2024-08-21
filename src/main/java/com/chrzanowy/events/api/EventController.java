package com.chrzanowy.events.api;

import com.chrzanowy.events.api.model.BaseEvent;
import com.chrzanowy.events.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(("/api/v1/events"))
public class EventController {

    private final EventService eventService;

    @PostMapping
    @Operation(summary = "Consume event")
    @ResponseStatus(HttpStatus.CREATED)
    public void consumeEvent(@Parameter(description = "Event to consume") @Valid @RequestBody BaseEvent event) {
        eventService.handle(event);
    }


    @PutMapping("/{eventId}/reprocess")
    @Operation(summary = "Reprocess event")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reprocessEvent(@Parameter(description = "Event uuid to reprocess") @PathVariable UUID eventId) {
        eventService.reprocessEvent(eventId);
    }

}
