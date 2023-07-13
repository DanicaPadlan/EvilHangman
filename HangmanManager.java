/*  Student information for assignment:
 *
 *  On my honor, Danica Padlan, this programming assignment is my own work
 *  and I have not provided this code to any other student.
 *
 *  Name: Danica Padlan
 *  email address: danica_padlan@yahoo.com
 *  UTEID: dmp3357
 *  Section 5 digit ID: 52288
 *  Grader name: Noah
 *  Number of slip days used on this assignment: 1
 */

// add imports as necessary
import java.util.*;

/**
 * Manages the details of EvilHangman. This class keeps
 * tracks of the possible words from a dictionary during
 * rounds of hangman, based on guesses so far.
 */
public class HangmanManager {

    // instance vars
    private final char BLANK = '-';
    private boolean debugOn;
    private Set<String> originBank;
    private ArrayList<String> curBank;
    private TreeMap<String, Character> guessedCharBank;
    private String curPattern;
    private int wordLen;
    private int maxGuessLeft;
    private int hardestMax;
    private int curHardCount;
    private boolean hardest;

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * pre: words != null, words.size() > 0
     *
     * @param words   A set with the words for this instance of Hangman.
     * @param debugOn true if we should print out debugging to System.out.
     */
    public HangmanManager(Set<String> words, boolean debugOn) {
        if (words == null || words.size() == 0) {
            throw new IllegalStateException("Pre-condition: words != null, words.size() > 0");
        }
        this.debugOn = debugOn;
        originBank = words;
    }

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * Debugging is off.
     * pre: words != null, words.size() > 0
     *
     * @param words A set with the words for this instance of Hangman.
     */
    public HangmanManager(Set<String> words) {
        this(words, false);
    }

    /**
     * Get the number of words in this HangmanManager of the given length.
     * pre: none
     *
     * @param length The given length to check.
     * @return the number of words in the original Dictionary
     * with the given length
     */
    public int numWords(int length) {
        Iterator read = originBank.iterator();
        int count = 0;
        while (read.hasNext()) {
            if (read.next().toString().length() == length) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get for a new round of Hangman. Think of a round as a
     * complete game of Hangman.
     *
     * @param wordLen    the length of the word to pick this time.
     *                   numWords(wordLen) > 0
     * @param numGuesses the number of wrong guesses before the
     *                   player loses the round. numGuesses >= 1
     * @param diff       The difficulty for this round.
     */
    public void prepForRound(int wordLen, int numGuesses, HangmanDifficulty diff) {
        this.wordLen = wordLen;
        maxGuessLeft = numGuesses;
        curHardCount = 0;
        curPattern = setDefaultPattern(wordLen);
        curBank = setWordBank(wordLen);
        guessedCharBank = new TreeMap<>();

        //sets max number of times hardest list will be called
        if (diff == HangmanDifficulty.EASY) {
            hardestMax = HangmanDifficulty.EASY.ordinal() + 1;
        } else if (diff == HangmanDifficulty.MEDIUM) {
            //only used hard for ordinal value, not actually set to hard mode
            hardestMax = HangmanDifficulty.HARD.ordinal() + 1;
        } else {
            //lowest value so hard mode choice is infinite
            hardestMax = Integer.MIN_VALUE;
        }
    }

    //creates default blank pattern
    private String setDefaultPattern(int wordLen) {
        StringBuilder temp = new StringBuilder();
        for (int x = 0; x < wordLen; x++) {
            temp.append(BLANK);
        }
        return temp.toString();
    }

    //creates active word bank
    private ArrayList<String> setWordBank(int wordLen) {
        ArrayList<String> temp = new ArrayList<>();
        Iterator read = originBank.iterator();
        while (read.hasNext()) {
            String curWord = read.next().toString();

            //adds words that meet the length requirement
            if (curWord.length() == wordLen) {
                temp.add(curWord);
            }
        }
        return temp;
    }

    /**
     * The number of words still possible (live) based on the guesses so far.
     * Guesses will eliminate possible words.
     *
     * @return the number of words that are still possibilities based on the
     * original dictionary and the guesses so far.
     */
    public int numWordsCurrent() {
        return curBank.size();
    }

    /**
     * Get the number of wrong guesses the user has left in
     * this round (game) of Hangman.
     *
     * @return the number of wrong guesses the user has left
     * in this round (game) of Hangman.
     */
    public int getGuessesLeft() {
        return maxGuessLeft;
    }

    /**
     * Return a String that contains the letters the user has guessed
     * so far during this round.
     * The characters in the String are in alphabetical order.
     * The String is in the form [let1, let2, let3, ... letN].
     * For example [a, c, e, s, t, z]
     *
     * @return a String that contains the letters the user
     * has guessed so far during this round.
     */
    public String getGuessesMade() {
        if (guessedCharBank.size() != 0) {
            StringBuilder temp = new StringBuilder();
            Iterator read = guessedCharBank.keySet().iterator();

            //creates string in fencepost-like pattern
            temp.append("[" + read.next());
            while (read.hasNext()) {
                temp.append(", " + read.next());
            }
            temp.append("]");
            return temp.toString();
        }
        return "[]";
    }

    /**
     * Check the status of a character.
     *
     * @param guess The characater to check.
     * @return true if guess has been used or guessed this round of Hangman,
     * false otherwise.
     */
    public boolean alreadyGuessed(char guess) {
        return guessedCharBank.containsValue(guess) ? true : false;
    }

    /**
     * Get the current pattern. The pattern contains '-''s for
     * unrevealed (or guessed)
     * characters and the actual character for "correctly guessed" characters.
     *
     * @return the current pattern.
     */
    public String getPattern() {
        return curPattern;
    }

    /**
     * Update the game status (pattern, wrong guesses, word list),
     * based on the give guess.
     *
     * @param guess pre: !alreadyGuessed(ch), the current guessed character
     * @return return a tree map with the resulting patterns and the number of
     * words in each of the new patterns.
     * The return value is for testing and debugging purposes.
     */
    public TreeMap<String, Integer> makeGuess(char guess) {
        if (alreadyGuessed(guess)) {
            throw new IllegalStateException(guess + " was already guessed.");
        }

        //sorts words in wordbank with associated pattern
        TreeMap<String, ArrayList<String>> familyTree = sortFamilies(guess);

        //then implement frequency map
        TreeMap<String, Integer> freq = setFrequency(familyTree);

        //sets up new pattern and wordbank
        curPattern = getNewPattern(freq);
        curBank = familyTree.get(curPattern);
        guessedCharBank.put(Character.toString(guess), guess);

        //checks if the pattern used the given char
        if (!curPattern.contains(Character.toString(guess))) {
            maxGuessLeft--;
        }

        debugFamilyResult(debugOn, hardest, sortPattern(freq).size() == 1);
        return freq;
    }

    //finds all possible patterns in wordbank with the given char
    private ArrayList<String> findPattern(char guess) {
        ArrayList<String> temp = new ArrayList<>();

        //add in current pattern bc loop checks for patterns WITH new guess
        temp.add(curPattern);
        for (int x = 0; x < curBank.size(); x++) {
            if (curBank.get(x).contains(Character.toString(guess))) {

                //adds key to map if found word that contains given char
                temp.add(createPattern(curBank.get(x), curPattern, guess));
            }
        }
        return temp;
    }

    //creates pattern based on given word and char
    private String createPattern(String word, String curPattern, char guess) {
        StringBuilder temp = new StringBuilder();
        for (int x = 0; x < wordLen; x++) {

            //checks if chars match with guess or curPattern
            if (word.charAt(x) == guess) {
                temp.append(guess);
            } else if (curPattern.charAt(x) != BLANK) {
                temp.append(word.charAt(x));
            } else {
                temp.append(BLANK);
            }
        }
        return temp.toString();
    }

    //sorts and sets ArrayList to corresponding pattern
    private TreeMap<String, ArrayList<String>> sortFamilies(char guess) {
        TreeMap<String, ArrayList<String>> family = new TreeMap<>();
        ArrayList<String> patterns = findPattern(guess);

        //goes through all possible patterns
        for (int x = 0; x < patterns.size(); x++) {
            ArrayList<String> smallBank = new ArrayList<>();

            //goes through the curBank
            for (int y = 0; y < curBank.size(); y++) {

                //if words matches pattern
                if (patterns.get(x).equals(createPattern(curBank.get(y), curPattern, guess))) {
                    smallBank.add(curBank.get(y));
                }
            }

            //add only if there are words in bank (greater than 0)
            if (smallBank.size() > 0) {
                family.put(patterns.get(x), smallBank);
            }
        }
        return family;
    }

    //returns new pattern based on difficulty level
    private String getNewPattern(TreeMap<String, Integer> pattern) {
        ArrayList<HardestList> patterns = sortPattern(pattern);
        final int SECOND_HARD = 2;
        if (curHardCount == hardestMax) {
            hardest = false;
            curHardCount = 0;
            if (patterns.size() == 1) {

                //return the only one in the ArrayList
                return patterns.get(patterns.size() - 1).getPattern();
            }

            //return the second to hardest one from the end
            return patterns.get(patterns.size() - SECOND_HARD).getPattern();
        }
        curHardCount++;
        hardest = true;
        
        //else return the hardest one
        return patterns.get(patterns.size() - 1).getPattern();
    }

    //sorts ArrayList of HardestList from easiest to hardest
    private ArrayList<HardestList> sortPattern(TreeMap<String, Integer> list) {
        ArrayList<HardestList> temp = new ArrayList<>();
        Iterator read = list.keySet().iterator();

        //goes through map
        while (read.hasNext()) {
            String pattern = read.next().toString();

            //implements and adds new HardestList into ArrayList
            temp.add(new HardestList(pattern, list.get(pattern)));
        }
        Collections.sort(temp);
        return temp;
    }

    //adds up number of words that follow a certain pattern
    private TreeMap<String, Integer> setFrequency(TreeMap<String, ArrayList<String>> list) {
        TreeMap<String, Integer> temp = new TreeMap<>();
        Iterator read = list.keySet().iterator();

        //goes through map
        while (read.hasNext()) {
            String pattern = read.next().toString();
            temp.put(pattern, list.get(pattern).size());
        }
        return temp;
    }

    //prints out on what list is chosen
    private void debugFamilyResult(boolean debugOn, boolean hardest, boolean onlyOne) {
        if (debugOn) {
            System.out.println();
            if (!hardest) {

                //checks if there needed to be a backup family (automatic first hard pattern)
                if (onlyOne) {
                    System.out.println("DEBUGGING: Should pick second hardest pattern this turn, but only one pattern available.");
                    System.out.println();
                    System.out.println("DEBUGGING: Picking hardest list.");
                } else {
                    System.out.println("DEBUGGING: Difficulty second hardest pattern and list.");
                    System.out.println();
                }
            } else {
                System.out.println("DEBUGGING: Picking hardest list.");
            }
            System.out.println("DEBUGGING: New pattern is: " + curPattern + ". New family has " + numWordsCurrent() + " words.");
            System.out.println();
        }
    }

    /**
     * Return the secret word this HangmanManager finally ended up
     * picking for this round.
     * If there are multiple possible words left one is selected at random.
     * <br> pre: numWordsCurrent() > 0
     *
     * @return return the secret word the manager picked.
     */
    public String getSecretWord() {
        if (numWordsCurrent() == 0) {
            throw new IllegalStateException("No words left.");
        }

        //if multiple words, choose random word
        if (curBank.size() > 1) {
            return curBank.get((int) (Math.random() * curBank.size()));
        }
        return curBank.get(0);
    }

    //stores and sorts HardestList objects
    private static class HardestList implements Comparable<HardestList> {

        private String pattern;
        private int value;
        private final char BLANK = '-';

        //sets up pattern and value
        public HardestList(String pattern, int value) {
            this.pattern = pattern;
            this.value = value;
        }

        //returns pattern
        private String getPattern() {
            return pattern;
        }

        //returns value
        private int getValue() {
            return value;
        }

        //compares HardList objects to be sorted
        @Override
        public int compareTo(HardestList o) {
            if ((value - o.value) == 0) {
                if ((getTotalRevealed(o.getPattern()) - getTotalRevealed(pattern)) == 0) {

                    //determined which pattern has smallest lexicographical ordering
                    return o.getPattern().compareTo(pattern);
                }
                //determine by which pattern has the least revealed chars
                return getTotalRevealed(o.getPattern()) - getTotalRevealed(pattern);
            }

            //determined by which pattern has the greatest value
            return value - o.getValue();
        }

        //counts how many chars are revealed in given pattern
        private int getTotalRevealed(String word) {
            int count = 0;
            for (int x = 0; x < word.length(); x++) {
                if (word.charAt(x) != BLANK) {
                    count++;
                }
            }
            return count;
        }
    }
}
