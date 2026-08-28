package com.fidecode.biteflow.domain.printing.entity;
import com.fidecode.biteflow.domain.shared.StoreGuard; import java.util.UUID;
public class PrintRule implements StoreGuard.StoreScoped {
    private UUID id;private final UUID storeId;private final String name;private final String operation;private final String documentType;private UUID sectorId;private final UUID printerId;private boolean active;
    public PrintRule(UUID storeId,String name,String operation,String documentType,UUID printerId){if(storeId==null||printerId==null)throw new IllegalArgumentException("storeId e printerId são obrigatórios.");this.storeId=storeId;this.name=name;this.operation=operation;this.documentType=documentType;this.printerId=printerId;this.active=true;}
    public static PrintRule reconstitute(UUID id,UUID storeId,String name,String operation,String documentType,UUID sectorId,UUID printerId,boolean active){var r=new PrintRule(storeId,name,operation,documentType,printerId);r.id=id;r.sectorId=sectorId;r.active=active;return r;}
    public void routeToSector(UUID sectorId){this.sectorId=sectorId;} public void changeActive(boolean active){this.active=active;}
    public UUID getId(){return id;}public UUID getStoreId(){return storeId;}public String getName(){return name;}public String getOperation(){return operation;}public String getDocumentType(){return documentType;}public UUID getSectorId(){return sectorId;}public UUID getPrinterId(){return printerId;}public boolean isActive(){return active;}public void setId(UUID id){this.id=id;}
}
