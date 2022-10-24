<img src="https://user-images.githubusercontent.com/60986919/171057466-8197f0c7-f908-4cb0-8978-deab86a79eac.png" alt="Logo" width="150">

![](actions/output/tagline.png)

![](actions/output/author.png)

![](actions/output/stats.png)

![](actions/output/total.png)

## What is Cyder

Funny you should ask this question, I'm asked it quite a lot and usually fail to give a comprehensive and elegant
answer. The best I can do is something along the lines of "Cyder is a multi-purpose, desktop manager, GUI tool." It is
written using a custom Java UI library which was built on top of lightweight Swing components. No modern GUI
dependencies such as [FlatLaf](https://github.com/JFormDesigner/FlatLaf), [MaterialFX](https://github.com/palexdev/MaterialFX)
or [FXML](https://openjfx.io/) were used, thus all Cyder components are closely related to [java/awt/Component.java](https://developer.classpath.org/doc/java/awt/Component-source.html).

Some examples of what you can do with Cyder include:

* Downloading Audio from a YouTube video, playlist, uuid, or link
* Image transforms, markup, and painting
* Evaluating mathematical expressions as simple as 2 * 2 or as complex as sin(e^pi*cos(64^cos(e^-1)))
* Visualizing algorithms such as A*, Graham Scan, Game of Life
* Converting audio files between formats such as wav and mp3
* Playing local audio files with the ability to "dreamify"
* Hashing inputs with a nice hashing widget using algorithms such as MD5, SHA1, and SHA256
* Reading, writing, and storing notes
* Demonstrating how Perlin noise works in both 2D and 3D with a visualizer
* Converting between temperature formats such as Kelvin, Fahrenheit, and Celsius
* Storing and running shortcuts
* Playing games such as hangman or nxn tic-tac-toe

## Screenshots

<details open>
<summary><b>Cyder Console</b></summary>
<br>
<img src="https://user-images.githubusercontent.com/60986919/197438724-45645f74-082e-4be3-9fb1-1e2d4de8339c.png" alt="Liminal Cyder" width="800">
</details>

<details>
<summary><b>Weather Widget</b></summary>
<img src="https://user-images.githubusercontent.com/60986919/190870600-a7ce2b39-2099-4365-ba8b-e2f38adff432.png" alt="Weather" width="400">
</details>

<details>
<summary><b>Music Widget (audio present)</b></summary>
<p>
<br>

https://user-images.githubusercontent.com/60986919/190871970-86091b80-b2ec-4c93-a7c1-c27128a67e8f.mp4

</p>
</details>

<details>
<summary><b>Paint Widget</b></summary>
<br>
<img src="https://user-images.githubusercontent.com/60986919/190871241-1ef14f0b-50d7-4cec-b484-7c6e1c9f9f43.png" alt="Paint widget" width="600">
<img src="https://user-images.githubusercontent.com/60986919/190871244-cd183604-3fbe-4f13-94c8-40ce6069f825.png" alt="Paint widget controls" width="600">
</details>

<details>
<summary><b>Pathfinding Visualizer</b></summary>
<p>
<br>


https://user-images.githubusercontent.com/60986919/190872205-b9ccf6d0-d1b5-41fb-abc8-0ca2a492075b.mp4


</p>
</details>

<details>
<summary><b>Game of Life Widget</b></summary>
<p>
<br>


https://user-images.githubusercontent.com/60986919/190872371-323bb51d-f678-4965-b1f6-3f7fe7976b28.mp4


</p>
</details>

## Usage and Setup

To get started with Cyder, first download your favorite Java IDE such as IntelliJ, NetBeans, Eclipse, etc. You'll then
want to make sure the IDE supports gradle operations. Next, clone Cyder via `git clone https://github.com/NathanCheshire/Cyder.git --depth 1`.
If you don't absolutely require the entire git history, I highly recommend shallow cloning as the extensive git history is quit large.
Now load the project in your IDE and allow the gradle setup task to run and the IDE to synchronize. 
Now you'll be able to run Cyder by a runtime configuration which invokes the
main method inside of `Cyder.java` which is located in the `genesis` package. Once started, Cyder should recognize there
are no users found and prompt for the creation of a user. Go ahead and create an account now.

For development purposes, you may want to add two props within a props file (you may create your own if you choose).
Within the chosen props file, set `debug_hash_name` to the value of your username and `debug_hash_password` to the value
of your password, hashed once using SHA256 (hint: you can hash your password inside of Cyder using the hashing widget).
Follow the ini key-value format or copy from `props.ini` if creating your own props file. You may name your props file
whatever you wish, just make sure you tell Cyder to load it by ensuring the filename starts with `prop` and is an `ini`
file located inside of the `props` directory. One last thing, make sure that you annotate these key props and any other
props whose values you do not want appearing in the log files with `@no_log`. This will prevent the logger from writing
the value to the log file when props are loaded at runtime.
