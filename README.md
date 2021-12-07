# Benchmark
CityBench is a java-based benchmarking toolset for RSP engines, currently CQELS, C-SPARQL, RDFox, C-SPARQL2.0, and YASPER (see https://github.com/NathanGr01-Bachelorarbeit/CityBench-C-SPARQL2-YASPER/) are supported.

## Prerequisite
* JVM 1.7
* Webserver of your choice (Apache, JBoss,Tomcat etc.)
* Java IDE (for debugging and extensions)

## Folders & Files
1. *CQELS_DB*: necessary for CQELS to work;
2. *cqels_query*: sample queries in CQELS syntax;
3. *csparql_query*: sample queries in C-SPARQL syntax;
5. *dataset*: background knowledge base, mostly sensor service repositories, WebGlCity is a folder that can instantly be used under an Apache server (for YASPER experiments);
5. *experiment_scripts*: execution scripts for scalability experiments with RDFox and C-SPARQL;
6. *ontology*: ontologies used;
7. *rdfox_query*: sample queries in RDFox syntax (window information + SPARQL query);
8. *result_log*: output files generated by CityBench, e.g., query latency, result count, memory consumption, number of observed unique ObIds, completeness, estimated throughput;
9. *src*: source code;
10. *lib*: libraries used;
11. *streams*: sensor observation raw data in .csv formats, used to generate RDF streams;
12. *EC-log*: logger file output;
13. *citybench.properties*: configuration file loaded by CityBench;
14. *start.sh*: central execution for scalability experiments.

## To run
1. Download all resources and source code
2. Provide 'CQELS_DB' folder if you want to run CQELS experiments
3. Provide static data for queries (e.g. via Apache Web Server)
4. Run ISWC2015-CityBench.jar with suitable arguments **OR**
5. Import the project to your Java IDE and integrate all libraries (provided as jars in the li-folder) as dependencies  
6. Run CityBench.java

## Configuration file
* dataset = dataset/[your_sensor_repository_file]  // tell CityBench where to look for static background knowledge.
* ontology = [your_ontology_folder] // tell CityBench where to look for ontologies used.
* streams = [your_streams_folder] // tell CityBench where to look for raw data to simulate sensor streams.
* cqels_query = [your_cqels_queries_folder] // tell CityBench where to look for cqels queries.
* csparql_query = [your_csparql_queries_folder] // tell CityBench where to look for csparql queries.
* rdfox_query = [your_rdfox_queries_folder] // tell CityBench where to look for RDFox queries.

// All paths are relative path to the project root

## Program Parameters
**Acceptable params:**      
* _rate_ = (double)x, // sensor stream acceleration rate (based on real world sensor observation intervals)
* _frequency_ = (double)c.  // fixed frequency for sensors, only has effects when rate=1.0
* _queryDuplicates_ = (int)y, // number of duplicates to run concurrently
* _duration_ = (long)z,  // duration of the experiment in milliseconds
* _startDate_ = (date in the format of "yyyy-MM-dd'T'HH:mm:ss")a, // start time of the sensor observations used
* _endDate_ = (date in the format of "yyyy-MM-dd'T'HH:mm:ss")b,  // ending time of the sensor observations used
* _engine_ = (String)"cqels" or "csparql" or "rdfox" // engine to test
* _query_ = (String)q // file name of the query to run (under cqels_query, csparql_query, or rdfox_query)
* _rdfoxLicense_ = (String)path // path to RDFox license-file
* _queryInterval_ = (int)i // tick interval for RDFox SR extension

engine, start and end dates are mandatory.
rdfoxLicense is mandatory if RDFox should be tested.

**Example for commandline execution:** java -jar CityBench.jar engine=rdfox startDate=2014-08-11T11:00:00 endDate=2014-08-31T11:00:00 query=Q1.txt duration=600s queryDuplicates=1 frequency=2 rdfoxLicense=./<RDFoxLicenseFile> queryInterval=15

## Providing static data
For some queries, C-SPARQL and RDFox load static knowledge from URLs like <http://127.0.0.1/WebGlCity/RDF/SensorRepository.rdf>.
For the experiments to work, you need to provide the static data at this URL. You can use the folder dataset/WebGICity/ to provide the necessary files e.g. within an Apache server.
