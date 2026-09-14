package com.careslot.repository;


import com.careslot.db.generated.enums.UserRole;
import com.careslot.db.generated.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

import static com.careslot.db.generated.tables.Users.USERS;

@Repository
public class AuthRepository {

    private final DSLContext dsl;


    public AuthRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void register(String email, String password, String role){
        UserRole inputRole=UserRole.valueOf(role);
        dsl.insertInto(USERS)
                .set(USERS.EMAIL,email)
                .set(USERS.PASSWORD_HASH,password)
                .set(USERS.ROLE,inputRole)
                .set(USERS.CREATED_AT, OffsetDateTime.now())
                .execute();
    }

}
