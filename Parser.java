//import com.sun.deploy.util.StringUtils;

import org.tartarus.snowball.ext.PorterStemmer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    public HashMap<String,String> StopWords = new HashMap<String, String>();
    public HashMap<String,TermIndex> all_terms = new HashMap<String, TermIndex>();
    public static HashMap<String,DocIndex> doc_info = new HashMap<String, DocIndex>();
    String st;
    boolean stem;
    Indexer indexer;
    public Parser(boolean stem){
        this.stem = stem;
    }
    public Parser(String path,boolean stem) {
        this.stem = stem;
        //building the Stop Word dictionary
        String word;
        File text = new File(path+"\\05 stop_words.txt");
        Scanner scanner = null;
        try {
            scanner = new Scanner(text);
            if(scanner.hasNextLine()){
                word = scanner.nextLine();

                while(!word.equals("zero")){
                    if(!StopWords.containsKey(word)){
                        StopWords.put(word,null);
                    }
                    word = scanner.nextLine();


                }
                StopWords.put(word,null);
                System.out.println("Stop-Words Done!");
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static int getNumOfDocs(){
        int ans = doc_info.size();
        return ans;
    }
    public HashMap<String,TermIndex> createTerms(Documents docs) {
        HashMap<String, TermIndex> terms = new HashMap<String, TermIndex>();
        String lower = "";
        int counter = 1;
        int x = 0;
        int y = 0 ;
        String temp = "";
        String temp2 = "";
        String date1 = "";//for the format of MM-DD
        String date2= "";//for the format of YYYY-MM
        String percent = "";
        String price = "";
        String num = "";
        String Seconds = "";
        boolean isInt = false;
        boolean isInt2 = false;
        int max = 0;
        for (Map.Entry doc : docs.All_Documents.entrySet()) {
            String token = "";
            String text = (String) doc.getValue();
            StringTokenizer tokenizer = new StringTokenizer(text);
            //start token parsing
            while (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken();

                token= fixWord(token);

                //checking if the token is int
                try {
                    x = Integer.parseInt(token);
                    isInt = true;

                }catch (NumberFormatException e){
                    //not int
                    isInt=false;
                }
                //section dealing if token is Int
                if(isInt){
                    if (isNumberToKeep(x)){
                        if(tokenizer.hasMoreTokens()) {
                            temp = tokenizer.nextToken();
                        }
                        if(isDay(x)){
                            try {//checking if the temp is int

                                temp = fixWord(temp);
                                y = Integer.parseInt(temp);
                                isInt2 = true;

                            } catch (NumberFormatException e) {
                                //not int
                                isInt2 = false;
                            }
                            if(isInt2){
                                // example 12 1992
                                if(isNumMonth(x) && isYear(y) ){
                                    date1 = y+"-"+x ;
                                    addToterms(date1, terms, doc);
                                    continue;

                                }
                                if(isNumMonth(x) && isDay(y) ){
                                    date1 = x+"-"+y ;
                                    addToterms(date1, terms, doc);
                                    continue;

                                }
                                if(isNumMonth(y)){
                                    date1 = y+"-"+x ;
                                    addToterms(date1, terms, doc);
                                    continue;
                                }
                                if(isNumberToKeep(y)){
                                    if(stem)
                                        token = stem(token);
                                    if(token.charAt(0)!='0')
                                        addToterms(token, terms, doc);
                                    if(temp.charAt(0)=='0')
                                        continue;
                                    if(stem)
                                        temp = stem(temp);
                                    temp = fixWord(temp);
                                    addToterms(temp, terms, doc);
                                }
                                else{
                                    if(token.charAt(0)=='0')
                                        continue;
                                    if(stem)
                                        token = stem(token);
                                    addToterms(token, terms, doc);
                                    continue;
                                }
                            }
                            else{
                                if(isMonth(temp)){
                                    date1 = translateDate(temp) +"-"+ x ;
                                    addToterms(date1, terms, doc);
                                    continue;
                                }
                                if(isPercent(temp)){
                                    percent=token+"%";//the format for precent
                                    addToterms(percent,terms,doc);
                                    continue;
                                }
                                if(isPrice(temp)){
                                    price=token+" Dollars";//the format for price
                                    addToterms(price,terms,doc);
                                    continue;
                                }
                                if(isSecond(temp) ){
                                    Seconds = x+"-"+"sec";
                                    addToterms(Seconds,terms,doc);
                                    continue;

                                }
                                if(isKM(temp)){
                                    Seconds = x+"-"+"km";
                                    addToterms(Seconds,terms,doc);
                                    continue;
                                }
                                else{
                                    if(stem)
                                        token = stem(token);
                                    if(token.charAt(0)!='0')
                                        addToterms(token, terms, doc);
                                    if(stem)
                                        temp = stem(temp);
                                    temp = fixWord(temp);
                                    if(isLegalToken(temp)&& !notToAdd(temp))
                                        addToterms(temp, terms, doc);
                                    continue;
                                }
                            }
                        }

                    }// 0<token int <1000
                    if(isYear(x)){
                        if(tokenizer.hasMoreTokens()) {
                            temp = tokenizer.nextToken();
                        }
                        try {//checking if the temp is int

                            temp = fixWord(temp);
                            y = Integer.parseInt(temp);
                            isInt2 = true;

                        } catch (NumberFormatException e) {
                            //not int
                            isInt2 = false;
                        }
                        if(isInt2  && isMonth(temp) ){
                            date1 = x+"-"+y ;
                            addToterms(date1, terms, doc);
                            continue;
                        }
                        else{
                            if(isMonth(temp)){
                                date1 =  x +"-"+translateDate(temp) ;
                                addToterms(date1, terms, doc);
                                continue;
                            }
                            if(isPrice(temp)){
                                price=token+" Dollars";//the format for price
                                addToterms(price,terms,doc);
                                continue;
                            }
                            if(1000<=x  || temp == "Thousand"){
                                y = x/1000;
                                num = y+"K";
                                addToterms(num,terms,doc);
                                continue;
                            }
                            else{
                                if(stem)
                                    temp = stem(temp);
                                temp = fixWord(temp);
                                if(isLegalToken(temp)&& !notToAdd(temp))
                                    addToterms(temp, terms, doc);
                                continue;
                            }

                        }

                    } // 1800<token int<2020
                    else{
                        if(tokenizer.hasMoreTokens()) {
                            temp = tokenizer.nextToken();
                        }

                        if(1000<=x && x<1000000 && temp == "Thousand"){
                            y = x/1000;
                            num = y+"K";
                            addToterms(num,terms,doc);
                            continue;
                        }
                        if(100000<=x && x<1000000000 && temp == "Million"){
                            y = x/1000000;
                            num = y+"M";
                            addToterms(num,terms,doc);
                            continue;
                        }
                        if(x>1000000000 && temp =="Billion"){
                            y = x/1000000000;
                            num = y+"B";
                            addToterms(num,terms,doc);
                            continue;
                        }
                        if(isPrice(temp)){
                            price=token+" Dollars";//the format for price
                            addToterms(price,terms,doc);
                            continue;
                        }

                        else{
                            if(stem)
                                temp = stem(temp);
                            temp = fixWord(temp);
                            if(isLegalToken(temp)&& !notToAdd(temp))
                                addToterms(temp, terms, doc);
                            continue;
                        }
                    }//1001<tokenint<1799 or tokenint>2020

                }

                else{//token not int
                    if(isMonth(token)){
                        if(tokenizer.hasMoreTokens()) {
                            temp = tokenizer.nextToken();
                        }
                        try {//checking if the temp is int

                            temp = fixWord(temp);
                            y = Integer.parseInt(temp);
                            isInt2 = true;

                        } catch (NumberFormatException e) {
                            //not int
                            isInt2 = false;

                        }
                        if(isInt2){
                            if(isYear(y)){
                                date1 =  y +"-"+translateDate(token) ;
                                addToterms(date1, terms, doc);
                                continue;

                            }
                            if(isDay(y)){
                                date1 =translateDate(token)+"-"+ y  ;
                                addToterms(date1, terms, doc);
                                continue;
                            }
                            if(isNumberToKeep(y)){
                                if(stem)
                                    token = stem(token);
                                addToterms(token, terms, doc);
                                if(stem)
                                    temp = stem(temp);
                                temp = fixWord(temp);
                                if(temp.charAt(0)=='0')
                                    continue;
                                addToterms(temp, terms, doc);
                                continue;
                            }
                            else{
                                if(stem)
                                    token = stem(token);
                                if(isLegalToken(token)&& !notToAdd(token))
                                    addToterms(token, terms, doc);
                                continue;
                            }
                        }
                        else{

                            if(stem)
                                token = stem(token);
                            if(isLegalToken(token)&& !notToAdd(token))
                                addToterms(token, terms, doc);
                            if(stem)
                                temp = stem(temp);
                            temp = fixWord(temp);
                            if(isLegalToken(temp)&& !notToAdd(temp))
                                addToterms(temp, terms, doc);
                            continue;
                        }
                    }//end of token month like DEC
                    else{
                        if(token.contains("$")){
                            if(tokenizer.hasMoreTokens())
                                temp = tokenizer.nextToken();
                            token = token.replace("$", "");
                            if(temp.equals("million") || temp.equals("Million")){
                                token = token +" M Dollars";
                                addToterms(token,terms,doc);
                                continue;
                            }
                            if(temp.equals("billion") || temp.equals("Billion")){
                                token = token +" B Dollars";
                                addToterms(token,terms,doc);
                                continue;
                            }
                        }

                        else{
                            if(stem)
                                token = stem(token);
                            if(isLegalToken(token)&& !notToAdd(token)){
                                addToterms(token, terms, doc);
                                continue;
                            }
                        }
                    }
                }
            }





            // System.out.println(terms.size());
            max=0;
            for (Map.Entry<String,TermIndex> term:terms.entrySet()) {
                if(term.getValue().NumOfDocs >max){
                    max = term.getValue().NumOfDocs;
                }
            }
            DocIndex di = new DocIndex(terms.size(),max);
            doc_info.put(doc.getKey().toString(),di);
        }

        return terms;
    }
    public void addToterms(String token, HashMap<String, TermIndex> terms,Map.Entry doc){

        boolean existsInLowerCase = false;
        boolean existInUpperCase = false;
        String lowerCaseCheck = "";
        String upperCaseCheck = "";
        String temp = "";
        if (Character.isUpperCase(token.charAt(0))) {

            lowerCaseCheck = token.toLowerCase();
            if (terms.containsKey(lowerCaseCheck) || all_terms.containsKey(lowerCaseCheck)) {
                existsInLowerCase = true;
            }
            if (existsInLowerCase) {
                token = lowerCaseCheck;
            }

        }

        else  if(Character.isLowerCase(token.charAt(0))){
            upperCaseCheck=token.toUpperCase();
            temp = upperCaseCheck.substring(0,1);
            upperCaseCheck=temp+token.substring(1);

            if(terms.containsKey(upperCaseCheck) ){
                TermIndex termIndexToKeep = terms.get(upperCaseCheck);
                terms.remove(upperCaseCheck);
                terms.put(token,termIndexToKeep);
            }
            if(all_terms.containsKey(upperCaseCheck)){
                TermIndex termIndexToKeep = all_terms.get(upperCaseCheck);
                all_terms.remove(upperCaseCheck);
                all_terms.put(token,termIndexToKeep);
            }
        }



        if (!terms.containsKey(token)) {
            TermIndex t = new TermIndex();
            t.NumOfDocs = 1;
            t.frequency = 1 ;
            t.fts2.put((String)doc.getKey(),1);
            terms.put(token, t);
            all_terms.put(token,t);
        }
        else {// if term exist

            TermIndex ti = terms.get(token);
            //if term appear again in same document
            if(ti.fts2.containsKey((String)doc.getKey())){
                Integer  feqInDoc = ti.fts2.get((String)doc.getKey());
                ti.fts2.put((String)doc.getKey(),feqInDoc+1);

            }
            // term appear again but in a new document
            else {
                // counter = ti.fts2.size();
                ti.NumOfDocs = ti.NumOfDocs + 1;
                // counter++;
                ti.fts2.put((String) doc.getKey(), 1);
            }
            ti.frequency = ti.frequency + 1 ;
            // terms.put(token,ti);
            all_terms.put(token,ti);
        }
    }
    public String stem(String token){
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(token); //set string you need to stem
        stemmer.stem();  //stem the word
        token = stemmer.getCurrent();//get the stemmed word
        return token;

    }


    public boolean notToAdd(String string){
        if(st==null)
            return false;
        if(st.equals("Article") || st.equals("[Text]")|| st.equals("<p>")|| st.equals("</p>") || st.equals("Type:BFN")|| st.equals("Text")){
            return true;
        }
        if(st.contains("(")|| st.contains(")")){
            return true;
        }
        return false;
    }

    public boolean ifStopWord(String name){
        name = name.toLowerCase();
        if(StopWords.containsKey(name) )
            return true;
        else
            return false;


    }




    public boolean isMonth(String s){
        if(s.equals("JANUARY") || s.equals("FEBRUARY") || s.equals("MARCH") || s.equals("APRIL") ||
                s.equals("MAY") || s.equals("JUNE") || s.equals("JULY") || s.equals("AUGUST") || s.equals("SEPTEMBER")
                || s.equals("OCTOBER") || s.equals("NOVEMBER") || s.equals("DECEMBER")){
            return true;
        }
        if(s.equals("January") || s.equals("February") || s.equals("March") || s.equals("April") ||
                s.equals("May") || s.equals("June") || s.equals("July") || s.equals("August") || s.equals("September")
                || s.equals("October") || s.equals("November") || s.equals("December")){
            return true;
        }
        if(s.equals("january") || s.equals("february") || s.equals("march") || s.equals("april")||
                s.equals("may") || s.equals("june") || s.equals("july") || s.equals("august") || s.equals("september")
                || s.equals("october") || s.equals("november") || s.equals("december")){
            return true;
        }
        return false;
    }
    public String translateDate(String s){
        if(s.equals("JANUARY") || s.equals("January") || s.equals("january")){
            return "01";
        }
        if(s.equals("FEBRUARY") || s.equals("February") || s.equals("february")){
            return "02";
        }
        if(s.equals("MARCH") || s.equals("March") || s.equals("march")){
            return "03";
        }
        if(s.equals("APRIL") || s.equals("April") || s.equals("april")){
            return "04";
        }
        if(s.equals("MAY") || s.equals("May") || s.equals("may")){
            return "05";
        }
        if(s.equals("JUNE") || s.equals("June") || s.equals("june")){
            return "06";
        }
        if(s.equals("JULY") || s.equals("July") || s.equals("july")){
            return "07";
        }
        if(s.equals("AUGUST") || s.equals("August") || s.equals("august")){
            return "08";
        }
        if(s.equals("SEPTEMBER") || s.equals("September") || s.equals("september")){
            return "09";
        }
        if(s.equals("OCTOBER") || s.equals("October") || s.equals("october")){
            return "10";
        }
        if(s.equals("NOVEMBER")|| s.equals("November") || s.equals("november")){
            return "11";
        }
        if(s.equals("DECEMBER")|| s.equals("December") || s.equals("december")){
            return "12";
        }
        return "";
    }

    public boolean isPrice(String s){
        if (s.equals("$") || s.equals("U.S. Dollars") || s.equals("Dollars")){
            return true;
        }
        return false;
    }

    public boolean isPercent(String s){
        if(s.equals("%") || s.equals("percent") || s.equals("percentage")){
            return true;
        }
        return false;
    }

    // fix tokens before adding them to tree map
    public String fixWord(String token){
        if(token.contains(",")){
            token = token.replaceAll(",","");
        }
        if(token.contains(";")){
            token = token.replaceAll(";","");
        }
        if( token.length()>0 &&  token.charAt(token.length()-1)=='.') {
            token = token.substring(0,token.length()-1);
        }
        if(token.contains("]")){
            token = token.replaceAll("]","");
        }
        if(token.contains("'")){
            token = token.replaceAll("'","");
        }
        if( token.length()>0 &&  token.charAt(token.length()-1)=='-') {
            token = token.substring(0,token.length()-1);
        }
        if( token.length()>0 &&  token.charAt(0)=='-') {
            token = token.substring(1,token.length());
        }
        if( token.length()>0 &&  token.charAt(token.length()-1)=='"') {
            token = token.substring(0,token.length()-1);
        }
        if( token.length()>0 &&  token.charAt(0)=='"') {
            token = token.substring(1,token.length());
        }
        if( token.length()>0 &&  token.charAt(token.length()-1)==')') {
            token = token.substring(0,token.length()-1);
        }
        if(token.contains(":")){
            token = token.replaceAll(":","");
        }
        if( token.length()>0 &&   token.charAt(0)=='?')
            token = token.substring(1,token.length());
        //  if (token.length()>0   &&   Character.isUpperCase(token.charAt(0))) {
        //     token = token.toLowerCase();

        // }

        return token;

    }
    // elemenates wrong tokens
    public boolean isLegalToken (String token){
        Pattern p = Pattern.compile("([0-9])");
        Matcher m = p.matcher(token);

        if (m.find() || token.contains(")") ){
            return false;

        }
        if (m.find() || token.contains("-") ){
            return false;
        }
        if(token.contains("/") || token.contains("!") || token.contains("?"))
            return false;
        if( token.length() > 0 &&   (token.charAt(0)=='(' || token.charAt(0)=='.' || token.charAt(0)=='-') )
            return false;
        if(token.equals(""))
            return false;
        if (token.contains("&") || token.equals(" ") )
            return false;

        else
            return true;



    }

    public boolean isYear (Integer yearToCheck ){
        if (yearToCheck>=1800 && yearToCheck<=2020 )
            return true;
        else
            return  false ;
    }

    public boolean isDay (Integer dayToCheck ){
        if (dayToCheck>=1 && dayToCheck<=31 )
            return true;
        else
            return  false ;
    }

    public boolean isNumberToKeep(Integer numToCheck){
        if (numToCheck>= 0 && numToCheck<=1000)
            return true;
        else
            return false;
    }
    public boolean isNumMonth(Integer num){
        if(num>=1 && num<=12)
            return true;
        else
            return false;
    }
    public boolean isSecond(String temp){
        if(temp.equals("sec")|| temp.equals("Sec") || temp.equals("seconds") || temp.equals("Seconds")||temp.equals("second")||temp.equals("Second") )
            return true;
        else
            return false;
    }

    public boolean isKM(String temp){
        if ( temp.equals("KM")||temp.equals("km")||temp.equals("Km")||temp.equals("Kilometer")||temp.equals("kilometer")||temp.equals("kilometers")||temp.equals("Kilometers"))
            return true;
        else
            return true;
    }

}
