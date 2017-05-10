# lambda-runner
A simple command line tool for defining and running lambda pipelines in Java 8

## Features:
* Define a pipeline specifying method name and parameters, just like you would in a Java class
* Only supports `Stream`s of `String`s - only supports `Stream` methods returning `Stream` and lambda return type **must** be `String`

## Compiling:
Get dependencies:
* [protonpack](https://github.com/poetix/protonpack)
* [InMemoryJavaCompiler](https://github.com/trung/InMemoryJavaCompiler)

Then compile.

## Running:
    java -cp (...) it.sorintlab.lambda.Main [ options ]

Options are like:
    -<method name> { arguments }

Supported methods are:
* `distinct()`
* `filter(Predicate<String>)`
* `flatMap(Function<String, Stream<String>>)`
* `limit(long)`
* `map(Function<String, String>)`
* `peek(Consumer<String>)`
* `skip(long)`
* `sorted()`
* `sorted(Comparator<String>)`

Arguments have to be supplied just like you would in a standard invocation, enclosed in quotes as usual if necessary.
