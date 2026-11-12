package com.fidecode.beebite.domain.printing.receipt;

/** Elementos neutros (independentes de ESC/POS) que compõem um recibo. */
public sealed interface ReceiptElement {

    enum Align {
        LEFT, CENTER, RIGHT
    }

    /** Texto com alinhamento; {@code large} = dobro de largura/altura (metade das colunas). */
    record Text(String text, Align align, boolean bold, boolean large) implements ReceiptElement {
    }

    /** Texto à esquerda e valor à direita na mesma linha (ex.: item e preço). */
    record Columns(String left, String right, boolean bold) implements ReceiptElement {
    }

    record Separator(char ch) implements ReceiptElement {
    }

    record Blank() implements ReceiptElement {
    }
}

