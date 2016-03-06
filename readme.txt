Readme
The software provided under this package performs data loading, analysis, and report generation
using multiple data sources. Summary files from Census ACS, associated geographies, and mortgage data source from consumer complaints data source were used as base data. Subsets of these data sources are extracted for loading and analysis. Margin of error data set is not used for this analysis

The software uses sqlite for database. The library jar used for sqlite has the needed
libraries to support sqlite on any host. So no sqlite is needed on the host in order to run this application. The software is written in java. Minimum version required is java 1.7.
The software runs on any host that has a java runtime of 1.7 or higher. The reports are generated in html format. An html 5.0 compatible browser is recommended.

Source code, sample DB, sample reports, data extracts used for loading, and an executable jar are provided with the package. The software generates summary reports by state, by year,or by a zip code prefix. Zip code reports provide income and population estimates. 

To run the software, pull down the zip archive from the repository. Unzip the archive, navigate to the top level directory, cfProj, and the run the following commands.

Generate a summary level report by state:

java -Dsummary=state -jar dist/cfProj.jar

Generate a summary report by year:

java -Dsummary=yearly -jar dist/cfProj.jar

Generate a zip code-median income summary report using a zip code prefix. In this example, all zip codes that have a 20 prefix are aggregated

java -Dsummary=zipin -Dzip=20 -jar dist/cfProj.jar

Generate a zip code-population summary report using a zip code prefix. In this example, all zip codes that have a 20 prefix are aggregated

java -Dsummary=zipop -Dzip=20 -jar dist/cfProj.jar


