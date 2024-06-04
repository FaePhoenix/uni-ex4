package fae;


import java.io.File;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;



public class FileHelper {
    private ArrayList<String> content;


    public FileHelper() {
        this.setEmpty();
    }


    public FileHelper(String fileName) {
        this.content = new ArrayList<String>();
        File openedFile = new File(fileName);
        try {
            //Prepare File reading
            try (BufferedReader reader = new BufferedReader(new FileReader(openedFile))){
                //Reading FileContent into Array
                
                String read = "";
                while(read != null) {
                    read = reader.readLine();
                    this.content.add(read);
                }

                //remove null from fileContentArray
                this.content.remove(content.size() - 1);
            }
        
        //Handle Exceptions
        } catch (NullPointerException | IOException exception) {
        System.out.println("Encountered Error Opening File, object will be empty");
        System.out.println(exception);
        exception.printStackTrace();
        this.setEmpty();
        }
    }


    public FileHelper(ArrayList<String> content){
        this.content = content;
    }


    public void setEmpty(){
        this.content = new ArrayList<String>();
    }


    public ArrayList<String> getContent(){
        return this.content;
    }


    public Boolean saveToFile(String fileName){
        File fileToWriteTo = new File(fileName);
        if (!fileToWriteTo.isFile()) {
            try {
                fileToWriteTo.createNewFile();
            } catch (IOException exception) {return false;}
        }
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileToWriteTo))) {
            String toWrite = String.join("\n", this.content);
            fileWriter.write(toWrite);
        } catch (IOException exception) {
            return false;
        }
        return true;
    }
}


