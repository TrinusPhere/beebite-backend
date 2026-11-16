package com.trinusphere.beebite.application.printing.dto;

import java.util.List;

/** Resultado do preview: cada linha já vem quebrada e alinhada na largura (colunas) da bobina. */
public record ReceiptPreviewResponse(int columns, int paperWidthMm, List<Line> lines) {
    public record Line(String text, boolean bold, boolean large) {
    }
}

