Prolog-JVM
==========

Introduction
------------
Prolog-JVM is a simple Prolog interpreter written in Java. It combines a
bytecode compiler and a virtual machine grounded in the stack-based ZIP
architecture described by Bowen, Byrd and Clocksin in "A portable Prolog
compiler" (1983), and by Clocksin in "Design and simulation of a sequential
Prolog machine" (1985). I wrote Prolog-JVM mainly for self-educational purposes.

Requirements
------------
* Java SE7
* Gradle 2.7 or higher

Installation
------------
If you have Git installed, executing the following command will create a
directory `prolog-jvm/`, clone the entire repository therein and checkout the
master branch:
```
git clone https://github.com/arnobastenhof/prolog-jvm.git
``` 
Alternatively, Github offers the possibility to download a ZIP file containing
the sources from whichever branch is being viewed. E.g., to download the
master branch, run
```
wget https://github.com/arnobastenhof/prolog-jvm/archive/master.zip
unzip master.zip
mv prolog-jvm-master prolog-jvm
```
Next, navigate to the project root and run a build:
```
cd prolog-jvm
gradle build
```

Usage instructions
------------------
After building, a JAR file `prolog-jvm-${version}.jar` will have been placed in
`build/libs`, where `${version}` parameterizes over the version number (e.g.,
`0.1.0-SNAPSHOT`). Running the following command from the project root prints
a help message:
```
java -jar build/libs/prolog-jvm-${version}.jar
```
To load a program, add the name of a Prolog file as a command line argument.
Several sample files have been included in
`src/test/resources/com/prolog/jvm/main/`. Thus, for example, one may run:
```
java -jar build/libs/prolog-jvm-${version}.jar
src/test/resources/com/prolog/jvm/main/lists.pl
```
