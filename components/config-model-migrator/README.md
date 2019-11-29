# New Config Model Migration Tool

This tool will help to identify the configurations done in the migrated WSO2 Identity Server version 
which uses old xml config model compared to the default Identity Server which uses new config model. 

* It will create the `deployement.toml` file with the existing knowledge that identified before and 
committed in the `Catalog.csv` file hosted in resources folder of this tool.

* The rest of the newly identified changes will be recorded in a new `outputCatalog.csv` 
file inside the output folder.

* The not-templated, changed files will be filtered to a folder called `unTemplatedFiles` inside the 
output folder.

* The logs will be recorded in the `log.txt` file inside the output folder.

## How to run the tool

1. Build the `identity-tools` repository and get the `org.wso2.configuration.migrater-<version>-bundle.zip` 
file and unzip it.

2. Go to its bin and run the tool using below shell command.

       sh config-migrate.sh

3. This will asks for 2 inputs and it will generate the `deployment.toml` file using the 
existing knowledge in https://raw.githubusercontent.com/wso2/identity-tools/master/components/config-model-migrator/resources/Catalog.csv


##Inputs to the tool

1. **Migrated IS home** : The migrated/old Identity Server Home path.
2. **Default IS home** :  The default Identity Server Home path.

##Possible Outputs

1. **Complete Success** : This will provide the complete `deployment.toml` file which contain all the 
custom changes. You can directly use this to run the new Identity Server that uses new 
config model.

For that replace the existing `deployment.toml` with this created 
`deployment.toml` file and restart the server.

2. **Partial Success** : This will create the 'deployment.toml' file using the 
existing knowledge found and add the other differences to the `outputCatalog.csv` (This contains the
changes in the `Catalog.csv` and newly found changes)

#####To complete the flow you can do one of the following

* You can update the `outputCatalog.csv` and update the `Catalog.csv` by committing the content to the
 identity-tools repo and re-run the tool again. This will give you complete success in the second attempt.
 
 or
 
 * Add the differences found in `outputCatalog.csv` manually to the `deployment.toml` 
 file and replace it with the existing file.
 Restart the server.
 
 ---------------------End of the Tool ----------------------
 