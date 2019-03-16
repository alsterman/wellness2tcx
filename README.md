# wellness2tcx

A Clojure program to convert convert MyWellness® json training data to Training Center XML.

The resulting tcx file can then be uploaded to other sites such as [stava.com](strava.com).

## Usage

### Getting the raw data from [mywellness.com](mywellness.com)
1. Open up Google Chrome.
2. Log in to [mywellness.com](mywellness.com) and navigate to the workout you want to convert.
3. Open the chrome developer tools and navigate to the network tab.
4. Refresh the page
5. Filter the requests by `Details?` 
![filtering requests](images/filter-requests.png)
6. Right click the last response and `Copy -> Copy respose`
![copy the response](images/copy-response.png)
7. The raw data is now on the clipboard.
8. Save it to a file.

### Converting the data
Running the jar
```
$ java -jar wellness2tcx-0.1.0-standalone.jar readfilename hh:mm:ss writefilename
```

Example
```
$ java -jar wellness2tcx-0.1.0-standalone.jar input-viktor.json 12:00:00 output-viktor.tcx
```
## License

Copyright © 2019 Marcus Alsterman