# Cyder - A Programmers Swiss Army Knife
 
## Screenshots
Desktop full of Cyder Widgets <br/>
<img src="https://i.imgur.com/4hEv3kG.png" data-canonical-src="https://i.imgur.com/4hEv3kG.png" width="800" height="440" />

## Background

Cyder started as a way to test my basic java skills and have fun while taking AP Computer Science in 2017. As I continued to grow, work piled up and I had college and internships. Recently, however, I have began to work on it in my free time as a hobby. I know so much more now than when I started and hope to keep adding features for quite some time. Ultimately though, I'll probably call it adequte at best and either move on, or try to recreate it using C++ and completely platform-independent resources.

The best way I can describe Cyder is a multipurpose tool that launches all of the java projects I have done, are currently working on, or will do. I know certain ways I do things are not great and PLEASE do not comment about me building a java LAF from scratch essentailly and not using JavaFX. I am aware of these (I should hope I know since I'm the developer behind Cyder) and consider it a challenge to not work with modern UI depdencies.

tl;dr for how to describe it, it's a version of Amazon's Alexa that does similar but not identical tasks that you might need done on a PC.

The name comes from, well, I don't know to be completely transparent. I think it's cool naming builds after different kinds of cider like apple, Ace, White Star, or Soultree in this case. The "mispelling" of cider is on purpose I assure you. While I am sometimes a mindless fool, I chose this name because someones online I go by the alias of Cypher (when I don't go by Natche which is a combination of my first and last name). Combining Cypher and Cider you get the (IMHO with absolutely no bias) very cool name Cyder.

## Usage

Currently you can fork the project or download it I suppose. Running it in an IDE like Eclipse or IntelliJ shouldn't be that hard if you know what you're doing. For IntelliJ which is what I use, you might have to import the libraries inside the jars folder (sys/jars). Other than that, it should be pretty simple to get it working on your machine. ~~At some point I'll make it a jar that can download all the files and resources it needs from the internet and be independent of an IDE.~~

## Disclaimers

As of 5.18.21, the program is intended for 2560x1440 resolution displays (my main display). I plan to fix this using SVG and other vector based graphics approaches in the future but currently, seeing as this message is still here, the program will look and operate best using a 2560x1440 display, Windows 10+, and Java 8+.

## TODO

- [ ]  take into account possible secondary/tertiary monitors and know which one you're on
- [ ]  be able to adapt to different screen resolutions and maintain relative size
- [ ]  add a device manager so that you can see what's connected to the PC through the program
- [ ]  add @return, @param, @args, etc. to most methods
- [ ]  comment the code
- [ ]  log exit codes at what time and by whom
- [ ]  make chat IO logs for each session
- [ ]  move start data to user specific and place in a logs dir for the user. You should zip these logs upon completion
- [ ]  incorporate CyderProgressUI for ProgressBars wherever it might fit
- [ ]  put spotlight inside of the program
- [ ]  get ImAvg working and put it inside the program
- [ ]  allow users to map up to three internet links and place them on the menu panel
- [ ]  allow users to rearrange the menu panel and remember what they do
- [ ]  change the login animation to actual sliding JLabels with text and not a sliding image
- [ ]  make a perlin noise widget
- [ ]  make a Conway's Game of Life widget
- [ ]  make pixelating a picture it's own widget
- [ ]  add multiple image support (not just PNG)
- [ ]  add a light/dark mode toggle
- [ ]  be able to change the CyderFrame/ConsolFrame border color from navy to some other color and repaint it. Remmeber this change
- [ ]  if your location cannot be found, restrict features that rely on IP data and inform the user of this
- [ ]  get start animations working and use them for CyderFrame instanceof objects
- [ ]  utilize system.exit more and give more data on why the program was exited
- [ ]  be able to download missing files from the internet and if you cannot and you need them, inform the user and exit the program
- [ ]  move file.txt, string.txt, and inputmessage.txt to the tmp directory
- [ ]  utilze html inside of notifications (line breaks, color, bold, italics, etc.)
- [ ]  be able to set the background to a solid color. On doing so, save the background
- [ ]  Algorithms package with euclidian algorithm: Algorithms.euclidian(47,63) should return ints in same order to multiply alpha and beta inputs by to get the gcd
- [ ]  make an entire minecraft widget which can launch minecraft via the offical launcher, lunar client, or any other client provided the user can provide a path to the executable
- [ ]  allow starting executables to be mapped to links within the menu
- [ ]  convert all non vector based images to SVGs or other vector based data structures
- [ ]  if the operating system is OSX, swap the location of the menu and window toggle trio and make it resemble the green, yellow, red dots on OSX
- [ ]  implement convex hull algorithm and visualizer
- [ ]  implement path finding visualizer but also link to pac-man game
- [ ]  make certain classes final, don't let people instantiate the class by making the constructor private like "private Math() {}"
- [ ]  take and recongize a pastebin link or UUID and print the contents to the console, should be reading from the RAW paste
- [ ]  allow once instance of console frame but multiple for dev mode (See Runtime native class)
- [ ]  make launching and testing easier
