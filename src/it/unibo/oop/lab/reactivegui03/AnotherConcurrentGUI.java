package it.unibo.oop.lab.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class AnotherConcurrentGUI extends JFrame{
    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");
    
    public AnotherConcurrentGUI() {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        
        final Agent agent = new Agent();
        new Thread(agent).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // with this, i'm waiting for 10 seconds and after that, i automatically click stop
                    Thread.sleep(10_000);
                    stop.doClick();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }   
        }).start();
        
        
        up.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                agent.goUp();
            }  
        });
        
        down.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                agent.goDown();
            }     
        });
        
        stop.addActionListener(new ActionListener() {
            /**
             * event handler associated to action event on button stop.
             * 
             * @param e
             *            the action event that will be handled by this listener
             */
            @Override
            public void actionPerformed(final ActionEvent e) {
                // Agent should be final
                agent.stopCounting();
                AnotherConcurrentGUI.this.up.setEnabled(false);
                AnotherConcurrentGUI.this.down.setEnabled(false);
                AnotherConcurrentGUI.this.stop.setEnabled(false);
            }
        });
    }
    
    private class Agent implements Runnable {

        private volatile boolean isUp = true;
        private volatile boolean stop;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    /*
                     * we created a specific String variable in order to avoid making the counter variable as volatile
                     */
                    final String nextString = Integer.toString(this.counter);
                    /*
                     * All the operations on the GUI must be performed by the
                     * Event-Dispatch Thread (EDT)!
                     */
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            // This will happen in the EDT: since i'm reading counter it needs to be volatile.
                            AnotherConcurrentGUI.this.display.setText(nextString);
                        }
                    });
                    if (this.isUp) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }
        
        public void goUp() {
            this.isUp = true;
        }
        
        public void goDown() {
            this.isUp = false;
        }
    }
}
