<p align="center">
<a>
<img  src="https://img.shields.io/github/license/NathanCheshire/Cyder?color=26A8FF&style=for-the-badge"/>
</a>
<a>
<img  src="https://img.shields.io/github/issues/NathanCheshire/Cyder?color=26A8FF&style=for-the-badge"/>
</a>
<a>
<img  src="https://img.shields.io/tokei/lines/Github.com/NathanCheshire/Cyder?color=26A8FF&style=for-the-badge"/>
</a>
<a>
<img  src="https://img.shields.io/github/repo-size/NathanCheshire/Cyder?color=26A8FF&style=for-the-badge"/>
</a>
</p>

# Cyder - A Programmers Swiss Army Knife

## Screenshots
<img src="https://i.imgur.com/IgwRizU.png" data-canonical-src="https://i.imgur.com/IgwRizU.png"/>

## Background

Cyder started as a way to test my basic java skills and have fun while taking AP Computer Science in 2017. As I continued to grow, work piled up and I had college and internships. Recently, however, I have begun to work on it in my free time as a hobby. I know so much more now than when I started and hope to keep adding features for quite some time. Ideally I should recreate this in electron-js but I doubt I'll ever get around it since I use Java for this project as a challenge since I've also built my own GUI library.

The best way I can describe Cyder is a multipurpose tool that launches all of the java projects I have done, are currently working on, or will do. I know certain ways I do things are not great and PLEASE do not comment about me building a java LAF from scratch essentailly and not using JavaFX. I am aware of these (I should hope I know since I'm the developer behind Cyder) and consider it a challenge to not work with modern UI depdencies.

tl;dr for how to describe it, it's a version of Amazon's Alexa that does similar but not identical tasks that you might need done on a PC.

The name comes from, well, I don't know to be completely transparent. I think it's cool naming builds after different kinds of cider like apple, Ace, White Star, or Soultree in this case. The "mispelling" of cider is on purpose I assure you. While I am sometimes a mindless fool, I chose this name because someones online I go by the alias of Cypher (when I don't go by Natche which is a combination of my first and last name). Combining Cypher and Cider you get the (IMHO with absolutely no bias) very cool name Cyder.

## Usage

Since this is a Gradle project, you can simply fork this project via it's GitHub url and run it using your favorite Java IDE (Eclipse, IntelliJ, NetBeans, etc.). If you don't download an official release/build and instead fork the project, you'll need to change a couple properties to allow Cyder to start. First, you'll need to change the released key/value pair in Sys.json. Second, you'll need to make sure that you're running either Windows or a Linux distribution as your top-level operating system. Cyder is currently not intended for OS X based systems and will exit upon an attempted launch should a user attempt to start Cyder on an OS X system.

## Pull Requests

After forking the project and learning Cyder Programming Standard and Cyder API (TODO LINK), you can submit a PR after you have considered ALL the following points:

* Have I used already existing methods from the utilities package?
* Have I added javadoc comments before any methods I created as well as @param and @return annotations?
* Have I commented any code that could potentially be confusing (take note of what a magic number is: https://en.wikipedia.org/wiki/Magic_number_(programming))?
* Have I tested my modifications properly and thoroughly? 

Additionally, the PR should contain the following information:

* A description of each file added/modified/removed and why the modification was performed. Walk me and other reviewers through your thought process for each of these changes. Keep it brief and if further information is required, reviewers will request more information before merging the PR. Try to check the state of your un-merged PR at least bi-weekly so that the modifications you made can be merged in ASAP.
* A brief summary of your PR and how it affects Cyder. This should differ from the first in that the first is for developers whilst this is simply a summary for users/enthusiasts to monitor Cyder's progress.

## Disclaimers

Currently, the program is intended for 2560x1440 resolution displays (my main display). I plan to fix this using SVG and other vector based graphics approaches in the future but currently, seeing as this message is still here, the program will look and operate best using a 2560x1440 display.

## TODO

- [x]  take into account possible secondary/tertiary monitors and know which one you're on
- [ ]  be able to adapt to different screen resolutions and maintain relative size
- [ ]  add a device manager so that you can see what's connected to the PC through the program
- [ ]  add @return, @param, @args, etc. to most methods
- [x]  comment the code
- [x]  log exit codes at what time and by whom
- [x]  make chat IO logs for each session
- [x]  move start data to user specific and place in a logs dir for the user. You should zip these logs upon completion
- [x]  incorporate CyderProgressUI for ProgressBars wherever it might fit
- [x]  put spotlight inside of the program
- [x]  get ImAvg working and put it inside the program
- [x]  allow users to map up to three internet links and place them on the menu panel
- [x]  allow users to rearrange the menu panel and remember what they do
- [x]  change the login animation to actual sliding JLabels with text and not a sliding image
- [x]  make a perlin noise widget
- [x]  make a Conway's Game of Life widget
- [x]  make pixelating a picture it's own widget
- [ ]  add multiple image support (not just PNG)
- [ ]  add a light/dark mode toggle
- [ ]  be able to change the CyderFrame/ConsolFrame border color from navy to some other color and repaint it. Rememeber this change
- [x]  if your location cannot be found, restrict features that rely on IP data and inform the user of this
- [x]  get start animations working and use them for CyderFrame instanceof objects
- [x]  utilize system.exit more and give more data on why the program was exited
- [x]  be able to download missing files from the internet and if you cannot and you need them, inform the user and exit the program
- [x]  move file.txt, string.txt, and inputmessage.txt to the tmp directory (removed in 6.22.21)
- [x]  utilze html inside of notifications (line breaks, color, bold, italics, etc.)
- [x]  be able to set the background to a solid color. On doing so, save the background
- [ ]  Algorithms package with euclidian algorithm: Algorithms.euclidian(47,63) should return ints in same order to multiply alpha and beta inputs by to get the gcd
- [x]  make an entire minecraft widget which can launch minecraft via the offical launcher, lunar client, or any other client provided the user can provide a path to the executable
- [x]  allow starting executables to be mapped to links within the menu
- [ ]  convert all non vector based images to SVGs or other vector based data structures
- [x]  exit the program if in OSX (program not intended for OSX and should only run on Windows 10+ and possible Linux distros)
- [x]  implement convex hull algorithm and visualizer
- [x]  implement path finding visualizer
- [x]  make certain classes final, don't let people instantiate the class by making the constructor private like "private Math() {}"
- [x]  take and recongize a pastebin link or UUID and print the contents to the console, should be reading from the RAW paste
- [x]  allow one instance of console frame
- [x]  make launching and testing easier
- [ ]  make a music lab using lines that correspond to notes; inspired by Wintergatan Marble Machine
- [x]  add work with binary files (read/write to/from bin files)
- [x]  determine common files even without extension based on meta data binary reader
- [x]  bin dump function
- [x]  hex dump function
- [ ]  ncview functionality
- [ ]  add quick start functionality for user's in a hurry
