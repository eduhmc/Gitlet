package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;


/** Gitlet commit class
 * @author Eduardo Huerta Mercado
 */
public class TreeP implements Serializable {

    /** Constructor method 
     */
    public TreeP() {
        myCommits = new HashSet<>();
        myChildren = new HashMap<>();
    }

    /** Sets the current branch we're at to this branch.
     * @param b the branch we're gonna set our current branch to.
     */
    public void setCurBranch(Branch b) {
        curBranch = b;
    }

    /** Hashset containing the string ID of all the commits.
     */
    private HashSet<String> myCommits;
    /** Get method for my commitments as a parent of 0.
     * @return SYK I HAVE NONE MUHAHAHAHAHAHAHAH.
     */
    public HashSet<String> getMyCommits() {
        return myCommits;
    }

    /** Mapping of all the children branches' names to them.
     */
    private Map<String, Branch> myChildren;
    /** Get method for those kids next door.
     * @return Because they are definitely my kids I swear.
     */
    public Map<String, Branch> getMyChildren() {
        return myChildren;
    }

    /** The random generator object to ensure each sha1 is unique.
     */
    private static Random randomGen = new Random(1);
    /** Get method for a random number generator.
     * @return the random number generator.
     */
    public Random getRandomGen() {
        return randomGen;
    }

    /** The current branch we are working with.
     */
    private Branch curBranch;

    /** Get method for the current branch we're at.
     * @return the current branch.
     */
    public Branch getCurBranch() {
        return curBranch;
    }

    /** The separator symbol.
     */
    private char separator = File.separatorChar;
}