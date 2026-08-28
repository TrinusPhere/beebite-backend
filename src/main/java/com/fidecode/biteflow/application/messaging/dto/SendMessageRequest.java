package com.fidecode.biteflow.application.messaging.dto; import jakarta.validation.constraints.NotBlank; public record SendMessageRequest(@NotBlank String content){}
