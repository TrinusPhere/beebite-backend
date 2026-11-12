package com.fidecode.beebite.domain.printing.receipt;

import com.fidecode.beebite.domain.printing.receipt.ReceiptElement.Align;
import java.util.ArrayList;
import java.util.List;

/**
 * Documento de impressão neutro. A mesma renderização alimenta o preview na tela e a geração dos bytes ESC/POS,
 * garantindo que o que o usuário vê é o que sai na bobina.
 */
public record ReceiptDocument(List<ReceiptElement> elements, int columns) {

    /** Linha já quebrada e alinhada. Em linhas {@code large}, o texto ocupa metade das colunas. */
    public record RenderedLine(String text, boolean bold, boolean large) {
    }

    public ReceiptDocument {
        if (columns <= 0)
            throw new IllegalArgumentException("columns deve ser maior que zero.");
        elements = List.copyOf(elements);
    }

    public List<RenderedLine> render() {
        List<RenderedLine> out = new ArrayList<>();
        for (ReceiptElement element : elements) {
            switch (element) {
                case ReceiptElement.Text t -> renderText(t, out);
                case ReceiptElement.Columns c -> renderColumns(c, out);
                case ReceiptElement.Separator s -> out.add(new RenderedLine(String.valueOf(s.ch()).repeat(columns), false, false));
                case ReceiptElement.Blank b -> out.add(new RenderedLine("", false, false));
            }
        }
        return out;
    }

    private void renderText(ReceiptElement.Text t, List<RenderedLine> out) {
        int width = t.large() ? Math.max(1, columns / 2) : columns;
        for (String line : wrap(t.text(), width)) {
            out.add(new RenderedLine(align(line, width, t.align()), t.bold(), t.large()));
        }
    }

    private void renderColumns(ReceiptElement.Columns c, List<RenderedLine> out) {
        String right = c.right() == null ? "" : c.right().trim();
        int leftWidth = columns - right.length() - 1;
        if (leftWidth < 8) {
            for (String line : wrap(c.left(), columns))
                out.add(new RenderedLine(line, c.bold(), false));
            for (String line : wrap(right, columns))
                out.add(new RenderedLine(align(line, columns, Align.RIGHT), c.bold(), false));
            return;
        }
        List<String> left = wrap(c.left(), leftWidth);
        String first = left.get(0);
        out.add(new RenderedLine(first + " ".repeat(columns - first.length() - right.length()) + right, c.bold(), false));
        for (int i = 1; i < left.size(); i++)
            out.add(new RenderedLine(left.get(i), c.bold(), false));
    }

    private static String align(String line, int width, Align align) {
        int free = Math.max(0, width - line.length());
        return switch (align) {
            case LEFT -> line;
            case RIGHT -> " ".repeat(free) + line;
            case CENTER -> " ".repeat(free / 2) + line;
        };
    }

    /** Quebra por palavras; palavras maiores que a largura são partidas à força. */
    static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            lines.add("");
            return lines;
        }
        StringBuilder current = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            String w = word;
            while (w.length() > width) {
                if (current.length() > 0) {
                    lines.add(current.toString());
                    current.setLength(0);
                }
                lines.add(w.substring(0, width));
                w = w.substring(width);
            }
            if (current.length() == 0) {
                current.append(w);
            } else if (current.length() + 1 + w.length() <= width) {
                current.append(' ').append(w);
            } else {
                lines.add(current.toString());
                current.setLength(0);
                current.append(w);
            }
        }
        if (current.length() > 0)
            lines.add(current.toString());
        return lines;
    }
}

