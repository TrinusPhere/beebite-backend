package com.fidecode.beebite.domain.kds.exception;

import com.fidecode.beebite.domain.shared.exception.EntityNotFoundException;

public class KdsStationNotFoundException extends EntityNotFoundException {
    public KdsStationNotFoundException(String id) {
        super("Estação KDS não encontrada: " + id);
    }
}

