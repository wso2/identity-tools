# New Config Model Migration Tool

## Why to use this tool?
This tool will help to identify and migrate the old xml configurations to the new configuration model 
and create the deployment.toml file, which suites the new config model that comes with
 Identity Server 5.9.0 or above. This will be knowledge gathering tool which will gather all the configurations
 that change by the clients and save them as a knowledge for the future use. At one time developer has to commit
 changes to the `Catalog.csv` and thereafter, that knowledge can be used by other developers .

## What this tool gives?
* It will create the `deployement.toml` file with the existing knowledge that identified before and 
committed in the `Catalog.csv` file hosted in resources folder of this tool.

* The rest of the newly identified changes will be recorded in a new `outputCatalog.csv` 
file inside the output folder.

* The not-templated, changed files will be filtered to a folder called `unTemplatedFiles` inside the 
output folder.

* The logs will be recorded in the `log.txt` file inside the output folder.

## How to run the tool

1. Clone the repository [identity-tools](https://github.com/wso2/identity-tools).

2. Build it using maven by running the below command. 

       mvn clean install

3. Go to the `identity-tools/components/config-model-migrator/org.wso2.is.configuration.migrator/target` folder 
and copy the `org.wso2.configuration.migrater-<version>-bundle.zip` zip file to a different location.

4. Unzip the `org.wso2.configuration.migrater-<version>-bundle.zip`.

5. Go to `org.wso2.configuration.migrater-<version>-bundle/bin` folder, open a terminal and run the tool 
using below shell command.

       sh config-migrate.sh

3. This will asks for 2 inputs and it will generate the `deployment.toml` file using the 
existing [knowledge](https://raw.githubusercontent.com/wso2/identity-tools/master/components/config-model-migrator/resources/Catalog.csv)


## Inputs to the tool

1. **Migrated IS home** : The migrated Identity Server (Identity Server 5.8.0 or below)  Home path.
2. **Default IS home** :  The default Identity Server (Identity Server 5.9.0 or above)  Home path.

## Possible Outputs

1. **Complete Success** : This will provide the complete `deployment.toml` file which contain all the 
custom changes. You can directly use this to run the new Identity Server that uses new 
config model.

Next, replace the existing `deployment.toml` with this created `deployment.toml` file and restart the server.

2. **New config migration Failed, New Configs Found** : This tells that new config migration is not fully completed.
 This means this has found some more configs which has not found earlier and which are not available in the existing knowledge(`Catalog.csv`),
  
  This will create the 'deployment.toml' file using the 
existing knowledge found and add the other differences to the `outputCatalog.csv` (This contains the
changes in the `Catalog.csv` and newly found changes)

##### To complete the flow at the failure stage, you can do one of the following:

* You can update the `outputCatalog.csv` and update the `Catalog.csv` by committing the content to the
 identity-tools repo and re-run the tool again. This will give you complete success in the second attempt.
 
 or
 
 * Add the differences found in `outputCatalog.csv` manually to the `deployment.toml` 
 file and replace it with the existing file.
 Restart the server.
 