package gitlet;

import org.junit.Before;
import org.junit.ComparisonFailure;
import ucb.junit.textui;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Eduardo Huerta Mercado
 */
public class UnitTest implements Serializable {

    /** The wait time before deleting everything
     * in milliseconds.
     */
    int waitTime = 0;

    /** Whether I want to run deleteFolder or not.
     */
    boolean deleteOrNah = true;

    /** helper function that simply waits for the given
     * time.
     * @param time in millisecons we should wait for.
     */
    private void wait(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            boolean bugger = false;
        }

    }

    /** Deletes all the files in a certain directory.
     * @author baeldung.com
     * @param directoryToBeDeleted the directory to be deleted.
     * @return boolean value denoting whether something was deleted
     */
    public boolean deleteDirectory(File directoryToBeDeleted) {
        if (!directoryToBeDeleted.exists()) {
            System.out.println("there ain't nothin' to delete");
            return false;
        }
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    boolean deleteDirectory(String fileName) {
        File theFile = new File(fileName);
        return deleteDirectory(theFile);
    }

    /** Writes a given string into a file.
     * @author Lokesh Gupta
     * @param fileContent String of the content of the file.
     * @param file the file we want to write to
     */
    public void writeToFile(File file, String fileContent) {
        try {
            Utils.writeContents(file, fileContent);
        } catch (IllegalArgumentException e) {
            System.out.println("File DNE: " + e.getMessage());
        }
    }

    /** Reads a file and returns the string content of the file.
     * @param toBeRead the file that we're gonna read.
     * @return the String we read from the file.
     */
    public String readFile(File toBeRead) {
        String retString = "";
        try {
            FileReader theFile = new FileReader(toBeRead);
            int tempChar = theFile.read();
            while (tempChar != -1) {
                retString = retString + (char) tempChar;
                tempChar = theFile.read();
            }
            theFile.close();
            return retString;
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "wtf, I made mistake";
    }

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    @Before
    public void resetTextFiles() throws IOException {
        System.out.println("----- RESETTED -----");
        File hello = new File("hello.txt");
        File pineapple = new File("pineapple.txt");
        File wug = new File("wug.txt");
        File ponpon = new File("myDir\\ponpon.txt");
        File lemon = new File("myDir\\lemon.txt");
        File thug = new File("thug.txt");
        File bonny = new File("bonny.txt");
        File grapes = new File("grapes.txt");

        if (!hello.exists()) {
            Utils.writeContents(hello, "");
        }
        if (!pineapple.exists()) {
            Utils.writeContents(pineapple, "");
        }
        if (!wug.exists()) {
            Utils.writeContents(wug, "");
        }
        if (!ponpon.exists()) {
            Utils.writeContents(ponpon, "");
        }
        if (!lemon.exists()) {
            Utils.writeContents(lemon, "");
        }
        if (!thug.exists()) {
            Utils.writeContents(thug, "");
        }
        if (!bonny.exists()) {
            Utils.writeContents(bonny, "");
        }
        if (!grapes.exists()) {
            Utils.writeContents(grapes, "");
        }

        writeToFile(hello, "Hello mah fwend :D");
        writeToFile(pineapple, "Lumps");
        writeToFile(wug, "wug pug\npug wug\nwug pug likes to bug the lug");
        writeToFile(ponpon, "Hit me with that\ndudu dudu");
        writeToFile(lemon, "tree");
        writeToFile(thug, "gansta!");
        writeToFile(bonny, "bunny");
        writeToFile(grapes, "SYK I'M GRAPEFRUIT");
    }

    @Before
    public void deleteFolder() throws InterruptedException {
        if (!deleteOrNah) {
            waitTime = 0;
            deleteOrNah = true;
            return;
        }
        Thread.sleep(waitTime);
        if (!deleteDirectory(".gitlet")) {
            return;
        }
        waitTime = 0;
        deleteOrNah = true;
        System.out.println("------ DELETED -----");
        System.out.println();
    }

    @Test
    public void initTest() {
        try {
            Main.main("init");
            File gitlet = new File(".gitlet");
            assertTrue(gitlet.exists());
            deleteDirectory(gitlet);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void commitTestBasic() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File ponpon = new File("myDir\\ponpon.txt");
            Main.main("init");

            File gitlet = new File(".gitlet");
            assertTrue(gitlet.exists());

            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("add", "hondacivic.txt");
            Main.main("add", "ponpon.txt");
            Main.main("add", "myDir\\ponpon.txt");

            Main.main("commit", "hello.txt, "
                    + "pineapple.txt and myDir\\ponpon.txt committed");

            Map myMap = Main.getDirectory().getCurBranch()
                    .getHeadCommit().getMyFilePointers();
            for (Object key: myMap.keySet()) {
                System.out.println("fileName: " + key);
                System.out.println("fileID: " + myMap.get(key));
            }
            System.out.println();

            writeToFile(hello, "FROM THE OTHER SIIIIIDDDEEE!!!");
            writeToFile(ponpon, "Konichiwa~~");

            Main.main("checkout", "--", "hello.txt");
            assertEquals(readFile(hello), "Hello mah fwend :D");
            assertEquals(readFile(ponpon), "Konichiwa~~");

            Main.main("checkout", "--", "myDir\\ponpon.txt");
            assertEquals(readFile(ponpon), "Hit me with that\ndudu dudu");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void multiCommitTest() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File ponpon = new File("myDir\\ponpon.txt");
            Main.main("init");
            File gitlet = new File(".gitlet");
            assertTrue(gitlet.exists());

            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("add", "myDir\\ponpon.txt");

            Main.main("commit", "hello.txt, "
                    + "pineapple.txt and myDir\\ponpon.txt committed");
            String firstCommitID = Main.getDirectory()
                    .getCurBranch().getHeadCommitID();

            writeToFile(hello, "FROM THE OTHER SIIIIIDDDEEE!!!");
            writeToFile(ponpon, "Konichiwa~~");

            Main.main("checkout", "--", "hello.txt");
            assertEquals(readFile(hello), "Hello mah fwend :D");
            assertEquals(readFile(ponpon), "Konichiwa~~");

            Main.main("checkout", "--", "myDir\\ponpon.txt");
            assertEquals(readFile(ponpon), "Hit me with that\ndudu dudu");

            writeToFile(hello, "but honey, I'm pregorante ;(");
            writeToFile(pineapple, "it is your time "
                    + "to shine LUMPUS MAXIMUS");
            Main.main("add", "pineapple.txt");

            Main.main("commit", "Only pineapple.txt committed");
            String secondCommitID = Main.getDirectory()
                    .getCurBranch().getHeadCommitID();

            assertEquals("but honey, I'm pregorante ;(", readFile(hello));
            assertEquals("it is your time to "
                    + "shine LUMPUS MAXIMUS", readFile(pineapple));

            multiCommitTestPt2(firstCommitID,
                    secondCommitID, hello, pineapple, ponpon);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    public void multiCommitTestPt2(String firstCommitID, String secondCommitID,
                                   File hello, File pineapple, File ponpon) {
        writeToFile(pineapple, "Chris Pine, I'm not feeling so good");
        Main.main("checkout", "--", "hello.txt");

        assertEquals("Hello mah fwend :D", readFile(hello));
        assertEquals("Chris Pine, I'm "
                + "not feeling so good", readFile(pineapple));

        writeToFile(hello, "Is it me you're looking for?");

        Main.main("checkout", "--", "pineapple.txt");
        assertEquals("it is your time to "
                + "shine LUMPUS MAXIMUS", readFile(pineapple));

        writeToFile(ponpon, "pop pop");
        assertEquals("pop pop", readFile(ponpon));

        Main.main("add", "wug.txt");
        Main.main("add", "hello.txt");
        Main.main("commit", "hello and wug.txt was added");

        Main.main("checkout", "--", "myDir\\ponpon.txt");
        assertEquals("Hit me with that\ndudu dudu", readFile(ponpon));

        Main.main("log");

        assertEquals("it is your time to "
                + "shine LUMPUS MAXIMUS", readFile(pineapple));
        Main.main("checkout", firstCommitID, "--", "pineapple.txt");
        System.out.println(firstCommitID);
        assertEquals("Lumps", readFile(pineapple));

        assertEquals("Is it me you're looking for?", readFile(hello));
        Main.main("checkout", "--", "hello.txt");
        assertEquals("Is it me you're looking for?", readFile(hello));
        Main.main("checkout", secondCommitID, "--", "hello.txt");
        assertEquals("Hello mah fwend :D", readFile(hello));
    }

    @Test
    public void multiCommitTest2() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File ponpon = new File("myDir\\ponpon.txt");
            File lemon = new File("myDir\\lemon.txt");
            File gitlet = new File(".gitlet");
            ArrayList<String> commitIDs = new ArrayList<String>();

            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());

            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(ponpon, "1");
            writeToFile(lemon, "1");

            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("add", "myDir\\ponpon.txt");
            Main.main("add", "wug.txt");
            System.out.println("adding no problems");

            writeToFile(hello, "2");
            writeToFile(ponpon, "2");

            Main.main("add", "myDir\\ponpon.txt");
            Main.main("commit", "It should be "
                    + "hello.1, pineapple.1, ponpon.2 wug.1");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());

            assertEquals("1", readFile(
                    new File(".gitlet\\objectRepository\\folder"
                            + commitIDs.get(1) + "\\hello.txt")));
            assertEquals("2", readFile(
                    new File(".gitlet\\objectRepository\\folder"
                            + commitIDs.get(1) + "\\myDir@ponpon.txt")));
            assertEquals("1", readFile(
                    new File(".gitlet\\objectRepository\\folder"
                            + commitIDs.get(1) + "\\wug.txt")));
            assertEquals("1", readFile(
                    new File(".gitlet\\objectRepository\\folder"
                            + commitIDs.get(1) + "\\pineapple.txt")));

            System.out.println("committing 1 no problems");
            System.out.println();
            multiCommitTest2Pt2(commitIDs, hello,
                    pineapple, ponpon, wug, lemon);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    public void multiCommitTest2Pt2(ArrayList<String> commitIDs,
                                    File hello, File pineapple, File ponpon,
                                    File wug, File lemon) {
        assertEquals("2", readFile(hello));

        Main.main("checkout", "--", "hello.txt");
        assertEquals("1", readFile(hello));
        System.out.println("checkout 1 no problems \n");

        writeToFile(hello, "2");
        writeToFile(pineapple, "2");

        Main.main("add", "wug.txt");
        Main.main("commit", "this should "
                + "throw error as wug is unchanged");
        System.out.println("committing 2 no "
                + "problem IFF an error was thrown \n");

        Main.main("add", "hello.txt");
        Main.main("add", "myDir\\ponpon.txt");
        Main.main("add", "wug.txt");
        Main.main("add", "myDir\\lemon.txt");
        Main.main("commit", "hello.2, "
                + "lemon.1 (wug and ponpon are the same)");
        commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());

        assertEquals("2", readFile(
                new File(".gitlet\\objectRepository\\folder"
                        + commitIDs.get(2) + "\\hello.txt")));
        assertEquals("1", readFile(
                new File(".gitlet\\objectRepository\\folder"
                        + commitIDs.get(2) + "\\myDir@lemon.txt")));
        try {
            assertEquals("2", readFile(
                    new File(".gitlet\\objectRepository\\folder"
                            + commitIDs.get(2) + "\\myDir@ponpon.txt")));
            System.out.println("wtf, this should have erred");
        } catch (ComparisonFailure e) {
            writeToFile(wug, readFile(wug));
        }
        try {
            assertEquals("2", readFile(
                    new File(".gitlet\\objectRepository\\folder"
                            + commitIDs.get(2) + "\\wug.txt")));
            System.out.println("wtf, this should have errored");
        } catch (AssertionError e) {
            writeToFile(wug, readFile(wug));
        }
        multiCommitTest2Pt3(commitIDs, hello, pineapple, ponpon, wug, lemon);
    }

    public void multiCommitTest2Pt3(ArrayList<String> commitIDs,
                                    File hello, File pineapple, File ponpon,
                                    File wug, File lemon) {
        System.out.println("committing 2.1 no problems IFF 2 FNF errors\n");

        assertEquals("2", readFile(hello));
        assertEquals("2", readFile(pineapple));
        assertEquals("2", readFile(ponpon));
        assertEquals("1", readFile(wug));
        assertEquals("1", readFile(lemon));

        System.out.println(commitIDs);
        Main.main("checkout", "--", "hello.txt");
        Main.main("checkout", commitIDs.get(1), "--", "pineapple.txt");
        Main.main("checkout", commitIDs.get(2), "--", "myDir\\ponpon.txt");
        Main.main("checkout", "--", "wug.txt");

        assertEquals("2", readFile(hello));
        assertEquals("1", readFile(pineapple));
        assertEquals("2", readFile(ponpon));
        assertEquals("1", readFile(wug));
        System.out.println("checkouts 2 no problem");
        System.out.println();

        writeToFile(wug, "2");
        Main.main("add", "wug.txt");
        Main.main("commit", "wug.2 committed");
        commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
        writeToFile(wug, "3");
        Main.main("checkout", "--", "wug.txt");
        assertEquals("2", readFile(wug));
        writeToFile(wug, "3");
        Main.main("checkout", commitIDs.get(1), "--", "wug.txt");
        assertEquals("1", readFile(wug));
        writeToFile(wug, "3");
        Main.main("checkout", commitIDs.get(2), "--", "wug.txt");
        assertEquals("1", readFile(wug));

        writeToFile(lemon, "2");
        Main.main("checkout", "--", "myDir\\lemon.txt");
        assertEquals("1", readFile(lemon));
        writeToFile(lemon, "2");
        Main.main("checkout", commitIDs.get(2), "--", "myDir\\lemon.txt");
        assertEquals("1", readFile(lemon));
        writeToFile(lemon, "2");
        Main.main("checkout", commitIDs.get(1), "--", "myDir\\lemon.txt");
        writeToFile(pineapple, "2");
        Main.main("checkout", "--", "pineapple.txt");
        assertEquals("1", readFile(pineapple));

        System.out.println("checkout 3 no problem "
                + "IFF 1 File DNE error was thrown");

        Main.main("log");
        Main.main("global-log");
        System.out.println(commitIDs);
    }

    @Test
    public void multiCommitTest2Logs() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File ponpon = new File("myDir\\ponpon.txt");
            File lemon = new File("myDir\\lemon.txt");
            File gitlet = new File(".gitlet");
            ArrayList<String> commitIDs = new ArrayList<String>();

            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());

            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(ponpon, "1");
            writeToFile(lemon, "1");

            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("add", "myDir\\ponpon.txt");
            Main.main("add", "wug.txt");
            System.out.println("adding no problems");

            writeToFile(hello, "2");
            writeToFile(ponpon, "2");

            Main.main("add", "myDir\\ponpon.txt");
            Main.main("commit", "It should be "
                    + "hello.1, pineapple.1, ponpon.2 wug.1");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());

            Main.main("checkout", "--", "hello.txt");
            writeToFile(hello, "2");
            writeToFile(pineapple, "2");

            Main.main("add", "wug.txt");
            Main.main("commit", "this should throw error as wug is unchanged");
            Main.main("add", "hello.txt");
            Main.main("add", "myDir\\ponpon.txt");
            Main.main("add", "wug.txt");
            Main.main("add", "myDir\\lemon.txt");
            Main.main("commit", "hello.2, "
                    + "lemon.1 (wug and ponpon are the same)");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            multiCommitTest2Logs2(commitIDs, wug, lemon, pineapple);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    public void multiCommitTest2Logs2(List<String> commitIDs,
                                      File wug, File lemon, File pineapple) {
        try {
            assertEquals("2", readFile(
                    new File(".gitlet\\objectRepository\\folder"
                            + commitIDs.get(2) + "\\myDir@ponpon.txt")));
            System.out.println("wtf, this should have erred");
        } catch (ComparisonFailure e) {
            writeToFile(wug, readFile(wug));
        }
        try {
            assertEquals("2", readFile(
                    new File(".gitlet\\objectRepository\\folder"
                            + commitIDs.get(2) + "\\wug.txt")));
            System.out.println("wtf, this should have errored");
        } catch (AssertionError e) {
            writeToFile(wug, readFile(wug));
        }
        Main.main("checkout", "--", "hello.txt");
        Main.main("checkout", commitIDs.get(1), "--", "pineapple.txt");
        Main.main("checkout", commitIDs.get(2), "--", "myDir\\ponpon.txt");
        Main.main("checkout", "--", "wug.txt");

        writeToFile(wug, "2");
        Main.main("add", "wug.txt");
        Main.main("commit", "wug.2 committed");
        commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
        writeToFile(wug, "3");
        Main.main("checkout", "--", "wug.txt");
        writeToFile(wug, "3");
        Main.main("checkout", commitIDs.get(1), "--", "wug.txt");
        writeToFile(wug, "3");
        Main.main("checkout", commitIDs.get(2), "--", "wug.txt");

        writeToFile(lemon, "2");
        Main.main("checkout", "--", "myDir\\lemon.txt");
        writeToFile(lemon, "2");
        Main.main("checkout", commitIDs.get(2), "--", "myDir\\lemon.txt");
        writeToFile(lemon, "2");
        Main.main("checkout", commitIDs.get(1), "--", "myDir\\lemon.txt");
        writeToFile(pineapple, "2");
        Main.main("checkout", "--", "pineapple.txt");
        Main.main("log");
        Main.main("global-log");
    }

    @Test
    public void removeAndCommitTest() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File ponpon = new File("myDir\\ponpon.txt");
            File lemon = new File("myDir\\lemon.txt");
            ArrayList<String> commitIDs = new ArrayList<String>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(ponpon, "1");
            writeToFile(wug, "1");
            writeToFile(lemon, "1");
            Main.main("add", "wug.txt");
            Main.main("add", "myDir\\ponpon.txt");
            Main.main("add", "pineapple.txt");
            Main.main("add", "myDir\\lemon.txt");
            writeToFile(wug, "2");
            writeToFile(ponpon, "2");
            Main.main("rm", "hello.txt");
            Main.main("rm", "myDir\\lemon.txt");
            Main.main("rm", "wug.txt");
            Main.main("rm", "pineapple.txt");
            Main.main("rm", "myDir\\ponpon.txt");
            Main.main("add", "hello.txt");
            Main.main("add", "wug.txt");
            Main.main("add", "myDir\\lemon.txt");
            Main.main("commit", "wug.2 and lemon.1 committed");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            assertEquals("2", readFile(
                    new File(".gitlet\\objectRepository\\folder"
                            + commitIDs.get(1) + "\\wug.txt")));
            assertEquals("1", readFile(
                    new File(".gitlet\\objectRepository\\folder"
                            + commitIDs.get(1) + "\\myDir@lemon.txt")));
            try {
                assertEquals("2", readFile(
                        new File(".gitlet\\objectRepository\\folder"
                                + commitIDs.get(1) + "\\myDir@ponpon.txt")));
                System.out.println("wtf, this should have erred");
            } catch (ComparisonFailure e) {
                writeToFile(wug, readFile(wug));
            }
            try {
                assertEquals("1", readFile(
                        new File(".gitlet\\objectRepository\\folder"
                                + commitIDs.get(1) + "\\pineapple.txt")));
                System.out.println("wtf, this should have errored");
            } catch (AssertionError e) {
                writeToFile(wug, readFile(wug));
            }
            removeAndCommitTest2(lemon, commitIDs, pineapple, hello, wug);
        } catch (IllegalArgumentException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    public void removeAndCommitTest2(File lemon, List<String> commitIDs,
                                     File pineapple, File hello, File wug) {
        System.out.println("add, rm and commit completed without problems "
                + "IFF 2 FNF and 1 No reason to remove errors thrown");
        System.out.println();
        Main.main("add", "pineapple.txt");
        Main.main("add", "myDir\\ponpon.txt");
        writeToFile(lemon, "2");
        writeToFile(pineapple, "2");
        Main.main("rm", "myDir\\lemon.txt");
        Main.main("rm", "wug.txt");
        Main.main("commit", "wug deleted, ponpon.1 and pineapple.1 staged");
        commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
        System.out.println("committing 2 succeeded without problems");
        writeToFile(hello, "2");
        writeToFile(lemon, "3");
        Main.main("checkout", "--", "hello.txt");
        Main.main("checkout", "--", "myDir\\lemon.txt");
        Main.main("checkout",
                commitIDs.get(1), "--", "myDir\\lemon.txt");
        assertEquals("1", readFile(hello));
        assertEquals("1", readFile(lemon));
        assertTrue(new File("wug.txt").exists());
        System.out.println("checkout 1 succeeded without "
                + "problem");
        Utils.writeContents(wug, "3");
        Main.main("add", "wug.txt");

        Main.main("rm", "hello.txt");
        Main.main("rm", "myDir\\lemon.txt");
        Main.main("commit", "hello and "
                + "lemon removed, ponpon.2 and wug.3 committed");
        commitIDs.add(Main.getDirectory()
                .getCurBranch().getHeadCommitID());
        System.out.println(commitIDs);
        assertEquals("3", readFile(
                new File(".gitlet\\objectRepository\\folder"
                        + commitIDs.get(3) + "\\wug.txt")));
        boolean dummy = true;
        try {
            assertEquals("1", readFile(
                    new File(".gitlet\\objectRepository\\folder"
                            + commitIDs.get(3) + "\\hello.txt")));
            System.out.println("wtf, this should have erred");
        } catch (ComparisonFailure e) {
            dummy = !dummy;
        }
        try {
            assertEquals("1", readFile(
                    new File(".gitlet\\objectRepository\\folder"
                            + commitIDs.get(3) + "\\myDir@lemon.txt")));
            System.out.println("wtf, this should have erred");
        } catch (AssertionError e) {
            dummy = !dummy;
        }
        System.out.println("committing 2 succeeded without "
                + "problem IFF 2 FNF errors thrown");
        System.out.println(commitIDs);
    }

    @Test
    public void findTest() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File ponpon = new File("myDir\\ponpon.txt");
            File lemon = new File("myDir\\lemon.txt");
            File gitlet = new File(".gitlet");

            ArrayList<String> commitIDs = new ArrayList<String>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());

            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(ponpon, "1");
            writeToFile(wug, "1");
            writeToFile(lemon, "1");

            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("add", "myDir\\ponpon.txt");
            Main.main("add", "myDir\\lemon.txt");
            Main.main("commit", "commit A");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());

            writeToFile(hello, "2");
            writeToFile(ponpon, "2");
            Main.main("add", "pineapple.txt");
            Main.main("add", "myDir\\ponpon.txt");
            Main.main("commit", "commit B");

            writeToFile(wug, "2");
            writeToFile(lemon, "3");
            Main.main("add", "wug.txt");
            Main.main("rm", "hello.txt");
            Main.main("commit", "commit A");

            Main.main("find", "commit A");
            System.out.println("this should print out 2 commit IDs");
            System.out.println();

            Main.main("find", "commit B");
            System.out.println("this should print out 1 commit IDs");
            System.out.println();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void noCommandErr() {
        try {
            File hello = new File("hello.txt");
            Main.main("init");
            Main.main("glorp", "foo");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void removeAndFindTestStatus() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File ponpon = new File("myDir\\ponpon.txt");
            File lemon = new File("myDir\\lemon.txt");
            File gitlet = new File(".gitlet");
            ArrayList<String> commitIDs = new ArrayList<String>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(ponpon, "1");
            writeToFile(wug, "1");
            writeToFile(lemon, "1");

            Main.main("status");
            Main.main("add", "wug.txt");
            Main.main("add", "myDir\\ponpon.txt");
            Main.main("add", "pineapple.txt");
            Main.main("add", "myDir\\lemon.txt");

            Main.main("rm", "hello.txt");
            Main.main("rm", "myDir\\lemon.txt");
            Main.main("rm", "wug.txt");
            Main.main("status");
            Main.main("rm", "pineapple.txt");
            Main.main("rm", "myDir\\ponpon.txt");
            Main.main("status");

            Main.main("add", "hello.txt");
            Main.main("add", "wug.txt");
            Main.main("status");
            Main.main("add", "myDir\\lemon.txt");
            Main.main("commit", "wug.2 and lemon.1 committed");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            Main.main("status");

            System.out.println("add, rm and commit completed without problems "
                    + "IFF 2 FNF and 1 No reason to remove errors thrown");
            System.out.println();

            Main.main("add", "pineapple.txt");
            Main.main("add", "myDir\\ponpon.txt");

            writeToFile(lemon, "2");
            writeToFile(pineapple, "2");

            Main.main("rm", "myDir\\lemon.txt");
            Main.main("rm", "wug.txt");

            removeAndFindTestStatus2(commitIDs, wug);
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    public void removeAndFindTestStatus2(List<String> commitIDs, File wug) {
        Main.main("commit", "wug deleted, ponpon.1 and pineapple.1 staged");
        commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
        System.out.println("committing 2 succeeded without problems");

        Main.main("checkout", "--", "hello.txt");
        Main.main("checkout", "--", "myDir\\lemon.txt");
        Main.main("checkout", commitIDs.get(1), "--", "myDir\\lemon.txt");
        System.out.println("checkout 1 succeeded "
                + "without problem IFF 1 FNF error thrown");

        Utils.writeContents(wug, "3");

        Main.main("add", "wug.txt");
        Main.main("rm", "hello.txt");
        Main.main("rm", "myDir\\lemon.txt");
        Main.main("commit", "hello and lemon removed, "
                + "ponpon.2 and wug.3 committed");
    }

    @Test
    public void simpleBranchTest() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File ponpon = new File("myDir\\ponpon.txt");
            File lemon = new File("myDir\\lemon.txt");
            File gitlet = new File(".gitlet");

            ArrayList<String> commitIDs = new ArrayList<String>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(ponpon, "1");
            writeToFile(wug, "1");
            writeToFile(lemon, "1");
            Main.main("add", "hello.txt");
            Main.main("commit", "hello.1");
            writeToFile(hello, "2");
            Main.main("add", "hello.txt");
            Main.main("commit", "hello.2");
            writeToFile(hello, "3");
            Main.main("add", "hello.txt");
            Main.main("commit", "hello.3");
            Main.main("status");
            Main.main("branch", "newBranch");
            Main.main("checkout", "newBranch");
            System.out.println("---------BRANCHED OFF---------");
            writeToFile(hello, "5");
            Main.main("add", "hello.txt");
            Main.main("commit", "hello.5");
            Main.main("log");
            System.out.println("log should be 5 commits long");
            System.out.println("================NEWTEST================");
            Main.main("checkout", "master");
            System.out.println("--------BRANCHED BACK---------");
            Main.main("log");
            System.out.println("log should be 4 commits long");
            writeToFile(hello, "pie");
            Main.main("add", "hello.txt");
            Main.main("commit", "hello.pie");
            writeToFile(hello, "die");
            Main.main("add", "hello.txt");
            Main.main("commit", "hello.die");
            writeToFile(hello, "why");
            Main.main("add", "hello.txt");
            Main.main("commit", "hello.why");
            Main.main("log");
            System.out.println("log should be 7 commits long");
            System.out.println("================NEWTEST================");
            simpleBranchTest2(hello, pineapple);
        } catch (IllegalArgumentException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    public void simpleBranchTest2(File hello, File pineapple) {
        Main.main("checkout", "newBranch");
        Main.main("log");
        System.out.println("log should (still) be 5 commits long");

        writeToFile(hello, "4");
        Main.main("add", "hello.txt");
        Main.main("commit", "hello.4");

        Main.main("add", "hello.txt");
        Main.main("commit", "hello.4");

        writeToFile(pineapple, "2");
        Main.main("add", "hello.txt");
        Main.main("commit", "hello.4");

        Main.main("log");
        System.out.println("log should be 6 commits long");
        System.out.println("================NEWTEST================");
        System.out.println();
        Main.main("global-log");

        Main.main("status");
        System.out.println(Main.getDirectory().getMyChildren().keySet());
    }

    @Test
    public void addRemoveStatus() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File ponpon = new File("myDir\\ponpon.txt");
            File lemon = new File("myDir\\lemon.txt");

            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(ponpon, "1");
            writeToFile(wug, "1");
            writeToFile(lemon, "1");

            ArrayList<String> commitIDs = new ArrayList<String>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());

            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");

            Main.main("rm", "pineapple.txt");

            Main.main("status");
        } catch (IllegalArgumentException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void moreInterestingMergeTest() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");

            ArrayList<String> commitIDs = new ArrayList<String>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());

            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(bonny, "1");
            writeToFile(wug, "1");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");

            Main.main("add", "hello.txt");
            Main.main("add", "bonny.txt");
            Main.main("add", "wug.txt");
            Main.main("commit", "hello.1, bonny.1 "
                    + "and wug.1 committed to master");

            Main.main("branch", "newBranch");
            Main.main("checkout", "newBranch");
            Main.main("status");

            writeToFile(hello, "2");
            writeToFile(grapes, "2");
            writeToFile(pineapple, "2");
            Main.main("add", "hello.txt");
            Main.main("add", "grapes.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "hello.2, grapes.2 "
                    + "and pineapple.2 committed to newBranch");

            Main.main("checkout", "master");
            Main.main("status");
            writeToFile(hello, "3");
            writeToFile(grapes, "3");
            writeToFile(bonny, "3");
            writeToFile(thug, "3");
            Main.main("add", "hello.txt");
            Main.main("add", "grapes.txt");
            Main.main("add", "bonny.txt");
            Main.main("add", "thug.txt");
            Main.main("commit", "hello.3, grapes.3, "
                    + "bonny.3 and thug.3 committed to master");

            moreInterestingMergeTests2(pineapple);
        } catch (IllegalArgumentException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Continuation of the moreInterestingMergeTest, halved to
     * make the autograder happy.
     * @param pineapple pineapples are fairly self-explanatory right?
     */
    private void moreInterestingMergeTests2(File pineapple) {
        try {
            System.out.println("======== NEW TEST =======");
            Main.main("status");
            System.out.println("status no problem "
                    + "IFF current branch is master\n");
            Main.main("log");
            System.out.println("log no problem IFF "
                    + "commits are init, 1 and 3\n");
            Main.main("checkout", "newBranch");
            System.out.println("checkout no problem IFF 1 "
                    + "\"Untracked file\" error thrown\n");
            System.out.println("======SWITCHAROO!!!======");
            Utils.restrictedDelete(pineapple);
            Main.main("checkout", "newBranch");
            Main.main("log");
            System.out.println("log no problem IFF "
                    + "commits are init, 1, and 2\n");

            Main.main("merge", "Master");
            System.out.println("merge no problem IFF "
                    + "\"branch DNE\" error thrown\n");

            Main.main("merge", "master");
            Main.main("status");
            Main.main("log");

            System.out.println("==========GLOBAL LOG TIME===========");
            Main.main("global-log");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void simpleAddTwoStatusTest() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");

            ArrayList<String> commitIDs = new ArrayList<String>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());

            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("status");
        } catch (IllegalArgumentException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("====== END OF PROGRAM ======");
    }

    @Test
    public void removeDeletedFiles() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            ArrayList<String> commitIDs = new ArrayList<String>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "two files");
            Utils.restrictedDelete(hello);
            Main.main("rm", "hello.txt");
            Main.main("status");
            System.out.println("status no problem IFF status blank "
                    + "except for hello.txt under Removed Files");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void removeDeletedFile() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            ArrayList<String> commitIDs = new ArrayList<String>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "two files");
            Utils.restrictedDelete(hello);
            Main.main("rm", "hello.txt");
            Main.main("status");
            System.out.println("status no problem IFF blank OTHER"
                    + " THAN hello.txt under removed files");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void successfulFind() {
        try {
            ArrayList<String> commitIDs = new ArrayList<String>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "two files");

            Main.main("rm", "hello.txt");

            Main.main("commit", "removed hello.txt");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void removeStatustest() {
        try {
            File hello = new File("hello.txt");
            ArrayList<String> commitIDs = new ArrayList<String>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());

            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "two files");

            Main.main("rm", "hello.txt");
            assertFalse(hello.exists());

            Main.main("status");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void branches() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");

            writeToFile(hello, "1");
            writeToFile(pineapple, "1");

            ArrayList<String> commitIDs = new ArrayList<String>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());

            writeToFile(pineapple, "2");
            Main.main("branch", "other");
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");

            Main.main("commit", "Main two files");

            Main.main("checkout", "other");

            assertFalse(hello.exists());
            assertFalse(pineapple.exists());

            Utils.writeContents(hello, "2");
            Main.main("add", "hello.txt");

            Main.main("commit", "Alternative file");

            assertEquals("2", readFile(hello));
            assertFalse(pineapple.exists());

            Main.main("checkout", "master");

            assertEquals("1", readFile(hello));
            assertEquals("2", readFile(pineapple));

            Main.main("checkout", "other");

            assertEquals("2", readFile(hello));
            assertFalse(pineapple.exists());
            System.out.println("branches() test completed without problem!\n");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println("branches() test erred: " + e.getMessage());
        }
    }

    @Test
    public void addRemoveStatus2() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            Main.main("init");

            writeToFile(hello, "1");
            writeToFile(pineapple, "1");

            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");

            Main.main("rm", "hello.txt");
            Main.main("status");
            System.out.println("==============");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void removeAddStatus2() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "1");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");
            Main.main("init");
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "Two files");

            Main.main("rm", "hello.txt");
            assertFalse(hello.exists());
            Utils.writeContents(new File("hello.txt"), "1");
            System.out.println("==============MARKER=============");
            Main.main("add", "hello.txt");
            Main.main("status");
            System.out.println("Test no problem if status blank");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void emptyCommitErr() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "1");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");
            Main.main("init");
            Main.main("commit", "nothing here");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void statusAfterCommit() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "1");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");
            Main.main("init");
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "Two files");

            Main.main("status");
            System.out.println("status no problem IFF status blank\n");

            Main.main("rm", "hello.txt");
            Main.main("commit", "Removed hello.txt");

            Main.main("status");
            System.out.println("status no problem IFF status blank\n");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void rmBranch() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "1");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");
            Main.main("init");

            Main.main("branch", "other");
            Main.main("add", "hello.txt");
            Main.main("commit", "File hello.txt");
            Main.main("checkout", "other");
            writeToFile(pineapple, "2");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "File pineapple.txt");

            Main.main("checkout", "master");
            Main.main("rm-branch", "other");

            Main.main("checkout", "other");
            System.out.println("checkout no problem IFF"
                    + " branch DNE error thrown");
            assertFalse(pineapple.exists());
            assertEquals("1", readFile(hello));
        } catch (IllegalArgumentException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Note: h.txt = bonnny; k.txt = grapes;
     */
    @Test
    public void mergeNoConflict() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "2");
            writeToFile(wug, "1");
            writeToFile(bonny, "1");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");
            Main.main("init");
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "Two files");
            Main.main("branch", "other");

            writeToFile(bonny, "2");
            Main.main("add", "bonny.txt");
            Main.main("rm", pineapple.getPath());
            Main.main("commit", "Added bonny and removed pineapple.txt");
            Main.main("checkout", "other");
            Main.main("rm", hello.getPath());
            writeToFile(grapes, "3");
            Main.main("add", grapes.getPath());
            Main.main("commit", "Added grapes and removed hello");
            Main.main("checkout", "master");
            Main.main("merge", "other");
            assertFalse(hello.exists());
            assertFalse(pineapple.exists());
            assertEquals("2", readFile(bonny));
            assertEquals("3", readFile(grapes));

            Main.main("log");
            System.out.println("log no problem IFF only 4 commits in log\n");

            Main.main("status");
            System.out.println("status no problem IFF blank\n");

        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Note: h.txt = bonnny; k.txt = grapes;
     */
    @Test
    public void mergeConflicts() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "2");
            writeToFile(grapes, "3");
            writeToFile(thug, "1");
            Main.main("init");
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "Two files");
            Main.main("branch", "other");
            Main.main("add", bonny.getPath());
            Main.main("rm", pineapple.getPath());
            writeToFile(hello, "2.1");
            Main.main("add", hello.getPath());
            Main.main("commit", "Add bonny.2, remove pineapple"
                    + ", and changed hello.2.1");
            Main.main("checkout", "other");
            writeToFile(hello, "2");
            Main.main("add", hello.getPath());
            Main.main("add", grapes.getPath());
            Main.main("commit", "Added grapes.3, modified hello.2");
            Main.main("checkout", "master");
            Main.main("log");
            System.out.println("log no problem if latest commit is "
                    + "\"Add bonny.2, remove pineapple, changed hello.2.1\"\n");

            Main.main("merge", "other");
            System.out.println("merge no problem if "
                    + "\"Encountered a merge conflcit\"\n");

            assertFalse(pineapple.exists());
            assertEquals("2", readFile(bonny));
            assertEquals("3", readFile(grapes));
            System.out.println(readFile(hello));
            System.out.println("reading hello.txt no problem "
                    + "if a conflict has occurred\n");

            Main.main("log");
            System.out.println("log no problem if latest commit "
                    + "is \"Merged into master\'");

            Main.main("status");
            System.out.println("status no problem IFF blank "
                    + "except other branch under branches\n");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Note that wug is hello.
     */
    @Test
    public void badCheckoutErr() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            ArrayList<String> commitIDs = new ArrayList<>();
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "1");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch()
                    .getHeadCommit().getMyID());
            Main.main("add", "hello.txt");
            Main.main("commit", "version 1 of hello.1");
            commitIDs.add(Main.getDirectory().getCurBranch()
                    .getHeadCommit().getMyID());
            writeToFile(hello, "2");
            Main.main("add", "hello.txt");
            Main.main("commit", "version 2 of hello.2");
            commitIDs.add(Main.getDirectory().getCurBranch()
                    .getHeadCommit().getMyID());
            Main.main("log");
            System.out.println("log no problem IFF 3 commits "
                    + "in log, version 2 most recent\n");

            Main.main("checkout", commitIDs.get(2), "--", "hello.txt");
            System.out.println("checkout no problem IFF no "
                    + "commit error thrown\n");

            Main.main("checkout", "5d0bc169a1737e955f9"
                    + "cb26b9e7aa21e4afd4d12", "--", "hello.txt");
            System.out.println("checkout no problem IFF No"
                    + " commit with that ID error thrown\n");

            Main.main("checkout", commitIDs.get(2), "++", "hello.txt");
            System.out.println("checkout no problem IFF Incorrect"
                    + " operands error thrown\n");

            Main.main("checkout", "foobar");
            System.out.println("checkout no problme IFF No"
                    + " such branch exist error thrown\n");

            Main.main("checkout", "master");
            System.out.println("checkout no problem IFF No"
                    + " need to checkout current error thrown\n");


        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void successfulFindOrphan() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "1");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");
            ArrayList<String> commitIDs = new ArrayList<>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "Two files");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            Main.main("rm", "hello.txt");
            Main.main("commit", "Removed hello.txt");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            Main.main("log");
            System.out.println("log no problem IFF contains 3 "
                    + "commits with Removed hello.txt as latest\n");

            Main.main("reset", commitIDs.get(1).substring(6, 12));
            Main.main("find", "Removed hello.txt");
            System.out.println("find no problem if " + commitIDs.get(2)
                    + " is printed");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void mergeRmConflicts() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "2");
            writeToFile(grapes, "3");
            writeToFile(thug, "1");
            Main.main("init");
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "Two files");
            Main.main("branch", "other");
            Main.main("add", "bonny.txt");
            Main.main("rm", "pineapple.txt");
            writeToFile(hello, "2");
            Main.main("add", "hello.txt");
            Main.main("commit", "Added bonny.wug2, remove "
                    + "pineapple, and changed hello.2");
            Main.main("checkout", "other");
            Main.main("rm", "hello.txt");
            Main.main("add", "grapes.txt");
            Main.main("commit", "Added grapes.1 and remove"
                    + "d hello");
            Main.main("checkout", "master");
            Main.main("log");
            Main.main("merge", "other");
            System.out.println("merge no problem IFF Encountered"
                    + " a merge conflict is thrown.\n");

            assertFalse(pineapple.exists());
            assertEquals("2", readFile(bonny));
            assertEquals("3", readFile(grapes));
            System.out.println(readFile(hello));
            System.out.println("readFile no problem IFF merge conflict"
                    + " encountered in hello.txt\n");

            Main.main("status");
            System.out.println("Status no problem IFF status mostly"
                    + " blank except other under branches.");

        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void mergeErr() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "2");
            writeToFile(grapes, "3");
            writeToFile(thug, "1");
            Main.main("init");
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "Two files");
            Main.main("branch", "other");
            Main.main("add", "bonny.txt");
            Main.main("rm", "pineapple.txt");
            Main.main("commit", "Add bonny.2, removed pineapple");
            Main.main("checkout", "other");
            Main.main("merge", "other");
            System.out.println("merge no problem IFF cannot merge with"
                    + " itself error thrown\n");

            Main.main("rm", "hello.txt");
            Main.main("add", "grapes.txt");
            Main.main("commit", "Added grapes.3, removed hello");
            Main.main("checkout", "master");
            Main.main("merge", "foobar");
            System.out.println("merge no problem IFF branch DNE "
                    + "error thrown\n");

            writeToFile(grapes, "1");
            Main.main("merge", "other");
            System.out.println("merge no problem IFF untracked file"
                    + " error thrown\n");

            Utils.restrictedDelete(grapes);
            Main.main("status");
            System.out.println("status no problem IFF status blank except"
                    + "other under branches\n");
            writeToFile(grapes, "1");
            Main.main("add", "grapes.txt");
            Main.main("merge", "other");
            System.out.println("merge no problem IFF uncommitted"
                    + " changes error thrown");

            Main.main("rm", "grapes.txt");
            Utils.restrictedDelete(grapes);
            Main.main("status");
            System.out.println("status no problem IFF blank except"
                    + "other under branches\n");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void shortUID() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "1");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");
            ArrayList<String> commitIDs = new ArrayList<>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            Main.main("add", "hello.txt");
            Main.main("commit", "version 1 of hello");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            writeToFile(hello, "2");
            Main.main("add", "hello.txt");
            Main.main("commit", "version 2 of hello");
            commitIDs.add(Main.getDirectory().getCurBranch().getHeadCommitID());
            Main.main("checkout", commitIDs.get(1)
                    .substring(6, 12), "--", "hello.txt");
            assertEquals("1", readFile(hello));

            Main.main("checkout", commitIDs.get(2)
                    .substring(6, 12), "--", "hello.txt");
            assertEquals("2", readFile(hello));
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void specialMergeCases() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "2");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");
            Main.main("init");
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "Two files");

            Main.main("branch", "b1");
            Main.main("add", "bonny.txt");
            Main.main("commit", "Add bonny.txt");
            Main.main("branch", "b2");
            Main.main("rm", "hello.txt");
            assertFalse(hello.exists());
            Main.main("commit", "Removed hello.txt");

            Main.main("merge", "b1");
            System.out.println("merge no problem IFF Given branch"
                    + "is ancestor of the current branch error thrown\n");

            Main.main("checkout", "b2");
            assertEquals("1", readFile(hello));

            Main.main("merge", "master");
            System.out.println("merge no problem IFF Current branch is "
                    + "fast-forwarded message thrown\n");
            assertFalse(hello.exists());
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void simpleCommitCheckoutTest() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "1");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");
            ArrayList<String> commitIDs = new ArrayList<>();
            Main.main("init");
            commitIDs.add(Main.getDirectory().getCurBranch()
                    .getHeadCommitID());
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("add", "wug.txt");
            Main.main("commit", "Three files");
            commitIDs.add(Main.getDirectory().getCurBranch()
                    .getHeadCommitID());
            writeToFile(hello, "2");
            writeToFile(pineapple, "2");
            writeToFile(bonny, "1");
            writeToFile(wug, "2");
            Main.main("rm", "hello.txt");
            Main.main("rm", "wug.txt");
            Main.main("add", "bonny.txt");
            Main.main("commit", "hello + wug removed, bonny, added");
            commitIDs.add(Main.getDirectory().getCurBranch()
                    .getHeadCommitID());
            writeToFile(hello, "3");
            writeToFile(pineapple, "3");
            writeToFile(bonny, "2");
            writeToFile(wug, "1");
            Main.main("add", "hello.txt");
            Main.main("add", "wug.txt");
            Main.main("commit", "hello restored");
            commitIDs.add(Main.getDirectory().getCurBranch()
                    .getHeadCommitID());
            writeToFile(hello, "5");
            writeToFile(pineapple, "5");
            writeToFile(bonny, "5");
            Main.main("checkout", "--", "hello.txt");
            assertEquals("3", readFile(hello));

            Main.main("checkout", commitIDs.get(2), "--", "hello.txt");
            System.out.println("checkout no problem IFF file"
                    + " DNE error thrown\n");

            Main.main("checkout", commitIDs.get(1), "--", "hello.txt");
            assertEquals("1", readFile(hello));

        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void mergeParent2() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "1");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");
            Main.main("init");
            Main.main("branch", "b1");
            Main.main("branch", "b2");
            Main.main("checkout", "b1");
            Main.main("add", "bonny.txt");
            Main.main("commit", "Add bonny.txt");
            Main.main("checkout", "b2");
            Main.main("add", "hello.txt");
            Main.main("commit", "Add hello.txt");
            Main.main("branch", "c1");
            Main.main("add", "pineapple.txt");
            Main.main("rm", "hello.txt");
            Main.main("commit", "pineapple.txt added, hello removed");
            assertEquals("1", readFile(pineapple));
            assertFalse(hello.exists());
            assertFalse(bonny.exists());

            Main.main("checkout", "b1");
            assertEquals("1", readFile(bonny));
            assertFalse(hello.exists());
            assertFalse(pineapple.exists());

            Main.main("merge", "c1");
            assertEquals("1", readFile(hello));
            assertEquals("1", readFile(bonny));
            assertFalse(pineapple.exists());

            Main.main("merge", "b2");
            assertFalse(hello.exists());
            assertEquals("1", readFile(pineapple));
            assertEquals("1", readFile(bonny));

        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void setup1() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "1");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");
            Main.main("init");
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void mergeSetup() {
        try {
            File hello = new File("hello.txt");
            File pineapple = new File("pineapple.txt");
            File wug = new File("wug.txt");
            File bonny = new File("bonny.txt");
            File grapes = new File("grapes.txt");
            File thug = new File("thug.txt");
            writeToFile(hello, "1");
            writeToFile(pineapple, "1");
            writeToFile(wug, "1");
            writeToFile(bonny, "2");
            writeToFile(grapes, "1");
            writeToFile(thug, "1");
            Main.main("init");
            Main.main("add", "hello.txt");
            Main.main("add", "pineapple.txt");
            Main.main("commit", "Two files");
            Main.main("branch", "other");
            Main.main("add", "bonny.txt");
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}

