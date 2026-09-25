package com.careslot.repository;


import com.careslot.db.generated.enums.CarePlanStatus;
import com.careslot.db.generated.tables.records.CarePlansRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.careslot.db.generated.tables.CarePlans.CARE_PLANS;

@Repository
public class CarePlanRepository {


    private final DSLContext dsl;

    public CarePlanRepository(DSLContext dsl) {
        this.dsl = dsl;
    }


    public CarePlansRecord save(UUID patientId, UUID clinicianId, String title, String description) {
        var record= dsl.newRecord(CARE_PLANS);
        record.setPatientId(patientId);
        record.setClinicianId(clinicianId);
        record.setTitle(title);
        record.setDescription(description);
        record.store();
        return record;

    }

    public void updateProgress(UUID planId, BigDecimal progress){
        dsl.update(CARE_PLANS)
                .set(CARE_PLANS.PROGRESS_PERCENTAGE,progress)
                .where(CARE_PLANS.ID.eq(planId))
                .execute();
    }

    public List<CarePlansRecord> findByPatientId(UUID patientId){
        return dsl.selectFrom(CARE_PLANS)
                .where(CARE_PLANS.PATIENT_ID.eq(patientId))
                .fetch();
    }
    public Optional<CarePlansRecord> findById(UUID planId) {
        return dsl.selectFrom(CARE_PLANS)
                .where(CARE_PLANS.ID.eq(planId))
                .fetchOptional();
    }
    public boolean existsActivePlan(UUID patientId, UUID clinicianId) {
        return dsl.fetchCount(
                dsl.selectFrom(CARE_PLANS)
                        .where(CARE_PLANS.PATIENT_ID.eq(patientId))
                        .and(CARE_PLANS.CLINICIAN_ID.eq(clinicianId))
                        .and(CARE_PLANS.STATUS.eq(CarePlanStatus.ACTIVE))
        ) > 0;
    }
    public void updateProgressAndStatus(UUID planId, BigDecimal progress, CarePlanStatus status) {
        dsl.update(CARE_PLANS)
                .set(CARE_PLANS.PROGRESS_PERCENTAGE, progress)
                .set(CARE_PLANS.STATUS, status) //
                .where(CARE_PLANS.ID.eq(planId))
                .execute();
    }


    public List<CarePlansRecord> findByClinicianId(UUID clinicianId) {
        return dsl.selectFrom(CARE_PLANS)
                .where(CARE_PLANS.CLINICIAN_ID.eq(clinicianId))
                .fetch();
    }
}
