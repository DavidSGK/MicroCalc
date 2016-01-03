package com.davidsgk.microcalc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    //different classifications of buttons
    private static Button[] numButtons = new Button[10];        //0,1,2,3,4,5,6,7,8,9
    private static Button[] operatorButtons = new Button[7];    //+,-,*,/,(,),^
    private static Button decimalButton;                        //.
    private static Button returnButton;                         //=
    private static Button deleteButton;                         //backspace/clear for long press
    private static TextView output;                             //outputs all input and answer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = (TextView) findViewById(R.id.output);
        output.setMovementMethod(new ScrollingMovementMethod());

        initButtons();
    }

    //initialize buttons and their functions
    protected void initButtons() {
        int id;
        for(int i = 0; i < 10; i++){
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
        returnButton = (Button) findViewById(R.id.button_return);
        deleteButton = (Button) findViewById(R.id.button_del);

        for(int j = 0; j < 10; j++){
            numButtons[j].setOnClickListener(
                    new Button.OnClickListener(){
                        public void onClick(View v){
                            output.setText(output.getText().toString() + ((Button) v).getText().toString());
                            ScrollToBottom();
                        }
                    }
            );
        }

        for(int k = 0; k < 7; k++){
            operatorButtons[k].setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            output.setText(output.getText().toString() + " " + ((Button) v).getText().toString() + " ");
                        }
                    }
            );
        }

        decimalButton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        output.setText(output.getText().toString() + ".");
                    }
                }
        );

        returnButton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        output.setText(output.getText().toString() + " =\n");
                        output.setText(output.getText().toString() + Interpreter(CurrentLine()) + "\n");
                        ScrollToBottom();
                    }
                }
        );

        deleteButton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        String text = output.getText().toString();
                        if(text.length() != 0 && text.charAt(text.length() - 1) != '\n'){
                            output.setText(output.getText().subSequence(0, output.getText().length() - 1));
                        }
                    }
                }
        );

        deleteButton.setOnLongClickListener(
                new Button.OnLongClickListener(){
                    public boolean onLongClick(View v){
                        output.setText("");
                        return true;
                    }
                }
        );
    }

    //method to make the output automatically scroll to the bottom
    protected static void ScrollToBottom() {
        final Layout layout = output.getLayout();
        if(layout != null){
            int scrollDelta = layout.getLineBottom(output.getLineCount() - 1) - output.getScrollY() - output.getHeight();
            if(scrollDelta > 0) output.scrollBy(0, scrollDelta);
        }
    }

    //method to reference just last line in TextView
    protected static String CurrentLine() {
        String text = output.getText().toString();
        String currentLine;
        int lineCount = 0;
        for(int i = 0; i < text.length(); i++){
            if(text.charAt(i) == '\n') lineCount++;
        }

        if(lineCount != 1) {
            currentLine = text.substring(text.lastIndexOf('\n', text.length() - 2) + 1, text.length() - 2);
        } else {
            currentLine = text.substring(0, text.length() - 2);
        }
        return currentLine;
    }

    //method to perform the calculations based on input
    protected String Interpreter(String line) {
        String[] splitArray;
        if(line.charAt(0) == ' '){
            splitArray = line.substring(1, line.length() - 1).split("\\s+");
        } else {
            splitArray = line.split("\\s+");
        }

        System.out.println(line);
        for(String piece:splitArray){
            System.out.print(piece);
        }
        System.out.print("\n");
        //Check for syntax errors
        int count1 = 0;
        int count2 = 0;
        int bracketCount = 0;

        //The number of ( and ) must be the same
        for(String piece : splitArray){
            if(piece.equals("(")) count1++;
            if(piece.equals(")")) count2++;
        }
        if(count1 != count2) {
            System.out.println(1);
            return "Syntax Error";
        } else {
            bracketCount = count1;
        }

        //Can't begin or end with non-number characters (other than brackets)
        //Note: All non-number characters other than the decimal point adds spaces to either side
        if(!isNumeric(splitArray[0])){
            if(!splitArray[0].equals("(")){
                System.out.println(-1);
                System.out.println(splitArray[0]);
                return "Syntax Error";
            }
        }
        if(!isNumeric(splitArray[splitArray.length - 1])){
            if(!splitArray[splitArray.length - 1].equals(")")){
                System.out.println(-2);
                return "Syntax Error";
            }
        }

        //Can't have the first ) before ( and can't have the last ( after )
        if(Arrays.asList(splitArray).indexOf(")") < Arrays.asList(splitArray).indexOf("(") || Arrays.asList(splitArray).lastIndexOf("(") > Arrays.asList(splitArray).lastIndexOf(")")){
            return "Syntax Error";
        }

        //Can't have two numbers or non-numbers side by side
        for(int i = 1; i < splitArray.length; i++){
            if((isNumeric(splitArray[i]) && isNumeric(splitArray[i-1])) || (!isNumeric(splitArray[i]) && !isNumeric(splitArray[i-1]))){
                //Unless it's a ) directly followed by (
                if(!(splitArray[i].equals("(") && splitArray[i-1].equals(")"))) {
                    System.out.println(3);
                    return "Syntax Error";
                }
            }
        }

        //Interpretation & calculation
        StringBuilder newLine = new StringBuilder(line);

        int nonNumCount;

        while(true) {
            System.out.println("newLine: " + newLine);
            nonNumCount = 0;
            for (int i = 0; i < newLine.length(); i++) {
                if (!isNumeric(newLine.charAt(i)) && newLine.charAt(i) != '.' && newLine.charAt(i) != ' ' && newLine.charAt(i) != ' ') {
                    nonNumCount++;
                }
            }

            if(nonNumCount == 0) break;

            System.out.println("nonNumCount: " + nonNumCount);

            int startBracketIndex = newLine.lastIndexOf("(", newLine.indexOf(")"));
            int endBracketIndex = newLine.indexOf(")");

            ArrayList<String> checkList = new ArrayList<>(Arrays.asList(newLine.substring(startBracketIndex + 2, endBracketIndex).split("\\s+")));
            System.out.println("substring: " + newLine.substring(startBracketIndex, endBracketIndex + 1));
            newLine.delete(startBracketIndex, endBracketIndex + 1);
            newLine.insert(startBracketIndex, Calculate(checkList));
        }

        return newLine.toString().replace(' ', '\0');
    }

    protected double Calculate(ArrayList<String> arrayList) {
        double tempNum;

        ListIterator iterator;

        //Exponent
        iterator = arrayList.listIterator();
        while(iterator.hasNext()){
            if(iterator.next().equals("^")){
                iterator.previous();
                tempNum = Math.pow(Double.parseDouble(iterator.previous().toString()), Double.parseDouble(arrayList.get(iterator.nextIndex()+2)));
                iterator.remove();
                iterator.next();
                iterator.remove();
                iterator.next();
                iterator.remove();
                iterator.add(Double.toString(tempNum));
            }
        }

        //Multiplication
        iterator = arrayList.listIterator();
        while(iterator.hasNext()){
            if(iterator.next().equals("x")){
                iterator.previous();
                tempNum = Double.parseDouble(iterator.previous().toString()) * Double.parseDouble(arrayList.get(iterator.nextIndex()+2));
                iterator.remove();
                iterator.next();
                iterator.remove();
                iterator.next();
                iterator.remove();
                iterator.add(Double.toString(tempNum));
            }
        }

        //Division
        iterator = arrayList.listIterator();
        while(iterator.hasNext()){
            if(iterator.next().equals("/")){
                iterator.previous();
                tempNum = Double.parseDouble(iterator.previous().toString()) / Double.parseDouble(arrayList.get(iterator.nextIndex()+2));
                iterator.remove();
                iterator.next();
                iterator.remove();
                iterator.next();
                iterator.remove();
                iterator.add(Double.toString(tempNum));
            }
        }

        //Addition
        iterator = arrayList.listIterator();
        while(iterator.hasNext()){
            if(iterator.next().equals("+")){
                iterator.previous();
                tempNum = Double.parseDouble(iterator.previous().toString()) + Double.parseDouble(arrayList.get(iterator.nextIndex()+2));
                iterator.remove();
                iterator.next();
                iterator.remove();
                iterator.next();
                iterator.remove();
                iterator.add(Double.toString(tempNum));
            }
        }

        //Subtraction
        iterator = arrayList.listIterator();
        while(iterator.hasNext()){
            if(iterator.next().equals("-")){
                iterator.previous();
                tempNum = Double.parseDouble(iterator.previous().toString()) - Double.parseDouble(arrayList.get(iterator.nextIndex()+2));
                iterator.remove();
                iterator.next();
                iterator.remove();
                iterator.next();
                iterator.remove();
                iterator.add(Double.toString(tempNum));
            }
        }

        System.out.println("Size: " + arrayList.size());
        System.out.println(arrayList.get(0));
        return Double.parseDouble(arrayList.get(0));
    }

    public boolean isNumeric(String text) {
        try {
            double number = Double.parseDouble(text);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public boolean isNumeric(char text) {
        try {
            double number = Double.parseDouble(Character.toString(text));
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
