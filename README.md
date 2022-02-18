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

# Cyder - A Programmers Swiss Army Knife

## Definition

Cyder is a multipurpose tool with it's own custom UI library built without modern UI utilities such as FXML. It can perform countless operations such as downloading audio from a youtube video, resizing an image, displaying the weather, calculating an expression such as `sin(e^pi - 14^(1/2))` and visualizing algorithms such as A*, convex hull, and Conways Game of Life.

## Background

Cyder started as a way to test my basic Java skills and have fun while taking AP Computer Science in 2017. As I continued to grow, work piled up and I had college and internships. Recently, however, I have begun to work on it in my free time as a hobby. I know so much more now than when I started and hope to keep adding features for quite some time. Ideally I should recreate this in electron-js but I doubt I'll ever get around it since I use Java for this project as a challenge since I've also built my own GUI library.

The best way I can describe Cyder is a multipurpose tool that launches all of the Java projects I have done, are currently working on, or will do. I know certain ways I do things are not great and PLEASE do not comment about me building a Java LAF from scratch essentailly and not using JavaFX. I am aware of this (I should hope I know since I'm the developer behind Cyder) and consider it a fun challenge to not work with modern UI depdencies.

tl;dr Cyder is a virtual assistant and chatbot that performs tasks you might need on a PC.

## Screenshots

![Insomnia Cyder _2022-02-17](https://user-images.githubusercontent.com/60986919/154597225-75b1ae4f-6382-4a4a-a4c7-4d07819d8f03.png)

![Dallas' weather_2022-02-17](https://user-images.githubusercontent.com/60986919/154597156-a972b79c-5c13-4c31-b8a6-aae117557e70.png)

![Twillzzz - Lord_2022-02-17](https://user-images.githubusercontent.com/60986919/154597231-7a5e4e68-585c-45d7-b18d-503d65fd04c5.png)

### Pathfinder

Cyder comes with a path finding visualizer for Dijkstra's and A* with Euclidean and Manhattan distances as heuristics.

<img src="https://i.imgur.com/cRlfyQR.png" data-canonical-src="https://i.imgur.com/cRlfyQR.png" width=75% height=75%/>

### Self Analyzer

Cyder can analyze many things about itself such as its own Java code, comments, and files, your computer's properties, hardware components, the status of certain websites, the JVM, and even its own issues on GitHub.

![Screenshot 2022-02-17 220731](https://user-images.githubusercontent.com/60986919/154615732-c1d9adc9-0e01-4f5d-be70-7e4c22a70631.png)

## Usage

Since this is a Gradle project, you can simply fork this project via it's GitHub url and run the gradle tasks using your favorite IDE (Eclipse, IntelliJ, NetBeans, etc.).

## Disclaimers

Currently, the program is intended for high resolution displays, namely displays greater than or equal to 2560x1440 (my main display). I plan to fix this using SVG and other vector based graphics approaches in the future but currently, seeing as this message is still here, the program will look and operate best using a 2560x1440 display or one with a higher DPI.

## Contributing

Cyder utilizes an Agile development model. For features you would like to see implmeneted, please create an issue and describe the feature in as much detail as you can. Issues are addressed by assigned priority based on issue create data, idea originality, and idea relevance.

## Deveopment Model: Agile
<img src="https://i.imgur.com/VKeVG4F.png" data-canonical-src="https://i.imgur.com/VKeVG4F.png"/>
