package com.careslot.repository;


import com.careslot.db.generated.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static com.careslot.db.generated.tables.Users.USERS;

@Repository
public class UserRepository {

    private final DSLContext dsl;

    public UserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<UsersRecord> findByEmail(String email){
        return dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .and(USERS.DELETED_AT.isNull())
                .fetchOptional();
    }

    public Optional<UsersRecord> findById(UUID id){
        return dsl.selectFrom(USERS)
                .where(USERS.ID.eq(id))
                .and(USERS.DELETED_AT.isNull())
                .fetchOptional();
    }

    public UsersRecord save(UsersRecord user){
        user.store();
        return user;
    }

    public boolean existsByEmail(String email){
        return dsl.fetchCount(
                dsl.selectFrom(USERS)
                        .where(USERS.EMAIL.eq(email))
                        .and(USERS.DELETED_AT.isNull())
        )>0;
    }
}
