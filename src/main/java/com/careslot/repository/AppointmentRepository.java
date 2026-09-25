package com.careslot.repository;


import com.careslot.db.generated.enums.AppointmentStatus;
import com.careslot.db.generated.tables.records.AppointmentsRecord;
import jakarta.validation.constraints.NotNull;
import org.jooq.DSLContext;
import org.springframework.beans.factory.config.DeprecatedBeanWarner;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
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

    public Optional<AppointmentsRecord> findById(UUID id){
        return dsl.selectFrom(APPOINTMENTS)
                .where(APPOINTMENTS.ID.eq(id))
                .fetchOptional();
    }
    public void updateStatus(UUID id,AppointmentStatus status){
        dsl.update(APPOINTMENTS)
                .set(APPOINTMENTS.STATUS,status)
                .where(APPOINTMENTS.ID.eq(id))
                .execute();
    }

    public List<AppointmentsRecord> findByPatientId(UUID patientId){
        return dsl.selectFrom(APPOINTMENTS)
                .where(APPOINTMENTS.PATIENT_ID.eq(patientId))
                .orderBy(APPOINTMENTS.STARTS_AT.desc())
                .fetch();
    }

    public List<AppointmentsRecord> findAllAppointment(){
        return dsl.selectFrom(APPOINTMENTS)
                .orderBy(APPOINTMENTS.STARTS_AT.desc())
                .fetch();
    }

    public List<AppointmentsRecord> findByClinicianId(UUID clinicianId){
        return dsl.selectFrom(APPOINTMENTS)
                .where(APPOINTMENTS.CLINICIAN_ID.eq(clinicianId))
                .orderBy(APPOINTMENTS.STARTS_AT.desc())
                .fetch();
    }

    public List<AppointmentsRecord> findClinicianSchedule(UUID clinicianId,LocalDate date){
        return dsl.selectFrom(APPOINTMENTS)
                .where(APPOINTMENTS.CLINICIAN_ID.eq(clinicianId))
                .and(APPOINTMENTS.STARTS_AT.cast(LocalDate.class).eq(date))
                .and(APPOINTMENTS.STATUS.eq(AppointmentStatus.BOOKED))
                .orderBy(APPOINTMENTS.STARTS_AT.asc())
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
            if (e.getMessage() != null && e.getMessage().contains("exclusion constraint")) {
                throw new RuntimeException("Slot is already booked");
            }
            throw e;
        }
    }

    public Optional<AppointmentsRecord> findCancelledAppointment(UUID clinicianId, OffsetDateTime startsAt, OffsetDateTime endsAt) {
        return dsl.selectFrom(APPOINTMENTS)
                .where(APPOINTMENTS.CLINICIAN_ID.eq(clinicianId))
                .and(APPOINTMENTS.STARTS_AT.eq(startsAt))
                .and(APPOINTMENTS.ENDS_AT.eq(endsAt))
                .and(APPOINTMENTS.STATUS.eq(AppointmentStatus.CANCELLED))
                .fetchOptional();
    }

}
