# Introduction

This project give a really nice client GUI, it's very easy to use them in your local computer whatever your platform, ubuntu, windows, mac.

>## Environment
> + NetBean
> + Java 8 Oracle
> + MongoDB 3.0 above

>## How to Run?

> 1. run the sever jar file.
> 2. run the client jar file.
> 3. connect the client to server.
> 4. add files, start to transfer.

# Description
![Alt text](http://img.blog.csdn.net/20150911152605891)

As you can see above, the file transfer client GUI is consisted of five part:

+ add file pane.
+ transfer file table.
+ server setting.
+ system log
+ server status.
I think there is noting more I can tell you than the easy using interface.

Here I just want to talk something very special for you, this program will auto detect the file in the FileDirectory(this may be in FileTransferClient/), you do not need to write the .xls or .xlsx again, you can do it but you do not need to do that. You can add file through:

-	GUI interface
-	Modify the .xls or .xlsx file
-	Copy the file into the FileDirectory

The program using a watch service to watch the .xls, .xlsx file and the transfer directory.

***Is it Amazing?***

As for the server, just use it as the CLI version one.

# Tips

If you want to use this code in your own program, please keep the author in your code.**No manners, No success**.

# Contact

1. Email: 294101042@qq.com
2. QQ: 294101042
