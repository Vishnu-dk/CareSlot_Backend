package com.careslot.repository;


import com.careslot.db.generated.enums.CarePlanStatus;
import com.careslot.db.generated.enums.TaskStatus;
import com.careslot.db.generated.tables.CarePlanTasks;
import com.careslot.db.generated.tables.records.CarePlanTasksRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.careslot.db.generated.tables.CarePlanTasks.CARE_PLAN_TASKS;
import static com.careslot.db.generated.tables.CarePlans.CARE_PLANS;

@Repository
public class CarePlanTaskRepository {


    private final DSLContext dsl;

    public CarePlanTaskRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public CarePlanTasksRecord save(UUID planId, String title, String description, java.time.LocalDate dueDate, Integer weight) {

        var record= dsl.newRecord(CARE_PLAN_TASKS);
        record.setCarePlanId(planId);
        record.setTitle(title);
        record.setDescription(description);
        record.setDueDate(dueDate);
        record.setWeight(weight);
        record.store();

        return record;
    }

    public List<CarePlanTasksRecord> findByPlanId(UUID planId){
        return dsl.selectFrom(CARE_PLAN_TASKS)
                .where(CARE_PLAN_TASKS.CARE_PLAN_ID.eq(planId))
                .fetch();
    }
    public Optional<CarePlanTasksRecord> findById(UUID taskId) {
        return dsl.selectFrom(CARE_PLAN_TASKS)
                .where(CARE_PLAN_TASKS.ID.eq(taskId))
                .fetchOptional();
    }

    public void updateTaskStatus(UUID taskId, TaskStatus status){
        dsl.update(CARE_PLAN_TASKS)
                .set(CARE_PLAN_TASKS.STATUS,status)
                .set(CARE_PLAN_TASKS.COMPLETED_AT,status==TaskStatus.COMPLETED? OffsetDateTime.now():null)
                .where(CARE_PLAN_TASKS.ID.eq(taskId))
                .execute();
    }

}
