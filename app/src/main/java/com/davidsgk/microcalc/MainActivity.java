package com.davidsgk.microcalc;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Prevents crashing on devices lower than API 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }

        //different classifications of buttons
        Button[] numButtons = new Button[10];        //0,1,2,3,4,5,6,7,8,9
        Button[] operatorButtons = new Button[7];    //+,-,*,/,(,),^
        Button decimalButton;                        //.
        Button negativeButton;                       //(-)
        Button returnButton;                         //=
        final Button deleteButton;                   //backspace/clear for long press
        final TextView output;                       //outputs all input and answer

        output = (TextView) findViewById(R.id.output);
        output.setMovementMethod(new ScrollingMovementMethod());
        output.setVerticalScrollBarEnabled(false);

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

        //Number buttons
        for (int j = 0; j < 10; j++) {
            numButtons[j].setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            output.append(((Button) v).getText().toString());
                            ScrollToBottom(output);
                        }
                    }
            );
        }

        //Operator buttons
        for (int k = 0; k < 7; k++) {
            operatorButtons[k].setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            String text = output.getText().toString();
                            //if line is empty and there is a previous result available, fetch the result
                            if (text.length() != 0 &&
                                    text.charAt(text.length() - 1) == '\n' &&
                                    isNumeric(text.substring(text.lastIndexOf('\n', text.length() - 2) + 1,
                                            text.lastIndexOf('\n')))) {
                                output.append(text.substring(text.lastIndexOf('\n', text.length() - 2) + 1,
                                        text.lastIndexOf('\n')));
                            }
                            //detects if another operator was pressed right before to not add another space
                            if (text.length() != 0 && text.charAt(text.length() - 1) == ' ') {
                                output.append(((Button) v).getText().toString() + " ");
                            } else {
                                output.append(" " + ((Button) v).getText().toString() + " ");
                            }
                        }
                    }
            );
        }

        //Decimal button
        decimalButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        output.append(".");
                    }
                }
        );

        //Negative sign button
        negativeButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        output.append("-");
                    }
                }
        );

        //Equal sign button
        returnButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        output.append("=\n");
                        //taking the span of the previous lines and styling size and color for better readability
                        Spannable span = new SpannableString(output.getText());
                        span.setSpan(new RelativeSizeSpan(0.5f),
                                output.getText().toString().lastIndexOf('\n', output.getText().toString().length() - 2) + 1,
                                output.getText().toString().lastIndexOf('\n'),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.mds_orange_500)),
                                output.getText().toString().lastIndexOf('\n', output.getText().toString().length() - 2) + 1,
                                output.getText().toString().lastIndexOf('\n'),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        output.setText(span);
                        output.append(Interpreter(CurrentLine(output)) + "\n");
                        ScrollToBottom(output);
                    }
                }
        );

        //Delete button
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
                        } else if (text.length() != 0 && text.charAt(text.length() - 1) == '\n') {
                            output.setText("");
                        }
                    }
                }
        );

        //Delete button long-press clear behavior
        deleteButton.setOnLongClickListener(
                new Button.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        output.setText("");
                        return true;
                    }
                }
        );

        //Changes the text on the delete button depending on the state of the text
        output.addTextChangedListener(
                new TextWatcher() {
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    public void afterTextChanged(Editable s) {
                        String text = output.getText().toString();
                        if (text.length() != 0 && text.charAt(text.length() - 1) == '\n') {
                            deleteButton.setText("CLR");
                        } else {
                            deleteButton.setText("DEL");
                        }
                    }
                }
        );
    }

    //method to make the output automatically scroll to the bottom
    protected static void ScrollToBottom(TextView output) {
        final Layout layout = output.getLayout();
        if (layout != null) {
            int scrollDelta = layout.getLineBottom(output.getLineCount() - 1) - output.getScrollY() - output.getHeight();
            if (scrollDelta > 0) output.scrollBy(0, scrollDelta);
        }
    }

    //method to reference just last line in TextView
    protected static String CurrentLine(TextView output) {
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

        if (line.equals(" ") || line.length() == 0) return "No Input";

        String[] splitArray;
        if (line.charAt(0) == ' ') {
            splitArray = line.substring(1, line.length() - 1).split("\\s+");
        } else {
            splitArray = line.split("\\s+");
        }

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
            return getResources().getString(R.string.syntax_error);
        } else {
            bracketCount = count1;
        }

        //Can't begin or end with non-number characters (other than brackets)
        //Note: All non-number characters other than the decimal point adds spaces to either side
        if (!isNumeric(splitArray[0])) {
            if (!splitArray[0].equals("(")) {
                return getResources().getString(R.string.syntax_error);
            }
        }
        if (!isNumeric(splitArray[splitArray.length - 1])) {
            if (!splitArray[splitArray.length - 1].equals(")")) {
                return getResources().getString(R.string.syntax_error);
            }
        }

        //Can't have the first ) before ( and can't have the last ( after )
        if (Arrays.asList(splitArray).indexOf(")") < Arrays.asList(splitArray).indexOf("(") || Arrays.asList(splitArray).lastIndexOf("(") > Arrays.asList(splitArray).lastIndexOf(")")) {
            return getResources().getString(R.string.syntax_error);
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
                        return Integer.toString(R.string.syntax_error);
                    }
                }
            }
        }

        //Remove unnecessary leading 0's from input
        for (int i = 0; i < splitArray.length; i++) {
            if (splitArray[i].length() > 1 && splitArray[i].charAt(0) == '0' && !(splitArray[i].charAt(1) == '.')) {
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

                startBracketIndex = newLine.lastIndexOf("(", newLine.indexOf(")"));
                endBracketIndex = newLine.indexOf(")");

                ArrayList<String> checkList = new ArrayList<>(Arrays.asList(newLine.substring(startBracketIndex + 2, endBracketIndex).split("\\s+")));
                //Division by 0
                if (checkList.contains("/") && checkList.contains("0") && checkList.indexOf("0") == checkList.indexOf("/") + 1) {
                    return getResources().getString(R.string.division_by_0);
                }
                tempResult = Calculate(checkList);
                newLine.delete(startBracketIndex, endBracketIndex + 1);
                if (tempResult == Math.rint(tempResult) && !Double.toString(tempResult).contains("E")) {
                    newLine.insert(startBracketIndex, (int) tempResult);
                } else {
                    newLine.insert(startBracketIndex, tempResult);
                }
                bracketCount--;
            } else {                //if brackets don't exist
                ArrayList<String> checkList = new ArrayList<>(Arrays.asList(newLine.toString().split("\\s+")));
                //Division by 0
                if (checkList.contains("/") && checkList.contains("0") && checkList.indexOf("0") == checkList.indexOf("/") + 1) {
                    return getResources().getString(R.string.division_by_0);
                }
                tempResult = Calculate(checkList);
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
                } else if (arrayList.get(iterator.nextIndex()).equals("÷")) {
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

        //Circumvent issue of double precision loss by shortening decimal output
        if (arrayList.get(0).contains(".")) {
            if (arrayList.get(0).substring(arrayList.get(0).indexOf('.'), arrayList.get(0).length()).length() > 13) {
                DecimalFormat decimalFormat = new DecimalFormat("#.#############");
                decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
                return Double.parseDouble(decimalFormat.format(Double.parseDouble(arrayList.get(0))));
            }
        }

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
