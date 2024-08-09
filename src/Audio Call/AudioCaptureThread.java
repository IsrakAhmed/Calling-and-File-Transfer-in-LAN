import javax.sound.sampled.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class AudioCaptureThread implements Runnable {
    private static final int AUDIO_PORT = 5001;
    private static final int BUFFER_SIZE = 1024;

    private DatagramSocket socket;
    private InetAddress address;
    private volatile boolean running = true;

    public AudioCaptureThread(DatagramSocket socket, InetAddress address) {
        this.socket = socket;
        this.address = address;
    }

    public void run() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                return;
            }

            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[BUFFER_SIZE];

            while (running) {
                microphone.read(buffer, 0, buffer.length);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, AUDIO_PORT);
                socket.send(packet);
            }

            microphone.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }
}