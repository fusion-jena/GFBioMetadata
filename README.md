# GFBioMetadata

This repository provides a Java program to download metadata files from the [GFBio](https://www.gfbio.org) project.

## Installation

Prerequistes: Java 11 and Maven > 3.8 (it has been developed with [AdoptOpenJDK11](https://adoptopenjdk.net/) and Maven 3.8.3)

from the root folder run ```mvn install``` 

start the program as Java Application or execute it as jar. Open a command line, navigate to the root folder/target and type

```java -jar GFBioDatasets-0.0.1-SNAPSHOT-jar-with-dependencies.jar``` 

The results are XML metadata files. The default output folder is ```C:\tmp\output```

## Configuration

Configure the output folder, the search URLs and the maximum calls in the config file: /src/main/resources/config.properties

Configure the query (e.g., to filter for specific datasets) in the query file: /src/main/resources/query.json

In the query you can also specify how many results per call you want to retrieve.


## Changelog
01.12.2021 0.1 initial release

## License
This software is distributed under the terms of the GNU LGPL v3.0. (https://www.gnu.org/licenses/lgpl-3.0.en.html) 

