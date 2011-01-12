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

set _CLASSPATHCOMPONENT=%1
:argCheck
if %2a==a goto gotAllArgs
shift
set _CLASSPATHCOMPONENT=%_CLASSPATHCOMPONENT% %1
goto argCheck
:gotAllArgs
set LOCALCLASSPATH=%LOCALCLASSPATH%;%_CLASSPATHCOMPONENT%

