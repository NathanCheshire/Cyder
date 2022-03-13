# Creating a Cyder Widget

By <b>Nathan Cheshire</b>

Last updated: 3-13-21

## Gettering started

Making your own widget in Cyder is intended to be as easy and modern as possible. With the UI library as your skeleton,
the styling up to you, and the `utilities` packge full of rich and useful methods for logic implementation, the freedom
to manipulate Cyder is essentially limitless.

## The Cyder UI library

Lots of components exist within Cyder's `ui` package. The most important is the `CyderFrame`. The CyderFrame is
constructed as an extension of `JFrame` that is undecorated with it's own drag listeners, menu bar, menu icons, and
minimize and disposal animations. Other components include the `CyderTextField`, `CyderButton`, `CyderLabel`, and many
more. Before creating your own ui component, you should ensure that a pre-built Cyder specific component doesn't already
exist in this package.

## Setting up the frame

First, create a class in the `widgets` package following the same naming standard: `MyWidgetWidget`. Now you must decide
if you want to allow multiple instances of your widget or not.

### Option 1: Multiple Instances

Multiple instances are allowed for your widget, splendid! Start off your class by making the default constructor private
and make sure to include the following call to the `Logger` class: `Logger.log(LoggerTag.OJBECT_CREATION, this);`. Now
create a `public static MyClass` method named `getInstance()` to return a new instance of your class.

Now to allow your widget to be found and triggered via the `ReflectionUtil` widget finder and validator, you need to
create a `public static void` method named `showGUI()`. Additionally, this method must be annotated with the `@Widget`
annotation to allow it to be discovered. The annotation requries a single string or list of strings to allow a user to
trigger it as well as a description. Additionally, since multiple instances are allowed, this method should ONLY invoke
the following: `getInstance().showGUI()`. Thus, after following these steps, your class should look like the following:

```java
class MyWidget {
    private MyWidget() {
        Logger.log(LoggerTag.OJBECT_CREATION, this);
    }

    public static MyWidget getInstance() {
        return new MyWidget();
    }

    @Widget(triggers = "my trigger", description = "My widget description")
    public static void showGUI() {
        getInstance().innerShowGUI();
    }

    public void innerShowGUI() {
        // building the widget
    }
}
```

### Option 2: Singular Instance

If a singular instance is desired, restrict the default constructor by making it private. To follow good programming
practice set forth by [Effective Java](https://www.amazon.com/Effective-Java-Joshua-Bloch/dp/0134685997), include the
following throw statement in the private
constructor: `throw new IllegalMethodException(CyderStrings.attemptedInstantiation)`.

Now to allow your widget to be found and triggered via the `ReflectionUtil` widget finder and validator, you need to
create a `public static void` method named `showGUI()`. Additionally, this method must be annotated with the `@Widget`
annotation to allow it to be discovered. The annotation requries a single string or list of strings to allow a user to
trigger it as well as a description. Thus, after following these steps, your class should look like the following:

```java
class MyWidget {
    private MyWidget() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Widget(triggers = "my trigger", description = "My widget description")
    public static void showGUI() {
        // building the widget
    }
}

```

## Adding components

Now that the preliminaries are out of the way, we may begin building the widget itself. First, you'll want to create
a `CyderFrame` object and initialize it with a width and height. After that, make sure to set the title as well as the
title position if a center title is desired.

Calls:

```java
CyderFrame myFrame = new CyderFrame(600,600);
myFrame.setTitle("My Title");
myFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
```

Now comes the fun part, building the rest of the UI. As stated previously make sure to use already built Cyder
components before attempting to make your own. If the rare case of needing your own custom UI component does come about,
I'd prefer it that you make it in the ui/ library and create a separate PR to add that UI component to Cyder itself.

Adding a component to the frame:

```java
CyderButton myButton = new CyderButton("Button");
myButton.setBounds(40,40,200,40);
myButton.addActionListener(e- > {
    // your logic here or a lambda to a class level private method    
});
myFrame.getContentPane().add(myButton);
```

If you want to have a bit of fun with the UI and not use the default absolute layout, take a look at the `layouts`
package for layouts such as the `CyderFlowLayout` and `CyderGridLayout`.

## Logic

You have essentially two options for logic as the widget construction should be as minimal as possible. First, you
can simply invoke a method within or outside of the class. Alternatively, you could use a lambda for the action listener
to run the needed logic to drive your widget. The logic implementation is essentially all up to you, just make sure to
follow proper Java standards and remember to write javadoc.

Make sure to check the `utilities` package as it includes copious utililty classes for performing operations needed throughout
Cyder.

## Finishing calls

To finalize your frame, make sure to set the frame's visibility and location. Typically in Cyder, component's locations
are set relatively to the current dominant frame. You don't have to worry about this, however, `CyderShare` takes care
of this for you.

```java
myFrame.setLocationRelativeTo(CyderShare.getDominantFrame());
myFrame.setVisible(true);
```
