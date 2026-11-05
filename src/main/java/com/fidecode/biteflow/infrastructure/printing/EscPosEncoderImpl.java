package com.fidecode.biteflow.infrastructure.printing;

import com.fidecode.biteflow.application.printing.port.EscPosEncoder;
import com.fidecode.biteflow.domain.printing.entity.Printer;
import com.fidecode.biteflow.domain.printing.receipt.ReceiptDocument;
import com.fidecode.biteflow.domain.printing.receipt.ReceiptDocument.RenderedLine;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class EscPosEncoderImpl implements EscPosEncoder {

    private static final byte ESC = 0x1B;
    private static final byte GS = 0x1D;
    private static final byte LF = 0x0A;

    @Override
    public byte[] encode(ReceiptDocument document, Printer printer) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // 1. Inicializar impressora (ESC @)
            out.write(new byte[] { ESC, 0x40 });

            // 2. Selecionar tabela de caracteres (code page)
            int pageCode = resolveCodePageNumber(printer.getCodePage());
            out.write(new byte[] { ESC, 0x74, (byte) pageCode });

            Charset charset = resolveCharset(printer.getCodePage());

            boolean currentBold = false;
            boolean currentLarge = false;

            // 3. Renderizar cada linha formatada
            for (RenderedLine line : document.render()) {
                if (line.large() != currentLarge) {
                    currentLarge = line.large();
                    // GS ! n (0x11 = double height + width, 0x00 = normal)
                    out.write(new byte[] { GS, 0x21, (byte) (currentLarge ? 0x11 : 0x00) });
                }

                if (line.bold() != currentBold) {
                    currentBold = line.bold();
                    // ESC E n (0x01 = on, 0x00 = off)
                    out.write(new byte[] { ESC, 0x45, (byte) (currentBold ? 0x01 : 0x00) });
                }

                if (!line.text().isEmpty()) {
                    out.write(line.text().getBytes(charset));
                }
                out.write(LF);
            }

            // 4. Restaurar estado padrão
            if (currentBold) {
                out.write(new byte[] { ESC, 0x45, 0x00 });
            }
            if (currentLarge) {
                out.write(new byte[] { GS, 0x21, 0x00 });
            }

            // 5. Avanço final de linhas (feed)
            out.write(new byte[] { LF, LF, LF });

            // 6. Corte de papel (GS V 66 0 = corte parcial)
            if (printer.isCutPaper()) {
                out.write(new byte[] { GS, 0x56, 0x42, 0x00 });
            }

            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao gerar buffer ESC/POS", e);
        }
    }

    private static int resolveCodePageNumber(String codePage) {
        if (codePage == null)
            return 3; // CP860
        return switch (codePage.toUpperCase()) {
            case "CP437" -> 0;
            case "CP850" -> 2;
            case "CP860" -> 3;
            case "CP1252" -> 16;
            case "CP858" -> 19;
            default -> 3;
        };
    }

    private static Charset resolveCharset(String codePage) {
        if (codePage == null)
            return findCharset("CP860", "IBM860");
        return switch (codePage.toUpperCase()) {
            case "CP437" -> findCharset("CP437", "IBM437");
            case "CP850" -> findCharset("CP850", "IBM850");
            case "CP860" -> findCharset("CP860", "IBM860");
            case "CP1252" -> findCharset("windows-1252", "CP1252");
            case "CP858" -> findCharset("CP858", "IBM858");
            default -> StandardCharsets.ISO_8859_1;
        };
    }

    private static Charset findCharset(String... names) {
        for (String name : names) {
            try {
                return Charset.forName(name);
            } catch (Exception ignored) {
            }
        }
        return StandardCharsets.ISO_8859_1;
    }
}
