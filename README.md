# CornCompare

What is CornCompare?
--------------
CornCompare is a program that was written to compare the content of two Zea mays (Corn) databases, CornCyc and MaizeCyc. Both CornCyc and MaizeCyc were based on the B73 RefGen v2 gene model, but differences in their enzyme function prediction pipelines resulted in a large difference in the final databases.  CornCyc compares these two databases by computing the overlap in content for the major types of biological information stored in a BioCyc database: Genes, Proteins, Reactions, Compounds, and Pathways.

Installation and Requirements
--------------
CornCompare requires access to a running Pathway Tools server hosting both CornCyc and MaizeCyc.  There are three components necessary for using CornCompare.

1) Pathway Tools (http://bioinformatics.ai.sri.com/ptools/) is a bioinformatics software that allows for the browsing and manipulation of BioCyc database files.  Pathway Tools must be running on a Unix-like system and must be running in -api mode.

2) JavaCycO (https://github.com/DickersonLabHub/JavaCycO) is a library which facilitates communication with the Pathway Tools server using Java sockets.  JavaCycO must be installed on the same host server as Pathway Tools and must be set to actively listen for connections on port 4444.

3) CornCyc and MaizeCyc (http://www.maizegdb.org/metabolic_pathways/) database files must be installed in the local folder of the Pathway Tools server.  This will make them available to be queried through the Pathway Tools API using JavaCycO.

CornCompare itself is available as an executable java file (https://github.com/jrwalsh/CornCompare/releases/download/v0.3-beta/CornCompare_v0.3-beta.jar).  To use CornCompare, download the jar file.  Open a terminal and change directory to the location the jar file was downloaded to.  CornCompare takes two arguments: the IP address of the host machine which is running Pathway Tools and a path to a folder that CornCompare can write its output to.

Use
--------------
Example use:
````
  java -jar CornCompare_v0.3-beta.jar ip.of.ptools.host ~/CornCompare_output
````

Example Output
--------------
https://github.com/jrwalsh/CornCompare/files/300926/Results.of.Comparison.on.CornCyc.and.MaizeCyc.zip
