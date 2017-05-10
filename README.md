# lambda-runner
A simple command line tool for defining and running lambda pipelines in Java 8

## Features:
* Define a pipeline specifying method name and parameters, just like you would in a Java class
* Only supports `Stream`s of `String`s

## Compiling:
Get dependencies:
* [protonpack](https://github.com/poetix/protonpack)
* [InMemoryJavaCompiler](https://github.com/trung/InMemoryJavaCompiler)
Then compile.

## Running:
    java -cp (...) it.sorintlab.lambda.Main --options--

    
