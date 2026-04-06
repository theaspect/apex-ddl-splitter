# Apex DDL Splitter

Oracle APEX could export Schema DDL.
The problem is the order of objects in the SQL file is unstable.
You can't effectively compare dump files between each other in a version control system like git.

This app splits a single SQL file into many small SQL files, and in each file exactly one object is stored.

To run it, you can just

`java -jar apex-ddl-splitter.java -i your-dump.sql`
or
`apex-ddl-splitter.exe -i your-dump.sql`

The result will be in `your-dump/` folder relative to input SQL

Optionally, you can provide the output folder as a second parameter

## Native compilation

Open vscode developer command prompt and run `gradlew.bat nativeCompile`. Result will be in `build/native/nativeCompile/apex-ddl-splitter.ext`
