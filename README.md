![](./static/svgs/Primary.svg)

![](./static/svgs/Secondary.svg)

## What is Cyder

Funny you should ask this question, I'm asked it quite a lot and usually fail to give a comprehensive and elegant
answer. The best I can do is something along the lines of "Cyder is a multi-purpose, desktop manager, GUI tool." It is
written using a custom Java UI library which was built on top of lightweight Swing components.

Cyder is a multipurpose, desktop manager, GUI tool written using a custom UI library which on top of Swing/AWT. No
modern GUI dependencies such as FXML were used for Cyder hence all UI elements directly inherit from Component. Some
examples of what you can do with Cyder include:

* Downloading Audio from a YouTube video, playlist, uuid, or link
* Image transforms, markup, and painting
* Evaluating mathematical expressions as simple as 2 * 2 or as complex as sin(e^pi*cos(64^cos(e^-1)))
* Visualizing algorithm such as A*, Graham Scan, Game of Life
* Converting audio files between formats
* Playing local Audio with the ability to "dreamify it"
* Hashing inputs with a nice hashing widget
* Writing and saving notes
* Demonstrate how Perlin noise works in both 2D and 3D
* Converting between temperature formats
* Storing and running shortcuts
* Playing games such as hangman or tic tac toe

## Screenshots

<details>
<summary><b>Cyder Console</b></summary>
<br>
<img src="https://user-images.githubusercontent.com/60986919/163657071-17f9866c-bf2b-4307-9c8f-cf7e97982af9.png" alt="Liminal Cyder" width="700">
</details>

<details>
<summary><b>Weather Widget</b></summary>
<br>
<img src="https://user-images.githubusercontent.com/60986919/156911464-73221df7-68fa-4ce3-8211-555ddc9c0ac2.png" alt="Weather" width="400">
</details>

<details>
<summary><b>Paint Widget</b></summary>
<br>
<img src="https://user-images.githubusercontent.com/60986919/158036314-055f87d4-b21c-4eec-a92c-d65561c75483.png" alt="Paint widget" width="600">
<img src="https://user-images.githubusercontent.com/60986919/158036316-0abe20d4-3414-40e9-8da5-5ec83430d54d.png" alt="Paint widget controls" width="600">
</details>

<details>
<summary><b>Code Statistics</b></summary>
<img src="https://user-images.githubusercontent.com/60986919/160317468-6df0680f-8d49-413b-a09e-43d38839d441.png" alt="Code Statistics" width="300">
<br>
</details>

<details>
<summary><b>Pathfinding Visualizer</b></summary>
<br>
https://user-images.githubusercontent.com/60986919/161394292-cb7bf459-d405-4efc-8ea1-33a127036a69.mp4
</details>

<details>
<summary><b>Game of Life Widget</b></summary>
<br>
https://user-images.githubusercontent.com/60986919/160253262-8b10844a-6385-4328-ab84-961d05777526.mp4
</details>

## Usage and Setup

To get starting with Cyder, first download your favorite Java IDE such as IntelliJ, NetBeans, Eclipse, etc. You'll then
want to make sure the IDE supports gradle operations. Next, clone Cyder via
repo `git clone https://github.com/NathanCheshire/Cyder.git`. Now load the project in your IDE and allow the gradle
setup task to run and the IDE to synchronize. Make sure that you have a Java 17 SDK installed and set(I use temurin as
the vendor). Additionally make sure your java bytecode version is set to 17 and that the project language level is set
to `17 (Preview) - Pattern matching for switch`. Now you'll be able to run Cyder by a runtime configuration which
invokes the main method inside of `Cyder.java` which is located in the `genesis` package. Once started, Cyder should
recognize there are no users found and prompt for the creation of a user. Go ahead and create an account now.

For development purposes, you may want to initialize another props file within the root directory to store your keys,
passwords, and other sensitive data. This file should NOT be tracked via your VCS. Within this file, add two
props: `debug_hash_name` with the value of your username and `debug_hash_password` with the value of your password
hashed once using SHA256 (hint: you can hash your password inside of Cyder using the hashing widget). Follow the format
of `props.ini` when creating your own props file. You may name your props file whatever you wish, just make sure you
tell Cyder to load it in `PropLoader.java`s prop files list. One last thing, make sure you annotate these key props and
any other props whose values you do not want appearing in the log files with `@no_log`. This will prevent the logger
from writing the value to the log file when props are loaded at runtime.

