
![](./static/svgs/Primary.svg)

![](./static/svgs/Secondary.svg)

[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://forthebadge.com)

## Description

Cyder is a multipurpose GUI tool written using a custom UI library 
which was built on top of Swing/AWT without modern UI dependencies such as FXML.
It can perform countless operations such as downloading audio from a
youtube video, resizing/cropping an image, displaying the weather, 
calculating an expression such as `sin(e^pi - 14^(1/2))`, and even includes
visualizations for algorithms such as A*, Dijkstra's, Graham Scan for 
the Convex Hull problem, and Conway's Game of Life (Rest in peace John Conway).

## Screenshots

![Liminal Cyder](https://user-images.githubusercontent.com/60986919/163657071-17f9866c-bf2b-4307-9c8f-cf7e97982af9.png)

https://user-images.githubusercontent.com/60986919/161394292-cb7bf459-d405-4efc-8ea1-33a127036a69.mp4

![Weather](https://user-images.githubusercontent.com/60986919/156911464-73221df7-68fa-4ce3-8211-555ddc9c0ac2.png)

![Audio Player](https://user-images.githubusercontent.com/60986919/156904205-39fb8218-412e-4a20-9a27-7d2d7bc39902.png)

![Kirby](https://user-images.githubusercontent.com/60986919/158036314-055f87d4-b21c-4eec-a92c-d65561c75483.png)

![PaintControls](https://user-images.githubusercontent.com/60986919/158036316-0abe20d4-3414-40e9-8da5-5ec83430d54d.png)

https://user-images.githubusercontent.com/60986919/160253262-8b10844a-6385-4328-ab84-961d05777526.mp4

### Self Analyzing and Reflection

Cyder can analyze many things about itself 
such as its own Java code, comments, and files, 
your computer's properties, hardware components, 
the status of certain websites, the JVM, and even 
its own issues on GitHub. Cyder even features a custom 
`@Widget` annotation which is used to mark Cyder widgets which
can then be validated and found upon runtime.

![Self Analyze](https://user-images.githubusercontent.com/60986919/160317468-6df0680f-8d49-413b-a09e-43d38839d441.png)

## Usage

Since this is a Gradle project, you can simply clone this project 
via http or ssh, run the gradle setup task using your favorite IDE
(Eclipse, IntelliJ, NetBeans, etc.), and then execute the main function
inside of `src/cyder/genesis/Cyder.java`. Keep in mind that the minimum
SDK is Java 9.0.4 and the coresponding language level the SDK
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
ever happen as I've enjoyed the process of creating the custom GUI library.

## Disclaimers

Currently, the program is intended for high resolution displays,
namely displays greater than or equal to 2560x1440 (my main display).
I plan to attempt to fix this using SVGs, other vector graphic topics,
custom layouts, etc. Currently, seeing as this message is still here, 
the program will look and operate best using a 2560x1440 display 
or one with a higher DPI/resolution.

## Development Agenda

Cyder utilizes an Agile development model. For features you would like
to see implmeneted, please create an issue and describe the
feature in as much detail as you can. Issues are addressed by 
assigned priority based on issue create data, idea originality, 
and idea relevance.

## Deveopment Model: Agile
<img src="https://i.imgur.com/VKeVG4F.png" data-canonical-src="https://i.imgur.com/VKeVG4F.png"/>
