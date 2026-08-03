package org.example.deployment_app.services;

import org.springframework.stereotype.Service;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.net.Inet4Address;

@Service
public class NetworkService {
    public String getLocalNetworkIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr.getHostAddress().contains(":")) {
                        continue;
                    }
                    return addr.getHostAddress();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to inspect network interfaces..", e);
        }
        throw new RuntimeException("No usable address found!");

    }
}
