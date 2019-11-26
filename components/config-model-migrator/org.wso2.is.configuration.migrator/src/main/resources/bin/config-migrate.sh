#!/bin/sh

# ----------------------------------------------------------------------------
#  Copyright 2019 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.



#=================The start of the script:============================================
BASE_DIR="$(cd "$(dirname "$0")"; pwd)";

echo " =================================================================================== "
echo " |   ----------------------------------------------                                | "
echo " |   Welcome to the new config model migration tool                                | "
echo " |  ----------------------------------------------                                 | "
echo " |                                                                                 | "
echo " =================================================================================== "
echo " "
while :
do
        echo "Please enter the older IS home path that needs to be migrated :"
        read MIGRATE_IS_HOME_PATH
        if [ "$MIGRATE_IS_HOME_PATH" = " " ]
        then
        echo "You have not entered a IS home path that needs to be migrated"
        echo "Please try again."
        continue
        else
        break
        fi
done

while :
do
        echo "Please enter default IS home path :"
        read DEFAULT_IS_HOME_PATH
        if [ "$DEFAULT_IS_HOME_PATH" = "" ]
        then
        echo "You have not entered default IS home path"
        echo "Please try again."
        continue
        else
        break
        fi
done

echo "Started running the tool ....................... "
java -jar ../config-migrater.jar $MIGRATE_IS_HOME_PATH $DEFAULT_IS_HOME_PATH
echo "Completed executing the tool ................... "
exit
