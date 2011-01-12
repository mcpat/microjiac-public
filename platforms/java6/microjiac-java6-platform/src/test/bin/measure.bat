@REM
@REM MicroJIAC - A Lightweight Agent Framework
@REM This file is part of MicroJIAC Java6-Platform.
@REM
@REM Copyright (c) 2007-2011 DAI-Labor, Technische Universität Berlin
@REM
@REM This library includes software developed at DAI-Labor, Technische
@REM Universität Berlin (http://www.dai-labor.de)
@REM
@REM This library is free software: you can redistribute it and/or modify it
@REM under the terms of the GNU Lesser General Public License as published
@REM by the Free Software Foundation, either version 3 of the License, or
@REM (at your option) any later version.
@REM
@REM This library is distributed in the hope that it will be useful, but
@REM WITHOUT ANY WARRANTY; without even the implied warranty of
@REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
@REM GNU Lesser General Public License for more details.
@REM
@REM You should have received a copy of the GNU Lesser General Public License
@REM along with this library.  If not, see <http://www.gnu.org/licenses/>.
@REM

@echo off

for %%i in (%0) do cd %%~di%%~pi

set LOCALCLASSPATH=
for %%i in ("..\lib\*") do call "lcp.bat" %%i

"%JAVA_HOME%\bin\java" -Djava.library.path=..\lib -cp .;%LOCALCLASSPATH% de.jiac.micro.performance.SetupMeasurement

pause