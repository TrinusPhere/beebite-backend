package com.fidecode.beebite.domain.financial;

import com.fidecode.beebite.domain.financial.valueobject.CmvAlertLevel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CmvAlertLevelTest {

    @Test
    void deveRetornarGreenQuandoCmvMenorQue30() {
        assertEquals(CmvAlertLevel.GREEN, CmvAlertLevel.fromPercentage(new BigDecimal("0")));
        assertEquals(CmvAlertLevel.GREEN, CmvAlertLevel.fromPercentage(new BigDecimal("15.5")));
        assertEquals(CmvAlertLevel.GREEN, CmvAlertLevel.fromPercentage(new BigDecimal("29.99")));
    }

    @Test
    void deveRetornarYellowQuandoCmvEntre30E35() {
        assertEquals(CmvAlertLevel.YELLOW, CmvAlertLevel.fromPercentage(new BigDecimal("30")));
        assertEquals(CmvAlertLevel.YELLOW, CmvAlertLevel.fromPercentage(new BigDecimal("32.5")));
        assertEquals(CmvAlertLevel.YELLOW, CmvAlertLevel.fromPercentage(new BigDecimal("35")));
    }

    @Test
    void deveRetornarRedQuandoCmvMaiorQue35() {
        assertEquals(CmvAlertLevel.RED, CmvAlertLevel.fromPercentage(new BigDecimal("35.01")));
        assertEquals(CmvAlertLevel.RED, CmvAlertLevel.fromPercentage(new BigDecimal("50")));
        assertEquals(CmvAlertLevel.RED, CmvAlertLevel.fromPercentage(new BigDecimal("100")));
    }

    @Test
    void deveRetornarGreenQuandoNulo() {
        assertEquals(CmvAlertLevel.GREEN, CmvAlertLevel.fromPercentage(null));
    }
}
