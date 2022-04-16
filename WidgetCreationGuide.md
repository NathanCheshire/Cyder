# Creating a Cyder Widget

By <b>Nathan Cheshire</b>

Last updated: 4-16-21

## Getting started

Making your own widget in Cyder is intended to be as easy and modern as possible. With the UI library as your skeleton,
the styling up to you, and the `utilities` package full of rich and useful methods for logic implementation, the freedom
to manipulate Cyder is limitless.

## The Cyder UI library

Lots of components exist within Cyder's `ui` package. The most important is the `CyderFrame`. The CyderFrame is
constructed as an extension of `JFrame` that is undecorated with it's own drag MouseListeners, menu bar, menu icons, and
minimize and disposal animations. Other components include the `CyderTextField`, `CyderButton`, `CyderLabel`, and many
more. In the rare case a needed ui component is needed for your specific widget that isn't implemented yet, feel free to
make a separate PR and add the component to Cyder yourself (ensure you follow [EJ
](https://www.amazon.com/Effective-Java-Joshua-Bloch/dp/0134685997) items as usual).

## Setting up the frame

First, create a class in the `widgets` package following the same naming standard: `MyWidgetWidget`. Now you must decide
if you want to allow multiple instances of your widget or not.

### Option 1: Multiple Instances

Multiple instances are allowed for your widget. Start off your class by making the default constructor private
and make sure to include the following call to Cyder's `Logger` class: `Logger.log(LoggerTag.OBJECT_CREATION, this);`. Now
create a `public static MyClass` method named `getInstance()` to return a new instance of your class.

To allow your widget to be found and triggered via the `ReflectionUtil` widget finder and validator, you need to
create a `public static void` method named `showGui()`. Additionally, this method must be annotated with the `@Widget`
annotation to allow it to be discovered. The annotation requires a single string or list of strings to allow a user to
trigger it as well as a description. Additionally, since multiple instances are allowed, this method should ONLY invoke
the following: `getInstance().showGui()`. Thus, after following these steps, your class should look like the following:

```java
/**
* An example widget.
*/
class MyWidget {
    /**
    * Hide default constructor.
    */
    private MyWidget() {
        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    /**
    * Allow multiple instances of widget.
    */
    public static MyWidget getInstance() {
        return new MyWidget();
    }

    /**
    * showGUI standard.
    */
    @Widget(triggers = "my trigger", description = "My widget description")
    public static void showGui() {
        getInstance().innerShowGui();
    }

    /**
    * Construct widgets (method is public to allow invoking aside from widget finder).
    */
    public void innerShowGui() {
        // constructing the widget
    }
}
```

### Option 2: Singular Instance

If a singular instance is desired, restrict the default constructor by making it private. To follow good programming
practices set forth by [Effective Java](https://www.amazon.com/Effective-Java-Joshua-Bloch/dp/0134685997), include the
following throw statement in the private
constructor: `throw new IllegalMethodException(CyderStrings.attemptedInstantiation)`. Note that `IllegalMethodException`
is simply an exception extending `IllegalArgumentException`.

To allow your widget to be found and triggered via the `ReflectionUtil` widget finder and validator, you need to
create a `public static void` method named `showGui()`. Additionally, this method must be annotated with the `@Widget`
annotation to allow it to be discovered. The annotation requires a single string or list of strings to allow a user to
trigger it as well as a description. Thus, after following these steps, your class should look like the following:

```java
/**
* An example widget.
*/
class MyWidget {
    /**
    * Suppress default constructor.
    */
    private MyWidget() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
    * The only way this widget can be invoked is via the trigger(s).
    */
    @Widget(triggers = "my trigger", description = "My widget description")
    public static void showGUI() {
        // building the widget
    }
}

```

## Adding components

Now that the preliminaries are out of the way, you may begin building the widget itself. First, you'll want to create
a `CyderFrame` object and initialize it with a width and height. After that, make sure to set the title as well as the
title position if a center title is desired.

Calls:

```java
CyderFrame cyderFrame = new CyderFrame(600, 600);
cyderFrame.setTitle("My Title");
cyderFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
```

Now comes the fun part, building the rest of the UI. As stated previously make sure to use already built Cyder
components before attempting to make your own. If the rare case of needing your own custom UI component does come about,
create a separate PR for your ui element implementation.

Adding a component to the frame:

```java
CyderButton cyderButton = new CyderButton("Button");
cyderButton.setBounds((600 - 200) / 2, (600 - 40 + CyderDragLabel.DEFAULT_HEIGHT) / 2, 200, 40);
cyderButton.addActionListener(e -> {
    // your logic here or a lambda to a class level private method
    cyderFrame.notify("Hello World!");
});
cyderFrame.getContentPane().add(cyderButton);
```

If you want to have a bit of fun with the UI and not use the default absolute layout, take a look at the `layouts`
package for layouts such as the `CyderFlowLayout` and `CyderGridLayout`. They work in the way you'd expect but are, IMHO, 
much easier and intuitive than the Swing layouts.

## Logic

You have essentially two options for logic as the widget construction should be as minimal as possible. First, you
can simply invoke a method within or outside of the class. Alternatively, you could use a lambda for the action listener
to run the needed logic to drive your widget. The logic implementation is essentially all up to you, just make sure to
follow proper Java standards and remember to write javadocs for all non-private members.

Make sure to check the `utilities` package as it includes copious utility classes for performing operations needed throughout
Cyder.

## Finishing calls

To finalize your frame, make sure to set the frame's visibility and location. Typically in Cyder, component's locations
are set relatively to the current dominant frame. You don't have to worry about this, however, `CyderFrame` takes care
of this for you via the method `finalizeAndShow()` which performs the required actions and checks for you.

```java
cyderFrame.finalizeAndShow();
```

To view the complete version of this widget, see `widgets/ExampleWidget.java`.
