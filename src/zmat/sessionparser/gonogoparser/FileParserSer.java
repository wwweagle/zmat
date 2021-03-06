/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmat.sessionparser.gonogoparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import zmat.dnms_session.EventType;
import zmat.dnms_session.Session;
import zmat.dnms_session.Trial;

/**
 *
 * @author Libra
 */
public class FileParserSer extends zmat.dnms_session.FileParser {

    @Override
    public Queue<Session> processFile(File f) {
        int odor2Start = 0;
        EventType[] responses = {EventType.FalseAlarm, EventType.CorrectRejection, EventType.Miss, EventType.Hit};
        EventType[] odors = {EventType.OdorA, EventType.OdorB};
        ArrayList<Integer[]> licks = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            @SuppressWarnings("unchecked")
            ArrayList<int[]> eventList = (ArrayList<int[]>) ois.readObject();
            Queue<Trial> currentTrials = new LinkedList<>();
            Queue<Session> sessions = new LinkedList<>();
            EventType firstOdor = EventType.unknown;
            EventType secondOdor = EventType.unknown;
            boolean laserOn = false;
            EventType response;
            int lastLick = 0;
            for (int[] evt : eventList) {
                switch (evt[2]) {
                    case 0:
//                        System.out.println("LICK");
                        if (evt[0] - lastLick > 50) {
                            licks.add(new Integer[]{evt[0], evt[3]});
                            lastLick = evt[0];
                        }
                        break;
                    case 1:
                        if (evt[3] == 1) {
                            break;
                        }
                        evt[2] = 61;
                        evt[3] = 0;
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
//                        System.out.println(firstOdor + ", " + secondOdor);
                        response = responses[evt[2] - 4];
                        if (firstOdor != EventType.unknown) {
                            currentTrials.offer(new Trial(firstOdor, secondOdor, response, laserOn, licks, 0, odor2Start));
                        }
                        firstOdor = EventType.unknown;
                        secondOdor = EventType.unknown;
                        laserOn = false;
                        break;
                    case 9:
                    case 10:
                        if (evt[3] != 0) {
                            if (firstOdor == EventType.unknown) {
                                firstOdor = odors[evt[2] - 9];
                            } else {
                                secondOdor = odors[evt[2] - 9];
                            }
                        }
                        odor2Start=evt[0];
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

}
