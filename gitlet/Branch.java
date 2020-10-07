package gitlet;

import java.io.File;
import java.io.Serializable;

/** This class represents one long chain of unbranching (hon hon,
 * the irony of that) commits, a name, and a stage copy.
 * @author Definitely-not-Nick
 */

public class Branch implements Serializable {

    /** Each branch object has a name, a pointer to a following branch,
     * and a pointer to a staging area.
     * @param name the name of this branch.
     * @param head the current head commit.
     * @param parent the TreeP.
     */
    public Branch(String name, Commit head, TreeP parent) {
        myName = name;
        headCommit = head.getMyID();
        myStage = head.getMyStage().getMyID();
        myParent = parent;
        myID = Utils.sha1(head.getMyDateStr()
                + myParent.getRandomGen().nextDouble());
        parent.getMyChildren().put(name, this);
    }

    /** Updates the head commit to this commit.
     * @param commit the commit that will become the new head commit.
     */
    public void updateHead(Commit commit) {
        headCommit = commit.getMyID();
    }

    /** Updates the stage to this new stage.
     * @param stage the stage update my stage to.
     */
    public void updateStage(Stage stage) {
        myStage = stage.getMyID();
    }

    /** the toString() structure of a single branch, I don't think
     * this is ever actually needed by any of the methods so I'm just
     * making it visually appealing :D.
     * @return a String representing this branch.
     */
    @Override
    public String toString() {
        StringBuilder myStringRepr = new StringBuilder();
        myStringRepr.append("----- MY NAME ----- \n");
        myStringRepr.append(myName + "\n");
        myStringRepr.append("----- MY ID ----- \n");
        myStringRepr.append(myID + "\n");
        myStringRepr.append("----- MY STAGE ----- \n");
        myStringRepr.append(myStage.toString());
        myStringRepr.append("\n");
        myStringRepr.append("----- HEAD COMMIT ----- \n");
        myStringRepr.append(headCommit.toString() + "\n");
        myStringRepr.append("\n");
        return myStringRepr.toString();
    }

    /** Name of the branch.
     */
    private String myName;
    /** Get method for my name.
     * @return pineapple lumps :D WTF DO YOU THINK THIS RETURNS???
     */
    public String getMyName() {
        return myName;
    }

    /** Head of the branch.
     */
    private String headCommit;

    /** Get method for the head commit ID of this branch.
     * @return that.
     */
    public String getHeadCommitID() {
        return headCommit;
    }

    /** Get method for the head commit of this branch.
     * @return my head commit
     */
    public Commit getHeadCommit() {
        File parentFile = new File(".gitlet" + separator
                + "objectRepository" + separator + headCommit);
        return Utils.readObject(parentFile, Commit.class);
    }

    /** Picture of the current staging area.
     */
    private String myStage;
    /** get method for myStage.
     * @return my Stage
     */
    public Stage getMyStage() {
        File myFile = new File(".gitlet"
                + separator + "stages" + separator + "stage" + myStage);

        return Utils.readObject(myFile, Stage.class);
    }

    /** The TreeP of which I branched off from.
     */
    private TreeP myParent;
    /** get method for the TreeP.
     * @return the TreeP
     */
    public TreeP getMyParent() {
        return myParent;
    }

    /** A (hopefully) unique ID generated from the string representation
     * of the head commit at the moment of branch creation, plus a random
     * double generated by TreeP's random seed.
     */
    private String myID;
    /** get method for my commit ID.
     * @return my commit ID
     */
    public String getMyID() {
        return myID;
    }

    /** The separator symbol.
     */
    private char separator = File.separatorChar;
}

