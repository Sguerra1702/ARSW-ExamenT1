package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT = 5;

    public List<Integer> checkHost(String ipaddress, int N) {
        System.out.println("Revisando host: "+ipaddress);
        LinkedList<Integer> blackListOccurrences = new LinkedList<>();
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        int registeredServersCount = skds.getRegisteredServersCount();
        int segmentSize = registeredServersCount / N;
        BlackListValidatorThread[] threads = new BlackListValidatorThread[N];
        BlackListValidatorThread.occurrencesCount = 0;
        BlackListValidatorThread.totalCheckedLists = 0;

        for (int i = 0; i < N; i++) {
            int startRange = i * segmentSize;
            int endRange = (i == N - 1) ? registeredServersCount - 1 : (startRange + segmentSize - 1);
            threads[i] = new BlackListValidatorThread(startRange, endRange, ipaddress, skds, blackListOccurrences);
            threads[i].start();
        }

        for (int i = 0; i < N; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                Logger.getLogger(HostBlackListsValidator.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        if (BlackListValidatorThread.occurrencesCount >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{BlackListValidatorThread.totalCheckedLists, skds.getRegisteredServersCount()});

        return blackListOccurrences;
    }

    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
}