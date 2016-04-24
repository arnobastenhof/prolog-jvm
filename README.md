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
* Gradle 2.7+

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

Usage
-----
After building, a JAR file `prolog-jvm-${version}.jar` will have been placed in
`build/libs`, where `${version}` refers to the version number (e.g.,
`0.1.0-SNAPSHOT`). Running the following command from the project root prints
a help message:
```
java -jar build/libs/prolog-jvm-${version}.jar
```
To load a program, add the name of a Prolog file as a command line argument.
Several sample files have been included in
`src/test/resources/com/prolog/jvm/main/`. Thus, for example, one may run:
```
java -jar build/libs/prolog-jvm-${version}.jar src/test/resources/com/prolog/jvm/main/lists.pl
```

Language support
----------------
Prolog-JVM is not intended as a full implementation of the Prolog standard.
Rather, my motivations for writing it were self-educational, and I have
so far settled for a coverage of only the minimal core language in order to
concentrate more on the virtual machine. In particular, there is no support for
dynamic operators, Definite Clause Grammars or even for the usual syntactic
conveniences regarding lists. In addition, several restrictions apply to the
syntax of tokens as compared to Covington's "ISO Prolog: A Summary of the Draft
Proposed Standard" (1993):
* Graphic tokens are not allowed to begin with '.', '/' or ':'. This ensures
  we can make do with a single lookahead character. To compare, the proposed ISO
  standard only prohibited graphic tokens from beginning with '/*'.
* No support for '{}' as an atom.
* No support for arbitrary characters inside single quotes as an atom.
* No support for numbers or character strings.
* No reserved identifiers.
* Whitespace is always ignored. In particular, we do not prohibit it from
  occurring between a functor and its opening bracket, nor do we demand a
  period be followed by it.
