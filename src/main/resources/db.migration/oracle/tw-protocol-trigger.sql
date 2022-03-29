create or replace trigger CMD_NOTIFICATION_TW_PROTOCOL_TRIGGER
after insert on TW_PROTOCOL
referencing new as new old as old
for each row
begin
   if    :new.LEVEL_ID in (1000, 4000)
     and :new.ON_DATE between SYSDATE - 1 and SYSDATE + 14
   then
       insert into CMD_NOTIFICATION (ID, ST_STAFF_ID, LEVEL_ID, LAYER, ON_DATE, LASTMODIFIED, ACTION_TYPE)
       values (CMD_NOTIFICATION_ID.nextval, :new.ST_STAFF_ID, :new.LEVEL_ID, :new.LAYER, :new.ON_DATE, :new.LASTMODIFIED, :new.ACTION_TYPE);
   end if;
end;
