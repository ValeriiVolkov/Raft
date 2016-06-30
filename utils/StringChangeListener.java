package utils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Valerii Volkov on 25.06.2016.
 */
public class StringChangeListener implements KeyListener {
    private StringBuilder log;
    private OutputStream outStream;
    private final String CHARSET = "UTF-8";

    public StringChangeListener(StringBuilder log, OutputStream outStream) {
        this.log = log;
        this.outStream = outStream;
    }

    @Override
    public void keyTyped(KeyEvent e) {/*Do nothing*/}

    @Override
    public void keyPressed(KeyEvent e) {
        try {
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                log.deleteCharAt(log.length() - 1);
                outStream.write((RaftUtils.DELETE_ENTRY).getBytes(CHARSET));
            } else {
                log.append(e.getKeyChar());
                outStream.write(e.getKeyChar());
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {/*Do nothing*/}
}
