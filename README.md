# Apex DDL Splitter

Oracle APEX could export Schema DDL.
The problem is the order of objects in sql file is unstable.
You can't effectively compare dump files between each other in version control system like git.

This app split a single SQL file into many small sql files and in each file exactly one object stored.

To run it you can just

`java -jar apex-ddl-splitter.java your-dump.sql`
or
`apex-ddl-splitter.exe your-dump.sql`

The result will be in `your-dump/` folder relative to input sql

Optionally you can provide output folder as a second parameter
