package com.careslot.repository;

import com.careslot.db.generated.tables.records.PatientsRecord;
import com.careslot.db.generated.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static com.careslot.db.generated.tables.Patients.PATIENTS;
import static com.careslot.db.generated.tables.Users.USERS;

@Repository
public class PatientRepository {
    private final DSLContext dsl;

    public PatientRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<PatientsRecord> findByUserId(UUID userId) {
        return dsl.selectFrom(PATIENTS)
                .where(PATIENTS.USER_ID.eq(userId))
                .fetchOptional();
    }
    public boolean existsByUserId(UUID userId) {
        return dsl.fetchCount(
                dsl.selectFrom(PATIENTS)
                        .where(PATIENTS.USER_ID.eq(userId))
        ) > 0;
    }

    public List<PatientsRecord> findAll() {
        return dsl.selectFrom(PATIENTS).fetch();
    }

    public void saveProfile(UUID userId, String firstName, String lastName, LocalDate dateOfBirth, String phoneNumber) {
        var record = dsl.newRecord(PATIENTS);
        record.setId(userId);
        record.setUserId(userId);
        record.setFirstName(firstName);
        record.setLastName(lastName);
        record.setDateOfBirth(dateOfBirth);
        record.setPhoneNumber(phoneNumber);
        record.store();
    }

}