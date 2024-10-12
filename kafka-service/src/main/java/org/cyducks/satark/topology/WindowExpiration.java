package org.cyducks.satark.topology;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WindowExpiration implements Comparable<WindowExpiration>{
    private final String key;
    private final long expirationTime;
    @Override
    public int compareTo(WindowExpiration o) {
        return Long.compare(this.expirationTime, o.getExpirationTime());
    }
}
