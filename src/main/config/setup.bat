@echo off

if "%JAVACMD%"=="" (
	if exist "%~dp0\java-runtime" (
		set JAVACMD=%~dp0\java-runtime\bin\java
		echo Java used : %~dp0\java-runtime\bin\java
		%~dp0\java-runtime\bin\java -version
	) else (
		echo Java used :
		java -version
	)
) else (
	echo Java used : %JAVACMD%
	%JAVACMD% -version
)

echo ------------------------------------------------
