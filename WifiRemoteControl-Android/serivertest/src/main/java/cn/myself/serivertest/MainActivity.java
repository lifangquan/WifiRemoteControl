package cn.myself.serivertest;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            MulticastSocket ds = new MulticastSocket(12341);
            ds.joinGroup(InetAddress.getByName("224.0.0.1"));
            new ReadThread(ds).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReadThread extends Thread {
        private final MulticastSocket mSocket;

        ReadThread(MulticastSocket socket) {
            mSocket = socket;
        }

        @Override
        public void run() {
            byte buf[] = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf, 1024);
            while (true) {
                try {
                    mSocket.receive(dp);
                    String receiveString = new String(buf, 0, dp.getLength());
                    Log.e("123", new String(buf, 0, dp.getLength()) + dp.getAddress());
                    switch (receiveString) {
                        case "ACTION_SCANF": {
                            byte[] data = Build.VERSION.RELEASE.getBytes();
                            DatagramPacket mPackage = new DatagramPacket(data, data.length, dp.getAddress(), dp.getPort());
                            mSocket.send(mPackage);
                        }
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
