package com.careslot.repository;

import com.careslot.db.generated.tables.records.ClinicianAvailabilityRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import static com.careslot.db.generated.tables.ClinicianAvailability.CLINICIAN_AVAILABILITY;

@Repository
public class ClinicianAvailabilityRepository {

    private final DSLContext dsl;

    public ClinicianAvailabilityRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<ClinicianAvailabilityRecord> findByClinicianId(UUID clinicianId) {
        return dsl.selectFrom(CLINICIAN_AVAILABILITY)
                .where(CLINICIAN_AVAILABILITY.CLINICIAN_ID.eq(clinicianId))
                .and(CLINICIAN_AVAILABILITY.IS_AVAILABLE.eq(true))
                .fetch();
    }

    public void saveAvailability(UUID clinicianId, Short dayOfWeek, LocalTime startTime, LocalTime endTime) {
        var record = dsl.newRecord(CLINICIAN_AVAILABILITY);
        record.setClinicianId(clinicianId);
        record.setDayOfWeek(dayOfWeek);
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setIsAvailable(true);
        record.store();
    }
}