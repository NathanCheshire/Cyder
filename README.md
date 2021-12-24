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

### Console with AudioPlayer open

Cyder can download any youtube video. Along with the video, the thumbnail is downloaded to be used as album art for both the OS' taskbar and Cyder's taskbar.

<img src="https://i.imgur.com/eUEL4R3.png" data-canonical-src="https://i.imgur.com/eUEL4R3.png"/>

### Pathfinder

Cyder comes with a path finding visualizer for Dijkstra's and A* with Euclidean and Manhattan distances as heuristics.

<img src="https://i.imgur.com/cRlfyQR.png" data-canonical-src="https://i.imgur.com/cRlfyQR.png"/>

### Self Analyzer

Cyder can analyze many things about itself such as its own Java code, comments, and files, your computer's properties, hardware components, the status of certain websites, the JVM, and even its own issues on GitHub.

<img src="https://i.imgur.com/afWk4Qf.png" data-canonical-src="https://i.imgur.com/afWk4Qf.png"/>

## Usage

Since this is a Gradle project, you can simply fork this project via it's GitHub url and run it using your favorite Java IDE (Eclipse, IntelliJ, NetBeans, etc.). If you don't download an official release/build and instead fork the project, you'll need to change a couple properties to allow Cyder to start. First, you'll need to change the released key/value pair in Sys.json. Second, you'll need to make sure that you're running either Windows or a Linux distribution as your top-level operating system. Cyder is currently not intended for OS X based systems and will exit upon an attempted launch should a user attempt to start Cyder on an OS X system.

## Pull Requests

After forking the project and contributing in any way such as making your own widget (see: https://github.com/NathanCheshire/Cyder/wiki), you can submit a PR after you have considered ALL the following points:

* Have I used already existing methods from the utilities package?
* Have I added Javadoc comments before any methods I created as well as @param and @return annotations?
* Have I commented any code that could potentially be confusing (take note of what a magic number is: https://en.wikipedia.org/wiki/Magic_number_(programming))?
* Have I tested my modifications properly and thoroughly? 

Additionally, the PR should contain the following information:

* A description of each file added/modified/removed and why the modification was performed. Walk me and other reviewers through your thought process for each of these changes. Keep it brief and if further information is required, reviewers will request more information before merging the PR. Try to check the state of your un-merged PR at least bi-weekly so that the modifications you made can be merged in ASAP.
* A brief summary of your PR and how it affects Cyder. This should differ from the first in that the first is for developers whilst this is simply a summary for users/enthusiasts to monitor Cyder's progress.

## Disclaimers

Currently, the program is intended for 2560x1440 resolution displays (my main display). I plan to fix this using SVG and other vector based graphics approaches in the future but currently, seeing as this message is still here, the program will look and operate best using a 2560x1440 display.

## Contributing

Cyder utilizes an Agile development model. For features you would like to see implmeneted, please create an issue and describe the feature in as much detail as you can. Issues are addressed by assigned priority based on issue create data, idea originality, and idea relevance. Please do NOT add todos in the code. Utilize GitHub Issues as a todo list instead.

If you would like to create your own widget, see the wiki.


## Deveopment Model: Agile
<img src="https://i.imgur.com/VKeVG4F.png" data-canonical-src="https://i.imgur.com/VKeVG4F.png"/>
