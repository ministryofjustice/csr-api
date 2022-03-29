create or replace trigger CMD_NOTIFICATION_TW_SCHEDULE_TRIGGER
after insert on TW_SCHEDULE
referencing new as new old as old
for each row
begin
   if    :new.LEVEL_ID in (1000, 4000)
     and :new.ON_DATE between SYSDATE - 1 and SYSDATE + 14
   then
       insert into CMD_NOTIFICATION (ID, ST_STAFF_ID, LEVEL_ID, LAYER, ON_DATE, LASTMODIFIED,  TASK_START, TASK_END, REF_ID, OPTIONAL_1, PU_PLANUNIT_ID)
       values (CMD_NOTIFICATION_ID.nextval, :new.ST_STAFF_ID, :new.LEVEL_ID, :new.LAYER, :new.ON_DATE, :new.SCHED_LASTMODIFIED, :new.TASK_START, :new.TASK_END, :new.REF_ID, :new.OPTIONAL_1, :new.PU_PLANUNIT_ID);
   end if;
end;
