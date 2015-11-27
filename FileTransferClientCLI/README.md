# Introduction

 This project is designed for the user who like using *ubuntu* command line to communicate with the server.

## Environment
 + NetBean
 + Java 8 oracle
 + MongoDB 

 After you prepare all things above, just download this project or use the git clone it to your local disk.Do not forget to download the server end program along with it.

## How to ?

 1. open two terminals
 2. run the jar file under /projectdir/dist/FileTransferServer.jar
 3. run the FileTransferClient.jar in the other terminal. 

# Description

 While as we know, there are several differences between the CLI and the Swing version.
Here we just talk about the CLI client.

 The CLI version is designed to transfer the content in the .xls or .xlsx file to the server, then the server program will save the data in the MongoDB database.

 look at the sample table from the sync .xls file:

|name  |sex  |age  |salary |location |intrest  |self-intro |picture  |
|:----:|:-----:|:--:|:----:|:-------:|:-------:|:---------:|:-------:|
|John  |male   |23  |2000  |America  |soccer   |John.txt   |John.jpg |
|Lucy  |female |20  |3500  |Englend  |piano    |Lucy.txt   |Lucy.jpg |

 As for the name/sex/age/salary/location/intrest will save in the MongoDB as Mongo Objects, and the files the person contains will be sliced and indexed in the MongoDB, you can use **mongofile** command to get the file out from the database.

 Before you get the person file, you should look at the source code and find the database and collections where the file saved and indexed.

 Every time you want to transfer the data to the database, you just add the items in the .xls or .xlsx file in the /FileTransferClientCLI/FileList directory, and place the .txt and .jpg file in the /FileTransferClientCLI/FileDirectory directory.Finally just start the server first and start the client, the file will automatically transferred to the server, you can open several client at the same time because the server keep multi-thread work.

# Run Details

 I. in a terminal find the server jar file, type
  
    java -jar FileTransferServerCLI.jar  port [multi-client-mode]
    
    # the port is mandated, you can't ignore them.
    # the multi client flag is optional, if you want to use multi client just set the 
    # flag with true, like:
        java -jar .jar 8888 true
        
 II. in another terminal find the client and run the jar file,

    java -jar FileTransferClientCLI.jar ip port 
    
    # here the ip and port number is mandated.
    # the ip is the server ip when you open your server the ip will show in the window
    # Actually if you run two program in the same machine, the ip is same with your 
    # local machine.

# Tips

If you want to use this code in your own project please keep the author in your code.**No manners, No success**.

# Contact

 1. Email: 294101042@qq.com
 2. QQ: 294101042

