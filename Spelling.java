import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Spelling {
    class trieNode{
        String trieWord;
        String trieWordCorrect; // for mispelled word
        long frequency;
        boolean isWord;
        trieNode[] children;
        trieNode(String a){
            trieWord = a;
            frequency = 0;
            isWord = false;
            children = new trieNode[26];
            trieWordCorrect = "";
        }
    }
    trieNode root;
    Spelling(){
        root = new trieNode("");
    }
    // insert the whole word into the tree
    trieNode insert(String word, Long freq  ){
        trieNode curr = root;
        for(int i =0; i<word.length();i++){
            char c = word.charAt(i);
            String prefix = word.substring(0,i+1);
            int idx = c-'a';
            if(curr.children[idx]== null) {
                curr.children[idx] = new trieNode(prefix);
                //curr.children[idx].trieWord = prefix;
            }
            curr = curr.children[idx];
        }
        curr.frequency = freq;
        curr.isWord = true;
        curr.trieWord = word;
        return curr;
    }
    //search the node of the word in the tree
    public trieNode Search(String word){
        //misspell may have capital letters
        word = word.toLowerCase();
        trieNode curr = root;
        for(int i =0; i< word.length();i++){
            char c = word.charAt(i);
            int idx = c- 'a';
            if(curr.children[idx]== null){
                return null;
            }
            curr = curr.children[idx];
        }
        return curr;
    }
    public trieNode MarkWrong(String correct, String wrong){
        trieNode node = Search(wrong);
        if(node == null){
            node = insert(wrong,0L);
        }

        node.trieWordCorrect= correct;
        return node;
    }
    public List<List<String>> suggest(String token, int count){
        List<List<String>> SuggestList = new ArrayList<List<String>>();
        /*
        List<String> wordlist_complete = new ArrayList<String>();
        //make sure that there are 5 blank string
        for(int i = 0;i <count;i++){
            wordlist_complete.add("");
        }

         */

        for (int i=0; i<token.length(); i++){
            List<trieNode> nodelist = new ArrayList<trieNode>();
            String prefix = token.substring(0,i+1);
            trieNode node = Search(prefix);
            suggest(node, count, nodelist);
            List<String> wordlist = new ArrayList<String>();
            for(int j =0; j <nodelist.size();j++){
                wordlist.add(nodelist.get(j).trieWord);
            }
            /*
            //add words if there is not enough
            if(wordlist.size() != count){
                for(int k=wordlist.size();k<count;k++){
                    wordlist.add(wordlist_complete.get(k));
                }
            }


            // in case there are less words than needed
            for(int k=0;k<count;k++){
                wordlist_complete.set(k,wordlist.get(k));
            }

             */
            SuggestList.add(wordlist);
        }
        return SuggestList;
    }
    //improvements if it is a wrong word, suggest the correct words
    public List<String> Suggest_new(String token, int count){
        List<trieNode> nodelist = new ArrayList<trieNode>();
        trieNode node = Search(token);
        //if any, use correct word to get the node
        if(node.trieWordCorrect != "") {
            node = Search(node.trieWordCorrect) ;
        }
        suggest(node, count, nodelist);
        List<String> wordlist = new ArrayList<String>();
        for(int j =0; j <nodelist.size();j++){
            wordlist.add(nodelist.get(j).trieWord);
        }
        return wordlist;
    }

    public void suggest(trieNode node, int count, List<trieNode> nodelist){
        // If isWord=true, add <node> to lst
        if(node == null){
            return;
        }
        if(node.isWord == true){
            nodelist.add(node);
        }
        // sort nodelist
        for ( int i =0; i < nodelist.size()-1;i++){
            for( int j = 0; j< nodelist.size()-i-1; j++){
                trieNode node1 = nodelist.get(j);
                trieNode node2 = nodelist.get(j+1);
                if ( node1.frequency < node2.frequency ){
                    trieNode temp;
                    //temp = nodelist.get(j);
                    nodelist.set(j,node2);
                    nodelist.set(j+1,node1);
                }
            }
        }
        // Remove extra (lst.size <= 5)
        while(nodelist.size() > count){
            int n = nodelist.size();
            nodelist.remove(n-1);
        }
        //For each children[0~26] call suggest
        for (int i=0; i< node.children.length;i++){
            suggest(node.children[i],count,nodelist);
        }


    }

    public static void main(String[] args) {
        Spelling sp = new Spelling();
        int count = Integer.valueOf(args[1]);
        try {
            String filename = args[0];
            //File myObj = new File("/Users/kkhou/Desktop/USFca/CS245/untitled/src/unigram_freq.csv");
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            String line = myReader.nextLine();
            while (myReader.hasNextLine()) {
                line = myReader.nextLine();
                String[] tokens =line.split(",", 2);
                String word = tokens[0];
                long freq = Long.parseLong(tokens[1]);
                sp.insert(word,freq);
                //System.out.println(data);
            }
            myReader.close();
            //improvements
            if(args.length>=3 && args[2] != ""){
                filename = args[2];
                File myObj1 = new File(filename);
                myReader = new Scanner(myObj1);
                line = myReader.nextLine();
                while (myReader.hasNextLine()) {
                    line = myReader.nextLine();
                    String[] tokens =line.split(",", 2);
                    String correct = tokens[0];
                    String wrong = tokens[1];
                    sp.MarkWrong(correct, wrong);
                    //System.out.println(data);
                }
                myReader.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        trieNode node = sp.Search("app");
        System.out.println(node.trieWord);
        System.out.println(node.frequency);
        List<List<String>> suggestlist =  sp.suggest("onomatopoeia",count);
        for(int i =0; i <suggestlist.size(); i++){
            System.out.println(suggestlist.get(i).toString());
        }
        //improvements if it is a wrong word, suggest the correct words
        List<String> suggestnew= sp.Suggest_new("accomodate",count);
            System.out.println(suggestnew.toString());
    }
}