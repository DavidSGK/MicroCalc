package com.davidsgk.microcalc;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    //different classifications of buttons
    private static Button[] numButtons = new Button[10];        //0,1,2,3,4,5,6,7,8,9
    private static Button[] operatorButtons = new Button[7];    //+,-,*,/,(,),^
    private static Button decimalButton;                        //.
    private static Button negativeButton;                       //(-)
    private static Button returnButton;                         //=
    private static Button deleteButton;                         //backspace/clear for long press
    private static TextView output;                             //outputs all input and answer
    private static Spannable span;                              //used for syling portions of output

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = (TextView) findViewById(R.id.output);
        output.setMovementMethod(new ScrollingMovementMethod());
        output.setVerticalScrollBarEnabled(false);

        initButtons();
    }

    //initialize buttons and their functions
    protected void initButtons() {
        int id;
        for (int i = 0; i < 10; i++) {
            id = getResources().getIdentifier("button_" + i, "id", getPackageName());
            numButtons[i] = (Button) findViewById(id);
        }

        operatorButtons[0] = (Button) findViewById(R.id.button_add);
        operatorButtons[1] = (Button) findViewById(R.id.button_subtract);
        operatorButtons[2] = (Button) findViewById(R.id.button_multiply);
        operatorButtons[3] = (Button) findViewById(R.id.button_divide);
        operatorButtons[4] = (Button) findViewById(R.id.button_exponent);
        operatorButtons[5] = (Button) findViewById(R.id.button_bracketL);
        operatorButtons[6] = (Button) findViewById(R.id.button_bracketR);

        decimalButton = (Button) findViewById(R.id.button_decimal);
        negativeButton = (Button) findViewById(R.id.button_negative);
        returnButton = (Button) findViewById(R.id.button_return);
        deleteButton = (Button) findViewById(R.id.button_del);

        for (int j = 0; j < 10; j++) {
            numButtons[j].setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            output.append(((Button) v).getText().toString());
                            ScrollToBottom();
                        }
                    }
            );
        }

        for (int k = 0; k < 7; k++) {
            operatorButtons[k].setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            //if line is empty and there is a previous result available, fetch the result
                            if (output.getText().length() != 0 && output.getText().toString().charAt(output.getText().length() - 1) == '\n') {
                                output.append(output.getText().toString().substring(output.getText().toString().lastIndexOf('\n', output.getText().toString().length() - 2) + 1,
                                        output.getText().toString().lastIndexOf('\n')));
                            }
                            //detects if another operator was pressed right before
                            if (output.getText().length() != 0 && output.getText().charAt(output.getText().length() - 1) == ' ') {
                                output.append(((Button) v).getText().toString() + " ");
                            } else {
                                output.append(" " + ((Button) v).getText().toString() + " ");
                            }
                        }
                    }
            );
        }

        decimalButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        output.append(".");
                    }
                }
        );

        negativeButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        output.append("-");
                    }
                }
        );

        returnButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        output.append("=\n");
                        //taking the span of the previous lines and styling size and color for better readability
                        span = new SpannableString(output.getText());
                        span.setSpan(new RelativeSizeSpan(0.5f),
                                output.getText().toString().lastIndexOf('\n', output.getText().toString().length() - 2) + 1,
                                output.getText().toString().lastIndexOf('\n'),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorOperatorHighLight)),
                                output.getText().toString().lastIndexOf('\n', output.getText().toString().length() - 2) + 1,
                                output.getText().toString().lastIndexOf('\n'),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        output.setText(span);
                        output.append(Interpreter(CurrentLine()) + "\n");
                        ScrollToBottom();
                    }
                }
        );

        deleteButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        String text = output.getText().toString();
                        if (text.length() != 0 && text.charAt(text.length() - 1) != '\n') {   //can't delete past lines
                            if (text.charAt(text.length() - 1) == ' ') { //deleting an operator
                                output.setText(output.getText().subSequence(0, output.getText().length() - 3));
                            } else {
                                output.setText(output.getText().subSequence(0, output.getText().length() - 1));
                            }
                        }
                    }
                }
        );

        deleteButton.setOnLongClickListener(
                new Button.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        output.setText("");
                        return true;
                    }
                }
        );
    }

    //method to make the output automatically scroll to the bottom
    protected static void ScrollToBottom() {
        final Layout layout = output.getLayout();
        if (layout != null) {
            int scrollDelta = layout.getLineBottom(output.getLineCount() - 1) - output.getScrollY() - output.getHeight();
            if (scrollDelta > 0) output.scrollBy(0, scrollDelta);
        }
    }

    //method to reference just last line in TextView
    protected static String CurrentLine() {
        String text = output.getText().toString();
        String currentLine;
        int lineCount = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') lineCount++;
        }

        if (lineCount != 1) {
            currentLine = text.substring(text.lastIndexOf('\n', text.length() - 2) + 1, text.length() - 2);
        } else {
            currentLine = text.substring(0, text.length() - 2);
        }

        return currentLine;
    }

    //method to perform the calculations based on input
    protected String Interpreter(String line) {

        if (line.equals(" ")) return "No Input";

        String[] splitArray;
        if (line.charAt(0) == ' ') {
            splitArray = line.substring(1, line.length() - 1).split("\\s+");
        } else {
            splitArray = line.split("\\s+");
        }

        System.out.println(line);
        for (String piece : splitArray) {
            System.out.print(piece);
        }
        System.out.print("\n");

        //Check for syntax errors
        int count1 = 0;
        int count2 = 0;
        int bracketCount = 0;

        //The number of ( and ) must be the same
        for (String piece : splitArray) {
            if (piece.equals("(")) count1++;
            if (piece.equals(")")) count2++;
        }
        if (count1 != count2) {
            System.out.println(1);
            return "Syntax Error";
        } else {
            bracketCount = count1;
        }

        //Can't begin or end with non-number characters (other than brackets)
        //Note: All non-number characters other than the decimal point adds spaces to either side
        if (!isNumeric(splitArray[0])) {
            if (!splitArray[0].equals("(")) {
                System.out.println(-1);
                System.out.println(splitArray[0]);
                return "Syntax Error";
            }
        }
        if (!isNumeric(splitArray[splitArray.length - 1])) {
            if (!splitArray[splitArray.length - 1].equals(")")) {
                System.out.println(-2);
                return "Syntax Error";
            }
        }

        //Can't have the first ) before ( and can't have the last ( after )
        if (Arrays.asList(splitArray).indexOf(")") < Arrays.asList(splitArray).indexOf("(") || Arrays.asList(splitArray).lastIndexOf("(") > Arrays.asList(splitArray).lastIndexOf(")")) {
            System.out.println(4);
            return "Syntax Error";
        }

        //Can't have two numbers or non-numbers side by side
        for (int i = 1; i < splitArray.length; i++) {
            if ((isNumeric(splitArray[i]) && isNumeric(splitArray[i - 1])) || (!isNumeric(splitArray[i]) && !isNumeric(splitArray[i - 1]))) {
                //Unless it's an operator followed by ( or a ) followed by an operator
                if (!(splitArray[i].equals("(") && isOperator(splitArray[i - 1].charAt(0))) && !(isOperator(splitArray[i].charAt(0)) && splitArray[i - 1].equals(")"))) {
                    //Unless it's brackets directly next to each other
                    if (!(splitArray[i].equals("(") && splitArray[i - 1].equals(")")) &&
                            !(splitArray[i].equals("(") && splitArray[i - 1].equals("(")) &&
                            !(splitArray[i].equals(")") && splitArray[i - 1].equals(")"))) {
                        System.out.println(3);
                        return "Syntax Error";
                    }
                }
            }
        }

        //Remove unnecessary leading 0's from input
        for (int i = 0; i < splitArray.length; i++) {
            if (splitArray[i].length() > 1 && splitArray[i].charAt(0) == '0' && !(splitArray[i].charAt(1) == '.')) {
                System.out.println("leading 0's detected");
                StringBuilder tempString = new StringBuilder(splitArray[i]);
                if (splitArray[i].contains(".")) {
                    if (splitArray[i].charAt(splitArray[i].indexOf('.') - 1) == '0') {
                        for (int j = 0; j < splitArray[i].indexOf('.') - 1; j++) {
                            tempString.deleteCharAt(0);
                        }
                    } else {
                        for (int j = 0; j < splitArray[i].indexOf('.'); j++) {
                            tempString.deleteCharAt(0);
                        }
                    }
                } else {
                    while (tempString.charAt(0) == '0') {
                        tempString.deleteCharAt(0);
                    }
                }
                System.out.println("tempString: " + tempString);
                splitArray[i] = tempString.toString();
            }
        }

        //Generate new string with fixes made
        String fixedLine = "";
        for (String piece : splitArray) {
            fixedLine += piece + " ";
        }

        //Interpretation & calculation
        StringBuilder newLine = new StringBuilder(fixedLine);

        int nonNumCount;
        String[] testArray;
        boolean condition;
        int startBracketIndex;
        int endBracketIndex;
        double tempResult;

        while (true) {
            System.out.println("newLine: " + newLine);
            //Checks how many operators need to be resolved
            nonNumCount = 0;
            for (int i = 0; i < newLine.length(); i++) {
                if (!isNumeric(newLine.charAt(i)) && newLine.charAt(i) != '.' && newLine.charAt(i) != ' ' && newLine.charAt(i) != ' ' && newLine.charAt(i) != 'E') {
                    //Doesn't add one for negative sign
                    if (!(newLine.charAt(i) == '-' && !(newLine.charAt(i + 1) == ' '))) {
                        nonNumCount++;
                    }
                }
            }

            //Checks if any numbers are side by side; can result from brackets' positioning
            testArray = newLine.toString().split("\\s+");
            condition = false;
            for (int i = 0; i < testArray.length - 1; i++) {
                if (isNumeric(testArray[i]) && isNumeric(testArray[i + 1])) {
                    condition = true;
                }
            }

            //If there are no more calculations to be done, break loop
            if (nonNumCount == 0 && condition == false) break;

            if (bracketCount != 0) { //if brackets exist
                System.out.println("nonNumCount: " + nonNumCount);

                startBracketIndex = newLine.lastIndexOf("(", newLine.indexOf(")"));
                endBracketIndex = newLine.indexOf(")");

                ArrayList<String> checkList = new ArrayList<>(Arrays.asList(newLine.substring(startBracketIndex + 2, endBracketIndex).split("\\s+")));
                tempResult = Calculate(checkList);
                newLine.delete(startBracketIndex, endBracketIndex + 1);
                if (tempResult == Math.rint(tempResult) && !Double.toString(tempResult).contains("E")) {
                    newLine.insert(startBracketIndex, (int) tempResult);
                } else {
                    newLine.insert(startBracketIndex, tempResult);
                }
                bracketCount--;
            } else {                //if brackets don't exist
                System.out.println("nonNumCount: " + nonNumCount);
                ArrayList<String> checkList = new ArrayList<>(Arrays.asList(newLine.toString().split("\\s+")));
                tempResult = Calculate(checkList);
                System.out.println("tempResult: " + tempResult);
                newLine.delete(0, newLine.length());
                if (tempResult == Math.rint(tempResult) && !Double.toString(tempResult).contains("E")) {
                    newLine.insert(0, (int) tempResult);
                } else {
                    newLine.insert(0, tempResult);
                }
            }

            //clean up leading and trailing spaces left over
            newLine = new StringBuilder(newLine.toString().replace("  ", " "));
            if (newLine.charAt(0) == ' ') newLine.deleteCharAt(0);
        }

        if (newLine.toString().contains("E") &&
                newLine.toString().charAt(newLine.indexOf("E") + 1) != '-' &&
                Integer.parseInt(newLine.toString().substring(newLine.indexOf("E") + 1, newLine.length())) <= 15) {
            return String.format("%.0f", Double.parseDouble(newLine.toString().replace(' ', '\0')));
        }
        return newLine.toString().replace(' ', '\0');
    }

    protected double Calculate(ArrayList<String> arrayList) {
        double tempNum;

        if (arrayList.size() != 1) {

            ListIterator iterator;

            //Exponent
            iterator = arrayList.listIterator();    //reset
            while (iterator.hasNext()) {
                if (arrayList.get(iterator.nextIndex()).equals("^")) {
                    tempNum = Math.pow(Double.parseDouble(iterator.previous().toString()), Double.parseDouble(arrayList.get(iterator.nextIndex() + 2)));
                    iterator.remove();
                    iterator.next();    //unless next() is called, ListIterator cannot remove another element
                    iterator.remove();
                    iterator.next();
                    iterator.remove();
                    iterator.add(Double.toString(tempNum));
                    iterator = arrayList.listIterator();    //reset
                }
                if (iterator.hasNext()) iterator.next();
            }

            //Multiplication & Division
            iterator = arrayList.listIterator();    //reset
            while (iterator.hasNext()) {
                if (arrayList.get(iterator.nextIndex()).equals("x")) {
                    tempNum = Double.parseDouble(iterator.previous().toString()) * Double.parseDouble(arrayList.get(iterator.nextIndex() + 2));
                    iterator.remove();
                    iterator.next();
                    iterator.remove();
                    iterator.next();
                    iterator.remove();
                    iterator.add(Double.toString(tempNum));
                    iterator = arrayList.listIterator();    //reset
                    //For when numbers are left next to each other e.g. (2)(2) = 2 x 2 = 4
                } else if (iterator.hasNext() && iterator.hasPrevious() && isNumeric(arrayList.get(iterator.previousIndex())) && isNumeric(arrayList.get(iterator.nextIndex()))) {
                    tempNum = Double.parseDouble(arrayList.get(iterator.previousIndex())) * Double.parseDouble(arrayList.get(iterator.nextIndex()));
                    iterator.previous();
                    iterator.remove();
                    iterator.next();
                    iterator.remove();
                    iterator.add(Double.toString(tempNum));
                    iterator = arrayList.listIterator();    //reset
                } else if (arrayList.get(iterator.nextIndex()).equals("/")) {
                    tempNum = Double.parseDouble(iterator.previous().toString()) / Double.parseDouble(arrayList.get(iterator.nextIndex() + 2));
                    iterator.remove();
                    iterator.next();
                    iterator.remove();
                    iterator.next();
                    iterator.remove();
                    iterator.add(Double.toString(tempNum));
                    iterator = arrayList.listIterator();    //reset
                }
                if (iterator.hasNext()) iterator.next();
            }

            //Addition & Subtraction
            iterator = arrayList.listIterator();
            while (iterator.hasNext()) {
                if (arrayList.get(iterator.nextIndex()).equals("+")) {
                    tempNum = Double.parseDouble(iterator.previous().toString()) + Double.parseDouble(arrayList.get(iterator.nextIndex() + 2));
                    iterator.remove();
                    iterator.next();
                    iterator.remove();
                    iterator.next();
                    iterator.remove();
                    iterator.add(Double.toString(tempNum));
                    iterator = arrayList.listIterator();    //reset
                } else if (arrayList.get(iterator.nextIndex()).equals("-")) {
                    tempNum = Double.parseDouble(iterator.previous().toString()) - Double.parseDouble(arrayList.get(iterator.nextIndex() + 2));
                    iterator.remove();
                    iterator.next();
                    iterator.remove();
                    iterator.next();
                    iterator.remove();
                    iterator.add(Double.toString(tempNum));
                    iterator = arrayList.listIterator();    //reset
                }
                if (iterator.hasNext()) iterator.next();
            }
        }

        System.out.println("Size: " + arrayList.size());
        System.out.println(arrayList.get(0));

        //Circumvent issue of double precision loss by shortening decimal output
        if (arrayList.get(0).contains(".")) {
            if (arrayList.get(0).substring(arrayList.get(0).indexOf('.'), arrayList.get(0).length()).length() > 13) {
                DecimalFormat decimalFormat = new DecimalFormat("#.#############");
                decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
                return Double.parseDouble(decimalFormat.format(Double.parseDouble(arrayList.get(0))));
            }
        }

        System.out.println(Double.parseDouble(arrayList.get(0)));
        return Double.parseDouble(arrayList.get(0));
    }

    public boolean isNumeric(String text) {
        try {
            double number = Double.parseDouble(text);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public boolean isNumeric(char text) {
        try {
            double number = Double.parseDouble(Character.toString(text));
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public boolean isOperator(char text) {
        if (text == '+' || text == 'x' || text == '-' || text == '/' || text == '^') return true;
        else return false;
    }
}
