# wellness2tcx

A Clojure program to convert MyWellness® json training data to Training Center XML.

The resulting tcx file can then be uploaded to other sites such as [stava.com](strava.com).

## New alternative!!
I've developed a webversion that converts the raw data in the browser.
[https://alsterman.github.io/wellness2tcx-cljs/](https://alsterman.github.io/wellness2tcx-cljs/)
The code runs in your browser and the data never leaves your machine.

The source code is available at https://github.com/alsterman/wellness2tcx-cljs

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

### Downloading the program
The latest jar file can be downloaded here:
[wellness2tcx-0.1.1-standalone.jar](https://github.com/alsterman/wellness2tcx/releases/download/v0.1.1/wellness2tcx-0.1.1-standalone.jar)
Place the downloaded file in the same directory as the raw data file.

Make sure you have Java 8 installed.


### Converting the data
1. Open a terminal and move to the directory where you placed the downloaded the jar file and raw data file.
2. Run the program with the following command. Replace `readfilename` with the name of the saved file, `hh:mm:ss` with the time you started the exercise and `writefilename` with the desired save filename, ending with .tcx 
```
$ java -jar wellness2tcx-0.1.0-standalone.jar readfilename hh:mm:ss writefilename
```

Example
```
$ java -jar wellness2tcx-0.1.0-standalone.jar input-viktor.json 12:00:00 output-viktor.tcx
```

The resulting file can be found in the folder and can be uploaded to another site such as Strava.
## License

Copyright © 2019 Marcus Alsterman
