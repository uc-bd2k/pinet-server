# pinet-server
http://pinet-server.org 

### To compile and run
A docker container is provided in the docker folder to compile and run pinet on a local server.


### Three main comments:

##### pinet reads indexed protein files from this folder:
 
Currently indexed peptide files for fast queries are expected under `PEPTIDE_DATA_BASE_DIR` (default `/opt/raid10/genomics/behrouz/PeptideMatchCMD_src_1.0`) and `PEPTIDE_INDEX_BASE_DIR` (default `$PEPTIDE_DATA_BASE_DIR/jan-30-2019`).

Please contact pinet support to receive the indexed peptide files and put 
them in the appropriate local folder.
