package com.trinusphere.beebite.application.printing.dto;

/**
 * Resultado de uma impressão. {@code mode} = SENT (backend enviou pela rede) ou BROWSER (USB: o navegador deve enviar
 * {@code escPosBase64} à impressora).
 */
public record PrintResultResponse(String mode, boolean sent, String escPosBase64, String message) {
    public static PrintResultResponse sent(String message) {
        return new PrintResultResponse("SENT", true, null, message);
    }

    public static PrintResultResponse browser(String escPosBase64) {
        return new PrintResultResponse("BROWSER", false, escPosBase64,
                "Documento gerado. O navegador deve enviá-lo à impressora USB.");
    }
}

