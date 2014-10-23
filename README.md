Reorganizer
===========

Reorganize BioAnalyzer sample PDFs 

ReorganizeRunFiles -- v1.6
Tested on Windows and Mac.

======================================

This program reads PDF file saved from Bioanalyzer Expert 2100 software.
It creates directories with BID names and Pool IDs in the format Pxxxx where x is a number.
It splits PDF file saved from Expert 2100 into multiple PDF files, one for each sample and uploads to http://dmel.uchospitals.edu/~hgac/.
It reads the most recently modified PDF file in the current directory.
It creates a log file with messages and errors.
A table of samples and date on which these files were uploaded to dmel is available at
http://dmel.uchospitals.edu/~hgac/tracksamples.cgi
On a sample re-run, a PDF file with current time stamp is saved in the corresponding sample directory. Existing sample files from previous runs in the directory are not modified or deleted. 
Source code for this program is uploaded to GitHub

======================================
How to Run this program:

1.Make a config file by name "FTPcredentials.config" with the following content in the same directory as jar file:

server = xx.xx.xxx
username = xxx
password = xxx

2.Double click ReorganizeRunFilesv1.6.jar

Note: With out FTPcredentials.config, program makes samples pdfs in local directory but can not upload them to server.

======================================

Input:

PDF file from Bioanalyzer Expert 2100 software, saved as 1 sample per page.

======================================

Output:

Directories with BID names are created.

One PDF file for each sample consisting of Electrophoresis Assay Details page, Electropherogram Summary and Overall Results for Ladder page and Overall Results for sample page, is saved in each directory.

These sample files in PDF format are uploaded to dmel.uchospitals.edu

Log file that logs messages, list of BIDs uploaded and errors.

======================================
Updated by Padma Akella 10/2014.
For bugs or questions, please email 
pmakella  at  uchicago  dot  edu 
