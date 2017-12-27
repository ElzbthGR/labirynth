package edu.lmu.cs.networking;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;




public class Client {

    private JFrame frame = new JFrame("Labirynth");
    private JLabel messageLabel = new JLabel("");
    private ImageIcon icon;
    private ImageIcon opponentIcon;
    private ImageIcon white = createImageIcon("smallwhite.png", "White image");
    private ImageIcon barrier = createImageIcon("barrier.png", "Barrier image");
    private ImageIcon granite = createImageIcon("stop.png", "Stop image");
    private ImageIcon destroyed = createImageIcon("wall.png", "Wall image");

    private ImageIcon destroyedIcon;
    private ImageIcon destroyedOpponentIcon;

    private final int SIZE = 81;
    private final int columnLength = (int) Math.sqrt(SIZE);
    private Square[] map = new Square[SIZE];
    private Square[] opponentMap = new Square[SIZE];
    private int currentLocation;
    private int opponentCurrentLocation;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private boolean error = false;

    private boolean wasChange = false;


    private Client(String serverAddress) throws Exception {

        int PORT = 8102;
        try {
            socket = new Socket(serverAddress, PORT);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.out.println("CLIENT");
                    out.println("EXIT");
                }
            });


            messageLabel.setBackground(Color.WHITE);
            frame.getContentPane().add(messageLabel, "South");
            frame.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    int key = e.getKeyCode();
                    if ((key == KeyEvent.VK_LEFT)) {
                        out.println("MOVE LEFT");
                    }
                    if ((key == KeyEvent.VK_RIGHT)) {
                        out.println("MOVE RIGHT");
                    }
                    if ((key == KeyEvent.VK_UP)) {
                        out.println("MOVE UP");
                    }
                    if ((key == KeyEvent.VK_DOWN)) {
                        out.println("MOVE DOWN");
                    }
                }
            });

            frame.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        if (!wasChange) {
                            out.println("CHANGE");
                            wasChange = true;
                            messageLabel.setText("Change turn");
                        }
                    }
                }
            });

            JPanel boardPanel = new JPanel();
            JPanel opponentBoardPanel = new JPanel();
            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));

            labelPanel.setBackground(Color.BLACK);
            labelPanel.setForeground(Color.WHITE);

            JLabel label = new JLabel("Двигайтесь с помощью стрелок");
            JLabel label2 = new JLabel("Кликая на ЛКМ, вы взрываете");
            JLabel label3 = new JLabel("Кликая на ПКМ, вы пропускаете ход");

            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            label2.setAlignmentX(Component.CENTER_ALIGNMENT);
            label3.setAlignmentX(Component.CENTER_ALIGNMENT);

            labelPanel.add(label);
            labelPanel.add(label2);
            labelPanel.add(label3);

            boardPanel.setLayout(new GridLayout(columnLength, columnLength, columnLength - 1, columnLength - 1));
            opponentBoardPanel.setLayout(new GridLayout(columnLength, columnLength, columnLength - 1, columnLength - 1));

            for (int i = 0; i < map.length; i++) {
                final int j = i;
                map[i] = new Square();
                map[i].setIcon(white);
                map[i].addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (j == currentLocation - 1)
                                out.println("BOMB LEFT");
                            else if (j == currentLocation + 1)
                                out.println("BOMB RIGHT");
                            else if (j == currentLocation - columnLength)
                                out.println("BOMB UP");
                            else if (j == currentLocation + columnLength)
                                out.println("BOMB DOWN");
                        }

                    }
                });
                boardPanel.add(map[i]);
            }
            frame.getContentPane().add(boardPanel, "West");

            frame.getContentPane().add(labelPanel, "Center");

            for (int i = 0; i < opponentMap.length; i++) {
                opponentMap[i] = new Square();
                opponentMap[i].setIcon(white);
                opponentBoardPanel.add(opponentMap[i]);
            }
            frame.getContentPane().add(opponentBoardPanel, "East");

        } catch (ConnectException e) {
            errorMessage();
        }
    }

    public void errorMessage() {
        JOptionPane.showConfirmDialog(frame,
                "Sorry, server is not running",
                "Error",
                JOptionPane.CLOSED_OPTION);
        error = true;
    }

    public boolean error() {
        return error;
    }


    private void play() throws Exception {
        String response;
        try {
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);
                icon = mark == '1' ? createImageIcon("1.png", "1 image") : createImageIcon("2.png", "2 image");
                opponentIcon = mark == '1' ? createImageIcon("2.png", "1 image") : createImageIcon("1.png", "2 image");

                destroyedIcon = mark == '1' ? createImageIcon("destroyed1.png", "destroyedX image") : createImageIcon("destroyed2.png", "destroyedO image");
                destroyedOpponentIcon = mark == '1' ? createImageIcon("destroyed2.png", "destroyedX image") : createImageIcon("destroyed1.png", "destroyedO image");
                frame.setTitle("Labirynth - Player " + mark);
            }
            while (true) {
                response = in.readLine();

                if (response.startsWith("YOUR_MOVE")) {
                    wasChange = false;
                    messageLabel.setText("Your turn");
                }

                else if (response.startsWith("START")) {
                    startLocation(map, icon);

                } else if (response.startsWith("OPPONENT_START")) {
                    startLocation(opponentMap, opponentIcon);

                } else if (response.startsWith("OPEN")) {
                    openLocation(response, map, 5, true);

                } else if (response.startsWith("OPPONENT_OPEN")) {
                    openLocation(response, opponentMap, 14, false);

                } else if (response.startsWith("VALID_MOVE")) {
                    moveLocation(response, map, 11, icon, true);

                } else if (response.startsWith("OPPONENT_MOVED")) {
                    moveLocation(response, opponentMap, 15, opponentIcon, false);

                } else if (response.startsWith("DESTROYED")) {
                    destroyedLocation(response, map, 10, true);

                } else if (response.startsWith("OPPONENT_DESTROYED")) {
                    destroyedLocation(response, opponentMap, 19, false);

                } else if (response.startsWith("NOT_DESTROYED")) {
                    notDestroyedLocation(response, map, 14, true);

                } else if (response.startsWith("OPPONENT_NOT_DESTROYED")) {
                    notDestroyedLocation(response, opponentMap, 23, false);

                } else if (response.startsWith("THROW_INTO_THE_VOID")) {
                    throwIntoTheVoid(response, map, 20, icon, true);

                } else if (response.startsWith("OPPONENT_THROW_INTO_THE_VOID")) {
                    throwIntoTheVoid(response, opponentMap, 29, opponentIcon, false);


                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("You win");
                    break;

                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("You lose");
                    break;

                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                }
            }
            out.println("QUIT");
        } catch (SocketException e) {
            socket.close();
            errorMessage();
            System.exit(0);
        } finally {
            try {
                socket.close();
            }
            catch (NullPointerException e) {
                //NOTHING
            }
        }
    }

    private int direction(String subCommand, boolean current) {
        int location;
        if (current)
            location = currentLocation;
        else
            location = opponentCurrentLocation;

        if (subCommand.startsWith("LEFT"))
            return location - 1;
        else if (subCommand.startsWith("RIGHT"))
            return location + 1;
        else if (subCommand.startsWith("UP"))
            return location - columnLength;
        else if (subCommand.startsWith("DOWN"))
            return location + columnLength;
        else
            return -1;
    }

    private void startLocation(Square[] board, ImageIcon icon) {
        int startLocation = (SIZE - 1) / 2;
        if (icon.equals(this.icon)) {
            currentLocation = startLocation;
        } else {
            opponentCurrentLocation = startLocation;
        }
        board[startLocation].setIcon(icon);
        board[startLocation].repaint();
    }

    private void openLocation(String response, Square[] board, int n, boolean current) {
        int code = Integer.parseInt(response.substring(n, n + 1));
        String turn = response.substring(n + 2);
        int playerLocation;

        if (current)
            playerLocation = currentLocation;
        else
            playerLocation = opponentCurrentLocation;

        if (playerLocation % columnLength == 0 && turn.equals("LEFT")) {
            //NOTHING

        } else if ((playerLocation + 1) % columnLength == 0 && turn.equals("RIGHT")) {
            //NOTHING

        } else if (playerLocation <= columnLength && turn.equals("UP")) {
            //NOTHING

        } else if (SIZE - playerLocation <= columnLength && turn.equals("DOWN")) {
            //NOTHING

        } else {

            int location = direction(turn, current);

            if (code == 0) {
                board[location].setIcon(white);
            }
            if (code == 1 && board[location].getIcon() != granite) {
                board[location].setIcon(barrier);
            }
            board[location].repaint();

            if (current)
                messageLabel.setText("Open " + turn);
            else
                messageLabel.setText("Opponent open " + turn);
        }
    }

    private void moveLocation(String response, Square[] map, int n, ImageIcon icon, boolean current) {
        String turn = response.substring(n);
        int location = direction(turn, current);
        if (response.startsWith("VALID_MOVE_") || response.startsWith("OPPONENT_MOVED_")) {
            if (icon.equals(this.icon)) {
                map[location].setIcon(destroyedIcon);
            } else {
                map[location].setIcon(destroyedOpponentIcon);
            }
        }
        else {
            map[location].setIcon(icon);
        }

        map[location].repaint();

        if (icon.equals(this.icon)) {
            if (map[currentLocation].getIcon().equals(destroyedIcon)) {
                map[currentLocation].setIcon(destroyed);
            }
            else {
                map[currentLocation].setIcon(white);
            }
            map[currentLocation].repaint();
            currentLocation = location;

        } else {
            if (map[opponentCurrentLocation].getIcon().equals((destroyedOpponentIcon))) {
                map[opponentCurrentLocation].setIcon(destroyed);
            }
            else {
                map[opponentCurrentLocation].setIcon(white);
            }
            map[opponentCurrentLocation].repaint();
            opponentCurrentLocation = location;
        }
    }

    private void destroyedLocation(String response, Square[] map, int n, boolean current) {
        String turn = response.substring(n);
        int location = direction(turn, current);
        map[location].setIcon(destroyed);
        map[location].repaint();

        if (current)
            messageLabel.setText("Wall destroyed " + turn);
        else
            messageLabel.setText("Opponent destroyed wall " + turn);
    }

    private void notDestroyedLocation(String response, Square[] map, int n, boolean current) {
        String turn = response.substring(n);
        int location = direction(turn, current);
        map[location].setIcon(granite);
        map[location].repaint();

        if (current)
            messageLabel.setText("Wall not destroyed " + turn);
        else
            messageLabel.setText("Opponent not destroyed wall " + turn);

    }

    private void throwIntoTheVoid(String response, Square[] map, int n, ImageIcon icon, boolean current) {
        String turn = response.substring(n);
        if (response.startsWith("THROW_INTO_THE_VOID_") || response.startsWith("OPPONENT_THROW_INTO_THE_VOID_")) {
            int location = direction(turn, current);
            if (icon.equals(this.icon)) {
                map[location].setIcon(destroyed);
            }
            else {
                map[location].setIcon(destroyed);
            }
            map[location].repaint();
        }
        else {

            if (current)
                messageLabel.setText("Throw into the void " + turn);
            else
                messageLabel.setText("Opponent throw into the void " + turn);
        }
    }

    private boolean wantsToPlayAgain() {
        int response = JOptionPane.showConfirmDialog(frame,
                "Want to play again?",
                "It was cool, agree!",
                JOptionPane.YES_NO_OPTION);
        frame.dispose();
        return response == JOptionPane.YES_OPTION;
    }


    static class Square extends JPanel {
        JLabel label = new JLabel((Icon) null);

        Square() {
            setBackground(Color.white);
            add(label);
        }

        void setIcon(Icon icon) {
            label.setIcon(icon);
        }

        Icon getIcon() {
            return label.getIcon();
        }
    }

    public static void main(String[] args) throws Exception {
        String serverAddress = null;
        boolean askAddress = true;
        while (true) {
            if (askAddress) {
                IP ip = new IP();
                serverAddress = ip.setIP();
            }
            Client client = new Client(serverAddress);
            if (!client.error()) {
                client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                client.frame.setSize(860, 350);
                client.frame.setLocation(500, 500);
                client.frame.setVisible(true);
                client.frame.setResizable(false);
                client.play();

                if (!client.wantsToPlayAgain()) {
                    break;
                }
                else {
                    askAddress = false;
                }
            } else {
                System.exit(0);
            }
        }
    }

    private static class IP {

        String setIP() {
            int response = JOptionPane.showConfirmDialog(null,
                    "It's your server?",
                    "Input IP",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                return "localhost";
            }
            else if (response == JOptionPane.NO_OPTION) {
                return JOptionPane.showInputDialog(null, "Input IP:", "Input", JOptionPane.QUESTION_MESSAGE);
            }
            else {
                System.exit(0);
            }
            return null;
        }
    }

    private ImageIcon createImageIcon(String path,
                                      String description) {

        java.net.URL imgURL = getClass().getClassLoader().getResource(path);
        System.out.println(imgURL);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);

        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}