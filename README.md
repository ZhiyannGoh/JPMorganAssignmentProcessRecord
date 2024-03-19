### To run the the application locally
1. Pull this repository to local
2. Run the ProcessorApplication.java
3. Execute this `curl` command
   
   ```
   curl --location 'http://localhost:8080/upload-and-process-file' \
   --form 'file=@"/path/to/file"'
   ```

### Future work
1. Make this upload call async
   - Since is Async, we can have more endpoints to check on the status of the processing
