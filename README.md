<p align="center">
<a>
<img  src="https://img.shields.io/github/license/NathanCheshire/Cyder?color=26A8FF&style=for-the-badge"/>
</a>
<a>
<img  src="https://img.shields.io/github/issues/NathanCheshire/Cyder?color=26A8FF&style=for-the-badge"/>
</a>
<a>
<img  src="https://img.shields.io/github/issues-closed/NathanCheshire/Cyder?color=26A8FF&style=for-the-badge"/>
</a>
<a>
<img  src="https://img.shields.io/github/repo-size/NathanCheshire/Cyder?color=26A8FF&style=for-the-badge"/>
</a>
</p>

# Cyder - A Programmer's Swiss Army Knife

## Description

Cyder is a multipurpose GUI tool written using a custom UI library 
built on top of Swing/AWT without other modern UI dependencies such as FXML.
It can perform countless operations such as downloading audio from a
youtube video, resizing/cropping an image, displaying the weather, 
calculating an expression such as `sin(e^pi - 14^(1/2))`, and even includes
visualizations for algorithms such as A*, Dijkstra's, Graham Scan for 
the Convex Hull problem, and Conway's Game of Life (Rest in Peace John Conway).

## Screenshots

![Insomnia Cyder](https://user-images.githubusercontent.com/60986919/155893528-31fcbfae-6753-4b0c-ba82-4a450f7049b2.png)

![Weather](https://user-images.githubusercontent.com/60986919/154597156-a972b79c-5c13-4c31-b8a6-aae117557e70.png)

![Audio Player](https://user-images.githubusercontent.com/60986919/156295222-ec6a1644-928c-456b-8cc0-82ce7c73365f.png)

### Pathfinder

Cyder comes with a path finding visualizer for Dijkstra's algorithm
and the famous A* algorithm with Euclidean and Manhattan distances
as available heuristics.

https://user-images.githubusercontent.com/60986919/154868578-133588ec-5af9-4f40-9f75-550ff85febec.mp4

### Self Analyzation and Reflection

Cyder can analyze many things about itself 
such as its own Java code, comments, and files, 
your computer's properties, hardware components, 
the status of certain websites, the JVM, and even 
its own issues on GitHub. Cyder even features a custom 
`@Widget` annotation which is used to mark Cyder widgets which
can then be validated and found upon runtime.

![Self Analyzing](https://user-images.githubusercontent.com/60986919/156295692-c2b9f0ae-7afe-4ec1-90d4-d81434f5b6d8.png)


## Usage

Since this is a Gradle project, you can simply clone this project 
via http or ssh, run the gradle setup task using your favorite IDE
(Eclipse, IntelliJ, NetBeans, etc.), and then execute the main function
inside of `src/cyder/genesis/Cyder.java`. Keep in mind that the intended
SDK is Java 9.0.4 and the current language level is the corresonding SDK
default of `9 - Modules, private methods in interfaces etc.`

## History

Cyder started as a way to test and improve my Java skills and have fun
while taking AP Computer Science back in in 2017. When I reached the
university level of classes, they provided no help towards my passions
regarding Java since my university did not teach Java at all. As I learned
about better programming practices, software architecture, security, API
documentation, and so many more topics, Cyder continued to grow and improve
to the point it is at today which I am quite proud of. I know ideally Cyder
should be converted to an electron-js project but I doubt that will
ever happen as I've enjoyed the process of creating the custom UI library.


## Disclaimers

Currently, the program is intended for high resolution displays,
namely displays greater than or equal to 2560x1440 (my main display).
I plan to attempt to fix this using SVGs, other vector graphic topics,
custom layouts, etc. Currently, seeing as this message is still here, 
the program will look and operate best using a 2560x1440 display 
or one with a higher DPI/resolution.

## Features

Cyder utilizes an Agile development model. For features you would like
to see implmeneted, please create an issue and describe the
feature in as much detail as you can. Issues are addressed by 
assigned priority based on issue create data, idea originality, 
and idea relevance.

## Deveopment Model: Agile
<img src="https://i.imgur.com/VKeVG4F.png" data-canonical-src="https://i.imgur.com/VKeVG4F.png"/>
