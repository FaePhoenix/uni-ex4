package fae;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class FSUGenBank {
    private Fasta fasta;
    private String accessionNumbers;
    private String sequenceVersion;
    private String organismSpecies;
    private String keywords;
    private String description;
    private String idxLine;


    public FSUGenBank() {
        this.setEmpty();
    }

    
    public FSUGenBank(String fileName){
        FileHelper helper = new FileHelper(fileName);
        ArrayList<String> content = helper.getContent();

        this.fromStr(content);
    }


    private void fromStr(ArrayList<String> lines){
        //extract indices
        String[] indices = lines.get(0).split(";");
        int endOfComments = Integer.valueOf(indices[0]);
        int endOfSequence = Integer.valueOf(indices[1]);

        //extracting Fasta
        String fastaHeader = lines.get(1);
        ArrayList<String> fastaComments;
        if (endOfComments != 1) {
            fastaComments = new ArrayList<String>(lines.subList(2, endOfComments + 1));
        } else { 
            fastaComments = new ArrayList<String>();
        }
        List<String> sequenceLines = lines.subList(endOfComments + 1, endOfSequence + 1);
        String fastaSequence = String.join("", sequenceLines).toUpperCase();
        this.fasta = new Fasta(fastaHeader, fastaComments, fastaSequence);

        //extract Attributes
        int currentIdx = endOfSequence + 1;

        this.accessionNumbers = lines.get(currentIdx).replaceAll("AC  ", "");
        currentIdx++;

        this.sequenceVersion = lines.get(currentIdx).replaceAll("SV  ", "");
        currentIdx++;

        this.organismSpecies = lines.get(currentIdx).replaceAll("OS  ", "");
        currentIdx++;

        this.keywords = lines.get(currentIdx).replaceAll("KW  ", "");
        currentIdx++;

        this.description = lines.get(currentIdx).replaceAll("DE  ", "");
    }


    public Fasta getFasta() {return this.fasta;}

    public String getAccessionNumbers() {return this.accessionNumbers;}

    public String getSequenceVersion() {return this.sequenceVersion;}

    public String getOrganismSpecies() {return this.organismSpecies;}

    public String getKeywords() {return this.keywords;}
    
    public String getDescription() {return this.description;}

    private String getIdxLine() {
        String savedIdxLine = this.idxLine;
        if (savedIdxLine == null) {
            this.calcIdxLine();
            return this.idxLine;
        } else {
            return savedIdxLine;
        }
    }


    private void calcIdxLine() {

        //helpingValues
        int ammountsOfComments;
        try{
            ammountsOfComments = this.getFasta().getComments().size();
        } catch(NullPointerException e) {
            ammountsOfComments = 0;
        }
        int sequenceSize = this.getFasta().getSequenceSize();
        int sequenceLines = sequenceSize / 80;

        //test if last line of sequence is not full
        if (sequenceSize % 80 != 0) {
           sequenceLines += 1;
        } 
        
        //Calculate actual lines; File starts w/ line 0
        int endOfComments = ammountsOfComments + 1; 
        int endOfSequence = endOfComments + sequenceLines;

        this.idxLine = String.valueOf(endOfComments) + ";" + String.valueOf(endOfSequence);
    }


    public Boolean saveToFile(String fileName) {

        //Prepare Content
        String content = this.getIdxLine() + "\n" + this.toStr();
        ArrayList<String> toWrite = new ArrayList<String> (Arrays.asList(content.split("\n")));

        //Helper Class writes to File
        FileHelper helper = new FileHelper(toWrite);
        return helper.saveToFile(fileName);
    }
   

    public void setEmpty() {
        this.fasta = new Fasta();
        this.accessionNumbers = "";
        this.sequenceVersion = "";
        this.organismSpecies = "";
        this.keywords = "";
        this.description = "";
    }


    public String toStr() {

        String repr = "";

        repr +=  this.getFasta().toStr() + "\n";

        repr += "AC  " + this.getAccessionNumbers() + "\n";
        
        repr += "SV  " + this.getSequenceVersion() + "\n";

        repr += "OS  " + this.getOrganismSpecies() + "\n";

        repr += "KW  " + this.getKeywords() + "\n";
        
        repr += "DE  " + this.getDescription() + "\n";
        return repr;
    }


    public void EMBLToFSUGenBank(String emblFileName){
        FileHelper helper = new FileHelper(emblFileName);
        ArrayList<String> content = helper.getContent();

        this.emblFromContent(content);
    }


    private void emblFromContent(ArrayList<String> content) {
        //initialize positional arguemnts
        int sequenceStartIdx = -1;
        int accessionIdx = -1;
        int seqVersionIdx = -1;
        int orgSpeciesIdx = -1;
        int keywordsIdx = -1;
        int descriptionIdx = -1;

        //find positions
        int numOfLines = content.size();
        for(int idx = 0; idx < numOfLines; idx++){

            String line = content.get(idx);
            String firstWord = line.split(" ", 2)[0];

            switch (firstWord) {
                case "SQ":
                    sequenceStartIdx = idx + 1;
                    break;
                
                case "AC":
                    accessionIdx = idx;
                    break;
                
                case "SV":
                    seqVersionIdx = idx;
                    break;

                case "OS":
                    orgSpeciesIdx = idx;
                    break;

                case "KW":
                    keywordsIdx = idx;
                    break;

                case "DE":
                    descriptionIdx = idx;
                    break;

                default:
                    break; 
            }
        }

        //extract and set information
        String fastaDescription = ">" + content.get(sequenceStartIdx - 1).replaceAll("\\s+", " ").replaceAll("SQ ", "");
        List<String> sequence = content.subList(sequenceStartIdx, content.size());
        String fastaSequence = String.join("", sequence).replaceAll("[^acgtACGT]", "").toUpperCase();
        this.fasta = new Fasta(fastaDescription, new ArrayList<String>(), fastaSequence);

        this.accessionNumbers = content.get(accessionIdx).replaceAll("AC", "").replaceAll("\\s+", "");

        this.sequenceVersion = content.get(seqVersionIdx).replaceAll("SV", "").replaceAll("\\s+", " ");

        this.organismSpecies = content.get(orgSpeciesIdx).replaceAll("OS", "").replaceAll("\\s+", "");

        this.keywords = content.get(keywordsIdx).replaceAll("KW", "").replaceAll("\\s+", " ");

        this.description = content.get(descriptionIdx).replaceAll("DE", "").replaceAll("\\s+", ""); 
    }


    public void GenBankToFSUGenBank(String genbankFileName){
        FileHelper helper = new FileHelper(genbankFileName);
        ArrayList<String> content = helper.getContent();

        this.genbankFromContent(content);
    }


    private void genbankFromContent(ArrayList<String> content) {
        //initialize positional arguemnts
        int sequenceStartIdx = -1;
        int accessionIdx = -1;
        int seqVersionIdx = -1;
        int orgSpeciesIdx = -1;
        int keywordsIdx = -1;
        int descriptionIdx = -1;


        //find positions
        int numOfLines = content.size();
        for(int idx = 0; idx < numOfLines; idx++){

            String line = content.get(idx);
            String firstWord = line.split(" ", 2)[0];

            switch (firstWord) {
                case "ORIGIN":
                    sequenceStartIdx = idx + 1;
                    break;
                
                case "ACCESSION":
                    accessionIdx = idx;
                    break;
                
                case "VERSION":
                    seqVersionIdx = idx;
                    break;

                case "SOURCE":
                    orgSpeciesIdx = idx;
                    break;

                case "KEYWORDS":
                    keywordsIdx = idx;
                    break;

                case "DEFINITION":
                    descriptionIdx = idx;
                    break;

                default:
                    break; 
            }
        }

        //extract and set information
        String fastaDescription = ">" + content.get(0).replaceAll("\\s+", " ").replaceAll("LOCUS ", "");
        List<String> sequence = content.subList(sequenceStartIdx, content.size());
        String fastaSequence = String.join("", sequence).replaceAll("[^acgtACGT]", "").toUpperCase();
        this.fasta = new Fasta(fastaDescription, new ArrayList<String>(), fastaSequence);

        this.accessionNumbers = content.get(accessionIdx).replaceAll("ACCESSION", "").replaceAll("\\s+", "");

        this.sequenceVersion = content.get(seqVersionIdx).replaceAll("VERSION", "").replaceAll("\\s+", " ");

        this.organismSpecies = content.get(orgSpeciesIdx).replaceAll("SOURCE", "").replaceAll("\\s+", "");

        this.keywords = content.get(keywordsIdx).replaceAll("KEYWORDS", "").replaceAll("\\s+", " ");

        List<String> definitionLines = content.subList(descriptionIdx, accessionIdx);
        this.description = String.join("", definitionLines).replaceAll("DEFINITION", "").replaceAll("\\s+", "");     
    }
}