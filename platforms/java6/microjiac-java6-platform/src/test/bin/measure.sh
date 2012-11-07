#!/bin/bash
#
# MicroJIAC - A Lightweight Agent Framework
# This file is part of MicroJIAC Java6-Platform.
#
# Copyright (c) 2007-2012 DAI-Labor, Technische Universität Berlin
#
# This library includes software developed at DAI-Labor, Technische
# Universität Berlin (http://www.dai-labor.de)
#
# This library is free software: you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as published
# by the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This library is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this library.  If not, see <http://www.gnu.org/licenses/>.
#


# set classpath
DYNCLASSPATH=''
for i in $( ls ../lib ); do
	DYNCLASSPATH=$DYNCLASSPATH':'../lib/$i
done
# echo "Dynamic Classpath: "$DYNCLASSPATH

# construct classpath
export CLASSPATH='.:'$CLASSPATH':'$DYNCLASSPATH

# Java Options
JAVA_OPTIONS=''
# echo 'Java Options: '$JAVA_OPTIONS

# Main Class
MAIN_CLASS='de.jiac.micro.performance.SetupMeasurement'

# Main Class Parameters
MAIN_CLASS_PARAMETERS=''


#
# Java execution
#
echo $JAVA_HOME/bin/java $JAVA_OPTIONS $MAIN_CLASS $MAIN_CLASS_PARAMETERS

$JAVA_HOME/bin/java $JAVA_OPTIONS $MAIN_CLASS $MAIN_CLASS_PARAMETERS