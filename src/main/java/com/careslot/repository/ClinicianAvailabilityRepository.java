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
    public ClinicianAvailabilityRecord findByClinicianIdAndDay(UUID clinicianId, Short dayOfWeek) {
        return dsl.selectFrom(CLINICIAN_AVAILABILITY)
                .where(CLINICIAN_AVAILABILITY.CLINICIAN_ID.eq(clinicianId))
                .and(CLINICIAN_AVAILABILITY.DAY_OF_WEEK.eq(dayOfWeek))
                .and(CLINICIAN_AVAILABILITY.IS_AVAILABLE.eq(true))
                .fetchOne();
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
    public void updateAvailability(UUID clinicianId, Short dayOfWeek, LocalTime startTime, LocalTime endTime) {
        dsl.update(CLINICIAN_AVAILABILITY)
                .set(CLINICIAN_AVAILABILITY.START_TIME, startTime)
                .set(CLINICIAN_AVAILABILITY.END_TIME, endTime)
                .where(CLINICIAN_AVAILABILITY.CLINICIAN_ID.eq(clinicianId))
                .and(CLINICIAN_AVAILABILITY.DAY_OF_WEEK.eq(dayOfWeek))
                .and(CLINICIAN_AVAILABILITY.IS_AVAILABLE.eq(true))
                .execute();
    }
    public void deleteByClinicianIdAndDay(UUID clinicianId, Short dayOfWeek) {
        dsl.deleteFrom(CLINICIAN_AVAILABILITY)
                .where(CLINICIAN_AVAILABILITY.CLINICIAN_ID.eq(clinicianId))
                .and(CLINICIAN_AVAILABILITY.DAY_OF_WEEK.eq(dayOfWeek))
                .execute();
    }
}