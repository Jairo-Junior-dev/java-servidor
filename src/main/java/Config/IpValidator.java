package Config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class IpValidator {
    private final Set<String> blockedIps;
    private final Set<String> allowedIps;
    private final boolean allowAll;

    public IpValidator(ConfigLoader config) {
        String blocked = config.getProperty("security.blockedIps", "");
        blockedIps = Arrays.stream(blocked.split(","))
                .map(String::trim)
                .filter(ip -> !ip.isEmpty())
                .collect(Collectors.toSet());

        String allowed = config.getProperty("security.allowedIps", "*").trim();
        allowAll = allowed.equals("*");

        allowedIps = allowAll ? new HashSet<>() :
                Arrays.stream(allowed.split(","))
                        .map(String::trim)
                        .filter(ip -> !ip.isEmpty())
                        .collect(Collectors.toSet());
    }

    public boolean isBlocked(String ip) {
        return blockedIps.contains(ip);
    }

    public boolean isAllowed(String ip) {
        if (allowAll) return true;
        return allowedIps.contains(ip);
    }

    public boolean isValid(String ip) {
        if (isBlocked(ip)) return false;
        return isAllowed(ip);
    }

    public void printStatus() {
        System.out.println("Blocked IPs: " + blockedIps);
        System.out.println("Allowed IPs: " + (allowAll ? "ALL (*)" : allowedIps));
    }
}
