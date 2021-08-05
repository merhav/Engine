import org.tartarus.snowball.ext.PorterStemmer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Searcher {

    HashMap<String,Integer> query_terms;
    String query_text;
    boolean stemming;
    HashSet<String> stop_words;
    String work_path;
    String save_path;



    public Searcher(String work_path,String save_path,boolean stemming){
        this.stemming = stemming;
        stop_words = new HashSet<String>();
        this.work_path = work_path;
        this.save_path = save_path;

    }
    public void setStop_words(String path) throws FileNotFoundException {
        File file = new File(path+"\\05 stop_words.txt");
        Scanner scanner = new Scanner(file);
        String line = scanner.nextLine();
        while(!line.equals("zero")){
            if(line.equals("<top>")|| line.equals(" ")|| line.equals("")){
                line = scanner.nextLine();
                continue;
            }
            stop_words.add(line);
            line = scanner.nextLine();
        }
        stop_words.add(line);
        scanner.close();
    }


    public Query parse(Query query, boolean stemming) throws FileNotFoundException {
        this.stemming = stemming;
        setStop_words(work_path);
        query_text = query.getTitle();
        query_terms = new HashMap<String, Integer>();
        String charToDel = "~`!@#^&*(){}|+=[]';:?";
        charToDel += '"';
        String pat = "[" + Pattern.quote(charToDel) + "]";
        String removeChars = query_text.replaceAll(pat, " ");
        String[] words = removeChars.split("\\s+");
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
        for ( String word : words) {
            String token = "";
            String text = word;
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
                                    addToterms(date1, query_terms);
                                    continue;

                                }
                                if(isNumMonth(x) && isDay(y) ){
                                    date1 = x+"-"+y ;
                                    addToterms(date1, query_terms);
                                    continue;

                                }
                                if(isNumMonth(y)){
                                    date1 = y+"-"+x ;
                                    addToterms(date1, query_terms);
                                    continue;
                                }
                                if(isNumberToKeep(y)){
                                    if(stemming)
                                        token = stem(token);
                                    if(token.charAt(0)!='0')
                                        addToterms(token, query_terms);
                                    if(temp.charAt(0)=='0')
                                        continue;
                                    if(stemming)
                                        temp = stem(temp);
                                    temp = fixWord(temp);
                                    addToterms(temp, query_terms);
                                }
                                else{
                                    if(token.charAt(0)=='0')
                                        continue;
                                    if(stemming)
                                        token = stem(token);
                                    addToterms(token, query_terms);
                                    continue;
                                }
                            }
                            else{
                                if(isMonth(temp)){
                                    date1 = translateDate(temp) +"-"+ x ;
                                    addToterms(date1, query_terms);
                                    continue;
                                }
                                if(isPercent(temp)){
                                    percent=token+"%";//the format for precent
                                    addToterms(percent,query_terms);
                                    continue;
                                }
                                if(isPrice(temp)){
                                    price=token+" Dollars";//the format for price
                                    addToterms(price,query_terms);
                                    continue;
                                }
                                if(isSecond(temp) ){
                                    Seconds = x+"-"+"sec";
                                    addToterms(Seconds,query_terms);
                                    continue;

                                }
                                if(isKM(temp)){
                                    Seconds = x+"-"+"km";
                                    addToterms(Seconds,query_terms);
                                    continue;
                                }
                                else{
                                    if(stemming)
                                        token = stem(token);
                                    if(token.charAt(0)!='0')
                                        addToterms(token, query_terms);
                                    if(stemming)
                                        temp = stem(temp);
                                    temp = fixWord(temp);
                                    if(isLegalToken(temp)&& !notToAdd(temp))
                                        addToterms(temp, query_terms);
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
                            addToterms(date1, query_terms);
                            continue;
                        }
                        else{
                            if(isMonth(temp)){
                                date1 =  x +"-"+translateDate(temp) ;
                                addToterms(date1, query_terms);
                                continue;
                            }
                            if(isPrice(temp)){
                                price=token+" Dollars";//the format for price
                                addToterms(price,query_terms);
                                continue;
                            }
                            if(1000<=x  || temp == "Thousand"){
                                y = x/1000;
                                num = y+"K";
                                addToterms(num,query_terms);
                                continue;
                            }
                            else{
                                if(stemming)
                                    temp = stem(temp);
                                temp = fixWord(temp);
                                if(isLegalToken(temp)&& !notToAdd(temp))
                                    addToterms(temp, query_terms);
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
                            addToterms(num,query_terms);
                            continue;
                        }
                        if(100000<=x && x<1000000000 && temp == "Million"){
                            y = x/1000000;
                            num = y+"M";
                            addToterms(num,query_terms);
                            continue;
                        }
                        if(x>1000000000 && temp =="Billion"){
                            y = x/1000000000;
                            num = y+"B";
                            addToterms(num,query_terms);
                            continue;
                        }
                        if(isPrice(temp)){
                            price=token+" Dollars";//the format for price
                            addToterms(price,query_terms);
                            continue;
                        }

                        else{
                            if(stemming)
                                temp = stem(temp);
                            temp = fixWord(temp);
                            if(isLegalToken(temp)&& !notToAdd(temp))
                                addToterms(temp, query_terms);
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
                                addToterms(date1, query_terms);
                                continue;

                            }
                            if(isDay(y)){
                                date1 =translateDate(token)+"-"+ y  ;
                                addToterms(date1, query_terms);
                                continue;
                            }
                            if(isNumberToKeep(y)){
                                if(stemming)
                                    token = stem(token);
                                addToterms(token, query_terms);
                                if(stemming)
                                    temp = stem(temp);
                                temp = fixWord(temp);
                                if(temp.charAt(0)=='0')
                                    continue;
                                addToterms(temp, query_terms);
                                continue;
                            }
                            else{
                                if(stemming)
                                    token = stem(token);
                                if(isLegalToken(token)&& !notToAdd(token))
                                    addToterms(token, query_terms);
                                continue;
                            }
                        }
                        else{

                            if(stemming)
                                token = stem(token);
                            if(isLegalToken(token)&& !notToAdd(token))
                                addToterms(token, query_terms);
                            if(stemming)
                                temp = stem(temp);
                            temp = fixWord(temp);
                            if(isLegalToken(temp)&& !notToAdd(temp))
                                addToterms(temp, query_terms);
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
                                addToterms(token,query_terms);
                                continue;
                            }
                            if(temp.equals("billion") || temp.equals("Billion")){
                                token = token +" B Dollars";
                                addToterms(token,query_terms);
                                continue;
                            }
                        }

                        else{
                            if(stemming)
                                token = stem(token);
                            if(isLegalToken(token)&& !notToAdd(token)){
                                addToterms(token, query_terms);
                                continue;
                            }
                        }
                    }
                }
            }
        }
        query.q_terms = query_terms;
        return query;
    }

    public void addToterms(String token, HashMap<String, Integer> terms){
        int frequency;
        boolean existsInLowerCase = false;
        boolean existInUpperCase = false;
        String lowerCaseCheck = "";
        String upperCaseCheck = "";
        String temp = "";
        if (Character.isUpperCase(token.charAt(0))) {

            lowerCaseCheck = token.toLowerCase();
            if (terms.containsKey(lowerCaseCheck) || terms.containsKey(lowerCaseCheck)) {
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
                frequency = terms.get(upperCaseCheck);
                terms.remove(upperCaseCheck);
                frequency = frequency+1;
                terms.put(token,frequency);
            }
            if(terms.containsKey(upperCaseCheck)){
                frequency = terms.get(upperCaseCheck);
                terms.remove(upperCaseCheck);
                frequency = frequency+1;
                terms.put(token,frequency);
            }
        }



        if (!terms.containsKey(token)) {//if term does not exist
            terms.put(token, 1);
        }
        else {// if term exist

            frequency = terms.get(token);
            frequency = frequency + 1 ;
            terms.remove(token);
            terms.put(token,frequency);
        }
    }

    public String stem(String token){
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(token); //set string you need to stem
        stemmer.stem();  //stem the word
        token = stemmer.getCurrent();//get the stemmed word
        return token;

    }


    public boolean notToAdd(String st){
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
        if(stop_words.contains(name) )
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
