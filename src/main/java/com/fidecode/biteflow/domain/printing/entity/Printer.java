package com.fidecode.biteflow.domain.printing.entity;

import com.fidecode.biteflow.domain.printing.valueobject.PaperWidth;
import com.fidecode.biteflow.domain.shared.StoreGuard;
import java.util.Set;
import java.util.UUID;

public class Printer implements StoreGuard.StoreScoped {
    public static final String NETWORK = "NETWORK";
    public static final String USB_BROWSER = "USB_BROWSER";
    private static final Set<String> CONNECTION_TYPES = Set.of(NETWORK, USB_BROWSER, "USB", "LOCAL_AGENT");
    private static final Set<String> CODE_PAGES = Set.of("CP437", "CP850", "CP858", "CP860", "CP1252");
    private static final int DEFAULT_PORT = 9100;
    private static final String DEFAULT_CODE_PAGE = "CP860";

    private UUID id;
    private final UUID storeId;
    private String name;
    private String connectionType;
    private String address;
    private boolean active;
    private PaperWidth paperWidth = PaperWidth.MM80;
    private int port = DEFAULT_PORT;
    private String codePage = DEFAULT_CODE_PAGE;
    private boolean cutPaper = true;

    public Printer(UUID storeId, String name, String connectionType) {
        if (storeId == null)
            throw new IllegalArgumentException("storeId é obrigatório.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name é obrigatório.");
        this.storeId = storeId;
        this.name = name;
        this.connectionType = validConnectionType(connectionType);
        this.active = true;
    }

    public static Printer reconstitute(UUID id, UUID storeId, String name, String connectionType, String address,
            boolean active, int paperWidthMm, int port, String codePage, boolean cutPaper) {
        var p = new Printer(storeId, name, connectionType);
        p.id = id;
        p.address = address;
        p.active = active;
        p.configureHardware(paperWidthMm, port, codePage, cutPaper);
        return p;
    }

    private static String validConnectionType(String type) {
        if (type == null || type.isBlank())
            return NETWORK;
        String normalized = type.trim().toUpperCase();
        if (!CONNECTION_TYPES.contains(normalized))
            throw new IllegalArgumentException("Tipo de conexão inválido: " + type);
        return normalized;
    }

    /** Parâmetros físicos da impressora térmica. Valores nulos preservam o valor atual. */
    public void configureHardware(Integer paperWidthMm, Integer port, String codePage, Boolean cutPaper) {
        PaperWidth newWidth = paperWidthMm != null ? PaperWidth.fromMillimeters(paperWidthMm) : this.paperWidth;
        int newPort = port != null ? port : this.port;
        if (newPort < 1 || newPort > 65535)
            throw new IllegalArgumentException("Porta inválida: " + newPort);
        String newCodePage = this.codePage;
        if (codePage != null && !codePage.isBlank()) {
            newCodePage = codePage.trim().toUpperCase();
            if (!CODE_PAGES.contains(newCodePage))
                throw new IllegalArgumentException("Code page não suportada: " + codePage);
        }
        this.paperWidth = newWidth;
        this.port = newPort;
        this.codePage = newCodePage;
        if (cutPaper != null)
            this.cutPaper = cutPaper;
    }

    public void configureAddress(String address) {
        this.address = address;
    }

    public void changeActive(boolean active) {
        this.active = active;
    }

    public void rename(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name é obrigatório.");
        this.name = name;
    }

    public boolean isNetwork() {
        return NETWORK.equals(connectionType);
    }

    /** Impressoras USB são acionadas pelo navegador do caixa: o backend apenas entrega os bytes ESC/POS. */
    public boolean isBrowserDriven() {
        return USB_BROWSER.equals(connectionType);
    }

    public UUID getId() {
        return id;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public String getAddress() {
        return address;
    }

    public boolean isActive() {
        return active;
    }

    public PaperWidth getPaperWidth() {
        return paperWidth;
    }

    public int getPort() {
        return port;
    }

    public String getCodePage() {
        return codePage;
    }

    public boolean isCutPaper() {
        return cutPaper;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
