/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zmat.sessionparser.quarterparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author Libra
 */
public class FileParser {

    protected Queue<Day> days;

    public void parseFiles(String... s) {
        days = new LinkedList<>();
        for (String path : s) {
            days.add(new Day(path, processProcessFile(new File(path))));
        }
    }
   private Queue<Session> processProcessFile(File f) {
        EventType[] responses = {EventType.FalseAlarm, EventType.CorrectRejection, EventType.Miss, EventType.Hit};
        EventType[] odors = {EventType.OdorA, EventType.OdorB};
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            ArrayList<int[]> eventList = (ArrayList<int[]>) ois.readObject();
            Queue<Trial> currentTrials = new LinkedList<>();
            Queue<Session> sessions = new LinkedList<>();
            int laserType = -1;
            EventType firstOdor = EventType.unknown;
            EventType secondOdor = EventType.unknown;
            boolean laserOn = false;
            EventType response;

            for (int[] evt : eventList) {
                switch (evt[2]) {
                    case 61:
                        switch (evt[3]) {
                            case 1:
                                currentTrials = new LinkedList<>();
                                break;
                            case 0:
                                if (currentTrials.size() > 0) {
                                    sessions.offer(new Session(currentTrials));
                                }
                                break;
                        }
                        break;
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        response = responses[evt[2] - 4];
                        if (laserType != -1 && firstOdor != null && secondOdor != null) {
                            currentTrials.offer(new Trial(laserType, firstOdor, secondOdor, response, laserOn));
                        }
                        laserType = -1;
                        firstOdor = EventType.unknown;
                        secondOdor = EventType.unknown;
                        laserOn = false;
                        break;
                    case 9:
                    case 10:
                        if (evt[3] == 1) {
                            if (firstOdor == EventType.unknown) {
                                firstOdor = odors[evt[2] - 9];
                            } else {
                                secondOdor = odors[evt[2] - 9];
                            }
                        }
                        break;
                    case 65:
                        laserOn = (evt[3] == 1);
                        break;
                    case 58:
                        switch (evt[3]) {
                            case 10:
                                laserType = 0;
                                break;
                            case 91:
                            case 92:
                            case 93:
                            case 94:
                                laserType = evt[3] - 90;
                        }
                        break;
                }
            }
            if (currentTrials.size() > 0) {
                sessions.offer(new Session(currentTrials));
            }
            return sessions;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public Queue<? extends Day> getDays() {
        return days;
    }

    public int[][] getRawMat(String s) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(s)))) {
            @SuppressWarnings("unchecked")
            ArrayList<int[]> eventList = (ArrayList<int[]>) ois.readObject();
            return eventList.toArray(new int[eventList.size()][]);
        } catch (ClassNotFoundException | IOException ex) {
            System.out.println(ex.toString());
        }
        return new int[0][0];
    }

    public void mat2ser(int[][] mat, String pathToFile) {
        ArrayList<int[]> l = new ArrayList<>();
        l.addAll(Arrays.asList(mat));
        arrayList2ser(l, pathToFile);
    }

    public void arrayList2ser(ArrayList<int[]> l, String pathToFile) {

        File targetFile = new File(pathToFile);
        File parent = targetFile.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(targetFile))) {
            out.writeObject(l);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}