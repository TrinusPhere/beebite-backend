package com.trinusphere.beebite.domain.kds.exception;

import com.trinusphere.beebite.domain.shared.exception.EntityNotFoundException;

public class KdsStationNotFoundException extends EntityNotFoundException {
    public KdsStationNotFoundException(String id) {
        super("Estação KDS não encontrada: " + id);
    }
}

