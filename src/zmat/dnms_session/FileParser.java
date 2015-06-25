/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zmat.dnms_session;

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

    public FileParser parseFiles(String... s) {
        days = new LinkedList<>();
        for (String path : s) {
//            System.out.println("add day");
            days.add(new Day(path, processFile(new File(path))));
        }
        return this;
    }

    protected Queue<Session> processFile(File f) {
        EventType[] responses = {EventType.FalseAlarm, EventType.CorrectRejection, EventType.Miss, EventType.Hit};
        EventType[] odors = {EventType.OdorA, EventType.OdorB};
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            @SuppressWarnings("unchecked")
            ArrayList<int[]> eventList = (ArrayList<int[]>) ois.readObject();
            Queue<Trial> currentTrials = new LinkedList<>();
            Queue<Session> sessions = new LinkedList<>();
            EventType firstOdor = EventType.unknown;
            EventType secondOdor = EventType.unknown;
            boolean laserOn = false;
            EventType response;

            for (int[] evt : eventList) {
                switch (evt[2]) {
                    case 61:
//                        switch (evt[3]) {
//                            case 1:
//                            case 0:
                                if (currentTrials.size() > 0) {
//                                    System.out.println(evt[0]);
                                    sessions.offer(new Session(currentTrials));
                                    currentTrials = new LinkedList<>();
                                }
                                break;
//                        }
//                        break;
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        response = responses[evt[2] - 4];
                        if (firstOdor != null && secondOdor != null) {
                            currentTrials.offer(new Trial(firstOdor, secondOdor, response, laserOn));
                        }
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
                }
            }
            if (currentTrials.size() > 0) {
                sessions.offer(new Session(currentTrials));
            }
//            System.out.println(Integer.toString(sessions.size())+" sessions");
            return sessions;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.toString());
        }
        return null;
    }

    public Queue<Day> getDays() {
//        System.out.println(days.size()+" days");
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