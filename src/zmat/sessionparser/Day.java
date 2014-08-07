/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zmat.sessionparser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author Libra
 */
public class Day<T extends Session> {

    protected Queue<T> sessions;
    protected String fileName;

    protected Day(String fileName, Queue<T> q) {
        this.fileName = fileName;
        sessions = q;
    }

    public void removeBadSessions(int trialNum, boolean fullSession, int lickCount) {
        Queue<T> q = new LinkedList<>();
        for (T session : sessions) {
            boolean sessionFull = (fullSession && session.getTrialNumber() == trialNum) || !fullSession;
            if (sessionFull && session.getLickCount() >= lickCount) {
                q.offer(session);
            }
        }
        sessions = q;
    }

    public int getSessionNumber() {
        return sessions.size();
    }

    public List<int[]> getCorrectRates() {
        List<int[]> correctRates = new ArrayList<>();
//        System.out.println(sessions.length+"Sessions ");
        for (T session : sessions) {
            correctRates.add(session.getCorrectRate());
        }
        return correctRates;
    }

    public List<int[]> getFalseAlarmRates() {
        List<int[]> falseAlarms = new ArrayList<>();
        for (T session : sessions) {
            falseAlarms.add(session.getFalseAlarmRate());
        }
        return falseAlarms;
    }

    public List<int[]> getMissRates() {
        List<int[]> falseAlarms = new ArrayList<>();
        for (T session : sessions) {
            falseAlarms.add(session.getMissRate());
        }
        return falseAlarms;
    }

    public String getFileName() {
        return fileName;
    }

}