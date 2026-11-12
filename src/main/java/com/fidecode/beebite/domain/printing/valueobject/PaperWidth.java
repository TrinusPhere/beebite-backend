package com.fidecode.beebite.domain.printing.valueobject;

/** Largura da bobina térmica e quantidade de caracteres por linha (fonte A padrão da Epson). */
public enum PaperWidth {
    MM58(58, 32), MM80(80, 48);

    private final int millimeters;
    private final int columns;

    PaperWidth(int millimeters, int columns) {
        this.millimeters = millimeters;
        this.columns = columns;
    }

    public int millimeters() {
        return millimeters;
    }

    public int columns() {
        return columns;
    }

    public static PaperWidth fromMillimeters(int mm) {
        for (PaperWidth w : values()) {
            if (w.millimeters == mm)
                return w;
        }
        throw new IllegalArgumentException("Largura de bobina não suportada: " + mm + "mm (use 58 ou 80).");
    }
}

