package com.careslot.repository;

import com.careslot.db.generated.tables.records.CliniciansRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static com.careslot.db.generated.tables.Clinicians.CLINICIANS;

@Repository
public class ClinicianRepository {

    private final DSLContext dsl;

    public ClinicianRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public boolean existsById(UUID userId) {
        return dsl.fetchCount(dsl.selectFrom(CLINICIANS).where(CLINICIANS.ID.eq(userId))) > 0;
    }

    public Optional<CliniciansRecord> findById(UUID userId) {
        return dsl.selectFrom(CLINICIANS)
                .where(CLINICIANS.ID.eq(userId))
                .fetchOptional();
    }

    public List<CliniciansRecord> findAll() {
        return dsl.selectFrom(CLINICIANS).fetch();
    }

    public void saveProfile(UUID userId, String firstName, String lastName, String specialty, String licenseNumber) {
        var record = dsl.newRecord(CLINICIANS);

        record.setId(userId);
        record.setFirstName(firstName);
        record.setLastName(lastName);
        record.setSpecialty(specialty);
        record.setLicenseNumber(licenseNumber);

        record.store();
    }
}