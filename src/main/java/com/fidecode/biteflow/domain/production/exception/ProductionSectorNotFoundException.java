package com.fidecode.biteflow.domain.production.exception;
import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;
public class ProductionSectorNotFoundException extends EntityNotFoundException { public ProductionSectorNotFoundException(String id){super("Setor de produção não encontrado: " + id);} }
