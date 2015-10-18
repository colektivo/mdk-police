package com.mdk.police;

import java.net.URISyntaxException;
import java.util.*;

import io.socket.client.IO;
import io.socket.emitter.Emitter;
import org.json.JSONArray;
import purejavahidapi.*;
import io.socket.client.Socket;

public class Main {

    //static Manager manager;
    public static IO.Options opts;
    public static Socket socket;

    public static void main(String[] args) {


        // set as an option
        opts = new IO.Options();
        opts.port = 8001;
        opts.host = "localhost";

        try {
            socket = IO.socket("http://127.0.0.1:8001", opts);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        System.out.print("\n");
        System.out.print("+---------------------------------+\n");
        System.out.print("|                                 |\n");
        System.out.print("|     Police control started      |\n");
        System.out.print("|                                 |\n");
        System.out.print("+---------------------------------+\n");
        System.out.print("\nWaiting for Boss...\n\n");
        System.out.print("To start Boss type on the terminal: \n\n");
        System.out.print("   start-boss  \n\n\n");

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                System.out.print("[[ Boss connected ]]\n\n  Setting up devices\n\n");

            }

        }).on("devices", new Emitter.Listener() {

            @Override
            public void call(Object... args) {

                List<HidDeviceInfo> foundDevices = findByProduct("SYC ID&IC USB Reader");
                ArrayList<String> devices = new ArrayList<String>();

                for (HidDeviceInfo devInfo: foundDevices) {
                    devices.add(String.format("%s", devInfo.getLocationId()));
                }


                JSONArray mJSONArray = new JSONArray(devices);

                socket.emit("devices", mJSONArray);

                captureAndSendDeviceData(foundDevices);

                System.out.printf("\n  Total devices: %d\n", foundDevices.size());
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                System.out.print("\n\n =================");
                System.out.print("\n\n Boss disconnected");
                System.out.print("\n\n =================\n\n\n");
                socket.close();
                System.out.print("+------------------------------------------------------------+\n");
                System.out.print("|                                                            |\n");
                System.out.print("|    The police disconnect when the boss is not available    |\n");
                System.out.print("|                                                            |\n");
                System.out.print("+------------------------------------------------------------+\n");
                System.out.print("\n\n");
                System.out.print("To start Police type on the terminal: \n\n");
                System.out.print("   start-police  \n\n\n");
                System.out.print("To start Boss type on the terminal: \n\n");
                System.out.print("   start-boss  \n\n\n");
                System.exit(0);
            }

        });
        socket.connect();

    }

    private static List<HidDeviceInfo> findByProduct(String product) {
        List<HidDeviceInfo> foundDevices = new LinkedList();
        try {
            List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
            for (HidDeviceInfo info : devList) {
                if (info.getProductString().contains("SYC ID&IC USB Reader")) {
                    foundDevices.add(info);
                    System.out.printf("    added: %s\n", info.getLocationId());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return foundDevices;

    }

    private static void captureAndSendDeviceData(List<HidDeviceInfo> devices) {

        try {
            for (HidDeviceInfo devInfo: devices) {

                HidDevice dev = PureJavaHidApi.openDevice(devInfo.getPath());

                dev.setInputReportListener(new InputReportListener() {
                    public void onInputReport(HidDevice source, byte Id, byte[] data, int len) {
                        try {
                            long location = source.getHidDeviceInfo().getLocationId();

                            String deviceId = String.format("%s", location);
                            socket.emit(deviceId, data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}
