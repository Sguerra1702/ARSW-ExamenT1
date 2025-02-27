package edu.eci.arsw.blacklistvalidator;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

public class BlackListValidatorThread extends Thread {
    private int startRange;
    private int endRange;
    private String ipAddress;
    private HostBlacklistsDataSourceFacade skds;
    private List<Integer> blackListOccurrences;
    private static final int BLACK_LIST_ALARM_COUNT = 5;
    public static int occurrencesCount = 0;
    public static int totalCheckedLists= 0;
    private static final Object lock = new Object();
    private static final Logger LOG = Logger.getLogger(BlackListValidatorThread.class.getName());

    public BlackListValidatorThread(int startRange, int endRange, String ipAddress, HostBlacklistsDataSourceFacade skds, List<Integer> blackListOccurrences) {
        this.startRange = startRange;
        this.endRange = endRange;
        this.ipAddress = ipAddress;
        this.skds = skds;
        this.blackListOccurrences = blackListOccurrences;
    }

    @Override
    public void run() {

        for (int i = startRange; i <= endRange; i++) {
            synchronized (lock) {
                if (occurrencesCount >= BLACK_LIST_ALARM_COUNT) {
                    break;
                }
            }
            if (skds.isInBlackListServer(i, ipAddress)) {
                LOG.log(Level.INFO, "IP {0} found in server {1}", new Object[]{ipAddress, i});
                synchronized (lock) {
                    if (occurrencesCount < BLACK_LIST_ALARM_COUNT) {
                        blackListOccurrences.add(i);
                        occurrencesCount++;
                        if (occurrencesCount >= BLACK_LIST_ALARM_COUNT) {
                            break;
                        }
                    }
                }
            } else {
            // Conteo de listas revisadas   
            }
            synchronized (lock) {
                totalCheckedLists++;
            }
        }
    }
}