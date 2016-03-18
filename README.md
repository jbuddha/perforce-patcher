# Perforce Patcher

The perforce-patcher is an opensource utility developed in JavaFx to quickly create patch/diff files for perforce pending changelists.

## Dependencies: None

## Setup
If you just want to use the utility download the jar file in dist folder and it. You Need JRE 8 to run this without any additional configuration. If you are using JRE 7, please keep jfxrt.jar in class path or simply copy it to your jre/lib/ext folder. jfxrt.jar is available inside your {jre or jdk}/lib folder.

## Features
Generates Patch files for Perforce Pending changelists

## Usage
Upon opening the application you will be presented with the login screen.
![Login Screen](https://farm2.staticflickr.com/1626/25596793045_91c403dfd9_z.jpg)

Login with your regular perforce credentials, Deselect Remember me, if you don't want to store the data entered by you locally. By leaving the default value your credentials will be saved in the application cache so that you need not enter it again.If the login is successful, you will be taken to the next screen otherwise check the Log pane for error.
![Generate Patch](https://farm2.staticflickr.com/1501/25229331639_f3324f01b4_z.jpg)

Select the local perforce workspace name and the change list id and click Generate Patch button and save the file at your preferred location through the Save dialog which pops up.

You will be logged out upon closing the application automatically.

It is also possible to launch the application with flags to pre-populate the P4 Server and Username fields.    
For example:   
`java -jar perforce-patcher.jar -p workshop.perforce.com:1666 -u guest`


Go To this page for more detailed explanation: [Blog Post](http://controlspace.info/2016/03/08/patch-utility-for-perforce/)
