package com.careslot.repository;


import com.careslot.db.generated.enums.AppointmentStatus;
import com.careslot.db.generated.tables.records.AppointmentsRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.config.DeprecatedBeanWarner;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.careslot.db.generated.tables.Appointments.APPOINTMENTS;

@Repository
public class AppointmentRepository {

    private final DSLContext dsl;

    public AppointmentRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<AppointmentsRecord > findByClinicianAndDate(UUID clinicianId, LocalDate date){
        return dsl.selectFrom(APPOINTMENTS)
                .where(APPOINTMENTS.CLINICIAN_ID.eq(clinicianId))
                .and(APPOINTMENTS.STARTS_AT.cast(LocalDate.class).eq(date))
                .and(APPOINTMENTS.STATUS.ne(AppointmentStatus.CANCELLED))
                .fetch();
    }
    public boolean existsOverlappingForPatient(UUID patientId, OffsetDateTime startsAt, OffsetDateTime endsAt) {
        return dsl.fetchCount(
                dsl.selectFrom(APPOINTMENTS)
                        .where(APPOINTMENTS.PATIENT_ID.eq(patientId))
                        .and(APPOINTMENTS.STARTS_AT.lessThan(endsAt))
                        .and(APPOINTMENTS.ENDS_AT.greaterThan(startsAt))
                        .and(APPOINTMENTS.STATUS.ne(AppointmentStatus.CANCELLED))
        ) > 0;
    }
    public boolean hasBookedAppointment(UUID patientId){
        return dsl.fetchCount(
                dsl.selectFrom(APPOINTMENTS)
                        .where(APPOINTMENTS.PATIENT_ID.eq(patientId))
                        .and(APPOINTMENTS.STATUS.eq(AppointmentStatus.BOOKED))
        ) > 0;
    }


    public boolean existsValidAppointment(UUID patientId, UUID clinicianId) {
        return dsl.fetchCount(
                dsl.selectFrom(APPOINTMENTS)
                        .where(APPOINTMENTS.PATIENT_ID.eq(patientId))
                        .and(APPOINTMENTS.CLINICIAN_ID.eq(clinicianId))
                        .and(APPOINTMENTS.STATUS.eq(AppointmentStatus.BOOKED)) // As requested
        ) > 0;
    }
    public AppointmentsRecord save(UUID patientId, UUID clinicianId, OffsetDateTime startsAt, OffsetDateTime endsAt, String reason) {
        var record = dsl.newRecord(APPOINTMENTS);
        record.setPatientId(patientId);
        record.setClinicianId(clinicianId);
        record.setStartsAt(startsAt);
        record.setEndsAt(endsAt);
        record.setReasonForVisit(reason);
        record.setStatus(AppointmentStatus.BOOKED);

        try {
            record.store();
            return record;
        } catch (DataIntegrityViolationException e) {
            // SQLState 23P01 is Postgres for Exclusion Constraint Violation
            if (e.getMessage() != null && e.getMessage().contains("exclusion constraint")) {
                throw new RuntimeException("Slot is already booked");
            }
            throw e;
        }
    }
}
