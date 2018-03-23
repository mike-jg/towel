# The Towel Programming Language

[![Build Status](https://travis-ci.org/mike-jg/towel.svg?branch=master)](https://travis-ci.org/mike-jg/towel) [![Coverage Status](https://coveralls.io/repos/github/mike-jg/towel/badge.svg?branch=master)](https://coveralls.io/github/mike-jg/towel?branch=master)

Towel is a stack-oriented/concatenative interpreted programming language.

## Installation

`$ mvn package`

## Usage

Execute a source file:

`$  java -jar ./target/towel-LATEST.jar /path/to/source/file.twl`

Print the AST:

`$  java -jar ./target/towel-LATEST.jar /path/to/source/file.twl --print-ast`

View help:

`$  java -jar ./target/towel-LATEST.jar`

## Example program

The below program defines a function named 'palindrome', which tests a string to see if it is a palindrome.

It then runs this on two strings and prints the result to the console.

```
import reverse, lower from <strings>
import <io>
import dup from <stack>

def palindrome (str -> bool) {
    lower dup reverse ==
}

"level" palindrome io.println /* prints: true */
"nope" palindrome io.println  /* prints: false */
```

There are more example programs under `./example` and `./src/test/resources/integration-test`

## Primitive types and the stack

There are three primitive types in Towel: numbers, strings, and booleans. When a primitive is encountered it is pushed onto the top of the stack.

All functions and operators act upon the contents of the stack.

For example to add together two numbers:

``` 10 15 + ```

The above program pushes the numbers 10 and 15 onto the stack. The plus operator then pops the stack twice, and adds the two numbers together, pushing the result back onto the stack.

## Strings

Basic string formatting is provided via the `sformat` function in `strings`.

For example, the following will calculate the product of 5 and 5, then output the answer using the string formatter.

```
import <io>
import sformat from <strings>
import dup from <stack>

5 5 dup * "{1} x {1} = {0}" sformat io.print
```

The string formatter expects a number of parameters in the form {N}, N will be replaced by the value N positions from the top of the stack.

## Let

`let` can be used to declare a variable. Variables simply push a value onto the stack when mentioned.

```
5 let my5
my5 // pushes 5 onto the stack
```

`let`s are scoped statically meaning at runtime they will refer to their defining scope, not their runtime scope. e.g.

```
import print from <io>
import * from <sequences>
import * from <stack>

"wrong" let somevar

def sometest {
    "correct" let somevar
    { somevar } // this will always refer to the 'inner' declaration of 'somevar'
}

sometest exec print // prints 'correct'. With runtime scoping, this would print 'wrong'
```

## Arrays

Arrays can be defined as such:
```
[1, 2, 3, 4, 5] // create an array with some initial values
[] // create an empty array
```

As with primitives, arrays are pushed onto the stack when they are created.

There are several functions for working with arrays, such as `push`, `pop`, and `map`. `map` will execute a given sequence against each entry of an array.

## Sequences

A sequence is a list of instructions which instead of being executed, are pushed onto the stack for execution later.

```
{ 5 6 * } // a sequence which will calculate the product of 5 and 6
```

There are several functions for working with sequences: `repeat`, `curry`, and `exec`

`curry` will move something onto the beginning of a sequence, consider the following:

``` 10 { 5 + } curry ```

Which becomes:

``` { 10 5 + } ```

Sequences and `curry` can be used to emulate lambdas and closures.

The following example defines a function `create_multiplier` which will create a sequence to multiply by a given value. It then creates a multiplier and assigns it to a variable. Finally, it executes the sequence.


```
import curry, exec from <sequences>

def create_multiplier( num -> seq ) {
    { * } curry
}

6 create_multiplier let times6

5 times6 exec // 30
```

## Stack pre-conditions and post-conditions

Functions can be defined with pre and post condition checks.

Take the following example:

```
def mult ( num, num -> num ) {
    *
}
```

This defines a function which requires two numbers to be at the top of the stack when it is called. It also requires that a number be left at the top of the stack when it has returned.

Pushing a string onto the stack and calling this function would result in a stack-assertion error, as the stack is not in the correct state.

## Imports

Imports are all done via the `import` keyword, like so:

```
// import the whole namespace, accessible via somenamespace.identifier
import <somenamespace>

//import just 'afunc' from the namespace
import afunc <somenamespace>

//import just 'afunc' but rename it 'newname'
import afunc <somenamespace> as newname

// import everything, bypassing the need to qualify references with a namespace
import * from <somenamespace>
```

To import files within the same source directory, use the filename within a string literal:

```
import "myfile.twl"
```

To import files within a subdirectory, use `:` as a file separator like so:

```
import "subdir:myfile.twl"
// everything within myfile will be available within the 'myfile' namespace
```

The filename must end with `.twl`.

## Visibility modifiers

When importing a file, all function `def`s and `let`s marked `public` will be imported. The `public `keyword must be placed before the `def`/`let` keyword:

```
public def myfunctiondef {
    "this is public"
}

100 public let mylet

// this will not be visible outside the namespace
50 let privatelet
```

Everything else is considered private and will not be imported. There is no private keyword.

## To do

- ☐ Documentation (ongoing)
- ☐ More modularity between the components
- ☑ Multiline comments
- ☑ Boolean, Number, String, primitive data types
- ☑ User defined functions
- ☑ Sequences
- ☑ Conditionals (? operator)
- ☑ Single branch conditionals (?? operator)
- ☑ Iteration (sequences and repeat)
- ☑ Binary operators for arithmetic
- ☑ OR and AND operators
- ☑ Lambdas - kind of, can be emulated by using sequences and `exec`
- ☐ Standard library
    - ☐ Maths
        - ☑ `random` get a random number
        - ☑ `pi` get pi
        - ☑ `min` get the minimum number
        - ☑ `max` get the maximum number
        - ☑ `sqr` square
    - ☐ Sequence manipulation
        - ☑ `curry` prepend something onto a sequence
        - ☑ `exec` execute a sequence
        - ☑ `repeat` repeat a sequence a given number of times
    - ☐ Stack manipulation
        - ☑ `dup` duplicate top stack object
        - ☑ `dup2` duplicate the top two stack objects
        - ☑ `pop` discard the top stack object
        - ☑ `rotate` rotate the top three stack objects
        - ☑ `swap` swap the top two stack objects
    - ☐ File IO
    - ☐ Iteration
        - ☑ `repeat` using sequences
    - ☐ String manipulation
        - ☑ `reverse` reverse an input string
        - ☑ `lower` cast a string to lowercase
        - ☑ `sformat` simple string value interpolation
    - ☐ Input/output from console
        - ☑ output via `print` and `println`
        - ☑ input from console via `input_str` and `input_num`
    - ☐ Typecasting
- ☐ Complex data types, arrays, lists, dictionaries
    - ☑ arrays
        - ☑ `pop` `push` `length` `map` operations
- ☑ Importing standard library code
    - ☑ standard library can comprise Java code as well as Towel code, including within the same namespace
- ☑ Importing user-land code (needs work)
- ☑ Namespaces (implicit based on filename)
- ☑ Visibility modifiers (public, implicit private)
- ☐ Declare function arguments i.e. how the stack should be presented when the function is called
    - ☑ For built in functions
    - ☑ User-land functions
    - ☑ Allow typed arguments - `num` `bool` `str` `seq` `void`
    - ☑ Void for 'ignore'
    - ☑ Allow untyped
    - ☐ Function overloading based on arguments at runtime
- ☑ Declare function return i.e. how the stack will be left after calling the function
    - ☐ Overloaded functions must declare the same return type
- ☐ REPL
- ☑ Single line comments might be nice
- ☑ Variables via `let`
- ☐ Constants
- ☐ Optionally declared entry point
- ☐ Compilation to another language
