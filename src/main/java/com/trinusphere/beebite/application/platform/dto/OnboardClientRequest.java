package com.trinusphere.beebite.application.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public record OnboardClientRequest(
        @NotBlank(message = "A razão social é obrigatória.") String companyName,
        @NotBlank(message = "O nome fantasia é obrigatório.") String tradeName,
        @NotBlank(message = "O nome do proprietário é obrigatório.") String ownerName,
        String documentNumber,
        String email,
        String phone,
        @NotBlank(message = "O login do proprietário é obrigatório.") String ownerLogin,
        @NotBlank(message = "A senha inicial do proprietário é obrigatória.") String ownerInitialPassword,
        @NotEmpty(message = "Informe ao menos uma loja.") List<StoreSetupDto> stores,
        ContractSetupDto contract
) {
    public record StoreSetupDto(
            @NotBlank(message = "O nome da loja é obrigatório.") String name,
            String cnpj,
            String address,
            String publicSlug,
            @NotBlank(message = "O modo de sincronização é obrigatório.") String syncMode
    ) {
    }

    public record ContractSetupDto(
            @NotBlank(message = "O nome do plano é obrigatório.") String planName,
            BigDecimal monthlyFee,
            BigDecimal setupFee,
            int billingDay,
            boolean includesIfood,
            boolean includesEdgeSync
    ) {
    }
}

