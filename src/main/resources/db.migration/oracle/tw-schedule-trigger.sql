create or replace trigger CMD_NOTIFICATION_TW_SCHEDULE_TRIGGER
after insert on TW_SCHEDULE
referencing new as new old as old
for each row
begin
   if    :new.LEVEL_ID in (1000, 4000)
     and :new.ON_DATE between SYSDATE - 1 and SYSDATE + 2
     and :new.LAYER = -1
   then
       insert into CMD_NOTIFICATION (ID, ST_STAFF_ID, LEVEL_ID, ON_DATE, LASTMODIFIED, TASK_START, TASK_END, REF_ID, OPTIONAL_1)
       values (CMD_NOTIFICATION_ID.nextval, :new.ST_STAFF_ID, :new.LEVEL_ID, :new.ON_DATE, :new.SCHED_LASTMODIFIED, :new.TASK_START, :new.TASK_END, :new.REF_ID, :new.OPTIONAL_1);
   end if;
end;
