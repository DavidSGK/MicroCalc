# MicroCalc
My first Android application, a simple(?) calculator.

This calculator has operators for the four basic functions as well as brackets and exponents (^). It also features a decimal point button and a negative sign.
(More complex operations may be implemented in the future.)

MicroCalc takes the user's input as a string and breaks it down to pieces separated by the special characters in order to apply the correct order of operations. These functions are defined in the Interpret and Calculate methods within MainActivity.java.

Additional features include:

-> Super-minimalistic design

-> Colored ripple effects depending on the type of button pressed

-> Result-fetching from the previous line by inputting an operator at the start of an empty line

-> The DEL button changes text on an empty line to CLR, allowing the user to quickly clear the screen

-> Long-press DEL to clear whole screen

-> Scrolling TextView that keeps the history of calculations (given it isn't cleared)

-> Pressing enter takes the previous line (the input) and shrinks it while changing its color for better readability
