package utils;

import java.net.InetAddress;
import java.net.NetworkInterface;

public class GetMAC {
    public static String GetMACAddress(InetAddress ip) {
        try {
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                builder.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }

            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}