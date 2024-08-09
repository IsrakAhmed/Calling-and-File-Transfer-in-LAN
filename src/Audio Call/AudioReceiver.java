import javax.sound.sampled.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AudioReceiver {
    private static final int AUDIO_PORT = 5001;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(AUDIO_PORT);

            AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                return;
            }

            SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(info);
            speakers.open(format);
            speakers.start();

            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                System.out.println("Receiving audio...");
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                speakers.write(packet.getData(), 0, packet.getLength());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}