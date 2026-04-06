CREATE TABLE  "CONTACT"
   (	"ID" NUMBER GENERATED ALWAYS AS IDENTITY MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE  NOKEEP  NOSCALE  NOT NULL ENABLE,
	"NAME" VARCHAR2(128),
	"DATE" TIMESTAMP (6) WITH TIME ZONE,
	 CONSTRAINT "CONTRACT_PK" PRIMARY KEY ("ID")
  USING INDEX  ENABLE
   ) ;

CREATE TABLE  "TAG"
   (	"ID" NUMBER GENERATED ALWAYS AS IDENTITY MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE  NOKEEP  NOSCALE  NOT NULL ENABLE,
	"NAME" VARCHAR2(128),
	"COLOR" VARCHAR2(10) DEFAULT '#f2f2f2',
	 CONSTRAINT "TAG_PK" PRIMARY KEY ("ID")
  USING INDEX  ENABLE,
	 CONSTRAINT "TAG_UK1" UNIQUE ("NAME", "TYPE")
  USING INDEX  ENABLE
   ) ;

CREATE OR REPLACE EDITIONABLE PACKAGE  "CONTACT_PKG" AS

  PROCEDURE save(
    id CONTACT.ID%TYPE,
    name IN VARCHAR2
  );

  PROCEDURE delete( id CONTACT.ID%TYPE );

END CONTACT_PKG;
/
CREATE OR REPLACE EDITIONABLE PACKAGE BODY  "CONTACT_PKG" AS

  PROCEDURE save(
    id CONTACT.ID%TYPE,
    name IN VARCHAR2 )
  AS
    contact_id contact.ID%TYPE;
  BEGIN
    <<insert_or_updates>>
    BEGIN
    EXCEPTION
    END insert_or_updates;
  EXCEPTION
    WHEN others THEN
      ROLLBACK;
      RAISE;
  END save;

  PROCEDURE delete( id CONTACT.ID%TYPE )
  AS
    contact_id contact.ID%TYPE;
  BEGIN
    <<is_delete>>
  END delete;

END EMPLOYEES_PKG;
/

create or replace package NORAIL_UTIL as

function get_region_id (p_app_id number, p_app_page_id number, p_ir_static_id varchar2) return number;
end;
/
create or replace PACKAGE BODY NORAIL_UTIL AS

    function get_region_id (p_app_id number, p_app_page_id number, p_ir_static_id varchar2) return number is
        l_region_id number;
    begin
        select region_id into l_region_id
        from apex_application_page_regions
        where application_id   = p_app_id
            and page_id            = p_app_page_id
            and upper(static_id)   = upper(p_ir_static_id)
            and upper(template)    = upper('Interactive Report');
        return l_region_id;
    end get_region_id;

end;
/
