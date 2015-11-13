package cn.myself.wifiremotecontrol;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ConnectService extends Service {


    MulticastSocket multicastSocket;
    FindHostListener mFindListener;
    private InetAddress targetSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        createSocket();
    }

    @Override
    public void onDestroy() {
        multicastSocket.close();
        super.onDestroy();
    }

    private void createSocket() {
        try {
            multicastSocket = new MulticastSocket(12342);
            multicastSocket.setTimeToLive(4);
            new FindThread().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ConnectBinder();
    }


    public class ConnectBinder extends Binder {

        public int sendAction(final String action) {
            if (targetSocket == null)
                return 0;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] data = action.getBytes();
                        DatagramPacket mPackage = new DatagramPacket(data, data.length, targetSocket, 12341);
                        multicastSocket.send(mPackage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return 1;
        }

        public void scanfHost(final FindHostListener listener) {
            mFindListener = listener;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] data = "ACTION_SCANF".getBytes();
                        DatagramPacket mPackage = new DatagramPacket(data, data.length, InetAddress.getByName("224.0.0.1"), 12341);
                        multicastSocket.send(mPackage);
                        Log.e("123", "send ACTION_SCANF" + FindThread.activeCount());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        public void connectHost(InetAddress addr) {
            targetSocket = addr;
        }

        public void closeConnect() {
            targetSocket = null;
        }
    }

    public class HostInfo {
        HostInfo(InetAddress hostAddress, String hostName) {
            this.hostAddress = hostAddress;
            this.hostName = hostName;
        }

        private InetAddress hostAddress;
        private String hostName;

        public InetAddress getHostAddress() {
            return hostAddress;
        }

        public String getHostName() {
            return hostName;
        }
    }

    public interface FindHostListener {
        public void onFindFish(HostInfo host);
    }


    private class FindThread extends Thread {
        @Override
        public void run() {

            try {
                byte buf[] = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buf, 1024);
                while (true) {
                    if (multicastSocket == null)
                        createSocket();
                    multicastSocket.receive(dp);
                    String hostName = new String(buf, 0, dp.getLength()) + dp.getAddress();
                    Log.e("123", hostName);
                    if (mFindListener != null)
                        mFindListener.onFindFish(new HostInfo(dp.getAddress(), hostName));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.e("123", "FindThread-Closed");
        }
    }
}
