package com.fidecode.biteflow.domain.kds.exception;

import com.fidecode.biteflow.domain.shared.exception.EntityNotFoundException;

public class KdsStationNotFoundException extends EntityNotFoundException {
    public KdsStationNotFoundException(String id) {
        super("Estação KDS não encontrada: " + id);
    }
}

