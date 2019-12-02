# New Config Model Migration Tool

## Why to use this tool?
This tool will help to identify and migrate the old xml configurations to the [new configuration model](https://is.docs.wso2.com/en/5.9.0/references/new-configuration-model/) 
 (that comes with Identity Server 5.9.0 or above) and create the deployment.toml file which suites the new config model. 
 
 This tool will use the pre-identified toml configurations that is recorded in `catalog.csv` file. This `catalog.csv` file
 will be frequently updated with the new configs which will found by the tool itself.
 
## What this tool gives?

* Updated `deployement.toml` file based on the pre-identified toml configs in `catalog.csv` file hosted in resources folder of this tool.

* `catalog.csv` file will be the knowledge to this tool. Hence if new configs
 found without toml configurations in `output-catalog.csv`, `catalog.csv` file should be updated with the changes.
 
* The content of the `catalog.csv` file and newly found configs will be added to a new file called `output-catalog.csv` file inside the `output` folder.
  
* The untemplated, changed files will be filtered to a folder called `unTemplatedFiles` inside the 
output folder.

* The logs will be recorded in the `log.txt` file inside the output folder.

## How To Run The Tool

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
existing [knowledge](https://raw.githubusercontent.com/wso2/identity-tools/master/components/config-model-migrator/resources/catalog.csv)


## Inputs To The Tool

1. **Migrated IS home** : The migrated Identity Server (Identity Server 5.8.0 or below)  Home path.
2. **Default IS home** :  The default Identity Server (Identity Server 5.9.0 or above)  Home path.

## Possible Outputs

1. **Complete Success** : This will provide the complete `deployment.toml` file which contain all the 
custom changes. You can directly use this to run the new Identity Server that uses new 
config model.

Next, replace the existing `deployment.toml` with this created `deployment.toml` file and restart the server.

2. **Partial Success** : This tells that new config migration is not fully completed.
 This means this has found some more configs which has not found earlier and which are not available in the existing knowledge(`catalog.csv`),
  
  This will create the `deployment.toml` file using the 
existing knowledge found and add the other differences to the `output-catalog.csv` (This contains the
changes in the `catalog.csv` and newly found changes)

##### To complete the flow at the failure stage, you can do one of the following:

* You can update the `output-catalog.csv` and update the `catalog.csv` by committing the content to the
 identity-tools repo and re-run the tool again. This will give you complete success in the second attempt.
 
**Note** : 
  If you can not create a [pull request](https://github.com/wso2/identity-tools/pulls), create a [git issue](https://github.com/wso2/identity-tools/issues) with the identified changes so that we can update the `catalog.csv`
 
 or
 
 * Add the differences found in `output-catalog.csv` manually to the `deployment.toml` 
 file and replace it with the existing file.
 Restart the server.
 
  ---
 ###### To Improve This Tool:
 
 We really appreciate your feedback and your user experience about this tool. Create a 
 [git issue](https://github.com/wso2/identity-tools/issues) and let us know your feedback.