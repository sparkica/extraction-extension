OpenRefine Extraction extension
=====================
To use the Extraction extension you'll need an service that takes url and XPath as an input and returns extracted value(s) of HTML element defined with XPath. Extension creates a new column(s) for each provided XPath.

Extraction extension works (best) with sparkica/simex-service.

## Pre-installation
- Update SERVICEBASEURL in com.brainymachine.extraction.services.SparkExtract with the url of *your* service url. Currently it is set to localhost and it is assumed that you're using sparkica/simex-service.

## Installation

- Compile the source code to the module directory.
- Copy the module folder to your extensions folder.
To find your extensions folder, choose Browse workspace directory from the OpenRefine interface, and navigate to the folder extensions (create it if it doesn't exist).
- Start or restart OpenRefine.

## Usage
- Configure extraction settings by clicking on Extraction in the extension bar and set extraction parameters: XPath, attribute (optional) and column name (optional). If the name of the column is not set, service name will be used instead (defaulting to Custom _x_) and update settings.
- Click on a column head (little triangle left of column name), select Extract elements...
- Select which elements to extract and Start extraction.

