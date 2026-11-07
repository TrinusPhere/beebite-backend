package com.fidecode.biteflow.infrastructure.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fidecode.biteflow.domain.store.entity.Store;
import com.fidecode.biteflow.domain.store.repository.StoreRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@ApplicationScoped
public class StoreScheduler {

    private static final Logger LOG = Logger.getLogger(StoreScheduler.class);

    private final StoreRepository storeRepository;
    private final ObjectMapper objectMapper;

    @Inject
    public StoreScheduler(StoreRepository storeRepository, ObjectMapper objectMapper) {
        this.storeRepository = storeRepository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(every = "1m")
    @Transactional
    public void evaluateStoreSchedules() {
        List<Store> stores = storeRepository.findAll();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        String currentDay = now.getDayOfWeek().name();
        LocalTime currentTime = now.toLocalTime();

        for (Store store : stores) {
            String hoursJson = store.getOpeningHours();
            if (hoursJson == null || hoursJson.isBlank()) continue;

            // Se for texto livre legado (ex: "10:00 - 22:00") e não um JSON, ignora o agendamento automático
            if (!hoursJson.trim().startsWith("{")) {
                continue;
            }

            try {
                JsonNode root = objectMapper.readTree(hoursJson);
                if (root.has("autoSchedule") && root.get("autoSchedule").asBoolean()) {
                    JsonNode scheduleNode = root.get("schedule");
                    if (scheduleNode != null && scheduleNode.has(currentDay)) {
                        JsonNode dayNode = scheduleNode.get(currentDay);
                        if (dayNode.has("active") && dayNode.get("active").asBoolean()) {
                            LocalTime start = LocalTime.parse(dayNode.get("start").asText());
                            LocalTime end = LocalTime.parse(dayNode.get("end").asText());

                            boolean shouldBeOpen = false;
                            if (start.isBefore(end)) {
                                if (!currentTime.isBefore(start) && currentTime.isBefore(end)) {
                                    shouldBeOpen = true;
                                }
                            } else {
                                // Overnight shift (e.g. 18:00 to 02:00)
                                if (!currentTime.isBefore(start) || currentTime.isBefore(end)) {
                                    shouldBeOpen = true;
                                }
                            }

                            if (store.isOpen() != shouldBeOpen) {
                                if (shouldBeOpen) store.open();
                                else store.close();
                                storeRepository.save(store);
                            }
                        } else {
                            // Inativo para hoje
                            if (store.isOpen()) {
                                store.close();
                                storeRepository.save(store);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.debugf(e, "Horário de funcionamento não pôde ser interpretado como JSON na loja %s: %s",
                        store.getName(), e.getMessage());
            }
        }
    }
}
