package leddimmer;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SerialManager {

    private SerialPort activePort;
    private MainWindow gui;
    private StringBuilder lineBuffer = new StringBuilder();

    public SerialManager(MainWindow gui) { this.gui = gui; }

    public String[] getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] names = new String[ports.length];
        for (int i = 0; i < ports.length; i++) names[i] = ports[i].getSystemPortName();
        return names;
    }

    public void connect(String portName) {
        SerialPort found = null;
        for (SerialPort p : SerialPort.getCommPorts())
            if (p.getSystemPortName().equals(portName)) { found = p; break; }
        if (found == null) { gui.log("ERROR: Port " + portName + " not found."); return; }

        activePort = found;
        activePort.setBaudRate(9600);
        activePort.setNumDataBits(8);
        activePort.setNumStopBits(1);
        activePort.setParity(SerialPort.NO_PARITY);
        activePort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

        if (activePort.openPort()) {
            gui.log("[CONNECTED] " + portName + " @ 9600 baud");
            gui.setConnectionStatus(true, portName);
            startListening();
        } else {
            gui.log("ERROR: Could not open " + portName + ". Is it in use?");
            activePort = null;
        }
    }

    public void disconnect() {
        if (activePort != null && activePort.isOpen()) {
            activePort.removeDataListener();
            activePort.closePort();
            gui.log("[DISCONNECTED]");
            gui.setConnectionStatus(false, "");
        }
        activePort = null;
    }

    public void sendBrightness(int percent) {
        String cmd = "BRIGHT:" + percent + "\n";
        sendRaw(cmd);
        gui.log("SENT → " + cmd.trim());
    }

    public void sendStatus() { sendRaw("STATUS\n"); gui.log("SENT → STATUS"); }

    public boolean isConnected() { return activePort != null && activePort.isOpen(); }

    private void sendRaw(String msg) {
        if (!isConnected()) { gui.log("ERROR: Not connected."); return; }
        try {
            OutputStream out = activePort.getOutputStream();
            out.write(msg.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Exception e) { gui.log("SEND ERROR: " + e.getMessage()); }
    }

    private void startListening() {
        activePort.addDataListener(new SerialPortDataListener() {
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
                byte[] buf = new byte[activePort.bytesAvailable()];
                int n = activePort.readBytes(buf, buf.length);
                if (n <= 0) return;
                lineBuffer.append(new String(buf, 0, n, StandardCharsets.UTF_8));
                int idx;
                while ((idx = lineBuffer.indexOf("\n")) != -1) {
                    String line = lineBuffer.substring(0, idx).trim();
                    lineBuffer.delete(0, idx + 1);
                    if (!line.isEmpty()) processLine(line);
                }
            }
        });
    }

    private void processLine(String line) {
        gui.log("RECV ← " + line);
        if (line.startsWith("GAS:")) {
            try { gui.updateGasReading(Integer.parseInt(line.substring(4).trim())); }
            catch (NumberFormatException e) { gui.log("PARSE ERROR: " + line); }
        }
    }
}
